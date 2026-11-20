# Auditoría estática SPEC-13 — Ruta de formalización

**Refactorización posterior.** UC077–UC080 tienen entradas explícitas. El acceso a enlaces y vinculación exige PRESTADOR y la edición administrativa valida las URL. UC080 sigue deshabilitado. Quedan pendientes D-13, la señal de destino externo y el mantenimiento del contenido. Las matrices conservan la auditoría inicial. Maven compiló el proyecto y ejecutó la suite unitaria integrada de SPEC-09 a SPEC-14: 116 pruebas, cero fallos, incluidas 32 nuevas. Queda pendiente la aceptación HTTP y la verificación de concurrencia con PostgreSQL.

Alcance UC077–UC080. Fuentes: AGENTS.md, SPEC-13 y plan completos, Plan General secciones formalización/flags, D-13, código formalización/administración/seguridad, V6/V7 y pruebas inventariadas. B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`. No se ejecutaron pruebas, compilación ni migraciones. OpenAPI vivo no consultado; cotejo con controlador y plan. Tres CU MVP son Parcial; UC080 tiene aislamiento OFF presente y preparación Future parcial. Todos auditados: Revisado, sin aceptación acreditada.

## UC077 — Consultar ruta orientativa

Endpoint(s): GET /api/v1/formalizacion/ruta.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-077 información orientativa | ruta y AVISO_LEGAL | Cumple | Respuesta contiene rutas activas, pasos, recursos y disclaimer no certificante | Conservar | — |
| Contenido aprobado | V7 seed y D-13 | Parcial | Seed existe; aprobación de fuente/responsable de mantenimiento no acreditada | Confirmar contenido sin cambiar migración | Media |
| Orden y lectura | pasos por ruta y mapper propietario; lectura readOnly | Cumple | Persistencia separada del dominio; no tabla duplicada | Reutilizar | — |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | FormalizacionService#rutasActivas/#pasosDe y FormalizacionController#ruta y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/formalizacion/application/usecase/ConsultarRutaFormalizacionUseCase.java`, `service/ConsultarRutaFormalizacionService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | contenido aprobado D-13, pasos/enlaces y catálogo V7 |
| `B/modules/formalizacion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `B/modules/formalizacion/application/FormalizacionService.java`, `presentation/controller/FormalizacionController.java` | Modificar parcialmente tras autorización | Componer ruta dentro de aplicación, no consultar pasos por cada ruta desde controlador | Varias | contenido aprobado D-13, pasos/enlaces y catálogo V7 |

## UC078 — Consultar/abrir enlaces oficiales

Endpoint(s): GET /api/v1/formalizacion/enlaces/{id}; PUT /api/v1/admin/formalizacion/enlaces/{id} auxiliar.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-078 destino configurado y estado | EnlaceResponse(id,nombre,url,estado) | Parcial | Devuelve URL y DISPONIBLE/NO_DISPONIBLE, pero no indicador explícito externo ni flujo de apertura; frontend no auditado | Definir señal de externo y apertura segura | Media |
| Autorización Prestador | FormalizacionController#enlace sin exigirRol; SecurityConfig solo authenticated | No cumple | Cliente/Administrador autenticados pueden consultar esta función de Prestador | Aplicar autorización acorde al SPEC | Alta |
| URL y disponibilidad externa | Enlace record sin invariantes; actualizarEnlace solo trim | No cumple | Acepta URL arbitraria sin validar esquema/host; no se determina si enlace externo ha caído, solo estado manual | Validar URL y acordar política de mantenimiento/estado, sin convertir apertura en cumplimiento | Alta |
| Trazabilidad de edición administrativa | AdminFormalizacionController → FormalizacionAdminPort | Parcial | Edición no pasa actor ni registra AuditoriaPort; Plan General exige auditoría sensible | Coordinar corrección con administración sin inventar endpoints | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | FormalizacionService#enlace/#actualizarEnlace y FormalizacionController#enlace y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/formalizacion/application/usecase/ConsultarEnlaceInstitucionalUseCase.java`, `service/ConsultarEnlaceInstitucionalService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | contenido institucional, administración y frontend |
| `B/modules/formalizacion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `FormalizacionService.java`, `FormalizacionController.java`, `domain/model/Enlace.java`, DTO HTTP y AdminFormalizacionController | Modificar parcialmente tras autorización | Rol, URL externa y estados de enlace; no cambiar contrato sin decisión | Varias | contenido institucional, administración y frontend |

## UC079 — Progreso orientativo

Endpoint(s): GET y PUT /api/v1/formalizacion/progreso.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-079 no certificación | GET/PUT ProgresoResponse incluyen AVISO_LEGAL | Cumple | Disclaimer no depende del porcentaje, incluso 100 | Conservar | — |
| Propiedad y límites | Prestador desde sesión; validar 0..100 y ruta existente | Cumple | No elige cuenta ajena y unique prestador/ruta en V6 | Conservar | — |
| Fuente/fórmula del progreso | PUT acepta valor entero directo; D-13 | Parcial | Progreso agregado manual, sin fórmula aprobada ni checklist; ruta inactiva también aceptada y después oculta por consulta activas | Resolver D-13 y política de ruta antes de cambiar | Alta |
| Concurrencia de primera actualización | preconsulta e inserción UUID; V6 unique | Parcial | Dos altas simultáneas pueden competir por índice único; sin evidencia ejecutada | Diseñar upsert o manejo de conflicto compatible | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | FormalizacionService#progreso/#actualizarProgreso y controller y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/formalizacion/application/usecase/ConsultarProgresoFormalizacionUseCase.java`, `service/ConsultarProgresoFormalizacionService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | ProgresoRepository, D-13 y futura insignia SPEC-18 |
| `B/modules/formalizacion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `FormalizacionService.java`, controller/DTO/mapper y configuración; contrato adicional ActualizarProgresoFormalizacionUseCase | Modificar parcialmente tras autorización | Separar consulta/actualización y definir fuente del avance | Varias | ProgresoRepository, D-13 y futura insignia SPEC-18 |

