# Auditoría estática SPEC-08 — Contratación y ciclo de vida del servicio

**Estado de seguimiento:** el usuario solicitó marcar la refactorización como finalizada. Permanecen pendientes la idempotencia compartida, concurrencia, D-10 y aceptación integral; este estado no acredita dichas verificaciones.

**Avance posterior:** se extrajeron las operaciones UC045–UC056 a UseCases y servicios; las matrices siguientes conservan el diagnóstico inicial. Se ejecutaron 29 pruebas unitarias con JUnit local, todas exitosas, y se compiló de forma enfocada producción con javac. Maven wrapper falló antes de iniciar; no se acreditó compilación completa, arranque Spring, integración PostgreSQL ni concurrencia. Idempotencia compartida y D-10 siguen pendientes; no se ejecutaron migraciones.

Alcance: UC045–UC056. Los doce CU tienen implementación **Parcial** y auditoría estática completa (**Revisado**); esto no acredita refactorización ni aceptación. No se ejecutaron pruebas, compilación, migraciones o peticiones de escritura. La consulta de lectura al OpenAPI local agotó el tiempo de espera de 5 segundos: no se verificó el contrato en ejecución.

Fuentes consultadas: AGENTS.md; SPEC-08 y su plan completos; secciones pertinentes del Plan General de seguridad, persistencia, idempotencia, comunicación y decisiones; decisiones resueltas y abiertas D-03/D-05/D-10/D-18; requisitos relacionados de SPEC-04/07/09; código de servicios, propuestas, solicitudes, notificaciones e idempotencia; V4 y pruebas existentes. Contrato HTTP cotejado con el plan y controladores.

Abreviaturas: **B** = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`, **S** = `B/modules/servicios/`, **P** = `B/modules/propuestas/`.

## UC045 — Consultar propuestas recibidas

Endpoint: `GET /api/v1/solicitudes/{solicitudId}/propuestas`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-045: propuestas activas de solicitud propia | `P/application/PropuestaService.java#propuestasDeSolicitud/#activasDeSolicitud` | Cumple | Filtra propietario y devuelve solo ACTIVA, con disponibilidad y mensaje | Conservar | — |
| Autenticación y rol | `PropuestaController#propuestasDeSolicitud`, `SecurityUtils.exigirRol("CLIENTE")` | Cumple | Identificador de sesión; ajena/inexistente mismo 404 | Conservar | — |
| UseCase y mapeos | Controller inyecta PropuestaService y usa aRespuesta privado | No cumple | Servicio agrupado y dominio como salida | Extraer ListarPropuestasRecibidasUseCase/Service, Query/Result y mapper | Alta |
| Ubicación y dependencias | Código en propuestas; plan propone entrada en servicios | Parcial | Responsabilidad existente que debe preservarse; no mover por estética ni duplicar | Diseñar extracción con contrato consumidor y conservar endpoint | Media |
| Aceptación ejecutada | Sin pruebas específicas ejecutadas | No verificable | No se acreditó aislamiento/completitud | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `S/application/usecase/ListarPropuestasRecibidasUseCase.java`, Service y Query/Result según ubicación final | Crear | Contrato UC045 | Aplicación | Solicitudes y propuestas |
| Puerto consumidor y adaptadores de consulta | Crear/reutilizar | No depender de concretos | Aplicación/infraestructura | SPEC-04/07 |
| `P/presentation/controller/PropuestaController.java`, mapper y servicio legado | Modificar parcialmente | Interfaz sin cambiar ruta | Presentación/aplicación | Mantener PropuestaConsultaPort |

## UC046 — Aceptar propuesta

