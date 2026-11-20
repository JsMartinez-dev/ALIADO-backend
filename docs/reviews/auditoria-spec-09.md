# Auditoría estática SPEC-09 — Notificaciones de propuestas y servicios

**Refactorización posterior.** UC057 y UC058 usan contratos de aplicación para registrar avisos. Si el único canal es el log, quedan pendientes sin consumir intentos. El proveedor real, la coordinación entre workers y la idempotencia concurrente siguen pendientes. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Alcance UC057–UC058; ambos Parcial y Revisado. El estado de seguimiento finalizado solicitado por el usuario no acredita cumplimiento funcional de estos pendientes. Solo se crea este documento; no pruebas, compilación, aplicación ni migraciones ejecutadas. La lectura GET de OpenAPI local agotó el timeout de 4 segundos; no se identificó contrato estático y el contrato se cotejó con plan y controller.

Fuentes: AGENTS.md; SPEC-09 y plan completos; secciones pertinentes Plan General de eventos, notificaciones, integridad y proveedor pendiente; SPEC-08 y puntos de publicación ya refactorizados; SPEC-06/07/14 y decisiones pertinentes D-08; código de notificaciones, eventos y preferencias, esquema V4, pruebas existentes. N = `src/main/java/com/sebastian/spring/mas/aliado/aliado/modules/notificaciones/`.

## UC057 — Notificar aceptación de propuesta

Evento interno PropuestaAceptada; consulta GET /api/v1/notificaciones

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-057: evento y destinatario seleccionado | AceptarPropuestaService publica PropuestaAceptada; listener#alAceptarPropuesta | Cumple | Disparo automático de aceptación y destinatario prestador del evento; registro síncrono dentro transacción nominal | Conservar | — |
| Evento consultable en origen | Servicio e historial persistidos en SPEC-08 | Cumple | La caída de entrega externa no elimina por diseño la información del servicio | Preservar separación registro/entrega | — |
| SC-004: sin ruta pública para fabricar avisos | NotificacionController solo GET; listener interno | Cumple | No existe POST público de creación; productores de negocio publican eventos | Conservar; validación extra de origen sería recomendación a delimitar | — |
| Entrega real y falla de canal | LogCanalEntregaAdapter#entregar; NotificacionService#entregarPendientes | No cumple | ACEPTACION_PROPUESTA y CAMBIO_ESTADO_SERVICIO se marcan ENTREGADA por un log sin proveedor real; solo NUEVA_OPORTUNIDAD fue aislada | Definir e implementar proveedor autorizado o mantener pendiente sin falsear entrega | Alta |
| Unicidad de registro | NotificacionService#registrar; V4 uq_notificacion_evento | Parcial | Preconsulta y unique protegen fila; dos registros concurrentes pueden provocar rollback/conflicto de negocio en vez de converger | Verificar y coordinar registro idempotente en transacción | Alta |
| Entrega concurrente | findPendientesParaEntrega, entity sin versión/claim; scheduler cada minuto | Parcial | Varios workers pueden leer/enviar la misma fila; no hay reclamación atómica ni clave de idempotencia de proveedor acreditada | Diseñar reserva y entrega idempotente con esquema existente si viable | Alta |
| Reintentos | entregarPendientes y query intentos < 3 | Parcial | Reintenta por minuto hasta tres; tras tres queda FALLIDA sin selección posterior, no se garantiza recuperación si proveedor vuelve luego | Acordar política de reintento/backoff y recuperación | Alta |
| Preferencias frente a SC 100% | registrar y entregarPendientes consultan avisoHabilitado | Parcial | Aviso deshabilitado puede no registrarse; requiere interpretar SC-001/002 con SPEC-14 | Resolver política sin ignorar preferencias | Media |
| UseCase, mapeos y framework | NotificacionService agrupado; NotificacionController consulta NotificacionConsultaPort; scheduler EntregaNotificacionesPort | No cumple | Faltan Registrar/ConsultarPropias/Entregar UseCase ejecutar; Spring en aplicación y mapper privado general | Extraer interfaces/Services/DTO y mapper; preservar consulta oportunidades ya migrada | Alta |
| Aceptación y entrega ejecutadas | CorreccionesVerificacionTest cubre preferencias indirectamente | No verificable | No ejecutado; no demuestra proveedor, duplicados, rollback ni recuperación | Etapa autorizada de pruebas unitarias y PostgreSQL | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| N/application/usecase/RegistrarNotificacionUseCase.java, ConsultarNotificacionesPropiasUseCase.java, EntregarNotificacionesPendientesUseCase.java y Services | Crear | Contrato explícito por operación, conservando productores | Aplicación | SPEC-06/07/08/14 |
| N/application/dto/ registro/consulta/entrega, presentation/mapper/NotificacionPresentationMapper.java y config | Crear/reutilizar/ampliar | Datos independientes y composición | Aplicación/presentación/infraestructura | GET general y oportunidades |
| N/infrastructure/adapter/NotificacionEventListener.java, scheduler/EntregaNotificacionesScheduler.java, NotificacionController y NotificacionService | Modificar parcialmente | Entradas dependen de interfaces ejecutar | Varias | Conservar puertos actuales |
| CanalEntregaNotificacion, LogCanalEntregaAdapter y persistencia de entrega | Modificar según decisión de proveedor y concurrencia | Entrega real, reintentos y duplicados | Aplicación/infraestructura | Todos tipos de aviso; sin migración implícita |

