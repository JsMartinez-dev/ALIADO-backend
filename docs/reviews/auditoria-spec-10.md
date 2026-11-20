# Auditoría estática SPEC-10 — Calificaciones, reseñas y verificación

**Refactorización posterior.** UC059–UC064 usan UseCases, servicios y mapeos. La verificación documental compensa los archivos nuevos si revierte la transacción. Quedan pendientes la concurrencia del recálculo, la política documental y la deuda de excepciones HTTP en la orquestación compartida. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Alcance: UC059–UC064, auditoría completa. Los seis CU están Parcial/Revisado, no finalizados. Solo se creó este documento; no se ejecutaron pruebas, compilación, aplicación ni migraciones. La lectura GET del OpenAPI local agotó el timeout de 4 segundos; no se identificó un contrato estático y el contrato se cotejó con controlador y plan.

Fuentes: AGENTS.md; SPEC-10 y plan completos; secciones pertinentes del Plan General (confianza, integridad, storage y dependencias); SPEC-11 y plan, requisitos relacionados de SPEC-08/12 y código; decisiones D-07/D-11/D-12; V5, pruebas existentes. B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`; C = `B/modules/confianza/`; R = `B/modules/reportes/`.

## UC059 — Calificar servicio finalizado

POST /api/v1/servicios/{servicioId}/calificacion

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-059: propia, finalizada, una por servicio | ConfianzaService#calificar; V5 uq_calificacion_servicio | Cumple | Valida cliente asignado, FINALIZADO y duplicado; constraint protege unicidad | Conservar | — |
| Calificación más reputación | calificar @Transactional y recalcularReputacion | Parcial | Transacción nominal existente; reputación puede sufrir carrera con otra calificación al mismo prestador | Coordinar cálculo concurrente y verificar PostgreSQL | Alta |
| UseCase/Service/ejecutar y mapeo | ConfianzaController y servicios agrupados | No cumple | Sin interfaces propias; dominio como salida, mapeo en controller y Spring/BusinessException HTTP en aplicación | Extraer contrato por CU, DTO aplicación, mapper y composición/transacción en infraestructura | Alta |
| SC-001/002 ejecutados | ReputacionTest existente | No verificable | No ejecutado; mocks no acreditan rollback ni carreras | Pruebas autorizadas posteriores | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| C/application/usecase/CalificarServicioUseCase.java, service/CalificarServicioService.java, dto/CalificarServicioCommand.java y CalificacionResult.java | Crear | Entrada y salida propias | Aplicación | ServicioLecturaPort, UC061 |
| C/presentation/mapper/ConfianzaPresentationMapper.java, infrastructure/config/ConfianzaUseCaseConfig.java; controller, servicio y handler global | Crear/modificar parcialmente | Mapeo y transacción | Varias | Otros CU; preservar puertos existentes |

## UC060 — Agregar reseña textual

POST /api/v1/calificaciones/{id}/resena

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-060: calificación previa del autor | ConfianzaService#agregarResena; V5 unique calificacion_id | Cumple | Solo autor y una reseña; texto obligatorio | Conservar | — |
| UseCase/Service/ejecutar y mapeo | ConfianzaController y servicios agrupados | No cumple | Sin interfaces propias; dominio como salida, mapeo en controller y Spring/BusinessException HTTP en aplicación | Extraer contrato por CU, DTO aplicación, mapper y composición/transacción en infraestructura | Alta |
| SC y escenarios ejecutados | Inventario de pruebas | No verificable | Sin ejecución de propiedad, dependencia y duplicados | Verificar posteriormente | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| C/application/usecase/AgregarResenaUseCase.java, service/AgregarResenaService.java y dto/AgregarResenaCommand.java/ResenaResult.java | Crear | CU propio | Aplicación | CalificacionRepository, ResenaRepository |
| Mapper/config/controller/ConfianzaService y handler | Crear/reutilizar/modificar | Flujo y errores semánticos | Varias | UC059/064 y moderación |

## UC061 — Recalcular reputación

Operación interna incluida en calificación

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-061: recálculo automático y reconstruible | ConfianzaService#recalcularReputacion | Cumple | Lee todas las calificaciones y calcula media a dos decimales | Conservar fuente; confirmar fórmula adoptada si no hay aprobación formal | Media |
| Concurrencia de materialización | ReputacionEntity sin @Version; ReputacionPersistenceAdapter#guardar | Parcial | Dos transacciones sobre servicios distintos pueden guardar conteos/promedios incompletos; riesgo estático no reproducido | Serializar por prestador o estrategia equivalente sin presumir migración | Alta |
| Regla dominio y CU propio T004/T013 | Método privado dentro de servicio Spring | No cumple | Cálculo no está separado ni tiene interfaz ejecutar | Extraer cálculo puro y RecalcularReputacionUseCase/Service con entrada útil | Alta |
| SC-002 ejecutado | ReputacionTest | No verificable | Prueba existente no ejecutada | Verificar cálculo y concurrencia | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| C/application/usecase/RecalcularReputacionUseCase.java, service/RecalcularReputacionService.java y regla de cálculo dominio | Crear | Separar regla y orquestación | Dominio/aplicación | UC059 |
| ReputacionRepository/JPA/adapter y configuración | Modificar según estrategia aprobada | Concurrencia de materialización | Infraestructura | Todos productores; no columna sin autorización |

## UC062 — Mostrar confianza y nivel de verificación

GET /api/v1/prestadores/{prestadorId}/confianza

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-062: nivel vigente sin documentos | ConfianzaService#confianzaDe; ConfianzaDtos.ConfianzaResponse | Cumple | Comprueba rol prestador; deriva SIN_VERIFICAR/BASICO, sin documentos ni afirmación legal | Conservar campos | — |
| Semántica de niveles | NivelVerificacion y contarAprobadasDePrestador | Parcial | AMPLIADO está reservado/inaccesible; BASICO es tope efectivo; SPEC habla de verificación ampliada | Acordar niveles y revisión D-11 antes de cambiar | Alta |
| UseCase/Service/ejecutar y mapeo | ConfianzaController y servicios agrupados | No cumple | Sin interfaces propias; dominio como salida, mapeo en controller y Spring/BusinessException HTTP en aplicación | Extraer contrato por CU, DTO aplicación, mapper y composición/transacción en infraestructura | Alta |
| Perfil disponible | confianzaDe valida roles.esPrestador, no visibilidad de perfil | Parcial | Plan promete 404 perfil no disponible; código expone confianza aun sin perfil/oculto | Resolver política pública con SPEC-03/05 sin cambiar contrato silenciosamente | Media |
| Lenguaje UI SC-003 | Backend no devuelve certificación legal; UI no auditada | No verificable | No se evaluó presentación completa | Verificar interfaz posteriormente | Media |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| C/application/usecase/ConsultarConfianzaPrestadorUseCase.java y Service/Result | Crear | Consulta explícita | Aplicación | RolConsultaPort, reputación, verificación |
| Controller/mapper/config y puertos consumidores/adaptadores | Modificar/crear | Dirección dependencias y salida pública | Varias | SPEC-03/05; preservar ConfianzaConsultaPort |

## UC063 — Solicitar verificación documental

POST /api/v1/verificaciones; GET /api/v1/verificaciones/mia

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-063 pendiente única y documentos | VerificacionService#solicitarVerificacion; V5 unique parcial pendiente | Cumple | Exige archivos, valida todos antes de guardarlos y rechaza pendiente/tope efectivo | Conservar restricciones | — |
| Storage consistente | solicitarVerificacion guarda archivos y metadatos | No cumple | Fallo posterior puede dejar archivo huérfano aunque BD revierta | Compensación transaccional propia de verificación | Alta |
| Documentos requeridos y niveles | D-07/D-11; ArchivoValidacion.DOCUMENTOS | Parcial | Tipos provisionales PDF/JPEG/PNG/WebP y 5MB; documentos exactos y nivel ampliado pendientes | Definir política antes de cerrar | Alta |
| Privacidad y consulta documental | Controller solo devuelve id/estado; documentos por referencias privadas | Parcial | No expone documentos públicamente; no hay recorrido de descarga del solicitante acreditado por controller inspeccionado | Acordar acceso autorizado sin inventar endpoint | Media |
| UseCase y límites de capas | Controller depende VerificacionService, ArchivoSubido anidado | No cumple | Sin contratos Solicitar/Consultar; Spring/HTTP en aplicación | Extraer dos operaciones, Command/Result y mapper | Alta |
| Aceptación | CorreccionesVerificacionTest pruebas archivos y tope | No verificable | No ejecutadas; atomicidad y carrera no acreditadas | Etapa de pruebas autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| C/application/usecase/SolicitarVerificacionUseCase.java, ConsultarVerificacionPropiaUseCase.java y servicios/DTO | Crear | Dos entradas del CU | Aplicación | Repositorios y storage |
| C/infrastructure/adapter/ adaptador transaccional de documentos y config | Crear | Compensación sin alterar storage común | Infraestructura | SPEC-04 almacenamiento |
| VerificacionService/controller/mapper/handler | Modificar parcialmente | Extraer flujos conservando VerificacionAdminPort | Varias | Administración SPEC-12 |

## UC064 — Reportar reseña

POST /api/v1/resenas/{id}/reportes

| Requisito o regla | Evidencia en código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-064: objetivo disponible y motivo | ConfianzaService#reportarResena; ReporteService#crearReporteResena/#crear | Cumple | Rechaza reseña inexistente/oculta y motivo vacío; núcleo único PENDIENTE para cola | Conservar | — |
| Equivalente abierto | ReporteService#crear y V5 unique parcial | Parcial | Consolidación secuencial retorna ID; plan10 indica 409 equivalente. Carrera puede fallar constraint en vez de converger | Acordar contrato con SPEC-11 y coordinación concurrente | Alta |
| UseCase/Service/ejecutar y mapeo | ConfianzaController y servicios agrupados | No cumple | Sin interfaces propias; dominio como salida, mapeo en controller y Spring/BusinessException HTTP en aplicación | Extraer contrato por CU, DTO aplicación, mapper y composición/transacción en infraestructura | Alta |
| Cola disponible SC-004 | ReporteAdminPort#cola sobre misma tabla | Cumple | Mismo núcleo consumido por administración | Conservar | — |
| Aceptación | Sin ejecución de reporte/cola | No verificable | No acreditado en ejecución | Verificar posteriormente | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| R/application/usecase/ReportarResenaUseCase.java, Service/Command/Result | Crear | CU propietario de reportes | Aplicación | Consulta de reseña y ReporteRepository |
| ConfianzaController/ConfianzaService/ReporteService y mapper/config | Modificar parcialmente | Enrutar CU y preservar puertos sin ciclo | Varias | SPEC-11/12; conservar ResenaConsultaService |

## Hallazgos compartidos y tareas

Todos los CU Parcial/Revisado. Falta UseCase/Service/ejecutar, Commands/Results y mapper explícito. Aplicación usa Spring y BusinessException con semántica HTTP; dominio es mayormente records puros. Persistencia separa entidad/JPA/mapper/adapter coherente con V5; tablas, FK, unicidades y checks existentes se reutilizan, no ejecutar/editar migraciones. Puertos consumidos definidos por proveedores deben adaptarse a regla actual del consumidor sin ciclos. Conservar ConfianzaConsultaPort, ResenaModeracionPort y VerificacionAdminPort; ResenaConsultaService rompe deliberadamente un ciclo con reportes.

Reputación usa materialización sin control de concurrencia; calificación unique no protege carreras entre servicios distintos del mismo prestador. Verificación y evidencia comparten necesidad de compensación storage. Política documentos/niveles D-07/D-11 pendiente. El comentario de VerificacionService menciona UC063–064, pero UC064 es reporte de reseña: no inferir CU por comentario. El plan tiene package/enlaces obsoletos; no modificados.

| Tareas | Estado comprobado |
|---|---|
| T001 | Parcial: capas presentes, UseCases/mapeos ausentes |
| T002–T003 | Objetos/constraints presentes en V5; ejecución no verificada |
| T004/T013 | Parcial: recálculo privado, transacción declarada, regla y concurrencia pendientes |
| T005 | Parcial: storage privado por clave, falta compensación |
| T006/T030/T031 | Parcial: reporte/endpoint existen, contrato CU y aceptación pendientes |
| T007/T021/T022 | Confianza integrada por inspección; arquitectura y lenguaje UI no acreditados |
| T008–T010/T015–T016/T020/T023–T024/T028–T029 | No verificable por ejecución; ReputacionTest y pruebas de verificación existentes no ejecutadas |
| T011–T012/T014/T017–T019/T025–T027 | Modelos y operaciones presentes; arquitectura/aceptación parciales |
| F10-FE-* | No evaluadas |
| F10-QA-* | No acreditadas sin ejecución; privacidad/concurrencia/storage pendientes |

Orden: UC059 con UC061 interno, UC060, UC062, UC063, UC064; respetar dependencias hacia SPEC-08 y luego SPEC-11/12. No se propone eliminar archivos. No se hicieron modificaciones productivas.