## UC080 — Vinculación institucional futura

Endpoint(s): GET /api/v1/formalizacion/vinculacion.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-080 OFF por defecto | FeatureFlags integracionInstitucional, application.properties false | Cumple | OFF devuelve 404 y no interviene en ruta/progreso | Conservar aislada | — |
| Capacidad ON | vinculacion devuelve 501 | Parcial | No hay fuente institucional implementada; es Future no aprobado, no debe fingirse disponible | No implementar proveedor sin autorización; pruebas de aislamiento cuando se autoricen | Baja |
| Autorización del endpoint | vinculacion no exige PRESTADOR | Parcial | Con ON un actor autenticado distinto podría invocar la rama; OFF no expone contenido | Mantener control de rol en entrada y feature | Media |
| UseCase + Service + ejecutar; DTO y mapper; núcleo puro | FormalizacionController#vinculacion y FeatureFlags y controlador correspondiente | No cumple | Sin interfaz propia; servicio Spring y BusinessException HTTP; conversiones en controlador | Extraer contratos, Service puro, DTO útiles y mapper; composición/transacción en infraestructura, handler global | Alta |
| Aceptación y pruebas | Inventario src/test; sin ejecución | No verificable | No se acredita el 100% de escenarios ni transacciones | Pruebas en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `B/modules/formalizacion/application/usecase/ConsultarVinculacionInstitucionalUseCase.java`, `service/ConsultarVinculacionInstitucionalService.java` y contratos adicionales útiles por operación | Crear | Operación propia con ejecutar | Aplicación | FeatureFlags, D-13 y proveedor futuro |
| `B/modules/formalizacion/application/dto/`, `presentation/mapper/`, `infrastructure/config/` | Crear/reutilizar una sola vez | Command/Query/Result útiles, mapper y composición | Aplicación/presentación/infraestructura | Otros CU del SPEC |
| `FormalizacionController.java`, aplicación/config de flag y puerto futuro solo si se autoriza | Modificar parcialmente tras autorización | Extraer lectura de feature sin implementar proveedor productivo ni habilitarla | Varias | FeatureFlags, D-13 y proveedor futuro |

## Hallazgos comunes, tareas y dependencias

- Modelos y repositorios son puros; entidad/JPA/mapper/adaptador separados y V6/V7 presentes. Ninguna migración a crear ni ejecutar. No se afirma ejecución de seeds ni aprobación de contenido.
- No hay UseCases propios, Command/Result ni mapper HTTP. Controller arma jerarquía y consulta servicio varias veces. Aplicación usa Spring/BusinessException. Extraer composición sin duplicar lectura y preservando FormalizacionAdminPort.
- No halladas pruebas específicas de formalización en inventario consultado; no se ejecutó ninguna. No marcar SC-001/002/003 al 100% por existir código.
- Plan usa package anterior y flag con nombre distinto al real (formalizacion.integracion-institucional.enabled frente a aliado.features.integracion-institucional); actualizar solo en fase autorizada. Modelo VinculacionInstitucional del plan no está en V6; ser Future OFF no exige inventar una tabla.

| Tareas | Estado comprobado |
|---|---|
| T001 | Parcial: módulo presente, patrón faltante |
| T002 | Tablas MVP en V6; no ejecución ni configuración institucional persistida acreditada |
| T003 | Seed V7 presente, aprobación no verificable |
| T004–T005 | Disclaimer y flag OFF presentes |
| T006 | Validación URL no implementada |
| T008–T009 | Ruta/endpoints presentes; arquitectura pendiente |
| T011–T012 | Modelo/metadata presentes; externo/URL/rol pendientes |
| T014–T016 | Avance y aviso presentes; fuente/fórmula pendiente |
| T018–T020 | Future sin fuente/adaptador; no habilitar por esta auditoría |
| T007/T010/T013/T017, F13-QA-* | No verificable por ejecución |
| F13-FE-* | No evaluadas |

Orden: UC077 → UC078 → UC079; UC080 se conserva aislado y OFF. Las reglas de URL/fuente y progreso requieren decisión D-13; no realizar verificaciones legales ni trámites externos.