## UC058 — Notificar cambio de estado

Evento interno ServicioEstadoCambiado; consulta GET /api/v1/notificaciones

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-058: contraparte y transiciones | TransicionServicio publica; listener#alCambiarEstadoServicio | Cumple | Si actor cliente, destinatario prestador y viceversa; cubre estados actuales con versión de evento | Conservar y revisar deduplicación si amplían estados | — |
| Historial disponible | ServicioContratado y repositorio/historial SPEC-08 | Cumple | Estado e historial son fuente consultable independiente del canal | Preservar | — |
| Exposición propia | NotificacionController#listar usa sesión y filtro tipo | Cumple | Bandeja por destinatario, tipo opcional, sin cuerpo ni endpoint de envío; 401 sin sesión | Conservar | — |
| Entrega real y falla de canal | LogCanalEntregaAdapter#entregar; NotificacionService#entregarPendientes | No cumple | ACEPTACION_PROPUESTA y CAMBIO_ESTADO_SERVICIO se marcan ENTREGADA por un log sin proveedor real; solo NUEVA_OPORTUNIDAD fue aislada | Definir e implementar proveedor autorizado o mantener pendiente sin falsear entrega | Alta |
| Unicidad de registro | NotificacionService#registrar; V4 uq_notificacion_evento | Parcial | Preconsulta y unique protegen fila; dos registros concurrentes pueden provocar rollback/conflicto de negocio en vez de converger | Verificar y coordinar registro idempotente en transacción | Alta |
| Entrega concurrente | findPendientesParaEntrega, entity sin versión/claim; scheduler cada minuto | Parcial | Varios workers pueden leer/enviar la misma fila; no hay reclamación atómica ni clave de idempotencia de proveedor acreditada | Diseñar reserva y entrega idempotente con esquema existente si viable | Alta |
| Reintentos | entregarPendientes y query intentos < 3 | Parcial | Reintenta por minuto hasta tres; tras tres queda FALLIDA sin selección posterior, no se garantiza recuperación si proveedor vuelve luego | Acordar política de reintento/backoff y recuperación | Alta |
| Preferencias frente a SC 100% | registrar y entregarPendientes consultan avisoHabilitado | Parcial | Aviso deshabilitado puede no registrarse; requiere interpretar SC-001/002 con SPEC-14 | Resolver política sin ignorar preferencias | Media |
| UseCase, mapeos y framework | NotificacionService agrupado; NotificacionController consulta NotificacionConsultaPort; scheduler EntregaNotificacionesPort | No cumple | Faltan Registrar/ConsultarPropias/Entregar UseCase ejecutar; Spring en aplicación y mapper privado general | Extraer interfaces/Services/DTO y mapper; preservar consulta oportunidades ya migrada | Alta |
| Aceptación y entrega ejecutadas | CorreccionesVerificacionTest cubre preferencias indirectamente | No verificable | No ejecutado; no demuestra proveedor, duplicados, rollback ni recuperación | Etapa autorizada de pruebas unitarias y PostgreSQL | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| N/application/usecase/RegistrarNotificacionUseCase.java, ConsultarNotificacionesPropiasUseCase.java, EntregarNotificacionesPendientesUseCase.java y Services | Crear | Contrato explícito por operación, conservando productores | Aplicación | SPEC-06/07/08/14 |
| N/application/dto/ registro/consulta/entrega, presentation/mapper/NotificacionPresentationMapper.java y config | Crear/reutilizar/ampliar | Datos independientes y composición | Aplicación/presentación/infraestructura | GET general y oportunidades |
| N/infrastructure/adapter/NotificacionEventListener.java, scheduler/EntregaNotificacionesScheduler.java, NotificacionController y NotificacionService | Modificar parcialmente | Entradas dependen de interfaces ejecutar | Varias | Conservar puertos actuales |
| CanalEntregaNotificacion, LogCanalEntregaAdapter y persistencia de entrega | Modificar según decisión de proveedor y concurrencia | Entrega real, reintentos y duplicados | Aplicación/infraestructura | Todos tipos de aviso; sin migración implícita |

