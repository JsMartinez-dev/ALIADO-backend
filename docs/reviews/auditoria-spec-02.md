# Auditoría estática SPEC-02

Alcance: UC008–UC013. Fuentes: AGENTS.md, SPEC-02, su plan, secciones pertinentes del plan general, SPEC-08 y su plan, RNF-004 y decisión D-05, código de perfil, seguridad, cifrado, servicios y migración V1. No se ejecutaron aplicación, pruebas ni migraciones; no se auditó el frontend. El contrato HTTP cotejado es el del plan; no se encontró OpenAPI estático. El plan usa una ruta de paquetes obsoleta y enlaces antiguos; AGENTS prevalece. P significa `src/main/java/com/sebastian/spring/mas/aliado/aliado/modules/perfil/`; B es `src/main/java/com/sebastian/spring/mas/aliado/aliado/`.

**Avance posterior:** UC008, UC009, UC010 y UC013 se refactorizaron con UseCase, Service y mapeos; sus tablas siguientes conservan el diagnóstico inicial. Compilación y aceptación pendientes. UC012 está en curso: resta definir el filtro de estados de servicio y verificar la implementación. El usuario decidió mantener el teléfono sin cifrado (queda pendiente la discrepancia con RNF-004), dejar pendiente la clave de cifrado, mantener el 404 sin ubicación y conservar provisionalmente el bloqueo de acceso tras confirmar eliminación sin anonimización. PUBLICO y SOLO_ZONA mantienen la respuesta pública actual con zona; PRIVADO oculta el perfil. No se ejecutan pruebas sin autorización previa.

