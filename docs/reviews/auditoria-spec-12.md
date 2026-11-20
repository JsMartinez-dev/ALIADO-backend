# Auditoría estática SPEC-12 — Administración, moderación y riesgos

**Refactorización posterior.** UC068–UC076 usan contratos de aplicación y mapper. Al levantar un bloqueo se mantiene desactivada una cuenta con eliminación confirmada. Queda pendiente la coordinación ante confirmación simultánea. También siguen abiertos la paginación administrativa, la granularidad de moderación y D-12. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Las rutas B significan `src/main/java/com/sebastian/spring/mas/aliado/aliado/`. Auditoría estática: no se ejecutaron pruebas, compilación, aplicación ni migraciones. No se consultó OpenAPI de una instancia; contratos cotejados con controladores y planes. Las verificaciones ejecutables se marcan No verificable. Fuentes: AGENTS.md, SPEC y plan correspondientes completos (salvo salida inicial truncada del plan13, complementada por secciones de tareas y contratos), secciones pertinentes del Plan General y decisiones, código relacionado, esquema y pruebas inventariadas. Solo se autoriza crear este informe, sin refactorización.

Alcance UC068–UC076, nueve CU Parcial y Revisado. No confundir funciones auxiliares de revisión de verificación (SPEC-10/D-11), administración de enlaces (SPEC-13) y consulta de auditoría con CU adicionales de este SPEC.

## UC068 — Administrar categorías

