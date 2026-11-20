# Auditoría estática SPEC-05 — Creación y gestión del perfil profesional

**Avance posterior:** se extrajeron UC024–UC033 a contratos UseCase, servicios y mapeos; las matrices siguientes conservan el diagnóstico inicial. El usuario decidió mantener evidencia opcional y campos públicos actuales, y devolver 404 en vista previa cuando el cliente no pueda ver el perfil. La vista previa usa la consulta de descubrimiento, pero conserva un identificador adicional de disponibilidad: SC-003 no está acreditado. Persisten riesgos de privacidad de valores anidados, concurrencia del agregado, reglas temporales D-03 y limpieza de archivos ante caída abrupta. Se compiló parcialmente dominio y aplicación con javac; Maven completo falló antes de iniciar. No se ejecutaron pruebas ni migraciones.

Alcance: UC024–UC033. Los diez CU tienen implementación **Parcial** y auditoría estática completa (**Revisado**); esto no acredita refactorización, pruebas ni aceptación.

No se modificó código ni se ejecutaron pruebas, compilación, aplicación o migraciones. Frontend no evaluado. Fuentes: AGENTS.md; SPEC-05 completo (`docs/specs/spec-05-perfil-profesional.md`) y su plan; secciones pertinentes del Plan General; decisiones D-02/D-03/D-07; requisitos relacionados de SPEC-03/06/08; código de perfiles, descubrimiento, catálogo y storage; pruebas existentes y migración V3. No se encontró OpenAPI estático; existe configuración OpenAPI pero no se consultó una aplicación ejecutándose. Contratos cotejados con plan y controlador.

Abreviaturas: **B** = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`; **P** = `B/modules/perfiles/`; **D** = `B/modules/descubrimiento/`. Las rutas abreviadas se resuelven bajo ese package real, no el obsoleto del plan.

## UC024 — Crear perfil profesional

Endpoint: `POST /api/v1/perfiles`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-024: categorías y zonas obligatorias | `PerfilProfesionalService#crear/#validarCategorias/#validarZonas` | Cumple | Listas no vacías y elementos activos; experiencia opcional | Conservar | — |
| Un perfil por Prestador | `crear`; V3 `uq_perfil_profesional_prestador` | Cumple | Preconsulta y constraint único; duplicado responde conflicto | Preservar constraint y traducción | — |
| Actor y propiedad | `PerfilProfesionalController#crear`, `SecurityUtils.exigirRol` | Cumple | PRESTADOR y propietario de sesión | Conservar | — |
| UseCase y mapeos | Controller inyecta `PerfilProfesionalService`, mapea experiencias y `aRespuesta` | No cumple | Sin interfaz propia, Command/Result ni mapper dedicado | Extraer CrearPerfilProfesionalUseCase/Service y DTO útiles | Alta |
| Núcleo independiente | Servicio usa Spring y BusinessException HTTP | Parcial | Persistencia abstraída, aplicación acoplada a framework | Composición/transacción en infraestructura y errores semánticos | Media |
| Atomicidad y SC-001/004 | `@Transactional`, guardado de agregado; sin ejecución | No verificable | No rollback ni aceptación acreditados | Verificación posterior autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/CrearPerfilProfesionalUseCase.java`, `P/application/service/CrearPerfilProfesionalService.java` | Crear | Entrada propia y ejecutar | Aplicación | Repositorio, catálogo, UC026/027/028 |
| `P/application/dto/CrearPerfilProfesionalCommand.java`, `PerfilProfesionalResult.java` | Crear | Separar datos HTTP | Aplicación | Mapper y demás CU |
| `P/presentation/mapper/PerfilProfesionalPresentationMapper.java`, `P/infrastructure/config/PerfilProfesionalUseCaseConfig.java` | Crear/reutilizar | Conversión y transacción | Presentación/infraestructura | Todos CU |
| `P/presentation/controller/PerfilProfesionalController.java`, `P/application/PerfilProfesionalService.java`, `B/common/web/GlobalExceptionHandler.java` | Modificar parcialmente | Integrar y traducir errores | Varias | Preservar puertos consulta/moderación |

## UC025 — Editar datos generales

Endpoint: `PUT /api/v1/perfiles/mio`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-025: propio existente | `actualizarDatos`, `propio`, controller | Cumple | Perfil de sesión, ausente 404 y otros roles rechazados | Conservar | — |
| Estructura/privacidad | `ActualizarDatosRequest`, `PerfilProfesional.datosPublicos` | Parcial | Map libre; valores anidados privados y nulos admitidos pueden romper Map.copyOf | Definir esquema público y tratamiento de nulos antes de cambiar contrato | Alta |
| Concurrencia | `PerfilProfesionalEntity` sin versión; adapter reconstruye agregado | Parcial | Riesgo estático de sobrescribir datos/colecciones de otra mutación | Diseñar control con esquema existente o solicitar migración si se decide versión | Alta |
| UseCase/mapeos | `actualizarDatos`, controller `aRespuesta` | No cumple | Servicio concreto y dominio como salida | Extraer EditarPerfilProfesionalUseCase/Service/Command/Result/mapper | Alta |
| SC-002/004 | Sin ejecución específica | No verificable | No aislamiento/concurrencia acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/EditarPerfilProfesionalUseCase.java`, `P/application/service/EditarPerfilProfesionalService.java`, `P/application/dto/EditarPerfilProfesionalCommand.java` | Crear | CU propio | Aplicación | PerfilProfesionalRepository |
| Mapper, Result, config, controller y servicio legado | Crear/reutilizar/modificar | Flujo por capas | Varias | Otros CU |
| `P/domain/model/PerfilProfesional.java` y proyección pública compartida | Modificar tras decisión | Privacidad y nulos | Dominio/aplicación | UC033 y SPEC-03 |
| Repositorio/adaptador/JPA de perfiles | Modificar según diseño aprobado | Concurrencia | Dominio/infraestructura | Todas mutaciones; no columna nueva sin autorización |