## UC008 — Editar datos personales

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-002 y propiedad | `P/application/PerfilService.java#actualizarMe`, `PerfilController#actualizar` | Cumple | Valida antes de guardar; identidad de sesión | Conservar | — |
| UseCase y mapeo | `PerfilController`, `PerfilService` | No cumple | Servicio concreto, parámetros escalares y mapper privado | Extraer UseCase, Service, Command, Result y mapper | Alta |
| Concurrencia | `PerfilEntity` tiene `@Version` | Parcial | Detecta conflictos; no acredita conservar ambos cambios | Definir y verificar política de conflictos | Alta |
| Cifrado de contacto | `PerfilPersistenceMapper`, V1 | Parcial | Teléfono se copia en claro; cifrado de volumen no verificado | Acordar solución y esquema antes de cambiar | Alta |
| SC-002 y T003–T004 | Sin pruebas específicas identificadas | No verificable | Falta ejecución | Verificar válidos, inválidos y concurrencia | Alta |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ActualizarPerfilUseCase.java`, `P/application/service/ActualizarPerfilService.java` | Crear | Contrato y ejecución | Aplicación | PerfilRepository |
| `P/application/dto/ActualizarPerfilCommand.java`, `PerfilResult.java` | Crear | Entrada y salida | Aplicación | Mapper y otros CU |
| `P/presentation/mapper/PerfilPresentationMapper.java`, `P/infrastructure/config/PerfilUseCaseConfig.java` | Crear/reutilizar | Mapeo y composición transaccional | Presentación/infraestructura | UC009–UC011 |
| `P/presentation/controller/PerfilController.java`, `P/application/PerfilService.java` | Modificar parcialmente | Extraer operación | Presentación/aplicación | Conservar puertos existentes |
| Persistencia de teléfono | Por determinar | Cifrado | Infraestructura | Posible cambio de esquema requiere autorización |

## UC009 — Configurar zona aproximada

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-003 y propiedad | `PerfilService#configurarUbicacion`, `PerfilController#ubicacion` | Cumple | Persiste zona propia y conserva dirección omitida | Conservar | — |
| Dirección comprometida con servicio | Servicio de perfil y módulos de servicios/solicitudes | No verificable | No se demuestra snapshot del servicio | Revisar contrato con SPEC-04/08 | Alta |
| Cifrado | `UbicacionPersistenceAdapter`, `B/common/security/CryptoService.java` | Parcial | AES-GCM, pero clave vacía produce clave conocida de ceros | Definir configuración segura compartida | Alta |
| UseCase y mapeo | `PerfilController`, `PerfilService` | No cumple | Sin contrato propio ni DTO de aplicación | Extraer UseCase, Service, Command y mapper | Alta |
| Transacción y T008–T010 | `UbicacionEntity @Version`; sin pruebas específicas | No verificable | Atomicidad y escenarios no ejecutados | Verificar posteriormente | Alta |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConfigurarUbicacionUseCase.java`, `P/application/service/ConfigurarUbicacionService.java`, `P/application/dto/ConfigurarUbicacionCommand.java` | Crear | Flujo propio | Aplicación | UbicacionRepository |
| Controlador, `PerfilService`, mapper y configuración compartidos | Modificar | Integración limitada al CU | Varias | UC008/010/011 |
| `B/common/security/CryptoService.java` | Proponer modificación | Clave insegura por defecto | Infraestructura transversal | Consumidores de cifrado |

## UC010 — Configurar visibilidad

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-004 | `PerfilService#configurarVisibilidad`, `domain/model/Visibilidad.java` | Parcial | Persiste enum; semántica de valores no está claramente aprobada | Confirmar semántica antes de alterarla | Alta |
| Autenticación y persistencia | `PerfilController#visibilidad`, `PerfilRepository` | Cumple | Usa identidad de sesión y puerto | Conservar | — |
| UseCase y mapeo | `PerfilController`, `PerfilService` | No cumple | Servicio directo y mapeo privado | Extraer contrato, Service y DTO | Alta |
| Privacidad integral T008–T009 | Sin pruebas específicas | No verificable | No se acreditan todos los consumidores | Verificación posterior | Alta |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConfigurarPrivacidadUseCase.java`, `P/application/service/ConfigurarPrivacidadService.java`, `P/application/dto/ConfigurarPrivacidadCommand.java` | Crear | Flujo propio | Aplicación | PerfilRepository |
| Controlador, `PerfilService`, mapper y configuración compartidos | Modificar | Integrar CU | Varias | UC013 y puertos de visibilidad |
| `P/domain/model/Visibilidad.java` | Solo si se decide | Formalizar política | Dominio | Consumidores de visibilidad |

## UC011 — Consultar datos propios

**Actualización de refactorización:** se extrajeron UseCase, Service, resultado, mapper, excepción semántica y configuración transaccional de lectura. El GET conserva el ID de sesión y el contrato HTTP. Por decisión del usuario, UC011 se marca revisado y finalizado en el seguimiento. La compilación y las pruebas de aceptación siguen pendientes: el wrapper no arrancó y Maven local no pudo resolver el parent en el entorno disponible. Este estado de seguimiento no acredita esas verificaciones.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-001 y propiedad | `PerfilController#me`, `PerfilService#consultarMe` | Cumple | Consulta con identidad autenticada | Conservar | — |
| UseCase y mapeo | `PerfilController`, `PerfilService.PerfilPropio` | No cumple | Dependencia de servicio concreto y resultado interno | Extraer UseCase, Service, Result y mapper | Alta |
| Persistencia | Repositorios de dominio y adaptadores de perfil | Cumple | Entidades JPA separadas | Reutilizar | — |
| SC-004, T003–T007 y p95 | Sin pruebas específicas ni medición | No verificable | Inspección no prueba aislamiento ni rendimiento | Verificación posterior | Media |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConsultarPerfilUseCase.java`, `P/application/service/ConsultarPerfilService.java`, `P/application/dto/PerfilResult.java` | Crear | Contrato y resultado | Aplicación | Repositorios; UC008–UC010 |
| `P/presentation/mapper/PerfilPresentationMapper.java`, configuración compartida | Crear/reutilizar | Mapeo y composición | Presentación/infraestructura | DTO existentes |
| `P/presentation/controller/PerfilController.java`, `P/application/PerfilService.java` | Modificar parcialmente | Integrar lectura | Presentación/aplicación | Preservar PerfilBasePort |

## UC012 — Solicitar eliminación

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-006 y FR-007 | `PerfilService#solicitarEliminacion`, evento de confirmación, V1 | Cumple | Registra pendiente; solicitar no desactiva cuenta | Conservar | — |
| Servicios activos | `ServicioService#tieneServicioActivo`, `PerfilService` | Parcial | Se consulta al solicitar, no al confirmar; SPEC-02 y plan-08 difieren en estados | Resolver criterio y revalidación | Alta |
| Expiración al confirmar | `PerfilService#confirmarEliminacion`, `@Transactional` | No cumple | Guardar EXPIRADA y lanzar excepción de ejecución puede revertir transición | Separar resultado transaccional y respuesta 410 | Alta |
| Solicitud expirada por scheduler | `ExpiracionEliminacionScheduler`, consulta de pendientes | No cumple | Confirmar tras scheduler devuelve 404 en vez de 410 del plan | Distinguir expirada | Alta |
| Concurrencia | `SolicitudEliminacionEntity`, repositorio JPA | No cumple | Sin versión o bloqueo; posibles sobrescrituras y eventos duplicados | Transición atómica con esquema existente | Alta |
| Retención y eliminación | Listener de auth; decisión D-05 | Parcial | Bloqueo de cuenta existe; anonimización y retención pendientes | Resolver D-05 | Alta |
| Arquitectura | `PerfilService`, scheduler | No cumple | Servicio agrupado y scheduler depende de concreto | UseCases separados y puerto consumidor para servicios activos | Alta |
| T013–T018 y SC-003 | Sin pruebas específicas | No verificable | No se ejecutaron escenarios ni carreras | Verificar con PostgreSQL cuando se autorice | Alta |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/SolicitarEliminacionCuentaUseCase.java`, `ConfirmarEliminacionCuentaUseCase.java`, servicios correspondientes | Crear | Contratos separados | Aplicación | Repositorios y eventos |
| `P/application/dto/EliminacionResult.java`, mapper y configuración compartidos | Crear/reutilizar | Resultado y transacción | Aplicación/presentación/infraestructura | Controlador |
| `P/application/port/ConsultaServiciosActivosPort.java` y adaptador en servicios | Crear | Puerto definido por consumidor | Aplicación/infraestructura | SPEC-08 |
| `P/domain/model/SolicitudEliminacion.java`, `P/domain/repository/SolicitudEliminacionRepository.java` | Modificar | Transiciones y consultas | Dominio | Persistencia |
| `P/infrastructure/persistence/adapter/PerfilPersistenceAdapter.java`, `P/infrastructure/persistence/jpa/SolicitudEliminacionJpaRepository.java` | Modificar | Atomicidad y consulta | Infraestructura | Esquema existente |
| `P/infrastructure/scheduler/ExpiracionEliminacionScheduler.java`, controlador y `PerfilService` | Modificar | Usar contratos | Varias | Operación de expiración y otros puertos |

## UC013 — Separar zona y dirección exacta

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-005 en endpoint público | `PerfilController#publico`, `PerfilDtos.PerfilPublicoResponse` | Cumple | Respuesta solo contiene ID y zona | Conservar | — |
| Política de privacidad | `PerfilService#perfilPublico` | Parcial | Rechaza privado, pero política dispersa y resultado propio reutilizado | Resultado público y política explícita cuando corresponda | Media |
| Perfil sin ubicación | `perfilPublico` | Parcial | Devuelve 404; plan menciona usuario inexistente | Acordar comportamiento antes de cambiar contrato | Media |
| UseCase y mapeo | Controlador y servicio | No cumple | No hay contrato propio; el plan omite esta operación | Crear UseCase, Service, Result público y mapper | Alta |
| SC-001 integral | Sin pruebas integrales identificadas | No verificable | Endpoint aislado no acredita todas las rutas | Revisar consumidores y probar | Alta |

