# Auditoría estática SPEC-14 — Preferencias, historial e indicadores

**Refactorización posterior.** UC081–UC085 usan contratos de aplicación, mapper y lectura transaccional. La exportación lee por páginas con orden estable en una transacción REPEATABLE_READ y compensa el archivo si se revierte. El CSV completo sigue en memoria. El subtotal de finalizados cuenta TERMINADO y FINALIZADO según la decisión del usuario. El resto de D-15 queda pendiente. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Alcance UC081–UC085, cinco CU Parcial y Revisado. Fuentes: AGENTS.md, SPEC-14 y plan completos, Plan General secciones de lectura/privacidad/notificaciones, D-15, código preferencias/servicios/confianza/notificaciones/storage/seguridad y V6. B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`. Solo se escribió este diagnóstico; no código, pruebas, compilación ni migraciones. OpenAPI vivo no consultado, cotejo de contratos con controlador y plan.

## UC081 — Configurar preferencias de notificación

Endpoint(s): GET/PUT /api/v1/preferencias/notificaciones.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-081 preferencias configurables | actualizarPreferencia; listar genera tipos y PUSH/EMAIL | Cumple | Valida canal; guarda estado propio; default habilitado | Conservar | — |
| Avisos obligatorios | AVISO_SEGURIDAD_CUENTA; ConsultaPreferenciasService; V6 CHECK | Cumple | No permite desactivar seguridad y consulta fuerza habilitado | Conservar y verificar integración | — |
| Integración antes registro/entrega | NotificacionService#registrar y procesamiento consultan preferencias | Cumple | Existe lectura pública separada, sin Service concreto ajeno | Conservar ausencia de ciclo | — |
| Concurrencia y política | PK usuario/tipo/canal; lectura y reemplazo | Parcial | No versionado por cliente; última escritura gana; aceptación integral no ejecutada | Definir política de conflictos solo si se requiere | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | PreferenciasService#listarPreferencias/#actualizarPreferencia y ConsultaPreferenciasService y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/preferencias/application/usecase/ActualizarPreferenciasNotificacionUseCase.java`, `service/ActualizarPreferenciasNotificacionService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | notificaciones SPEC-09, catálogo TipoAviso y PreferenciaRepository |
| `B/modules/preferencias/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `B/modules/preferencias/application/PreferenciasService.java`, `ConsultaPreferenciasService.java`, controller y handler; contrato adicional ConsultarPreferenciasNotificacionUseCase | Modificar parcialmente tras autorización | Separar entrada/salida y preservar consulta dedicada que evita ciclo con notificaciones | Varias | notificaciones SPEC-09, catálogo TipoAviso y PreferenciaRepository |

## UC082 — Consultar historial propio

