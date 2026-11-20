# Auditoría estática SPEC-04 — Gestión de solicitudes de servicio

**Estado de seguimiento:** el usuario informó que validó su funcionamiento y solicitó dar por finalizada la refactorización. La idempotencia compartida permanece pendiente bajo su condición de no afectar otras funcionalidades; no se afirma que se hayan ejecutado pruebas automatizadas ni compilación Maven completa.

**Avance posterior:** se extrajeron las operaciones UC016–UC023 a contratos UseCase y servicios. Se implementó el manejo transaccional de fotos y la validación de descripción de solicitudes publicadas, pendientes de verificación ejecutable. El usuario aprobó el flujo borrador → fotos → publicar. La corrección de idempotencia compartida quedó pendiente: su autorización exige no afectar otros módulos, condición que no puede acreditarse sin compilación y verificaciones adicionales. No se ejecutaron pruebas ni migraciones. El wrapper Maven falló antes de iniciar; se compiló parcialmente con javac el dominio y aplicación nuevos. Las tablas siguientes reflejan la auditoría inicial.

Alcance: UC016–UC023. Auditoría estática integral completada; los ocho CU tienen implementación Parcial y pueden marcarse Revisado, sin considerarlos finalizados ni probados. No se modificó código ni se ejecutaron pruebas, compilación, aplicación o migraciones.

Fuentes consultadas: AGENTS.md; SPEC-04 completo y su Implementation Plan; secciones pertinentes del Plan General (seguridad, almacenamiento, idempotencia, dependencias y API); requisitos relacionados de SPEC-06/08/12; código de solicitudes, catálogo, seguridad, almacenamiento e idempotencia; esquema V1/V2/V7/V8. Hay configuración Swagger/OpenAPI, pero no se encontró un contrato OpenAPI estático ni se consultó una aplicación ejecutándose. Los contratos se cotejaron con el plan y los controladores. Frontend no evaluado.