## UC026 — Seleccionar y actualizar categorías

Endpoints: creación y `PUT /api/v1/perfiles/mio/categorias`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-026 en creación e independiente | `crear`, `actualizarCategorias`, `validarCategorias` | Cumple | Perfil existente, categorías activas, lista no vacía y Set elimina repetidos | Conservar | — |
| Actor/propiedad | Controller y `propio` | Cumple | Sesión y PRESTADOR | Conservar | — |
| Contrato HTTP | `IdsRequest` usa ids; plan dice categoriaIds | Parcial | Desfase documental | Acordar/documentar contrato real, sin cambio silencioso | Media |
| Atomicidad/concurrencia | Transacción y agregado sin versión | Parcial | Posibles pérdidas concurrentes | Estrategia compartida | Alta |
| UseCase y mapper | Controller/servicio agrupado | No cumple | Falta contrato propio | ActualizarCategoriasProfesionalesUseCase/Service/Command | Alta |
| SC-002 | Sin ejecución | No verificable | Sin aceptación acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ActualizarCategoriasProfesionalesUseCase.java`, Service y Command correspondientes | Crear | Operación propia | Aplicación | Repositorio y catálogo |
| Puerto consumidor categorías y adaptador | Crear según necesidad | Dirección intermodular vigente | Aplicación/infraestructura | UC024 y catálogo |
| Controller, servicio legado, mapper y config | Modificar/reutilizar | Integrar interfaz | Varias | Creación y otros CU |
| Persistencia compartida | Modificar tras diseño | Concurrencia | Infraestructura | Todas mutaciones |

## UC027 — Configurar zonas de atención

Incluido en creación; existe además `PUT /api/v1/perfiles/mio/zonas`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-027: zonas obligatorias | `validarZonas`, `crear` | Cumple | Al menos una zona existente activa | Conservar | — |
| Actualización propia | `actualizarZonas`, controller | Cumple | Sustituye zonas del perfil propio sin duplicarlo | Conservar comportamiento existente | — |
| API del plan | Plan enumera UseCase pero omite ruta zonas en API Contracts | Parcial | Documentación incompleta | Proponer actualización autorizada | Media |
| UseCase/mapeos | Servicio agrupado y mapper privado | No cumple | Falta contrato propio | ActualizarZonasAtencionUseCase/Service/Command | Alta |
| Interrupciones/concurrencia | Transacción y agregado sin versión | Parcial | Rollback declarado, protección concurrente no acreditada | Estrategia compartida y verificación | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ActualizarZonasAtencionUseCase.java`, Service y Command | Crear | Operación propia | Aplicación | Repositorio y catálogo |
| Puerto consumidor zonas y adaptador | Crear/reutilizar | Regla de dependencias | Aplicación/infraestructura | UC024 |
| Controller, mapper/config y servicio legado | Modificar parcialmente | Flujo por capas | Varias | Creación y SPEC-06 |