Endpoint(s): POST/GET /api/v1/admin/categorias; PUT/DELETE /api/v1/admin/categorias/{id}.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-068 y nombre único | CatalogoService#crearCategoria/#actualizarCategoria/#desactivarCategoria; repositorio | Cumple | Valida nombre, consulta duplicado y cambia estado lógico | Conservar | — |
| Borrador no publica categoría desactivada | CreacionSolicitud valida categoria activa; publicar revalida | Cumple | Se mantiene referencia y se rechaza nueva publicación inválida | Conservar integración SPEC-04 | — |
| Trazabilidad y seguridad | CatalogoService consume AuditoriaPort; SecurityConfig /admin/** | Cumple | Mutaciones registran auditoría y rutas exigen ADMINISTRADOR | Conservar, verificar atomicidad ejecutada posteriormente | — |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | CatalogoService y AdminCatalogoController y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/AdministrarCategoriasUseCase.java`, `service/AdministrarCategoriasService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | catalogo, solicitudes y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `B/modules/catalogo/application/CatalogoService.java`, `AdminCatalogoController.java` | Modificar parcialmente tras autorización | Conservar CRUD lógico y extracción por crear/editar/desactivar sin duplicar catálogo | Varias | catalogo, solicitudes y auditoría |

## UC069 — Administrar zonas

Endpoint(s): POST/GET /api/v1/admin/zonas; PUT/DELETE /api/v1/admin/zonas/{id}.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-069 y desactivación lógica | CatalogoService#crearZona/#actualizarZona/#desactivarZona | Cumple | Nombre único y estado INACTIVO sin borrar | Conservar | — |
| Cobertura previamente asociada | Desactivar guarda Zona INACTIVA; no borra puentes perfil | Cumple | Se conserva referencia temporal; nuevas selecciones validan ACTIVO | Conservar | — |
| Auditoría y rol | CatalogoService AuditoriaPort; SecurityConfig | Cumple | Cada mutación registra acción y /admin exige rol | Verificar transacción y unicidad concurrente | — |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | CatalogoService y AdminCatalogoController y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/AdministrarZonasUseCase.java`, `service/AdministrarZonasService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | catalogo, perfiles, solicitudes y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `B/modules/catalogo/application/CatalogoService.java`, `AdminCatalogoController.java` | Modificar parcialmente tras autorización | Separar crear/editar/desactivar y mantener referencias históricas | Varias | catalogo, perfiles, solicitudes y auditoría |

## UC070 — Consultar cola de reportes

Endpoint(s): GET /api/v1/admin/reportes?estado=.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-070 cola unificada | ReporteModeracionService#colaReportes → ReporteAdminPort#cola | Cumple | Consulta reporte unificado sin discriminar tipo de origen | Conservar | — |
| Estado por defecto y seguridad | AdminReportesController default PENDIENTE; SecurityConfig | Cumple | Solo ADMINISTRADOR; mismo modelo de reportes | Conservar | — |
| Entradas/paginación | Cola recibe String estado, lista sin página | Parcial | Sin validación explícita de enum en entrada ni paginación; puede devolver vacío para estado desconocido | Definir comportamiento de entrada/página antes de cambiar contrato | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | ReporteModeracionService#colaReportes y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/ConsultarColaReportesUseCase.java`, `service/ConsultarColaReportesService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | reportes SPEC-10/11 |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `B/modules/administracion/application/ReporteModeracionService.java`, `presentation/controller/AdminReportesController.java` | Modificar parcialmente tras autorización | Enrutar interfaz y construir Result/Response | Varias | reportes SPEC-10/11 |

## UC071 — Revisar detalle del reporte

Endpoint(s): GET /api/v1/admin/reportes/{id}; GET /api/v1/admin/reportes/{id}/evidencias/{evidenciaId}.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-071 detalle con evidencia | AdminReportesController#detalleReporte, descargarEvidencia | Cumple | Incluye motivo, objetivo y metadata; descarga binaria separada | Conservar permisos admin | — |
| Revisión previa | ReporteService#marcarRevisionDetalle | Cumple | Pasa a EN_REVISION y guarda detalleRevisadoEn; terminal no se reabre | Conservar | — |
| Identidad del revisor y atomicidad de respuesta | marcarRevisionDetalle no asigna administradorId a revisadoPor; controlador consulta evidencia después | Parcial | Revisión auditada, pero revisor del detalle no queda en Reporte; D-12 mismo revisor pendiente. Si falla lectura de evidencia después del commit ya queda revisado | Acordar definición de revisión y límite transaccional | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | ReporteModeracionService#detalleReporte y ReporteService#marcarRevisionDetalle y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/RevisarReporteUseCase.java`, `service/RevisarReporteService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | reportes, evidencia/storage y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `ReporteModeracionService.java`, `AdminReportesController.java`, `B/modules/reportes/application/ReporteService.java` | Modificar parcialmente tras autorización | Mantener marca de revisión y componer detalle con evidencia | Varias | reportes, evidencia/storage y auditoría |

## UC072 — Resolver reporte

Endpoint(s): POST /api/v1/admin/reportes/{id}/resolver.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-072 revisión y justificación | ReporteService#resolver | Cumple | Exige detalleRevisadoEn, justificación y RESUELTO/DESCARTADO; terminal rechazado | Conservar | — |
| Concurrencia | V5 reportes.version y entidad @Version | Parcial | Protección optimista existe, no se acreditaron dos administradores simultáneos | Prueba PostgreSQL autorizada | Alta |
| Resolución por mismo administrador | ReporteService usa detalleRevisadoEn; revisadoPor se asigna al resolver | Parcial | No comprueba mismo revisor; D-12 sigue pendiente, no requisito a inventar | Resolver decisión antes de restringir | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | ReporteModeracionService#resolverReporte y ReporteService#resolver y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/ResolverReporteUseCase.java`, `service/ResolverReporteService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | reportes y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `ReporteModeracionService.java`, `B/modules/reportes/application/ReporteService.java`, handler global | Modificar parcialmente tras autorización | Extraer contrato y conservar máquina de estados y 409 | Varias | reportes y auditoría |

## UC073 — Bloquear preventivamente cuenta

Endpoint(s): POST /api/v1/admin/usuarios/{id}/bloqueos; POST /api/v1/admin/usuarios/{id}/bloqueos/{bloqueoId}/levantar.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-073 trazabilidad | bloquearUsuario y V5 bloqueos_preventivos | Cumple | Guarda administrador, fecha y motivo; índice único de bloqueo activo | Conservar | — |
| Pérdida de capacidades | CuentaBloqueoPort; SesionService filtra usuario bloqueado | Cumple | La autenticación rechaza cuenta bloqueada; se conserva localización administrativa | Conservar y verificar ejecución | — |
| Levantamiento y causas de bloqueo | levantarBloqueo llama desbloquearCuenta incondicionalmente | Parcial | Podría quitar flag usado por otra causa como eliminación de cuenta; política D-05 pendiente, riesgo estático | Acordar causas/precedencia antes de ampliar esquema | Alta |
| Dependencia de auditoría | BloqueoCuentaService inyecta AuditoriaService concreto | No cumple | Se omite puerto transversal existente | Consumir common/auditoria/AuditoriaPort | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | BloqueoCuentaService y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/BloquearCuentaUseCase.java`, `service/BloquearCuentaService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | auth, sesiones, bloqueo y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `BloqueoCuentaService.java`, `AdminModeracionController.java`, puertos auth | Modificar parcialmente tras autorización | Separar bloquear/levantar y preservar controles del módulo propietario | Varias | auth, sesiones, bloqueo y auditoría |

## UC074 — Moderar contenido

Endpoint(s): POST /api/v1/admin/moderacion.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Sin bloqueo de cuenta | moderar invoca puertos ocultarResena/ocultarPerfil; no CuentaBloqueoPort | Cumple | Moderación y bloqueo son acciones independientes | Conservar | — |
| FR-074 contenido puntual | switch PERFIL/USUARIO oculta todo el perfil; SOLICITAR_CORRECCION también oculta | Parcial | No hay identificación de campo/contenido puntual; puede afectar exposición del resto del perfil | Definir granularidad y acción antes de alterar contrato | Alta |
| Auditoría y validaciones | moderar valida tipos/motivo y registra acción/auditoría | Cumple | Rechaza SERVICIO como contenido ocultable | Conservar | — |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | ModeracionService#moderar y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/ModerarContenidoUseCase.java`, `service/ModerarContenidoService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | perfil, perfiles, confianza y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `ModeracionService.java`, `AdminModeracionController.java`, puertos de moderación propietarios | Modificar parcialmente tras autorización | Separar operación y concretar alcance de moderación puntual | Varias | perfil, perfiles, confianza y auditoría |

## UC075 — Buscar cuentas y perfiles

Endpoint(s): GET /api/v1/admin/busqueda?q=.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-075 búsqueda por criterios | buscar usuarios texto, perfil nombre y solicitudes; LinkedHashMap por usuario | Cumple | Resuelve cuentas sin duplicarlas y no filtra bloqueadas | Conservar | — |
| Endpoint paginado T034 | buscar devuelve listas completas y controller no recibe página | No cumple | Paginación del plan ausente | Proponer contrato de paginación y autorización antes de cambiar API | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | BusquedaAdminService#buscar y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/BuscarCuentasPerfilesUseCase.java`, `service/BuscarCuentasPerfilesService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | auth, perfil y solicitudes |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `BusquedaAdminService.java`, `AdminBusquedaController.java` | Modificar parcialmente tras autorización | Separar Query/Result y decidir paginación | Varias | auth, perfil y solicitudes |

## UC076 — Marcar servicio de alto riesgo

Endpoint(s): POST /api/v1/admin/servicios/{id}/alto-riesgo.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-076 identificación | marcarAltoRiesgo; V5 unique marca activa | Cumple | Exige servicio existente y criterio no vacío; persiste administrador/fecha | Conservar | — |
| Criterios de producto | criterio es texto libre; D-12 | Parcial | No existe catálogo/política aprobada que compruebe criterio | Resolver D-12 antes de restringir | Alta |
| Auditoría | marcarAltoRiesgo registra HIGH_RISK_MARKED | Cumple | Misma operación transaccional declarada | Verificar atomicidad después | — |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | RiesgoAuditoriaService#marcarAltoRiesgo y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/administracion/application/usecase/MarcarServicioAltoRiesgoUseCase.java`, `service/MarcarServicioAltoRiesgoService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | servicios, marcas y auditoría |
| `B/modules/administracion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `RiesgoAuditoriaService.java`, `AdminRiesgoController.java` | Modificar parcialmente tras autorización | Extraer operación y validar política aprobada | Varias | servicios, marcas y auditoría |

## Hallazgos compartidos y persistencia

- SecurityConfig protege /api/v1/admin/** con ADMINISTRADOR: 401 sin sesión, 403 otro rol. Los controladores delegan a servicios concretos o puertos de proveedores, sin UseCases; crear una sola vez mapper, DTO y composición. Preservar common/auditoria/AuditoriaPort; algunos servicios aún inyectan AuditoriaService concreto.
- V5 contiene reportes.version, bloqueos, acciones, marcas y auditoría; trigger append-only rechaza UPDATE/DELETE de auditoría. Ejecución del trigger y privilegios no verificados. AuditoriaService genera traceId aleatorio distinto del request: correlación no acreditada. La sanitización regex no demuestra ausencia integral de datos sensibles en motivos libres.
- Las mutaciones tienen @Transactional y auditoría nominalmente en la misma transacción; exactamente una entrada y rollback requieren pruebas PostgreSQL.
- No añadir tablas/versiones ya existentes. No crear repositorios redundantes. Puertos consumidor y dirección intermodular deben revisarse sin ciclos.
- Plan usa package obsoleto y propone módulos auditoría distintos de ubicación real; no reorganizar globalmente. D-12 criterios y mismo revisor, D-05 bloqueo/retención, D-11 verificación siguen decisiones pendientes.

## Tareas y orden

| Tareas | Estado comprobado |
|---|---|
| T001 | Parcial: módulos existen, faltan UseCases/mappers |
| T002–T004 | Objetos, rol y versión presentes por inspección; ejecución DB no acreditada |
| T005–T008 | Auditoría y control de rol/revisión presentes; patrón propio pendiente |
| T010–T012, T014–T016 | CRUD lógico y validación de catálogo presentes; extracción y aceptación pendientes |
| T019–T022 | Cola/detalle/resolución y auditoría presentes; concurrencia no ejecutada |
| T024–T027 | Bloqueo/levantamiento/localización/auditoría presentes; efectos cruzados por decidir |
| T029–T031 | Moderación independiente presente; granularidad puntual pendiente |
| T033–T034 | Búsqueda presente; paginación ausente |
| T036–T038 | Marca y auditoría presentes; criterios de producto pendientes |
| T009/T013/T017/T018/T023/T028/T032/T035 y F12-QA-* | No verificable por ejecución; no se ejecutaron tests |
| F12-FE-* | No evaluadas |

Orden recomendado: UC068, UC069, UC070, UC071, UC072, UC073, UC074, UC075, UC076, compartiendo composición y auditoría. No se propone eliminar archivos en esta auditoría.