Endpoint: `POST /api/v1/propuestas/{id}/aceptar`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-046: aceptación propia activa | `ServicioService#aceptarPropuesta` | Cumple | Valida dueño, propuesta ACTIVA, solicitud PUBLICADA y ausencia de servicio | Conservar | — |
| Una contratación por solicitud/propuesta | V4 uq_servicio_por_solicitud/uq_servicio_por_propuesta; entidad y adaptador | Cumple | BD impide dos servicios por origen | Preservar constraints | — |
| Operación consistente | aceptarPropuesta @Transactional; puertos y evento síncrono | Parcial | Engloba nominalmente servicio, propuesta, cierre, solicitud e historial; rollback/concurrencia no ejecutados | Mantener transacción y verificar PostgreSQL | Alta |
| Replay aislado | ServicioController#aceptar → BaseController#ejecutarIdempotente | No cumple | Replay clave+operación antes de propiedad: otro CLIENTE puede obtener respuesta ajena con igual clave/ID | Corregir idempotencia transversal con alcance/verificación | Crítica |
| Atomicidad de respuesta | BaseController y servicio | No cumple | Respuesta se guarda tras commit del negocio | Coordinar intención/negocio/resultado, D-18 | Alta |
| Carrera con nuevas propuestas | Alta SPEC-07 frente al cierre en aceptación | Parcial | Una creación que leyó PUBLICADA podría entrar después de enumerar/cerrar activas; riesgo estático | Coordinar entre agregados y verificar | Alta |
| UseCase/mapeos/errores | Controller → ServicioService → dominio; BusinessException en aplicación | No cumple | Sin interfaz/Command/Result/mapper ni núcleo puro | Extraer AceptarPropuestaUseCase/Service y errores semánticos | Alta |
| SC-001/002 | Sin ejecución aceptación/concurrencia | No verificable | Constraints no acreditan todo el flujo | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `S/application/usecase/AceptarPropuestaUseCase.java`, Service y Command/Result | Crear | Entrada de contratación | Aplicación | UC047/048/049 |
| `S/application/port/` gestión de propuestas/solicitudes y adaptadores | Crear/reutilizar | Puertos consumidor | Aplicación/infraestructura | SPEC-04/07 |
| `S/presentation/mapper/ServicioPresentationMapper.java`, config y excepciones | Crear/reutilizar | Capas y transacción | Varias | Otros CU |
| ServicioController, ServicioService y handler | Modificar parcialmente | Integración | Varias | Preservar ServicioConsultaPort/ServicioLecturaPort |
| BaseController/idempotencia compartida | Corrección por delimitar | Aislamiento/atomicidad | Transversal | Auth, solicitudes, propuestas, servicios |

## UC047 — Cerrar propuestas no seleccionadas

Incluido en aceptación; sin endpoint independiente.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-047: cierre automático | aceptarPropuesta → PropuestaConsultaPort#cerrarActivasDeSolicitud; PropuestaService | Cumple | Cierra ACTIVA diferentes de la aceptada dentro de aceptación | Conservar | — |
| Concurrencia altas/retiros | Preconsulta y @Version propuesta | Parcial | Protege filas existentes; cierre de altas concurrentes no acreditado | Coordinar con UC046/SPEC-07 | Alta |
| UseCase propio | Operación de puerto en servicio agrupado | No cumple | Falta interfaz propia ejecutar | Contrato útil sin duplicar ni añadir endpoint | Alta |
| SC-001 | Sin prueba ejecutada de cierre atómico | No verificable | No acredita conjunto de propuestas al confirmar | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| Contrato/Service de cierre bajo aplicación según módulo propietario acordado | Crear | CU explícito | Aplicación | UC046 y propuestas |
| Puerto gestión propuestas y adaptador | Crear/reutilizar | Acceso a módulo dueño | Aplicación/infraestructura | SPEC-07 |
| PropuestaService#cerrarActivasDeSolicitud y composición aceptación | Modificar parcialmente | Delegar sin duplicar | Aplicación/infraestructura | Transacción UC046 |

## UC048 — Registrar servicio contratado