Endpoint(s): GET /api/v1/historial?pagina=&tamano=.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-082 autorización por participante | findByUsuario where clienteId=usuario or prestadorId=usuario | Cumple | Cuenta proviene de sesión, no admite usuario ajeno en request | Conservar | — |
| Lista vacía y paginación | historial controller valida pagina>=0 y tamano1..100; repo Pageable | Cumple | Filtrado y paginación existentes | Conservar | — |
| Orden estable | findByUsuario order by fechaContratacion desc | Parcial | No desempate por ID ante fechas iguales; páginas pueden ser inestables | Agregar criterio estable compatible y verificar | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | PreferenciasService#historial, ServicioService#porUsuario y ServicioJpaRepository#findByUsuario y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/preferencias/application/usecase/ConsultarHistorialTrabajosUseCase.java`, `service/ConsultarHistorialTrabajosService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | servicios SPEC-08 y sesión |
| `B/modules/preferencias/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `PreferenciasService.java`, `PreferenciasController.java`, puerto consumidor de servicios y adaptador | Modificar parcialmente tras autorización | Preservar lista HTTP y límites, extraer Query/Result y lectura | Varias | servicios SPEC-08 y sesión |

## UC083 — Exportar y descargar historial

Endpoint(s): POST /api/v1/historial/exportaciones; GET /api/v1/historial/exportaciones/{id}.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-083 solo cuenta autorizada | servicios.porUsuario(usuarioId); descargar filtra d.usuarioId | Cumple | No selecciona cuenta externa; ajena/inexistente mismo 404 | Conservar | — |
| Campos equivalentes | CSV servicio_id/estado/fecha_contratacion; ServicioResumen mismos datos | Cumple | Misma selección de campos, sin contacto ni descripción libre | Conservar | — |
| Volumen grande y equivalencia snapshot | exportar usa porUsuario sin página y construye StringBuilder completo | No cumple | Edge case exige paginación; exportación carga todo y no ofrece snapshot/filtros de la consulta de pantalla | Definir D-15 y procesar paginado/streaming coherente sin nueva tabla inferida | Alta |
| Consistencia storage/metadata | storage.guardar antes descargas.guardar | No cumple | Si falla persistencia, archivo puede quedar huérfano; no compensación | Adaptador transaccional acotado a exportación | Alta |
| Reintento | cada POST crea archivo y metadata nuevos; no modifica servicios | Parcial | Preserva historial, pero reintento no reutiliza snapshot; no hay evidencia de igualdad bajo mutaciones concurrentes | Resolver D-15; pruebas autorizadas | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | PreferenciasService#exportarHistorial/#descargarExportacion y HistorialCsvExporter y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/preferencias/application/usecase/ExportarHistorialTrabajosUseCase.java`, `service/ExportarHistorialTrabajosService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | historial UC082, servicios, DescargaRepository y ArchivoStorageService |
| `B/modules/preferencias/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `PreferenciasService.java`, `HistorialCsvExporter.java`, controller, adaptador de exportación; contrato adicional DescargarExportacionHistorialUseCase | Modificar parcialmente tras autorización | Exportación autorizada, paginada y consistente con storage | Varias | historial UC082, servicios, DescargaRepository y ArchivoStorageService |

## UC084 — Consultar indicadores propios

Endpoint(s): GET /api/v1/prestadores/mis-indicadores.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Rol y actividad propia | roles.esPrestador y servicios.porUsuario(prestadorId) | Cumple | Rechaza otros roles; identidad de sesión | Conservar | — |
| Cancelados y moderación | total=todos.size(); enCurso CONTRATADO/EN_EJECUCION; finalizados TERMINADO/FINALIZADO | Parcial | Total incluye cancelados y sin filtro moderación; subtotales excluyen cancelados. No afirmar exclusión total; regla y métricas requieren D-15 | Acordar significado de cada métrica y dataset | Alta |
| Fórmulas/version/periodo | Indicadores record sin formulaVersion ni periodo; D-15 abierto | Parcial | No fuente aprobada de fórmulas; TERMINADO contado como finalizado no significa finalización formal | Definir fórmula/version/periodo antes de modificar contrato | Alta |
| Reproducibilidad | Cálculo en memoria sobre servicios y reputación actuales | No verificable | Sin dataset ni pruebas ejecutadas de indicador | Pruebas tras política aprobada | Alta |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | PreferenciasService#misIndicadores y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/preferencias/application/usecase/ConsultarIndicadoresPrestadorUseCase.java`, `service/ConsultarIndicadoresPrestadorService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | servicios, confianza y rol PRESTADOR; D-15 |
| `B/modules/preferencias/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `PreferenciasService.java`, controller/DTO y puertos consumidor de servicios/confianza | Modificar parcialmente tras autorización | Separar cálculo y explicitar fórmulas aprobadas sin inventar rentabilidad | Varias | servicios, confianza y rol PRESTADOR; D-15 |

## UC085 — Consultar actividad reciente

Endpoint(s): GET /api/v1/actividad-reciente.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-085 propia y cronológica | servicios.porUsuario y notificaciones.listarPropias; sort fecha desc limit20 | Cumple | Proyección de fuentes autorizadas, sin tabla duplicada | Conservar | — |
| Desempate | Comparator.comparing(Actividad::fecha).reversed() | No cumple | Edge case pide desempate; Actividad no tiene referencia/secuencia y repositorios no garantizan empates | Ordenar por fecha y clave estable interna; decidir representación sin cambio silencioso | Media |
| Actualidad y volumen | Servicio usa fechaContratacion con estado actual; carga todo antes de limitar20 | Parcial | Un cambio reciente de servicio conserva fecha antigua; no aparece como evento de transición. Riesgo de coste creciente | Definir eventos de actividad y consumir fuente existente con límites, no duplicar historial | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | PreferenciasService#actividadReciente y ActividadNotificacionesPort y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/preferencias/application/usecase/ConsultarActividadRecienteUseCase.java`, `service/ConsultarActividadRecienteService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | servicios propios, notificaciones propias, UC081/082 |
| `B/modules/preferencias/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `PreferenciasService.java`, `ActividadNotificacionesPort`, controller/DTO y mapper | Modificar parcialmente tras autorización | Orden determinista, alcance de eventos y lectura eficiente | Varias | servicios propios, notificaciones propias, UC081/082 |

