# Auditoría estática SPEC-07 — Creación y gestión de propuestas

**Avance posterior:** se extrajeron UC039–UC044 a UseCases, servicios y mapeos; las tablas siguientes conservan el diagnóstico inicial. La disponibilidad se valida contra el límite existente, la edición exige estado activo en dominio y la detección de avisos comprueba el estado de la propuesta. Se ejecutaron 24 pruebas unitarias con JUnit local y pasaron; la compilación Maven completa y la integración permanecen pendientes. Idempotencia compartida, carrera con cierre de solicitud, política D-09 y entrega real de avisos siguen pendientes. No se modificaron ni ejecutaron migraciones.

Alcance: UC039–UC044. Los seis CU tienen implementación **Parcial** y auditoría estática completa (**Revisado**); no se acreditan refactorización ni aceptación. No se ejecutaron pruebas, compilación, migraciones ni peticiones de escritura. La consulta GET del OpenAPI local agotó el timeout; su contrato en ejecución no pudo cotejarse.

Fuentes consultadas: AGENTS.md; SPEC-07 y su plan completos; secciones pertinentes del Plan General (capas, idempotencia y notificaciones); decisiones D-03, D-09 y D-18; requisitos relacionados de SPEC-06/08/09 y secciones pertinentes de los planes de SPEC-08/09; código de propuestas, solicitudes/servicios como consumidores y productores relacionados, notificaciones, idempotencia y seguridad; V4 y pruebas existentes. Contrato HTTP cotejado con el plan y controlador, no con una respuesta OpenAPI disponible.