Incluido en aceptación.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-048: relaciones y estado inicial | aceptarPropuesta construye ServicioContratado e historial | Cumple | Vincula solicitud, propuesta, Cliente, Prestador; CONTRATADO | Conservar | — |
| Persistencia/esquema | ServicioEntity, mapper, adapter y V4 | Cumple | Campos, versión y constraints coherentes; JPA separado | Reutilizar, no migración | — |
| UseCase propio | Construcción inline en aceptarPropuesta | No cumple | Falta entrada explícita de CU incluido | Extraer responsabilidad útil sin transacción independiente | Alta |
| Unicidad/atomicidad ejecutada | Restricciones y transacción | No verificable | Sin prueba PostgreSQL de carreras/rollback | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| Contrato/Service de registro y entrada útil | Crear según diseño | CU incluido explícito | Aplicación | UC046 |
| Modelo/regla de creación e historial inicial | Reutilizar/extraer | No duplicar construcción | Dominio/aplicación | Repositorio |
| Config aceptación | Crear/reutilizar | Operación atómica | Infraestructura | UC047/049 |

## UC049 — Habilitar comunicación

Incluido en aceptación.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-049: habilitación al contratar | aceptarPropuesta asigna comunicacionHabilitada=true | Cumple | Se guarda en mismo servicio | Conservar | — |
| Canal mediado | enviar/listarMensajes y DTO | Cumple | No comparte correo/teléfono del perfil | Conservar | — |
| Contrato por CU | Flag inline en constructor | No cumple | Falta descomposición explícita de habilitación | Operación/regla útil sin clase vacía ni endpoint artificial | Alta |
| Canal tras cierre | transitarA conserva true | Parcial | Sigue habilitado tras CANCELADO/FINALIZADO; D-10 pendiente | Decidir política antes de restringir | Media |
| Verificación | Sin escenarios ejecutados | No verificable | No acredita bloqueo previo/habilitación atómica | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| Contrato/regla de habilitación y Service según diseño | Crear/reutilizar | Responsabilidad de CU incluido | Dominio/aplicación | UC046/048/056 |
| ServicioContratado y config aceptación | Modificar si necesario | Operación única | Dominio/infraestructura | D-10 si cambia cierre |
| No nuevos DTO HTTP/endpoints | No crear | CU interno | Presentación | Contrato existente |

## UC050 — Consultar detalle del servicio

Endpoint: `GET /api/v1/servicios/{id}`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-050: ambas partes | detalleCompleto y participante | Cumple | Solo Cliente/Prestador; tercero 404 genérico | Conservar | — |
| Detalle completo | detalleCompleto y controller#aRespuestaCompleta | Cumple | Solicitud, propuesta aceptada y estado | Conservar campos | — |
| Result/mapper | ServicioService.DetalleCompleto y mapper privado | No cumple | Agregados de tres módulos llegan a controller | Result y mapper explícito | Alta |
| UseCase/transacción | Controller inyecta ServicioService | No cumple | Sin ConsultarServicioUseCase ni Service puro | Extraer y configurar lectura | Alta |
| Verificación | Sin aceptación ejecutada | No verificable | No acredita igualdad ambas partes | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `S/application/usecase/ConsultarServicioUseCase.java`, Service y Query/Result | Crear | Lectura propia | Aplicación | Repositorio y consultas de origen |
| Mapper/config lectura | Crear/reutilizar | Result→Response | Presentación/infraestructura | DTO vigente |
| Controller y detalle legado | Modificar parcialmente | Interfaz y salida estable | Presentación/aplicación | ServicioLecturaPort |

## UC051 — Marcar en ejecución

Endpoint: `POST /api/v1/servicios/{id}/en-ejecucion`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-051: Prestador del servicio | enEjecucion y participante | Cumple | Comprueba identidad y estado por dominio | Conservar | — |
| Historial/transición | transitar y ServicioContratado#transitarA | Cumple | CONTRATADO→EN_EJECUCION con actor/evento | Conservar | — |
| Replay y autorización | BaseController antes de servicio | No cumple | Devuelve replay sin comprobar participante | Aislamiento compartido | Crítica |
| UseCase/errores | Servicio agrupado y dominio BusinessException | No cumple | Sin interfaz y HTTP en núcleo | Extraer contrato y excepción semántica | Alta |
| Concurrencia | @Version y handler 409 | Parcial | Protección presente; sin PostgreSQL ejecutado | Verificar transacción/historial | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| MarcarServicioEnEjecucionUseCase/Service, Command/Result | Crear | Operación propia | Aplicación | Repositorio/eventos |
| ServicioContratado, excepción y handler | Modificar/crear | Transición sin HTTP | Dominio/presentación | UC052–054 |
| Controller, mapper/config | Modificar/reutilizar | Flujo y transacción | Varias | Idempotencia compartida |