Abreviaturas: B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`; S = `B/modules/solicitudes/`.

## UC016 — Adjuntar fotos opcionales

Endpoints: POST `/api/v1/solicitudes/{id}/fotos`; GET, PUT y DELETE `/api/v1/solicitudes/{id}/fotos/{fotoId}`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-016: fotos opcionales | SolicitudService.crear/agregarFoto | Cumple | Crear sin fotos funciona; asociación a solicitud existente | Conservar | — |
| Adjuntar antes/durante publicación | SolicitudDtos.CrearRequest y controlador | Parcial | El plan incluye fotos[], pero request real no; carga solo después de crear | Acordar recorrido borrador → adjuntar → publicar o autorizar contrato alternativo | Alta |
| Autorización/propiedad | propiaAbierta y controlador | Cumple | Mutaciones solo CLIENTE propietario de solicitud abierta | Conservar | — |
| MIME/tamaño/firma | ArchivoValidacion.validar | Parcial | Hay firma, tipos y 5 MB; límite 10 fotos. D-07 mantiene pendiente aprobación | Confirmar política sin inventar límites | Media |
| Orden de fotos | agregarFoto usa size()+1; sinFoto; V2 uq_fotos_solicitud_orden | No cumple | Borrar foto intermedia deja huecos; siguiente orden puede duplicar uno existente | Siguiente orden libre o reordenamiento coherente | Alta |
| Consistencia storage/BD | agregarFoto, reemplazarFoto, eliminarFoto | No cumple | Alta puede dejar archivo huérfano con rollback; bajas borran archivo antes de commit y pueden dejar referencias rotas | Compensar altas; limpiar bajas tras commit | Alta |
| Lectura de fotos | descargarFoto | Parcial | Propietario o cualquier autenticado si PUBLICADA; no verifica compatibilidad de prestador | Acordar política con privacidad de detalle SPEC-06 | Alta |
| UseCase y mapeos | Controller inyecta SolicitudService; retorna FotoSolicitud | No cumple | Sin interfaces por operación, DTO aplicación ni mapper | Extraer contratos/servicios/DTO/mapper sin endpoints nuevos | Alta |
| Pruebas | CorreccionesVerificacionTest cubre firma; sin ejecución | No verificable | No acreditados asociación, orden, compensación ni autorización | Pruebas posteriores autorizadas | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/ y service/ para agregar/reemplazar/eliminar/descargar foto | Crear | Operaciones explícitas | Aplicación | Storage y SolicitudRepository |
| S/application/dto/ Commands y Results de archivo | Crear | Separación HTTP | Aplicación | Mapper |
| S/presentation/mapper/SolicitudPresentationMapper.java | Crear/reutilizar | Conversión explícita | Presentación | DTO actuales |
| S/presentation/controller/SolicitudController.java y S/application/SolicitudService.java | Modificar parcialmente | Delegar operaciones | Presentación/aplicación | Otros CU conservados |
| S/domain/model/Solicitud.java y configuración/adaptador de compensación | Modificar/crear según diseño | Orden y consistencia | Dominio/infraestructura | Storage común; no cambiar esquema |
| B/common/storage/ | Solo si solución compartida aprobada lo exige | Compensación técnica | Transversal | SPEC-05/10/11 |

## UC017 — Definir zona y urgencia obligatorias

Incluido en publicación; no tiene endpoint independiente.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-017: zona/urgencia al publicar | crear, validarZona, validarUrgencia, publicar | Cumple | Rechaza publicación incompleta antes de persistir | Conservar | — |
| Catálogo vigente | validarCategoria, validarZona, publicar | Cumple | Revalida ACTIVO | Reutilizar | — |
| Borrador parcial | Métodos anteriores y V8 | Cumple | Permite campos faltantes sin publicar | Conservar distinción | — |
| Invariante tras editar PUBLICADA | editar | No cumple | Acepta descripción en blanco y la guarda | Revalidar invariantes en edición | Alta |
| UseCase y errores | Validadores privados; BusinessException HTTP | No cumple | Regla agrupada, sin contrato propio y errores acoplados a HTTP | Regla reutilizable; resolver granularidad de CU incluido sin endpoint artificial | Alta |
| SC-001 | Inventario tests, sin ejecución | No verificable | No evidencia de rechazo y ausencia de registros | Verificar en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/domain/ regla de publicación/excepciones; contrato de validación si aporta entrada útil | Crear/modificar | Invariante reutilizable | Dominio/aplicación | UC018/019/020 |
| S/application/SolicitudService.java y futuros servicios publicación/edición | Modificar | Reutilizar regla | Aplicación | Catálogo |
| S/application/port/ consulta catálogo y adaptador | Crear según necesidad | Puerto del consumidor | Aplicación/infraestructura | Catálogo |
| B/common/web/GlobalExceptionHandler.java | Modificar puntualmente | Mantener errores 400 | Presentación transversal | Formato vigente |

## UC018 — Publicar solicitud

Endpoints: POST `/api/v1/solicitudes`; POST `/api/v1/solicitudes/{id}/publicar`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-018 publicación completa | crear y publicar | Cumple | Categoría/descripción/zona/urgencia obligatorias; fotos opcionales | Conservar | — |
| Visibilidad sin dirección exacta | Modelo/DTO; OportunidadService.compatible/listar | Cumple | Sin campo de dirección; solo PUBLICADA se ofrece | Conservar | — |
| Historial inicial/transición | crear y Solicitud.transitarA | Cumple | Registra estado inicial y publicación de borrador | Conservar | — |
| Actor CLIENTE | Controller/SecurityConfig | Cumple | Rol e identidad de sesión | Conservar | — |
| Idempotencia y SC-005 | BaseController.ejecutarIdempotente; IdempotencyService; adapter auth | No cumple | Replay por clave+operación sin usuario/payload; otra cuenta con misma clave puede obtener respuesta ajena | Aislar usuario/operación/intención; validar propietario | Crítica |
| Atomicidad de idempotencia | Mismos componentes; crear @Transactional | No cumple | Solicitud confirma antes de guardar respuesta; concurrencia permite duplicar creación | Coordinar reserva/ejecución/respuesta/transacción | Alta |
| Reintento publicar borrador | publicar, endpoint sin clave | Parcial | Segundo intento devuelve 409 aunque primero pudo confirmar y perder respuesta | Acordar protección de reintentos | Alta |
| UseCase/mapeos | Servicio agrupado y mapper privado | No cumple | Falta patrón y DTO aplicación | Extraer gradualmente | Alta |
| Contrato plan/código | Plan fotos[]/respuesta mínima; request real sin fotos y respuesta completa | Parcial | Desfase; /publicar no figura en API Contracts del plan | Documentar decisión autorizada; no cambiar silenciosamente | Media |
| Aceptación | Sin ejecución | No verificable | Sin evidencia conectividad/concurrencia/evento | Verificación posterior | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/CrearSolicitudUseCase.java, PublicarSolicitudUseCase.java y servicios | Crear | Creación/publicación explícitas | Aplicación | Repositorio, catálogo, eventos |
| S/application/dto/CrearSolicitudCommand.java, resultados y mapper compartido | Crear | Flujo entre capas | Aplicación/presentación | UC019 y lecturas |
| S/infrastructure/config/SolicitudUseCaseConfig.java | Crear | Composición/transacción | Infraestructura | Spring y puertos |
| SolicitudController y SolicitudService | Modificar parcialmente | Extraer operaciones | Presentación/aplicación | Preservar SolicitudConsultaPort |
| B/common/web/BaseController.java, common/idempotencia/, adapter auth | Corrección compartida por delimitar | Seguridad/atomicidad | Transversal/infraestructura | Auth, propuestas y servicios |
| Regla de publicación y excepciones | Crear/reutilizar | Invariantes | Dominio/aplicación | UC017/019/020 |

## UC019 — Guardar y retomar borrador

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-019 borrador parcial | crear resuelve BORRADOR; V8 | Cumple | Campos pueden faltar | Conservar | — |
| No visible | publicadas y OportunidadService.compatible | Cumple | Solo PUBLICADA entra al tablero | Conservar | — |
| Retomar/publicar | editar, publicar, lecturas propias | Cumple | Recorrido persistido y revalidación | Conservar | — |
| Idempotencia | Plantilla transversal al crear | No cumple | Replay ajeno y duplicación descritos en UC018 | Corregir una sola vez | Crítica |
| UseCase/mapeos | Rama de SolicitudService.crear | No cumple | Sin contrato propio ni DTO aplicación | Resolver extracción coherente sin duplicar reglas/endpoints | Alta |
| SC-002/005 | Sin ejecución | No verificable | Privacidad y recuperación no acreditadas | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| Contrato/servicio de guardar borrador y DTO útiles bajo S/application/ | Crear según descomposición aprobada | CU propio sin duplicar creación | Aplicación | UC018 |
| Regla crear/borrador y mapper/config | Reutilizar | Evitar duplicación | Varias | UC017/018 |
| Controller y servicio agrupado | Modificar parcialmente | Enrutar operación con contrato actual | Presentación/aplicación | POST existente |
| Idempotencia compartida | Reutilizar corrección UC018 | Consistencia | Transversal | Otros módulos |

## UC020 — Editar solicitud abierta

Endpoint: PUT `/api/v1/solicitudes/{id}`; fotos mediante rutas separadas.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Propiedad/estado abierto | propiaAbierta; Solicitud.abierta | Cumple | BORRADOR/PUBLICADA; ajena/inexistente 404; cerrada 409 | Conservar | — |
| Editar datos | editar | Parcial | Conserva omitidos, pero admite descripción vacía en PUBLICADA | Revalidar publicación | Alta |
| Carrera contratación | SolicitudEntity @Version; marcarContratada | Parcial | Bloqueo optimista presente; sin verificación de rollback completo | Preservar y probar PostgreSQL cuando se autorice | Alta |
| Reintentos | PUT sin plantilla; DTO sin versión | Parcial | No duplica ID, pero reintento puede sobrescribir cambio intermedio | Acordar política antes de cambiar contrato | Alta |
| Arquitectura | SolicitudService.editar y controller | No cumple | Concreto, dominio como salida y Spring en aplicación | UseCase/Service/Command/Result/mapper | Alta |
| Pruebas | SolicitudEstadoTest existe; sin ejecución del servicio | No verificable | Escenarios de edición no acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/EditarSolicitudUseCase.java, service/EditarSolicitudService.java, dto/EditarSolicitudCommand.java | Crear | Operación propia | Aplicación | Repositorio y catálogo |
| Regla de publicación | Crear/reutilizar | Invariantes | Dominio/aplicación | UC017/018 |
| Controller, mapper, config, excepciones y legado | Modificar parcialmente | Integración | Varias | UC016 fotos |
| DTO HTTP/versionado | Solo si se autoriza | Política de reintento | Presentación | Contrato público |

## UC021 — Cancelar solicitud abierta

Endpoint: POST `/api/v1/solicitudes/{id}/cancelar`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-021 propietario/estado | cancelar, propiaAbierta, transitarA | Cumple | Rechaza repetida o contratada | Conservar | — |
| Historial/ocultación | transitarA y consulta PUBLICADA | Cumple | Registra CANCELADA y deja tablero | Conservar | — |
| Concurrencia | @Version y transacción | Parcial | Protección presente; no acreditada carrera contratación/cancelación | Verificación posterior autorizada | Alta |
| Reintentos | Sin clave; plan general exige protección | Parcial | Segunda cancelación 409 según SPEC; plan general pide protección idempotente | Distinguir reintento de nueva intención con decisión de contrato | Media |
| Arquitectura/excepciones | SolicitudService; dominio importa BusinessException | No cumple | Falta UseCase y excepción semántica | Extraer y traducir globalmente | Alta |
| Verificación | SolicitudEstadoTest existente no ejecutado | No verificable | Sin escenarios HTTP/propiedad acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/CancelarSolicitudUseCase.java, service/CancelarSolicitudService.java | Crear | Operación propia | Aplicación | Repositorio |
| Resultado y mapper/config | Crear/reutilizar | Separar dominio y HTTP | Varias | Otros CU |
| S/domain/model/Solicitud.java, excepciones y handler | Modificar/crear | Retirar semántica HTTP del dominio | Dominio/presentación | Contratación SPEC-08 |
| Controller y servicio legado | Modificar parcialmente | Invocar interfaz | Varias | Preservar puerto público |

## UC022 — Listar solicitudes propias

Endpoint: GET `/api/v1/solicitudes/mias`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-022 propias/todos estados | listarMias; findByClienteIdOrderByFechaCreacionDesc | Cumple | Filtra cliente de sesión sin limitar estado | Conservar | — |
| Lista vacía/rol | Controller y repositorio | Cumple | 200 lista vacía; solo CLIENTE | Conservar | — |
| UseCase/Result/mapper | Controller inyecta concreto y mapea dominio | No cumple | Faltan contrato, salida aplicación y mapper | Extraer patrón | Alta |
| Persistencia/lectura | Transacción readOnly, mapper/entity separados | Cumple | JPA no sale al controlador | Preservar lectura completa | — |
| Escalabilidad | Sin paginación; mapeo carga fotos/historial | Parcial | Plan general pide paginar listas crecientes; SPEC pide lista completa | Evaluar contrato antes de paginar | Baja |
| SC-004 | Sin ejecución de escenarios específicos | No verificable | Aislamiento no acreditado | Verificación posterior | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/ListarSolicitudesPropiasUseCase.java, service/ListarSolicitudesPropiasService.java | Crear | Contrato propio | Aplicación | SolicitudRepository |
| S/application/dto/SolicitudResult.java, mapper/config | Crear/reutilizar | Salida compartida | Varias | UC023 y escrituras |
| Controller y servicio agrupado | Modificar parcialmente | Migrar lectura | Presentación/aplicación | Conservar lista HTTP |

## UC023 — Consultar detalle propio

Endpoint: GET `/api/v1/solicitudes/mias/{id}`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-023 propiedad/error genérico | detalleMio y sesión de controller | Cumple | Ajena/inexistente mismo 404 | Conservar | — |
| Detalle completo | aRespuesta | Cumple | Categoría, descripción, zona, urgencia, fotos y estado | Conservar | — |
| Referencias de fotos | SolicitudResponse.fotos solo archivoKey | Parcial | No incluye fotoId necesario para ruta de descarga; tampoco URL del plan | Acordar referencia navegable sin cambio silencioso | Media |
| UseCase/mapeo | Controller y SolicitudService | No cumple | Sin contrato propio/Result; mapper privado | Extraer patrón | Alta |
| Persistencia | Repositorio y mapper separados | Cumple | Sin JPA expuesto | Reutilizar | — |
| SC-004 | Sin ejecución de aislamiento/404 | No verificable | Aceptación no acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| S/application/usecase/ConsultarSolicitudPropiaUseCase.java, service/ConsultarSolicitudPropiaService.java | Crear | Contrato propio | Aplicación | Repositorio |
| Result, mapper, config y excepción semántica | Crear/reutilizar | Salida/error | Varias | UC022 y escrituras |
| Controller y servicio agrupado | Modificar parcialmente | Invocar interfaz | Presentación/aplicación | Preservar puerto intermodular |
| DTO fotos | Solo si se autoriza | Referencia descargable | Presentación | UC016 y clientes API |

## Problemas compartidos y decisiones

- Crítico: replay idempotente sin comprobar usuario. V1 tiene usuario_id, pero buscar usa clave+operación y la respuesta de puerto no expone propietario. No se presume necesidad de migración; delimitar impacto en auth/propuestas/servicios.
- Alta: solicitud y respuesta idempotente no son atómicas. Cabecera inválida/ausente se ignora; no garantiza SC-005 universalmente.
- Alta: compensación de storage, orden de fotos y revalidación al editar publicación.
- Todos los CU carecen de patrón obligatorio y mapeos. Solicitud usa BusinessException.Conflict con semántica HTTP. Crear elementos compartidos una sola vez.
- SolicitudService implementa SolicitudConsultaPort usado por oportunidades/propuestas/servicios; no eliminar sin preservar contrato.
- Catálogo y storage existen. CatalogoController usa CatalogoConsultaPort y no UseCases propios previstos por el plan: base técnica por revisar, sin inventar CU nuevos.
- V2 crea objetos, V7 seed y V8 borrador parcial; no modificar ni ejecutar migraciones. Plan tiene package obsoleto, pendientes con implementación existente y API incompleta.
- D-07 mantiene sin cerrar tipos/tamaño/cantidad. Código aplica 5 MB y 10 imágenes provisionalmente.
- Mantener 401 sin sesión y 403 por rol aunque plan mencione 401 sin rol Cliente.

## Tareas del plan

| Tarea | Estado comprobado |
|---|---|
| T001 | Parcial: módulos presentes; faltan patrones y mapeos |
| T002 | Objetos presentes en V2/V8; ejecución no acreditada |
| T003–T004 | Modelos/repositorios de catálogo presentes por inspección |
| T005 | Seed presente en V7; ejecución no acreditada |
| T006 | Puerto/adaptador local presentes; proveedor productivo pendiente |
| T007–T008 | No verificable por ejecución; sin contract/integración específicos identificados |
| T009–T010 | Agregado/modelos/repositorio/adaptadores presentes; evitar repositorio de fotos redundante |
| T011 | Parcial: crear/publicar/borrador existen; fotos/idempotencia/arquitectura pendientes |
| T012–T013 | Endpoint/historial presentes; aceptación no acreditada |
| T014–T015 | No verificable por ejecución |
| T016–T019 | Parcial: edición/cancelación/estado presentes; invariantes y verificaciones pendientes |
| T020–T021 | No verificable por ejecución |
| T022–T023 | Lecturas presentes; arquitectura/ejecución pendientes |
| F04-FE-* | No evaluadas |
| F04-QA-* | No acreditadas; idempotencia/compensación tienen defectos concretos |

Existen SolicitudEstadoTest y pruebas de firma en CorreccionesVerificacionTest; no se ejecutaron. No corresponde afirmar ausencia total de pruebas.

## Resumen y orden recomendado

| CU | Estado global | Dependencias principales |
|---|---|---|
| UC016 | Parcial; Revisado | Solicitud persistida, storage, propiedad, UC018/019 |
| UC017 | Parcial; Revisado | Catálogo y regla de publicación |
| UC018 | Parcial; Revisado | UC017, auth, idempotencia, repositorio, eventos |
| UC019 | Parcial; Revisado | Creación compartida y consultas propias |
| UC020 | Parcial; Revisado | Solicitud creada, catálogo, invariantes, contratación SPEC-08 |
| UC021 | Parcial; Revisado | Solicitud creada, estados/historial, contratación SPEC-08 |
| UC022 | Parcial; Revisado | Solicitudes creadas y autenticación |
| UC023 | Parcial; Revisado | Solicitudes creadas, autenticación y fotos |

Resolver primero seguridad/idempotencia y política del flujo de fotos. Orden sugerido: UC017 → UC018 → UC019 → UC016 → UC020 → UC021 → UC022 → UC023, conservando las dependencias técnicas del plan. UC016/017/019 son extensiones/inclusiones del flujo de creación: diseñar extracción sin nuevos endpoints ni orquestación duplicada. El plan enumera CrearSolicitud como entrada agrupadora mientras AGENTS exige interfaz propia por CU; acordar granularidad antes de implementar. No se propone eliminar archivos en auditoría. Compilación, pruebas y aceptación no acreditadas.
