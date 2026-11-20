# Auditoría estática SPEC-11 — Reportes y evidencias

**Refactorización posterior.** UC065–UC067 usan contratos de aplicación. Agregar evidencia, revisar y resolver serializan escrituras sobre el mismo reporte. El archivo nuevo se limpia ante rollback normal. Quedan pendientes la consolidación concurrente y verificar el bloqueo y la compensación con PostgreSQL. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Alcance UC065–UC067: auditoría completa, los tres CU Parcial/Revisado. Solo documento; sin pruebas, compilación, aplicación ni migraciones ejecutadas. Contrato cotejado con controller y plan; la lectura GET del OpenAPI local agotó timeout de 4 segundos y no se identificó un contrato estático.

Fuentes: AGENTS.md; SPEC-11/plan completos; secciones pertinentes Plan General (integridad, reportes, storage y dependencias); SPEC-10/plan y requisitos SPEC-08/12; decisiones D-07/D-12; código reportes, usuarios, servicios, reseñas y almacenamiento; V5 y pruebas inventariadas. R = `src/main/java/com/sebastian/spring/mas/aliado/aliado/modules/reportes/`.

## UC065 — Reportar usuario

POST /api/v1/usuarios/{id}/reportes

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-065: cuenta existente y motivo | ReporteService#crearReporteUsuario/#crear | Cumple | Valida existencia, evita autorreporte y exige motivo hasta 500 | Conservar | — |
| Reporte disponible para moderación | Estado inicial PENDIENTE; ReporteAdminPort#cola | Cumple | Misma tabla y núcleo de SPEC-10/12 | Conservar | — |
| Consolidación equivalente | crear; ReportePersistenceAdapter#abiertoEquivalente; V5 uq_reporte_abierto_consolidado | Parcial | Reutiliza reporte abierto secuencialmente; concurrencia puede terminar en violación unique, no devolver el existente | Coordinar inserción/convergencia y verificar PostgreSQL | Alta |
| Contrato duplicado | Plan API dice 409 equivalente; SPEC/plan reglas dicen consolidar | Parcial | La implementación retorna ID existente con 201 | Resolver contradicción explícitamente antes de cambiar respuesta | Media |
| UseCase/Service/ejecutar y mapeos | ReporteController → ReporteService | No cumple | Servicio concreto con Spring, dominio como salida y mapper privado que además consulta evidencias | Extraer contratos, Commands/Results, mapper puro y config técnica | Alta |
| Seguridad y aceptación | SecurityUtils.actual; SecurityConfig authenticated | No verificable | Identidad es sesión y escritura exige autenticación; escenarios no ejecutados | Verificar rol y aislamiento en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| R/application/usecase/ReportarUsuarioUseCase.java, service/ReportarUsuarioService.java y dto/ReportarUsuarioCommand.java/ReporteResultado.java | Crear | Entrada propia | Aplicación | Usuarios y repositorio |
| R/application/port/ consulta de usuario y adaptador | Crear según necesidad | Puerto consumidor | Aplicación/infraestructura | Auth |
| Controller/ReporteService/mapper/config/handler | Modificar/crear | Flujo y errores semánticos | Varias | UC064/066; preservar ReportePort/ReporteAdminPort |

## UC066 — Reportar servicio

POST /api/v1/servicios/{id}/reportes

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-066: servicio y participante | ReporteService#crearReporteServicio | Cumple | Servicio existente y actor participante; requiere motivo | Conservar | — |
| Cola y consolidación | crear y cola compartida | Parcial | PENDIENTE visible en núcleo; misma carrera de consolidación UC065 | Corregir una sola vez | Alta |
| UseCase/Service/ejecutar y mapeos | ReporteController → ReporteService | No cumple | Servicio concreto con Spring, dominio como salida y mapper privado que además consulta evidencias | Extraer contratos, Commands/Results, mapper puro y config técnica | Alta |
| Participación y estado | Consulta ServicioLecturaPort; no limita estado | Cumple | El SPEC admite servicio problemático participado; no inventar restricción a un estado | Conservar | — |
| Aceptación ejecutada | No ejecución de escenarios | No verificable | No se acreditó rechazo de tercero en API | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| R/application/usecase/ReportarServicioUseCase.java, Service y Command/Result | Crear | CU propio | Aplicación | ServicioLecturaPort y reportes |
| Puerto consumidor servicio y adaptador; mapper/config compartidos | Crear/reutilizar | Dirección de dependencias y conversión | Varias | UC065/064 y SPEC-08 |
| Controller y servicio legado | Modificar parcialmente | Invocar interfaz | Presentación/aplicación | Administración preservada |

## UC067 — Adjuntar evidencia y consultar reporte propio