## UC052 — Marcar terminado

Endpoint: `POST /api/v1/servicios/{id}/terminado`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-052: Prestador y ejecución previa | terminado y transitarA | Cumple | EN_EJECUCION→TERMINADO | Conservar secuencia ya resuelta documentalmente | — |
| Historial/evento | transitar | Cumple | Registra actor y emite evento | Preservar transacción | — |
| UseCase/mapas/excepciones | Controller y ServicioService | No cumple | Agrupación y dominio HTTP | Extraer contrato y errores semánticos | Alta |
| Replay/concurrencia | BaseController/@Version | Parcial | Concurrencia no ejecutada; replay crítico transversal | Corregir compartido/verificar | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| MarcarServicioTerminadoUseCase/Service y Command | Crear | Operación propia | Aplicación | Repositorio/eventos |
| Result/mapper/config/regla transición | Reutilizar | Evitar duplicación | Varias | UC051/053/054 |
| Controller y legado | Modificar parcialmente | Interfaz | Presentación/aplicación | Preservar puertos |

## UC053 — Confirmar finalización

Endpoint: `POST /api/v1/servicios/{id}/confirmar-finalizacion`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-053: Cliente y TERMINADO | confirmarFinalizacion y dominio | Cumple | Exige Cliente y estado TERMINADO | Conservar | — |
| Estado terminal | transitarA | Cumple | FINALIZADO sin transición posterior | Conservar | — |
| UseCase y mapeo | Controller/ServicioService | No cumple | Falta interfaz/Result | Extraer UseCase/Service/Command/Result | Alta |
| Replay/concurrencia | Plantilla y @Version | Parcial | Replay omite autorización del servicio; carreras no ejecutadas | Corregir compartido y verificar | Alta |
| SC-003/004 | ServicioEstadoTest no ejecutado | No verificable | Sin aceptación transaccional | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| ConfirmarFinalizacionServicioUseCase/Service y Command | Crear | Operación propia | Aplicación | Repositorio/eventos |
| Result, mapper/config y excepción transición | Reutilizar | Flujo/errores | Varias | UC051/052/054 |
| Controller y método legado | Modificar parcialmente | Interfaz | Presentación/aplicación | Consumidores del servicio finalizado |

## UC054 — Cancelar servicio

Endpoint: `POST /api/v1/servicios/{id}/cancelar`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-054: participante antes de finalizado | cancelar, participante y dominio | Cumple | CONTRATADO/EN_EJECUCION/TERMINADO→CANCELADO; rechaza FINALIZADO/CANCELADO | Conservar | — |
| Historial/terminalidad | transitarA | Cumple | CANCELADO terminal e historial generado | Conservar | — |
| UseCase/excepciones | Controller y dominio | No cumple | Sin contrato propio; HTTP en dominio | Extraer operación y excepción | Alta |
| Canal tras cancelación | Flag se conserva true | Parcial | D-10 pendiente, no incumplimiento automático | Decidir política específica | Media |
| Replay/concurrencia | BaseController/@Version | Parcial | Riesgos sin verificación | Corregir aislamiento/probar carreras | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| CancelarServicioUseCase/Service y Command | Crear | CU propio | Aplicación | Repositorio/eventos |
| Modelo/excepción/config/mapper | Reutilizar/modificar | Consistencia | Varias | UC051–053 |
| Política del canal | Solo tras decisión | D-10 | Dominio/aplicación | UC049/056 |

## UC055 — Consultar historial