Abreviaturas: **B** = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`, **P** = `B/modules/propuestas/`, **N** = `B/modules/notificaciones/`.

## UC039 — Crear propuesta

Endpoint: `POST /api/v1/propuestas`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-039: solicitud abierta y compatible | `P/application/PropuestaService.java#crear`; SolicitudConsultaPort y CompatibilidadPort | Cumple | Comprueba PUBLICADA y compatibilidad antes de guardar | Conservar, sujeto al criterio provisional de SPEC-06/D-03 | — |
| Disponibilidad y mensaje | `crear`; `PropuestaDtos.CrearRequest` | Parcial | Disponibilidad obligatoria y mensaje opcional; crear no limita disponibilidad a 255, a diferencia de editar y V4 | Validar longitud antes de persistir y mantener 400 semántico | Alta |
| Actor Prestador | `PropuestaController#crear`, SecurityUtils y SecurityConfig | Cumple | Identidad de sesión, rol PRESTADOR; sin sesión 401 y rol incorrecto 403 | Conservar | — |
| Duplicidad de propuesta activa | `activaDePrestadorEnSolicitud`; V4 índice `uq_propuesta_activa_por_prestador` | Parcial | Código y constraint permiten una sola ACTIVA por prestador/solicitud; D-09 mantiene pendiente política funcional, especialmente tras retiro | Conservar restricción mientras se resuelve D-09; no atribuirla al SPEC sin decisión | Alta |
| Idempotencia por actor/intención | `BaseController#ejecutarIdempotente`, `IdempotencyService#buscar` | No cumple | Lookup clave+operación sin actor ni payload; otro Prestador con igual clave puede recibir la respuesta ajena | Delimitar corrección transversal y asegurar aislamiento | Crítica |
| Atomicidad de reintento | Controller llama servicio transaccional y después guarda respuesta | No cumple | Negocio puede confirmar sin respuesta idempotente; no garantiza replay tras pérdida de conexión. Unique ACTIVA evita algunas duplicaciones, no resuelve la intención | Coordinar reserva/negocio/respuesta y verificar concurrencia | Alta |
| Carrera con cierre de solicitud | `crear` lee solicitud, `ServicioService#aceptarPropuesta` la marca contratada | Parcial | Crear no bloquea ni escribe solicitud: una transacción que leyó PUBLICADA puede insertar otra ACTIVA después del cierre; riesgo estático, no reproducido | Diseñar coordinación de lectura/escritura con SPEC-04/08 usando esquema existente si viable | Alta |
| UseCase y mapeo | Controller inyecta PropuestaService y usa `aRespuesta`; no DTO aplicación | No cumple | Falta CrearPropuestaUseCase/Service/ejecutar, Command/Result y mapper | Extraer flujo explícito | Alta |
| Persistencia y capas | Repositorio de dominio, entity/mapper/JPA/adapter separados; V4 | Cumple | Sin JPA en HTTP; @Version y campos coinciden con V4 | Reutilizar, no recrear esquema | — |
| Aceptación | Pruebas inventariadas, ninguna ejecutada | No verificable | No escenarios HTTP/transaccionales acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/CrearPropuestaUseCase.java`, `service/CrearPropuestaService.java` | Crear | Entrada y ejecución propias | Aplicación | Solicitudes, compatibilidad, eventos |
| `P/application/dto/CrearPropuestaCommand.java`, `PropuestaResult.java` | Crear | Entrada y salida independientes de HTTP | Aplicación | Mapper, UC040/041 |
| `P/presentation/mapper/PropuestaPresentationMapper.java`, `P/infrastructure/config/PropuestaUseCaseConfig.java` | Crear/reutilizar | Mapeo y transacción | Presentación/infraestructura | UC042/043 |
| Puertos de consulta del consumidor y adaptadores | Crear según necesidad | Dirección de dependencias vigente | Aplicación/infraestructura | Solicitudes y oportunidades |
| Controller, PropuestaService, handler global y DTO | Modificar parcialmente | Integrar y validar disponibilidad | Varias | Preservar PropuestaConsultaPort |
| `B/common/web/BaseController.java`, idempotencia y persistencia relacionada | Solo con alcance compartido delimitado | Aislamiento y atomicidad | Transversal | Auth, solicitudes y servicios |

## UC040 — Indicar disponibilidad obligatoria

Incluido en creación; no endpoint independiente.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-040: disponibilidad requerida | `CrearRequest @NotBlank`, `PropuestaService#crear` | Cumple | Rechaza null/blanco y recorta espacios antes de guardar | Conservar | — |
| Compatibilidad con esquema | V4 disponibilidad VARCHAR(255); `crear` no limita longitud | No cumple | Texto mayor al límite llega a persistencia en vez de rechazarse como entrada inválida; handler genérico puede clasificarlo como duplicado si se traduce a DataIntegrityViolationException | Validación consistente en caso de uso y presentación | Alta |
| Granularidad de contrato | Validación privada dentro de creación; plan enumera solo CrearPropuesta | No cumple | AGENTS exige interfaz por CU; el CU incluido no tiene contrato propio | Diseñar contrato/regla útil reutilizable sin endpoint ni DTO artificial | Alta |
| Temporalidad | Disponibilidad es texto; D-03 | Parcial | No hay interpretación horaria formal; no es defecto a resolver inventando fechas | Conservar texto hasta decisión D-03 | Media |
| SC-001 ejecutado | Sin prueba de caso de uso ejecutada | No verificable | No se acredita rechazo sin filas | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| Contrato/Service de disponibilidad bajo `P/application/` según granularidad acordada | Crear | CU incluido con entrada útil | Aplicación | UC039/042 |
| Regla y excepción semántica | Crear/reutilizar | Requerida y máximo compatible | Dominio/aplicación | Crear y editar |
| `PropuestaDtos.java`, mapper y handler | Modificar | Formato y 400 consistente | Presentación | POST y PUT existentes |

## UC041 — Agregar mensaje opcional