## UC028 — Registrar experiencia opcional

Endpoints: creación y `POST /api/v1/perfiles/mio/experiencia`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-028 opcional e independiente | `crear`, `agregarExperiencia` | Cumple | Ausencia no bloquea creación; agrega al perfil existente | Conservar | — |
| Invariantes | Constructor ExperienciaProfesional | Cumple | Descripción/inicio obligatorios; fin no anterior | Preservar reglas | — |
| Capas/errores | Dominio importa BusinessException.BadRequest; controller mapea | No cumple | HTTP en dominio; sin UseCase | Excepción semántica, UseCase/Service y mapper | Alta |
| Elementos anidados | CrearRequest experiencia y `crear` | Parcial | Un elemento null puede generar NPE; falta validación explícita | Validar lista/elementos conservando error acordado | Media |
| Concurrencia | Guardado completo sin versión | Parcial | Altas simultáneas pueden sobrescribirse | Control compartido | Alta |
| Aceptación | Sin ejecución específica | No verificable | No aceptación acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/RegistrarExperienciaProfesionalUseCase.java`, Service, Command y Result | Crear | CU propio | Aplicación | Repositorio |
| `P/domain/model/ExperienciaProfesional.java`, excepción de dominio y handler | Modificar/crear | Separar invariante y HTTP | Dominio/presentación | UC024 |
| DTO HTTP, mapper/config, controller y servicio legado | Modificar parcialmente | Validación y flujo | Varias | Creación |

## UC029 — Agregar servicio al portafolio

Endpoint: `POST /api/v1/perfiles/mio/portafolio`, multipart.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-029 descripción y evidencia | `agregarPortafolio`, controller | Parcial | Descripción obligatoria; archivo opcional/vacío ignorado pese FR; V3 evidencia nullable | Decidir obligatoriedad antes de cambiar contrato | Alta |
| Propiedad | `propio`, rol controller | Cumple | Asocia al perfil de sesión | Conservar | — |
| Validación de evidencia | ArchivoValidacion.DOCUMENTOS | Parcial | PDF/JPEG/PNG/WebP hasta 5 MB con firma; D-07 pendiente | Confirmar parámetros | Media |
| Storage/BD | Guardar archivo antes de persistir | No cumple | Archivo huérfano si falla BD | Compensación en adaptador de portafolio | Alta |
| Visibilidad | Preview y descubrimiento excluyen portafolio; sin descarga de evidencia en controller | Parcial | Objetivo de mostrar trabajos no cubierto por proyección actual | Acordar exposición/descarga segura con SPEC-03/UC033 | Alta |
| UseCase/mapeos | Servicio/controller concretos | No cumple | Falta interfaz y DTO aplicación | AgregarServicioPortafolioUseCase/Service | Alta |
| Aceptación | Sin ejecución | No verificable | Sin asociación/rollback/privacidad acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/AgregarServicioPortafolioUseCase.java`, Service y DTO archivo | Crear | Operación propia | Aplicación | Repositorio y storage |
| Adaptador transaccional evidencia en `P/infrastructure/adapter/` | Crear | Compensación sin cambiar storage común | Infraestructura | UC030 |
| Controller, mapper/config y servicio legado | Modificar parcialmente | Integración | Varias | UC033 |
| Proyección/DTO públicos y acceso de evidencia | Solo tras aprobación | Mostrar trabajos autorizados | Varias | SPEC-03 y contrato público |