Endpoint: `GET /api/v1/servicios/{id}/historial`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-055: propio y ordenado | historial, participante, ServicioEntity @OrderBy(fecha ASC) | Cumple | Solo participantes; orden por fecha al persistir/leer | Conservar | — |
| Registro de transiciones | Creación/transitarA | Cumple | Estado inicial/cambios con actor | Conservar | — |
| UseCase/mapper | Stream del dominio en controller | No cumple | Sin interfaz/Result/mapper | Extraer ConsultarHistorialServicioUseCase/Service | Alta |
| Orden bajo concurrencia | @Version y cascada | No verificable | No ejecución que acredite una transición y un historial coherente | Verificar PostgreSQL | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| ConsultarHistorialServicioUseCase/Service, Query/Result | Crear | Operación propia | Aplicación | Repositorio |
| Mapper/config lectura | Reutilizar | Sin dominio en controller | Presentación/infraestructura | ServicioDtos |
| Controller y legado | Modificar parcialmente | Interfaz | Presentación/aplicación | Consultas de participantes |

## UC056 — Usar canal de comunicación

Endpoints: `POST` y `GET /api/v1/servicios/{id}/mensajes`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-056: participante/habilitación | enviarMensaje, listarMensajes, participante | Cumple | Solo partes con flag true; false 409 | Conservar | — |
| Canal sin contactos de perfil | MensajeResponse y ServicioDtos | Cumple | Backend no añade correo/teléfono/dirección; contenido es texto del usuario | No inventar filtrado de texto | — |
| Persistencia/lectura | Agregado mensajes, mapper y V4 | Cumple | Remitente/fecha y lectura cronológica | Reutilizar, sin realtime nuevo | — |
| Replay/payload | BaseController usa operación por servicio | No cumple | Igual clave/servicio devuelve mensaje anterior con otro contenido o actor antes de acceso | Aislar actor/intención y validar payload, D-18 | Crítica |
| Canal tras cierre | Flag se conserva | Parcial | Envío/lectura después del cierre; D-10 pendiente | Definir estados sin cambio silencioso | Media |
| UseCase/Result/mapper | Controller y servicio agrupado | No cumple | Faltan contratos enviar/consultar y DTO aplicación | Extraer ambos UseCases/Services | Alta |
| Concurrencia/aceptación | Agregado completo; sin ejecución | No verificable | No se acreditó envío concurrente sin pérdidas ni bloqueo previo | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| EnviarMensajeServicioUseCase/Service y ConsultarMensajesServicioUseCase/Service | Crear | Operaciones del CU | Aplicación | Repositorio |
| Commands/Queries/Results de mensajes | Crear | Separar transporte | Aplicación | Mapper |
| Controller, mapper/config y legado | Modificar parcialmente | Interfaces/transacción | Varias | UC049 |
| Idempotencia común | Corrección delimitada | Actor/contenido | Transversal | Otros módulos |
| Regla envío/lectura postcierre | Solo con decisión | D-10 | Dominio/aplicación | SPEC-15 futuro |

## Hallazgos compartidos, decisiones y límites