## Dependencias, tareas y decisiones

V4 contiene tabla compartida notificaciones con clave única (tipo, aggregate_id, destinatario_id, event_version), campos de intentos y estado; mapper/entity/adapter separados. No tablas paralelas ni migraciones. Entrega externa ocurre dentro de una transacción del worker: un envío exitoso seguido de rollback puede repetirse; unicidad de fila no equivale a unicidad de recepción. Captura genérica Exception en canal oculta motivo y debe adaptarse técnicamente sin filtrar datos al cliente. Evitar convertir una falta de proveedor en fallo de negocio; conservar consultas de origen.

FCM fue recomendado en conversación, pero no hay implementación aprobada/configurada en código ni tokens de dispositivo; no asumir que recomendarlo aprueba credenciales, dependencias, endpoints o esquema. Mailtrap es canal de correo diferente del PUSH actual. D-08/proveedor pendiente requiere decisión concreta y límites. Una aprobación del usuario para mantener NUEVA_OPORTUNIDAD pendiente no se extrapola a todos los tipos sin autorización.

Notificaciones ya tiene UseCase/mapper para consulta oportunidades y adaptadores de productores UC038/044; no duplicarlos ni revertirlos. La consulta general/registro/worker siguen en servicios agrupados. Los eventos de aceptación/transición se publican en los nuevos casos de uso SPEC-08; el plan antiguo que pide modificar ServicioService debe adaptarse tras inspección, no reintroducir lógica legada.

| Tareas | Estado comprobado |
|---|---|
| T001 | Parcial: módulo y parte migrada por SPEC-06/07; entradas generales pendientes |
| T002 | V4 compartida e índice evento presentes; ejecución no verificada |
| T003 | Puerto/canal mínimo presentes; log no acredita entrega real |
| T004 | Parcial: servicio registra/reintenta, sin UseCases generales ni garantías concurrentes |
| T005–T006/T011–T012 | No verificable por ejecución |
| T007–T008 | Modelo/repositorio/mappers/adapter presentes |
| T009/T013 | Eventos y enganches presentes; arquitectura/entrega/verificación pendientes |
| T010 | GET propio y tipo opcional presentes; mapper/contrato UseCase pendientes |
| F09-FE-* | No evaluadas; token de dispositivo no acreditado |
| F09-QA-* | No acreditadas sin ejecución; entrega y concurrencia tienen hallazgos concretos |

Orden: base registro/consulta/entrega compartida → UC057 → UC058. Dependencias SPEC-08 productor, SPEC-06/07 otros tipos y SPEC-14 preferencias. No se propone eliminar archivos; no cambios de código ni pruebas realizados.

