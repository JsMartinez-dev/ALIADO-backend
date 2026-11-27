# F09-BANDEJA-05 — Validación de lectura y paginación en PostgreSQL

Fecha: 2026-11-27. Rama `task/f09-bandeja-05` (base `3262a48`, F09-BANDEJA-04).
Alcance: verificación del flujo completo de bandeja y lectura sobre el backend integrado
(SPEC-09, plan `plan-backend-lectura-bandeja.md`). No se modificó código de producción ni
migraciones; la entrega son la evidencia y el verificador reproducible.

## Entorno

| Elemento | Valor |
| --- | --- |
| Java | Temurin 21.0.13.1+2 (`/home/b44ki/jdk21-temurin/jdk-21.0.13.1+2`) |
| Build | `bash ./mvnw` (wrapper del repositorio) |
| PostgreSQL | 17.11 (Debian 17.11-0+deb13u1), clúster temporal propio en el puerto 55435 |
| Backend | war empaquetado (`target/ALIADO-0.0.1-SNAPSHOT.war`) en el puerto 8098 |
| FCM | proveedor `falso` (por defecto); no se contactó Firebase ni Azure |
| Datos | entorno desechable; el clúster y la app se detuvieron al terminar |

Bases creadas en el clúster temporal:

- `aliado_f09b05_upgrade`: esquema llevado a V9 con datos previos, luego V10 (prueba de migración).
- `aliado_f09b05_http`: Flyway V1→V10 desde cero; flujo HTTP con tres cuentas.
- `aliado_f09b05_tests`: base de las pruebas `*PostgresTest` del módulo.

Comandos de arranque (el clúster se creó con `initdb` y `pg_ctl` en un directorio de scratch;
`max_connections` se elevó a 250 para la suite que levanta varios contextos Spring):

```
java -jar target/ALIADO-0.0.1-SNAPSHOT.war \
  --spring.mail.host=127.0.0.1 --spring.mail.port=2525 \
  --aliado.mail.from=no-reply@f09b05.test
# worker: añadir --aliado.push.worker-habilitado=true
# migración detenida en V9: --spring.flyway.target=9 --spring.jpa.hibernate.ddl-auto=none
```