1. **D-18 crítica:** siete POST de servicios usan plantilla común. Replay ocurre antes de propiedad/rol específico del servicio. Puede revelar mensaje/remitente/estado a otro actor; no implica ejecutar una mutación ajena, sino omitir la validación al devolver replay. Negocio/respuesta también tienen commits separados. El usuario condicionó antes la corrección a no perjudicar otros módulos.
2. **Aceptación nominal consistente:** transacción, unique por solicitud/propuesta y @Version existen. No afirmar que faltan. La carrera con nueva propuesta después de enumerar/cerrar activas necesita coordinación SPEC-07 y verificación PostgreSQL.
3. **TERMINADO resuelto:** FR-052 y decisiones documentadas exigen EN_EJECUCION→TERMINADO pese nota vieja SPEC/plan. No se necesita decisión nueva para conservar secuencia estricta.
4. **D-10:** comunicación permanece habilitada después de FINALIZADO/CANCELADO. No deshabilitar por suposición: falta política envío/lectura postcierre.
5. **Arquitectura:** doce CU sin UseCase/Service/ejecutar propio. Dominio llega a presentación, mapper privado y Spring/HTTP en núcleo. UC047/048/049 no aparecen como interfaces independientes en plan: diseñar responsabilidad útil sin duplicar ni crear endpoints.
6. **Límites:** UC045 vive en propuestas y requiere cambiar ese controlador dentro de alcance. ServicioService implementa ServicioConsultaPort/ServicioLecturaPort consumidos por otros módulos; no eliminarlo indiscriminadamente. Adaptar puertos al consumidor evitando ciclos.
7. **Persistencia:** tablas/versiones/constraints ya presentes en V4 y coherentes. No crear repositorios separados de historial/mensajes redundantes ni alterar migraciones. Mapper reconstruye agregado con cascada/orphanRemoval: verificar concurrencia de colecciones e historial, no asumirla garantizada.
8. **Integridad:** handler devuelve 409 REGISTRO_DUPLICADO para toda DataIntegrityViolationException. Una carrera de segunda contratación podría no dar SERVICIO_YA_CONTRATADO; revisar traducción sin detalles internos.
9. **SPEC-09:** eventos aceptación/cambio estado existen; listener síncrono registra en transacción. Entrega externa/preferencias/proveedor no se cierran con auditoría SPEC-08. Ordinal de estado es versión de evento; no ampliar estados sin revisar deduplicación.
10. **SPEC-02:** adaptador provisional de eliminación considera CONTRATADO/EN_EJECUCION/TERMINADO. Usuario restringió bloqueo a ejecución y quedó pregunta sobre TERMINADO. No extrapolar Notes plan08 ni dar F08-QA-04 por cumplido.
11. **OpenAPI/documentación:** package y enlace SPEC del plan obsoletos. Contrato vivo no disponible por timeout; no afirmar schemas correctos/erróneos. Solo mensajes tiene request body `{contenido}`; otros comandos usan path/cabecera opcional.
12. **Pruebas:** ServicioEstadoTest contiene cinco pruebas de secuencia, terminalidad y cancelación; exige BusinessException.Conflict. Ninguna ejecutada en esta auditoría. No se acreditan HTTP, aceptación, mensajes o concurrencia.

## Tareas y orden recomendado

| Tarea | Estado comprobado |
|---|---|
| T001 | Parcial: módulo/capas existen, faltan UseCases/mapeos |
| T002 | Tablas/versiones/constraints presentes en V4; ejecución no acreditada |
| T003–T004 | No verificable por ejecución |
| T005–T006 | Modelo/repositorio/adapter presentes |
| T007 | Lista propia activa presente; arquitectura/aceptación pendientes |
| T008–T009 | Parcial: aceptación/side effects presentes; aislamiento/carreras/arquitectura pendientes |
| T010–T011 | No verificable por ejecución |
| T012–T013 | Detalle completo propio presente; arquitectura/aceptación pendientes |
| T014–T015 | No verificable por ejecución |
| T016–T017 | Historial integrado al agregado/persistencia; no duplicar repositorio |
| T018–T023 | Transiciones, @Version, historial/eventos presentes; patrón/concurrencia ejecutada pendientes |
| T024–T025 | No verificable por ejecución |
| T026–T027 | Historial propio presente; patrón/aceptación pendientes |
| T028–T029 | No verificable por ejecución |
| T030–T031 | Modelo/mensajes persistidos en agregado |
| T032–T034 | Envío/lectura por participante y flag presentes; D-18/D-10/arquitectura/aceptación pendientes |
| F08-FE-* | No evaluadas |
| F08-QA-* | No acreditadas por ejecución; idempotencia/carreras tienen hallazgos concretos |

Orden recomendado: **UC045 → UC046 con dependencias UC047/048/049 → UC050 → UC051 → UC052 → UC053 → UC054 → UC055 → UC056**. Antes de cerrar aceptación y escrituras debe resolverse alcance D-18/verificaciones. D-10 bloquea solo cambios de política de comunicación. Se pueden extraer responsabilidades sin inventar reglas de agenda/contacto. No se propone eliminar archivos ni crear migraciones durante auditoría.