## UC030 — Eliminar servicio del portafolio

Endpoint: `DELETE /api/v1/perfiles/mio/portafolio/{id}`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-030 propio y preservar resto | `eliminarPortafolio`, `sinPortafolio` | Cumple | Item dentro de perfil propio; ajeno/inexistente mismo 404 | Conservar | — |
| Consistencia | Guardar y luego eliminar archivo dentro transacción | No cumple | Archivo eliminado antes de commit, rollback restaura referencia perdida | Borrar después commit | Alta |
| Concurrencia | Agregado sin versión | Parcial | Posibles pérdidas de cambios en otros items | Estrategia compartida | Alta |
| UseCase/errores | Controller y servicio concretos | No cumple | Sin contrato ni error semántico propio | EliminarServicioPortafolioUseCase/Service y traducción global | Alta |
| SC-002 | Sin ejecución | No verificable | Sin aceptación acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/EliminarServicioPortafolioUseCase.java`, Service y Command | Crear | CU propio | Aplicación | Repositorio y archivo |
| Adaptador compensación UC029 | Reutilizar | Baja tras commit | Infraestructura | Storage |
| Controller, mapper/config, legado y handler | Modificar parcialmente | Contrato y errores | Varias | UC029 |

## UC031 — Configurar disponibilidad

Endpoint: `PUT /api/v1/perfiles/mio/disponibilidad`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-031 guardar y dejar disponible | `actualizarDisponibilidad`, puerto consulta perfiles y SPEC-06 | Cumple | Bloques persistidos y consultables | Conservar | — |
| Validación básica | `actualizarDisponibilidad` | Cumple | Fin posterior; día 1–7 o null | Conservar mientras se resuelve D-03 | — |
| Conflicto con agenda | `chocaConCompromiso` | Parcial | Solo usa día inicial/horas locales; falla en cruces medianoche/varios días y offsets sin normalizar | Definir regla temporal y comparación correcta | Alta |
| Entradas y solapes de bloques | DTO sin validación anidada; iteración directa | Parcial | Elementos null pueden producir NPE; política mutua no definida | Validar elementos y acordar política | Media |
| Concurrencia | Guardado agregado sin lock/version | Parcial | Riesgo con cambios de agenda | Control compartido | Alta |
| UseCase/DTO | DTO DisponibilidadDatos existe, pero controller inyecta concreto | No cumple | Falta contrato propio, Result y mapper | ConfigurarDisponibilidadUseCase/Service | Alta |
| Pruebas | CompatibilidadTest indirecto; sin ejecución específica | No verificable | No acredita conflicto de agenda | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConfigurarDisponibilidadUseCase.java`, Service y Command/Result | Crear | CU propio | Aplicación | Repositorio |
| Regla temporal de dominio | Crear/modificar tras decisión | Conflictos/zonas horarias | Dominio | UC032, SPEC-06/08 |
| Controller, mapper/config y legado | Modificar | Flujo por capas | Varias | Preview y compatibilidad |
| Persistencia compartida | Modificar según diseño | Mutación consistente | Infraestructura | Agenda y demás CU |

## UC032 — Gestionar agenda