Variables usadas por la app: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`,
`PORT`, `ALIADO_STORAGE_DIR`.

## 1. Migración V9 → V10 con datos previos

Se arrancó la app con `spring.flyway.target=9`; Flyway aplicó V1..V9 y la app se detuvo en la
validación de JPA (comportamiento esperado: `leida_en` aún no existe). Después se sembraron,
con SQL directo, dos usuarios y seis avisos de tres tipos (`NUEVA_OPORTUNIDAD`,
`PROPUESTA_RECIBIDA`, `ACEPTACION_PROPUESTA`), con **fechas de generación repetidas**
(tres avisos en `2026-03-01T09:00:00Z`) y estados de entrega mixtos; además una instalación,
un vínculo push, dos trabajos (`PENDIENTE_PREPARACION` y `FINALIZADO`) y una entrega pendiente.

Se arrancó la app sin `spring.flyway.target` y Flyway aplicó V10 (`success = t`).

Resultados comprobados con SQL:

- `leida_en TIMESTAMPTZ NULL` existe en `notificaciones`.
- `ix_notificaciones_bandeja` y `ix_notificaciones_bandeja_pendientes`
  (`WHERE leida_en IS NULL`) creados.
- `SELECT count(*) total, count(leida_en) leidas` → `6 | 0`: los avisos previos quedan
  pendientes, como documenta el plan.
- Volcados antes/después de `notificaciones`, `trabajos_push` y `entregas_push` comparados con
  `diff`: **sin diferencias**. Se conservan estado de entrega, intentos, fechas, trabajos
  (incluida la reclamación/finalización) y la entrega pendiente.

## 2. Inicialización desde cero

La base `aliado_f09b05_http` se creó vacía y la app aplicó Flyway desde cero:

```
version | description                          | success
1       | identidad perfil idempotencia        | t
...
9       | dispositivos y entregas push         | t
10      | lectura bandeja notificaciones       | t
```

La base de pruebas `aliado_f09b05_tests` también terminó en V10 (`10` migraciones, todas
exitosas) tras ejecutar la suite `*PostgresTest`.

## 3. Flujo HTTP completo (dos sesiones, dos destinatarios)

Verificador: `docs/reviews/f09-bandeja-05-verificar.py` (modo `principal`).
Cuentas creadas por HTTP: prestador A, prestador B (perfiles públicos, categoría y zona
compatibles, disponibilidad configurada) y cliente C. Cada publicación de C se hizo por
`POST /api/v1/solicitudes`, lo que dispara `SolicitudPublicada` y crea el aviso
`NUEVA_OPORTUNIDAD` para A y B.

**63/63 comprobaciones correctas.** Cubren:

- Publicación real → un aviso para A y otro para B, `leidaEn` nulo y campos del contrato
  vigente + `leidaEn`.
- `GET /resumen` con `hayNoLeidas=true/false` y su reacción a una publicación nueva.
- `401` en las cuatro rutas sin `Bearer`; `204` sin cuerpo en las dos escrituras.
- Marcar un aviso: la fecha ISO 8601 aparece en `estadoLectura=LEIDA`; cerrar sesión invalida
  el token (`401`); tras volver a entrar la fecha es **exactamente la misma**; repetir la
  petición también la conserva.
- Marcar todos: `204`, resumen `false`, bandeja por defecto vacía sin cursor; no altera la
  campana de B. Una publicación posterior vuelve a encender la campana y encabeza la página.
- Aislamiento: B recibe `404 NOTIFICACION_NO_ENCONTRADA` al marcar un aviso de A; el aviso
  ajeno y un UUID inexistente comparten status, código, mensaje y claves del error (solo
  cambian los campos volátiles `timestamp`, `path`, `traceId`); el aviso de A sigue pendiente.
- `400` con código propio para `estadoLectura`, `tipo`, `limite` (0, 101, no numérico), cursor
  malformado y UUID de aviso inválido. Un cursor de `NO_LEIDA` usado con `TODAS` responde
  `CURSOR_INVALIDO`.
- Paginación: `limite=100` y el valor por defecto (20) devuelven los 5 pendientes sin cursor;
  `limite=1` produce 5 páginas y `limite=2` produce 3 (2+2+1), sin IDs repetidos.
- **Desempate**: cinco avisos sembrados con la misma `fecha_generacion` se recorren con
  `limite=3`; los 10 pendientes aparecen una sola vez.
- Contratos previos intactos: `GET /api/v1/notificaciones` (siete campos),
  `GET /api/v1/notificaciones/oportunidades` y `GET /api/v1/notificaciones/{id}/oportunidad`.

### Concurrencia

- Dos `PUT .../lectura` simultáneos sobre el mismo aviso: ambos `204`; se persiste una sola
  fecha y una escritura posterior **no la sustituye**.
- `PUT .../lectura` individual y `PUT /lectura` masivo simultáneos: ambos `204`; todos los
  pendientes quedan leídos y un aviso insertado **después** de la masiva sigue pendiente.
- La lectura (individual y masiva) no modificó `estado_entrega`, `intentos`, `fecha_entrega`
  ni los trabajos push.

## 4. Worker del piloto push

Con la app en marcha sin worker se registró el dispositivo Android de A
(`PUT /api/v1/notificaciones/dispositivos/{id}` con prueba de instalación), se habilitó la
preferencia `NUEVA_OPORTUNIDAD/PUSH` por HTTP y se publicó una solicitud: el trabajo quedó
`PENDIENTE_PREPARACION`. El aviso se marcó leído por HTTP. Después se reinició la app con
`aliado.push.worker-habilitado=true`.

**4/4 comprobaciones correctas** (modo `postworker`):

- trabajo `FINALIZADO` con motivo `ENTREGAS_TERMINALES`;
- entrega `FALLIDA_DEFINITIVA` con `PROVEEDOR_NO_CONFIGURADO` (resultado terminal del proveedor
  `falso`, que por diseño nunca acepta);
- `leida_en` idéntica antes y después del procesamiento;
- la API sigue devolviendo la misma fecha de lectura tras el worker.

Observación: el worker F09-PUSH registra el resultado en `entregas_push`; no reescribe
`notificaciones.estado_entrega`, que pertenece al flujo de entrega anterior. El contrato de
bandeja expone `estadoEntrega` y `leidaEn` como campos independientes, y la lectura no depende
del resultado push.

## 5. Suites del módulo

| Comando | Resultado |
| --- | --- |
| `JAVA_HOME=<jdk21> bash ./mvnw -o test` | Tests run: **339**, Failures: 0, Errors: 0, Skipped: 46 — BUILD SUCCESS (H2, Flyway apagado) |
| `ALIADO_PG_URL=jdbc:postgresql://127.0.0.1:55435/aliado_f09b05_tests ALIADO_PG_USERNAME=aliado ALIADO_PG_PASSWORD=aliado bash ./mvnw -o -Dtest='*PostgresTest' test` | Tests run: **46**, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS |

Clases PostgreSQL ejercitadas: `ConsultarBandejaPostgresTest` (4),
`LecturaBandejaPersistenciaPostgresTest` (2), `MarcarLecturaAvisosPostgresTest` (3),
`PreparacionYReintentoPushPostgresTest` (13), `PublicacionPushPostgresTest` (7),
`PreferenciaPushPredeterminadaPostgresTest` (4), `ReclamacionTrabajoPushPostgresTest` (7),
`EscuchaTrabajoPushPostgresTest` (6).

Nota de entorno: la primera ejecución conjunta de las ocho clases produjo 11 errores de carga
de contexto por **agotamiento de conexiones** (`remaining connection slots are reserved`, el
clúster tenía `max_connections=60` y la suite levanta varios contextos Spring y una conexión
dedicada de LISTEN). Con `max_connections=250` la misma suite pasó completa, sin cambios de
código. Es una limitación del entorno de prueba, no del producto.

## 6. Reproducción

1. Crear un clúster PostgreSQL desechable y las bases `*_upgrade`, `*_http` y `*_tests`.
2. Empaquetar con `JAVA_HOME=<jdk21> bash ./mvnw -o -DskipTests package`.
3. Arrancar la app contra cada base con las variables de entorno indicadas.
4. Para el flujo HTTP:
   `ALIADO_BASE=http://127.0.0.1:8098 ALIADO_PG_DSN="host=... port=... user=... password=... dbname=..." python3 docs/reviews/f09-bandeja-05-verificar.py principal`
5. Reiniciar la app con `--aliado.push.worker-habilitado=true` y ejecutar el modo `postworker`.
6. Detener la app y el clúster; no reutilizar los datos.

El verificador imprime una línea `PASS/FAIL` por comprobación y termina con código 1 si alguna
falla. Las credenciales del ejemplo son las de la base temporal local; no se usaron secretos
reales.

## 7. Casos pendientes y riesgos

- Validación en Azure y entrega FCM real: fuera del alcance de esta tarjeta (no se usaron
  credenciales ni se desplegó nada).
- El verificador HTTP es un script manual: no está integrado en la suite Maven ni en CI; la
  suite del repositorio sigue siendo H2 y no cubre semántica de PostgreSQL.
- La política de retención de avisos leídos y la integración móvil quedan para su propio
  alcance.
- El desempate se validó con fechas sembradas iguales, que es el caso que H2 no reproduce.

## Trazabilidad de criterios de aceptación

| Criterio | Evidencia |
| --- | --- |
| Flujo completo con dos sesiones y al menos dos destinatarios | §3 (A/B/C, 63/63) |
| Los resultados concurrentes conservan la fecha inicial y el aislamiento | §1, §3 (concurrencia y aislamiento) |
| Desempate de páginas y bandeja tras marcar | §3 (limite 1/2/3/100 y fechas iguales) |
| Migración y pruebas del módulo, regresión push y rutas existentes | §1, §5, §3 (rutas previas) |
| Evidencia repetible con el backend de prueba | §6 y script adjunto |