| Ruta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `P/application/usecase/ConsultarPerfilPublicoUseCase.java`, `P/application/service/ConsultarPerfilPublicoService.java`, `P/application/dto/PerfilPublicoResult.java` | Crear | Flujo público propio | Aplicación | Repositorios y UC010 |
| Controlador, `PerfilService`, mapper y configuración compartidos | Modificar | Integrar flujo | Varias | Puerto de visibilidad |
| Política de dominio y repositorio de ubicación | Solo si se justifica | Centralizar política o evitar descifrado innecesario | Dominio/infraestructura | UC009/010 |

## Hallazgos transversales y planificación

Los seis CU se revisaron estáticamente. UC011 figura como revisado y finalizado por decisión del usuario, con compilación y aceptación pendientes. UC008, UC009, UC010 y UC013 están refactorizados con verificaciones pendientes. UC012 permanece parcial: se implementaron separación, expiración y transiciones condicionales, pero el adaptador usa provisionalmente el filtro anterior de servicios mientras se aclara la regla sobre TERMINADO; requiere verificación. Autenticación de SPEC-01 es prerrequisito; UC013 depende de ubicación y visibilidad; UC012 depende de servicios SPEC-08 y de la decisión D-05.

Crear una sola vez resultado, mapper, configuración y excepciones compartidos. `PerfilService` también implementa puertos para otros módulos: no eliminarlo indiscriminadamente. Conservar `PreferenciaVisibilidadService`, cuya separación evita un ciclo entre módulos. La clave de cifrado y el contacto afectan varios CU. La semántica de visibilidad, estados de servicios que bloquean eliminación y política de retención requieren decisión antes de cambiar comportamiento. El cifrado de teléfono puede requerir autorización de esquema; no modificar migraciones.

T001, T006, T011 y T016 parciales arquitectónicamente; T002 tiene objetos en V1 pero ejecución no verificada; T005, T010 y T015 tienen modelos; T007, T012 y T017 tienen endpoints; T018 tiene scheduler. Esto no acredita el cumplimiento íntegro de esas tareas. T003–T004, T008–T009, T013–T014 y suplementos QA no acreditados por ejecución. Las tareas frontend no se evaluaron. El plan debe actualizarse en una etapa autorizada por su ruta obsoleta, enlaces antiguos y desfase de dependencias. No se propone eliminar archivos.