Endpoints POST, PUT y GET `/api/v1/perfiles/mio/agenda`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-032 compromisos propios | AgendaService crear/actualizar/listar | Cumple | Perfil de sesión; ID debe pertenecer; conserva resto secuencialmente | Conservar | — |
| Intervalos y solapes | `guardarCompromiso` | Cumple | Fin posterior y no solape; excluye propio en update | Conservar | — |
| Relación con disponibilidad | Guardar no consulta disponibilidad | Parcial | T026 pide conflicto con disponibilidad; D-03 pendiente y SPEC expresa regla en dirección disponibilidad contra agenda | Resolver interpretación temporal antes de restringir | Alta |
| Origen | Guardar reconstruye con MANUAL; V3 admite MANUAL/SISTEMA | Parcial | PUT podría convertir SISTEMA a MANUAL; política no definida | Definir edición/preservación del origen | Media |
| Concurrencia/reintento | Sin lock/version, chequeo en memoria | Parcial | Dos altas pueden pasar chequeo; POST repetido puede duplicar intención | Diseñar protección sin extrapolar cambios transversales | Alta |
| UseCase/mapeos | Controller inyecta AgendaService | No cumple | Sin contratos por operación ni mapper | Extraer entradas crear/actualizar/listar de UC032 | Alta |
| Aceptación | Sin ejecución específica | No verificable | No agenda acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/` y `service/` crear/actualizar/listar compromiso | Crear | Entradas explícitas | Aplicación | Repositorio |
| Commands/Results y mapper | Crear | Separación HTTP | Aplicación/presentación | Controller |
| Regla temporal UC031 | Crear/reutilizar | Conflictos | Dominio | D-03 y disponibilidad |
| AgendaService, controller y config | Modificar parcialmente | Integrar contratos | Varias | Integraciones futuras |
| Repositorio/JPA/adaptador | Modificar tras diseño | Concurrencia | Infraestructura | Todas mutaciones |

## UC033 — Consultar vista previa

Endpoint: `GET /api/v1/perfiles/mio/vista-previa`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Actor y propiedad | Controller#vistaPrevia y service.mio | Cumple | Solo Prestador de sesión | Conservar | — |
| FR-033 misma información pública | Controller compone VistaPreviaResponse; D/application/service/ProyeccionPrestadorPublico | No cumple | Construcción duplicada; preview incluye id de bloque, descubrimiento no | Proyección canónica y contrato acordado | Alta |
| Oculto/incompleto | Preview no filtra visibilidad/exposición/disponibilidad; SPEC-03 sí | Parcial | Preview devuelve contenido donde Cliente recibe 404; puede servir para previsualizar, pero no equivale comportamiento | Aclarar semántica antes de exponer frente a igualdad exacta | Alta |
| Privacidad/nulos | PerfilProfesional.datosPublicos | Parcial | Filtrado superficial y NPE con nulos admitidos | Estructura pública segura y tratamiento de nulos | Alta |
| Responsabilidad controller | Consulta catálogo/confianza y compone reputación/reseñas | No cumple | Orquestación en presentación | Trasladar a UseCase y puertos consumidor sin ciclos | Alta |
| Portafolio | Preview/SPEC-03 lo excluyen; plan Phase7 describe exposición | Parcial | Desfase objetivo/salida | Resolver exposición sin añadir campos silenciosamente | Media |
| SC-003 | Test lista blanca existe; sin comparación ejecutada | No verificable | Igualdad no acreditada | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa arquitectónica | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConsultarVistaPreviaPerfilUseCase.java`, Service y Result | Crear | Orquestación propia | Aplicación | Perfil, catálogo y confianza |
| Proyección pública compartida e interfaces consumidoras | Diseñar/crear con alcance aprobado | Evitar divergencia | Aplicación/infraestructura | SPEC-03 y visibilidad |
| Controller, mapper/config y DTO | Modificar tras contrato | Result→Response | Presentación | Clientes API |
| `P/domain/model/PerfilProfesional.java#datosPublicos` | Modificar tras definición | Privacidad y nulos | Dominio | UC024/025 y SPEC-03 |

## Hallazgos compartidos y decisiones