Incluido en `POST /api/v1/propuestas`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-041: mensaje opcional | `CrearRequest`, `crear`, V4 mensaje nullable | Cumple | Sin mensaje crea igualmente; límite de 1000 validado en aplicación | Conservar | — |
| Persistencia | Propuesta y mapper incluyen mensaje nullable | Cumple | No requiere tabla ni campo nuevo | Reutilizar | — |
| Patrón obligatorio | Mensaje es parámetro escalar de crear | No cumple | Sin contrato propio ni DTO de aplicación | Resolver operación incluida útil sin duplicar creación | Alta |
| Verificación | Sin ejecución de escenarios con/sin mensaje | No verificable | Aceptación no acreditada | Etapa autorizada | Media |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| Contrato/Service de mensaje bajo `P/application/` según diseño acordado | Crear | Respetar CU incluido sin endpoint nuevo | Aplicación | UC039 |
| Command/Result y regla compartida de propuesta | Crear/reutilizar | Validar y mapear sin duplicación | Aplicación/dominio | UC042 |
| Mapper, config y servicio agrupado | Modificar parcialmente | Integración | Varias | Creación |

## UC042 — Editar propuesta activa

Endpoint: `PUT /api/v1/propuestas/{id}`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-042: propia y activa | `propiaActiva`, controller | Cumple | Rol/identidad de sesión, ajena/inexistente 404, inactiva 409 | Conservar | — |
| Campos editables y formato | `editar`, `EditarRequest` | Cumple | Omitidos conservan valor; valida blanco/255 disponibilidad y 1000 mensaje | Conservar; acordar aparte si se requiere borrar mensaje con null | — |
| Invariante de dominio | `Propuesta#editada` | Parcial | No exige ACTIVA y construye estado ACTIVA; el servicio sí lo protege, pero un consumidor directo podría reactivar | Proteger transición de dominio sin cambiar contratos | Media |
| Concurrencia con retiro/aceptación | PropuestaEntity @Version y V4 version | Parcial | Lock optimista presente; no se ejecutaron carreras ni reintentos de edición | Preservar versión y verificar PostgreSQL | Alta |
| UseCase/mapeo/excepciones | Controller y servicio Spring; dominio con BusinessException | No cumple | Sin interfaz propia/Command/Result; HTTP en núcleo | Extraer EditarPropuestaUseCase/Service y excepciones semánticas | Alta |
| Conectividad | PUT sin Idempotency-Key ni versión cliente | Parcial | No duplica ID, pero reintento podría sobrescribir cambios posteriores; política no definida | Acordar reintento/versionado antes de alterar API | Alta |
| SC-004 | PropuestaEstadoTest no cubre edición real; no ejecución | No verificable | Test editarNoCambiaEstado solo comprueba estado inicial | Verificación autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/EditarPropuestaUseCase.java`, Service y Command | Crear | CU propio | Aplicación | PropuestaRepository |
| `P/domain/model/Propuesta.java`, excepciones y handler | Modificar/crear | Invariantes sin HTTP | Dominio/presentación | UC043 y aceptación SPEC-08 |
| Result, mapper y config de creación | Reutilizar | Mapeos y transacción | Varias | UC039 |
| Controller y servicio legado | Modificar parcialmente | Invocar interfaz | Presentación/aplicación | Preservar puerto intermodular |

## UC043 — Retirar propuesta activa

Endpoint: `POST /api/v1/propuestas/{id}/retirar`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-043: propia activa y retiro | `propiaActiva`, `Propuesta#retirar`, controller | Cumple | Rechaza ajena/inexistente y no activa; transiciona a RETIRADA | Conservar | — |
| Dejar de ofrecerla al Cliente | `activasDeSolicitud`, lectura de propuestas en controller | Cumple | Solo lista ACTIVA; la lectura pertenece a UC045 SPEC-08 | Conservar contrato público | — |
| Concurrencia | @Version en entidad | Parcial | Protege modificaciones de la misma propuesta, sin evidencia ejecutada de aceptación/retiro concurrentes | Verificar atomicidad junto a SPEC-08 | Alta |
| Reintento protegido | Endpoint sin clave; Plan General 14 incluye retiro | Parcial | Segundo retiro recibe 409 como exige SPEC; no distingue replay de misma intención | Definir protección sin eliminar rechazo a nueva intención | Media |
| UseCase y excepciones | Controller llama servicio concreto; dominio importa BusinessException.Conflict | No cumple | Falta patrón propio y excepción semántica de dominio | Extraer RetirarPropuestaUseCase/Service y mapeo | Alta |
| Pruebas | `PropuestaEstadoTest` existente | No verificable | No ejecutado; exige BusinessException.Conflict y deberá ajustarse solo con permiso | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/RetirarPropuestaUseCase.java`, Service y Command útil de IDs | Crear | CU propio | Aplicación | PropuestaRepository |
| Modelo/excepción/handler de UC042 | Reutilizar/modificar | Transición y 409 | Dominio/presentación | Aceptación y cierre SPEC-08 |
| Controller, Result, mapper/config y servicio legado | Modificar parcialmente | Integración | Varias | No retirar PropuestaConsultaPort |
| Prueba de estado existente | Solo tras autorización | Cambio de tipo de excepción si se adopta | Test | No modificar en auditoría/refactor sin permiso |

## UC044 — Notificar propuesta recibida

Operación automática por evento; no endpoint público de creación de aviso.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-044: evento real y destinatario | `crear` publica Eventos.PropuestaCreada; `NotificacionEventListener#alCrearPropuesta` | Cumple | Registro automático a cliente propietario, tipo PROPUESTA_RECIBIDA | Conservar | — |
| Negocio y registro consistentes | `crear @Transactional`, SpringPublicadorEventos y listener síncrono; registrar transaccional | Parcial | Diseño participa en transacción de negocio; no se ejecutó rollback real | Preservar límite y verificar sin envío externo dentro | Alta |
| No generar aviso nuevo para retirada | `alCrearPropuesta` registra sin consulta a propuesta | No cumple | Un evento repetido para propuesta inactiva sin aviso previo puede registrar aviso nuevo; evento normal inicial es síncrono, no se afirma que falle ese recorrido | Validar estado/origen al detectar y acordar tratamiento de replay y avisos ya creados | Alta |
| Entrega efectiva | LogCanalEntregaAdapter, worker | No cumple | Solo NUEVA_OPORTUNIDAD queda pendiente sin proveedor; PROPUESTA_RECIBIDA aún se marca ENTREGADA al escribir log | Coordinar proveedor/estado con SPEC-09 y decisión explícita del usuario | Alta |
| Unicidad y preferencias | registrar preconsulta y UNIQUE evento en V4; consulta preferencias | Parcial | Unicidad protegida en DB pero carrera puede fallar transacción; preferencias pueden omitir aviso frente a SC-003 del 100% | Definir política y verificar concurrencia | Alta |
| UseCase y puertos | Listener → NotificacionPort; sin contrato productor propio | No cumple | Falta UseCase/Service de UC044 y puerto consumidor de registro | Extraer productor y adaptador sin tablas de notificación paralelas | Alta |
| SC-003 | Sin ejecución | No verificable | Registro no equivale a recepción real | Verificación posterior | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/NotificarPropuestaRecibidaUseCase.java`, Service y DTO de evento | Crear | Productor explícito | Aplicación | Propuestas, solicitudes y notificaciones |
| Puerto de registro y adaptador en notificaciones | Crear | Contrato del consumidor | Aplicación/infraestructura | Evitar ciclo propuestas↔notificaciones |
| `N/infrastructure/adapter/NotificacionEventListener.java` | Modificar parcialmente | Delegar solo evento de propuesta | Infraestructura | Otros eventos SPEC-06/08/09 |
| Canal, worker y servicio de notificaciones | Solo con alcance aprobado | Entrega real y preferencias | Varias | SPEC-09/14, otros tipos de aviso |

## Hallazgos comunes, decisiones y límites

1. Todos los CU carecen de patrón UseCase/Service/ejecutar y mapper. Crear Result, mapper, configuración y validaciones compartidas una sola vez. Las operaciones incluidas UC040/041 necesitan granularidad útil acordada; el plan solo enumera crear/editar/retirar, mientras AGENTS exige una interfaz propia por CU. No añadir endpoints artificiales.
2. La idempotencia crítica es transversal y sigue pendiente desde SPEC-04. El usuario condicionó su corrección a no afectar otras funcionalidades. No basta extraer servicios para resolver aislamiento ni atomicidad; afecta auth/solicitudes/propuestas/servicios y D-18.
3. D-09 mantiene pendiente la política de propuestas repetidas y recuperación del ID tras reinicio. V4/código actualmente restringen una ACTIVA por prestador/solicitud; no añadir listado de mis propuestas ni precio/moneda por extrapolación de demos.
4. PropuestaEntity sí tiene @Version y unique parcial ACTIVA. Esto no coordina automáticamente el alta de una nueva propuesta con cierre/contratación de la solicitud, que es otro agregado. La carrera descrita es inferencia estática, no reproducción ejecutada.
5. Propuesta.editada no comprueba estado, aunque el servicio sí. Dominio conoce excepción HTTP. PropuestaEstadoTest exige esa clase y no puede modificarse sin autorización.
6. La notificación se registra en transacción, entrega mediante worker; el canal actual sigue simulando recepción de propuestas mediante log. La decisión previa de SPEC-06 solo cambió NUEVA_OPORTUNIDAD, no autoriza extrapolar silenciosamente a todos los tipos. La recomendación FCM no equivale a una implementación aprobada.
7. T002 del plan solicita notificaciones_propuesta_recibida, contradiciendo Data Model/T005/T006, Plan General y V4, que unifican notificaciones. Proponer actualizar tarea en etapa autorizada; no crear otra tabla ni modificar migraciones. El package del plan y la mención a plan SPEC-08 aún no escrito están desactualizados.
8. Existen DTO homónimos CrearRequest y EditarRequest en otros módulos. Tras los incidentes de Swagger ya observados, deben verificarse nombres únicos para propuestas; no se pudo confirmar el esquema vivo porque GET /api-docs agotó timeout. No afirmar que el request vivo está bien o mal sin esa evidencia.
9. Hay GET /api/v1/solicitudes/{solicitudId}/propuestas en el controlador actual: corresponde a UC045 de SPEC-08, no ampliarlo como CU de SPEC-07. PropuestaService implementa PropuestaConsultaPort usado por servicios; no eliminarlo indiscriminadamente.
10. D-03 permanece: disponibilidad es texto y la compatibilidad conserva el criterio provisional autorizado en SPEC-06. No introducir calendario, tarifa, nuevos campos ni migraciones.

## Tareas del plan y estado

| Tarea | Estado comprobado |
|---|---|
| T001 | Parcial: módulo/capas existen; faltan UseCases y mapeos de aplicación |
| T002 | Contradictoria: propuestas/notificaciones existen en V4; no crear tabla puntual; ejecución no acreditada |
| T003–T004 | No verificable por ejecución; no pruebas contract/integración específicas identificadas |
| T005 | Modelo y evento PropuestaCreada presentes; dominio aún conoce HTTP |
| T006 | Repositorio/adaptadores y puerto notificación presentes; falta puerto consumidor y operación explícita |
| T007 | Parcial: creación validada y evento transaccional, pendientes longitud, carreras, idempotencia y arquitectura |
| T008 | Endpoint presente; patrón y aceptación pendientes |
| T009–T010 | No verificable por ejecución |
| T011–T012 | Parcial: edición protegida en servicio, faltan patrón y aceptación |
| T013–T014 | No verificable por ejecución |
| T015–T016 | Parcial: retiro presente; reintentos/aviso inactivo/patrón pendientes |
| F07-FE-* | No evaluadas |
| F07-QA-01/02/04 | No acreditadas sin ejecución; riesgos concretos anotados |
| F07-QA-03 | Cumple por inspección: no precio ni moneda añadidos al modelo |

Existen pruebas PropuestaEstadoTest (transiciones; el test de edición no ejecuta editada), CompatibilidadTest y pruebas indirectas de notificaciones en CorreccionesVerificacionTest; ninguna ejecutada. Orden recomendado: UC039 con las dependencias incluidas UC040/041 y registro UC044, luego UC042 y UC043; delimitar primero la idempotencia transversal y las decisiones de avisos/duplicidad. Mantener cada CU autorizado por separado, compartiendo componentes. No se propone eliminar archivos ahora ni nuevas migraciones.