## Hallazgos comunes y dependencias

- La implementación real agrupa historial, indicadores y actividad en preferencias. El árbol del plan separa módulos; no reorganizar globalmente sin autorización. Extraer por CU con nombres y paquetes reales, no crear repositorios de lectura redundantes.
- Controller depende de PreferenciasService y hace parseo/mapeo; servicios usan @Service/@Transactional y errores HTTP. No UseCase propio/ejecutar ni Command/Result dedicados. Mapper persistencia separado de HTTP y entidades JPA no salen al controlador.
- Servicios/confianza/rol usan puertos de proveedores; regla vigente pide contratos consumidor. ActividadNotificacionesPort ya está correctamente definido en consumidor; ConsultaPreferenciasService es separación deliberada para evitar ciclo NotificacionService→PreferenciasService→NotificacionService. No fusionarlo.
- V6 contiene preferencias y descargas, PK y CHECK de avisos obligatorios. No tablas duplicadas para historial/indicadores/actividad. Ejecución DB y transacciones no acreditadas.
- Exportación es storage privado, descarga verifica propietario; no inferir archivo público por devolver archivoKey. Persistencia y archivo no son atómicos; faltan compensación y volumen grande paginado.
- D-15: formato, límites, snapshot y fórmulas deben acordarse. SPEC exige incluir cancelados/en moderación, pero el código incluye todos en total y no en todo subtotal; no cambiar sin definir métrica.
- Seguridad: rutas autenticadas; historial/preferencias usan identidad de sesión; indicador valida Prestador mediante RolConsultaPort. 401 sin sesión, 403 para indicador sin rol.
- No pruebas específicas de estos cinco CU halladas en inventario, existen pruebas indirectas de preferencias/notificaciones en CorreccionesVerificacionTest. Ninguna ejecutada. No se acreditan SC-001–004 por inspección.

| Tareas | Estado comprobado |
|---|---|
| T001 | Parcial: implementación agrupada real, falta patrón |
| T002–T003 | V6 y enum tipos/canales existentes; ejecución no acreditada |
| T004–T005 | Puertos/lectura autorizada presentes; patrón y verificación pendientes |
| T006 | Paginación historial presente; exportación y desempate incompletos |
| T007 | Fórmulas sin decisión D-15 |
| T009–T011 | Preferencias y consultas previas a entrega presentes; aceptación no acreditada |
| T013–T014 | Historial propio paginado presente; UseCase pendiente |
| T016–T018 | CSV y descarga propia presentes; volumen/snapshot/compensación pendientes |
| T020–T023 | Indicadores actuales presentes, versión/fórmula y política pendientes |
| T025–T026 | Proyección propia limitada20 presente; desempate ausente |
| T008/T012/T015/T019/T024 y F14-QA-* | No verificable por ejecución |
| F14-FE-* | No evaluadas |

Orden recomendado: UC081 → UC082 → UC083 → UC084 → UC085. Resolver D-15 antes de cerrar exportación/indicadores; no cambiar contratos ni crear migraciones en auditoría.