POST /api/v1/reportes/{id}/evidencias; GET /api/v1/reportes/mios/{id}; GET /api/v1/reportes/{id}/evidencias/{evidenciaId}

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-067: reporte previo, propio y abierto | ReporteService#agregarEvidencia; V5 FK reporte_id | Cumple | Verifica propietario y abierto; FK impide fila sin reporte | Conservar | — |
| Tipos y tamaño | ArchivoValidacion.DOCUMENTOS | Parcial | Firma MIME, PDF/JPEG/PNG/WebP y 5 MB implementados; D-07 pendiente | Aprobar parámetros antes de cerrar | Media |
| Storage sin huérfanos SC-003 | storage.guardar antes de evidencias.guardar | No cumple | Si metadatos/commit fallan queda archivo sin referencia | Compensación transaccional de altas y limpieza durable según alcance | Alta |
| Carrera con resolución | agregarEvidencia solo lee Reporte; ReporteEntity @Version | No cumple | Insertar evidencia no modifica ni bloquea reporte: la versión no protege frente a resolución concurrente; riesgo inferido estáticamente | Coordinar autorización de estado con resolución usando bloqueo/transición común | Alta |
| Lectura autorizada | miReporte, evidenciasDe, descargarEvidenciaPropia | Cumple | Solo reportante accede desde rutas públicas; admin consume puerto separado | Conservar | — |
| UseCase/Service/ejecutar y mapeos | ReporteController → ReporteService | No cumple | Servicio concreto con Spring, dominio como salida y mapper privado que además consulta evidencias | Extraer contratos, Commands/Results, mapper puro y config técnica | Alta |
| Consulta consistente | Controller aRespuesta llama evidenciasDe tras miReporte | Parcial | Dos lecturas de servicio separadas, sin unidad de consulta explícita | Result compuesto desde un CU de lectura | Media |
| Aceptación | No pruebas ejecutadas | No verificable | No se acreditó compensación/carrera/privacidad | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| R/application/usecase/AdjuntarEvidenciaReporteUseCase.java, ConsultarReportePropioUseCase.java y DescargarEvidenciaPropiaUseCase.java, servicios/DTO | Crear | Operaciones propias existentes | Aplicación | Reportes, evidencias y storage |
| R/infrastructure/adapter/ adaptador transaccional de evidencia | Crear | Compensación limitada al módulo | Infraestructura | ArchivoStorageService |
| ReporteRepository/JPA/adapter y coordinación de resolución | Modificar según diseño | Evitar altas tras cierre | Dominio/infraestructura | SPEC-12 |
| Controller/mapper/config/servicio legado | Modificar parcialmente | Mapeo sin consultas y lectura compuesta | Varias | Admin y UC064/065/066 |

## Hallazgos compartidos, esquema y tareas

V5 ya incluye tipos RESENA/USUARIO/SERVICIO, check de exactamente un objetivo, FK de evidencia, @Version en reportes y unique parcial de equivalente abierto. No recrear ni ejecutar migraciones. El índice evita duplicados físicos, pero no garantiza retorno consolidado bajo carrera. Consultas equivalentes filtran objetivo en memoria tras consultar por tipo/reportante/motivo: optimizar solo con evidencia de necesidad. No se halló necesidad de idempotencia común para implementar consolidación de estos endpoints; usan respuesta directa, no plantilla BaseController.

No hay ciclo directo confianza↔reportes en la lectura de reseña gracias a ResenaConsultaService independiente; conservarlo. Los puertos de proveedores actuales deben adaptarse a regla consumidor sin crear ciclos. ReporteService también implementa administración, no eliminarlo indiscriminadamente. Archivos privados se sirven por endpoint autorizado, no por URL estática; no hay URL firmada acreditada aunque el suplemento QA la menciona. Plan usa package/enlaces obsoletos. D-07 y D-12 requieren decisión; no inferir equivalencia por similitud ni otros estados.

| Tareas | Estado comprobado |
|---|---|
| T001/T002 | Tipos, tabla e índices presentes V5; ejecución no verificada |
| T003 | Parcial: consolidación secuencial y unique existen; convergencia concurrente pendiente |
| T004–T005 | Parcial: servicios/puertos existentes, faltan UseCases y compensación |
| T006 | PENDIENTE inicial cumple por inspección |
| T007–T008/T011–T012/T015–T016 | No verificable por ejecución |
| T009–T010/T013–T014 | Operaciones/endpoints presentes, arquitectura/aceptación parciales |
| T017 | Modelo/repositorio/mapper/adapter presentes |
| T018–T020 | Carga y consulta implementadas; consistencia/carrera/arquitectura pendientes |
| F11-FE-* | No evaluadas |
| F11-QA-* | No acreditadas; storage y concurrencia tienen hallazgos concretos |

Orden UC065 → UC066 → UC067, compartiendo creación/consolidación y mapper. Dependencias: SPEC-10 UC064 y cola SPEC-12; servicios SPEC-08; storage SPEC-04. No se propone eliminar archivos ni tablas. No se ejecutó ninguna prueba; pruebas de firma comunes existentes no acreditan estos escenarios.