1. Los diez CU carecen de interfaces propias y ejecutar. Crear Result, mapper, config y errores comunes una sola vez. PerfilProfesionalService implementa puertos de consulta/moderación consumidos por descubrimiento, oportunidades y administración: preservarlos al extraer operaciones.
2. V3 y entidad no tienen versión ni bloqueo. Mapper reconstruye agregado con cascadas/orphanRemoval; riesgo estático de pérdida de cambios y solapes simultáneos. Requiere verificación PostgreSQL. No añadir @Version sin columna/autorización; evaluar locking sobre esquema existente.
3. Portafolio conserva problemas de compensación y baja antes commit. La solución de solicitudes no se aplica automáticamente a perfiles. Reutilizar concepto sin trasladar lógica específica a common ni alterar consumidores externos.
4. Map público libre, filtrado superior y nulos afectan ambos perfiles de descubrimiento y preview. La autorización anterior de conservar contratos no aprueba una nueva estructura pública.
5. D-03 temporal sigue pendiente: semanal LocalTime frente a OffsetDateTime exige política de zona horaria y cruces de día. No inventar restricciones ni prometer matching completo SPEC-06.
6. FR-029 pide evidencia, mientras código y V3 admiten ausencia. Proyecciones actuales excluyen portafolio. Acordar evidencia obligatoria, exposición y descarga autorizada.
7. Dominio ExperienciaProfesional usa BusinessException.BadRequest; aplicación usa Spring/BusinessException. Reutilizar handler global con excepciones semánticas.
8. El puerto de catálogo es del proveedor; AGENTS vigente exige consumidor. Preview compone confianza en controller por evitar ciclo; extraer sin cerrar perfiles→confianza→perfiles.
9. Mapper de persistencia captura Exception al leer JSON y sustituye por raw: oculta corrupción. Delimitar traducción técnica sin exponer internos.
10. Plan usa package obsoleto y API omite rutas zonas/exposición existentes. Cuerpos y respuestas reales difieren de ejemplos mínimos. No modificar plan sin autorización ni crear CU nuevos por esas rutas.
11. V3 ya contiene tablas, FK, puentes y unicidad. Ejecución no verificada. No recrear ni añadir repositorios individuales redundantes si agregado resuelve operaciones.
12. Existen CorreccionesVerificacionTest#datosPublicosExcluyeTelefonoDireccionYCorreo y CompatibilidadTest con uso indirecto de perfiles/disponibilidad. No se identificaron pruebas específicas de gestión completa, agenda/portafolio. Ninguna ejecutada.

## Tareas y orden recomendado

| Tarea del plan | Estado comprobado |
|---|---|
| T001 | Parcial: capas existen; faltan UseCases, servicios separados, DTO completos y mapper |
| T002 | Objetos presentes en V3; ejecución no acreditada |
| T003–T004 | No verificable por ejecución |
| T005–T006 | Agregado/modelos/repositorio/mapper/adapter presentes; no crear interfaces redundantes |
| T007 | Parcial: creación y validaciones existen; arquitectura/verificación pendientes |
| T008–T009 | Categorías/experiencia y endpoints presentes; patrón/aceptación pendientes |
| T010–T011 | No verificable por ejecución |
| T012–T013 | Edición propia presente; arquitectura/concurrencia pendientes |
| T014–T015 | No verificable por ejecución |
| T016–T017 | Portafolio en agregado; repositorio individual no necesariamente necesario |
| T018–T020 | Parcial: operaciones existen; consistencia, evidencia/exposición pendientes |
| T021–T022 | No verificable por ejecución |
| T023–T024 | Modelos/colecciones y persistencia presentes |
| T025–T027 | Parcial: disponibilidad/agenda existen; temporalidad, concurrencia y arquitectura pendientes |
| T028–T029 | No verificable por ejecución |
| T030–T031 | Parcial: endpoint existe; igualdad proyección no cumple y composición está en controller |
| F05-FE-* | No evaluadas |
| F05-QA-* | No acreditadas sin ejecución; privacidad/concurrencia/temporalidad con hallazgos concretos |

Orden recomendado: UC024 con UC026/UC027/UC028 como dependencias incluidas de creación; luego operaciones independientes de esos CU, UC025, UC029, UC030, UC031, UC032 y UC033, respetando las fases del plan. Trabajar cada CU individualmente y compartir componentes sin duplicación. Estrategia común de concurrencia y proyección pública requieren acuerdo previo a cambios funcionales. No se propone eliminar archivos en auditoría.
