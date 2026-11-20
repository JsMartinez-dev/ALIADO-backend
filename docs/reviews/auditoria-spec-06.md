# Auditoría estática SPEC-06 — Compatibilidad y oportunidades

**Estado de seguimiento:** el usuario solicitó marcar la refactorización como finalizada. Los pendientes de temporalidad, entrega real, privacidad y verificaciones continúan abiertos; este estado no acredita su cumplimiento.

**Avance posterior:** UC034–UC038 se extrajeron a UseCases y servicios; las tablas siguientes reflejan el diagnóstico inicial. El usuario decidió conservar el criterio provisional de disponibilidad y el resultado booleano; D-03 y la distinción de no evaluable siguen pendientes. Para nuevas oportunidades, el canal de log ya no marca entrega sin canal real y el aviso queda pendiente; no se modificaron avisos previamente entregados. Entrega real, idempotencia, preferencias, acceso a fotos y aceptación siguen pendientes. Se compiló parcialmente el núcleo con javac; Maven falló antes de iniciar. No se ejecutaron pruebas ni migraciones.

Alcance: UC034–UC038. Los cinco CU tienen implementación Parcial y auditoría estática completa (Revisado); esto no acredita refactorización, pruebas ni aceptación. No se modificó código ni se ejecutaron pruebas, compilación, migraciones o peticiones de escritura.

Fuentes: AGENTS.md; SPEC-06 y su plan completos; secciones pertinentes del Plan General; decisión D-03; requisitos relacionados de SPEC-04/05/08/09 y plan de SPEC-09; código de oportunidades, notificaciones, solicitudes, perfiles, visibilidad, catálogo y seguridad; V4 y pruebas existentes. Se consultó en modo lectura OpenAPI local `/api-docs`. Frontend no evaluado.

Abreviaturas: B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`; O = `B/modules/oportunidades/`; N = `B/modules/notificaciones/`.

## UC034 — Calcular compatibilidad

Incluido en tablero, detalle, notificaciones y validación de propuestas; sin endpoint propio.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Categoría y zona | `O/application/OportunidadService.java#compatible` | Cumple | Exige pertenencia a categorías y zonas del perfil | Conservar | — |
| Solicitud abierta y perfil visible | `compatible` | Cumple | Exige PUBLICADA, exposición PUBLICO y preferencia distinta de PRIVADO | Conservar | — |
| Disponibilidad | `compatible`: `!perfil.disponibilidades().isEmpty()` | Parcial | Solo comprueba bloques existentes; no horas, fecha ni agenda | Resolver D-03 antes de cambiar matching | Alta |
| No evaluable | `compatible`, `esCompatible`, `prestadoresCompatibles` | No cumple | Booleano no distingue datos insuficientes de incompatibilidad ni valida explícitamente todos los datos faltantes | Resultado distinguible y guardas; decidir estado calculado o persistido | Alta |
| UseCase y dominio | Método privado y CompatibilidadPort | No cumple | Sin CalcularCompatibilidadUseCase/Service; regla en servicio Spring | Extraer contrato, Service y cálculo puro | Alta |
| Puertos | Consume puertos definidos por proveedores | Parcial | Sin JPA externo, pero regla actual pide puertos del consumidor | Contratos necesarios y adaptadores sin ciclos | Media |
| Persistencia | Oportunidad record puro | Cumple | Proyección sin tabla propia | Conservar | — |
| Pruebas | CompatibilidadTest existente | No verificable | No ejecutado; no acredita horarios/no evaluable | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `O/application/usecase/CalcularCompatibilidadUseCase.java`, `service/CalcularCompatibilidadService.java` | Crear | Operación propia | Aplicación | Tablero, detalle, notificaciones y propuestas |
| Regla bajo `O/domain/model/` y resultado útil | Crear | Cálculo y NO_EVALUABLE | Dominio/aplicación | D-03, solicitudes y perfiles |
| `O/application/port/` y adaptadores | Crear según necesidad | Puertos consumidor | Aplicación/infraestructura | Solicitudes, perfiles y visibilidad |
| `O/application/OportunidadService.java` | Modificar parcialmente | Delegar cálculo | Aplicación | Preservar CompatibilidadPort |
| DTO/esquema temporal | Solo con autorización | D-03 | Entre módulos | SPEC-04/05/07/08; no migración inferida |

## UC035 — Consultar tablero

Endpoint: `GET /api/v1/oportunidades`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Publicadas compatibles | `OportunidadService#listar`, `SolicitudConsultaPort#publicadas` | Cumple | Usa misma función compatible | Conservar, sujeto alcance temporal UC034 | — |
| Tablero vacío | `listar` | Cumple | 200 lista vacía sin coincidencias o perfil público | Conservar | — |
| Rol e identidad | `OportunidadController#listar`, SecurityConfig | Cumple | PRESTADOR de sesión; otro rol 403, sin sesión 401 | Conservar | — |
| UseCase y mapper | Controller inyecta servicio y usa `aRespuesta` privado | No cumple | Sin contrato, Result o mapper dedicado | Extraer UseCase/Service, Query/Result y mapper | Alta |
| Framework | OportunidadService Spring/transacción | Parcial | Aplicación acoplada | Composición read-only en infraestructura | Media |
| Rendimiento | Carga publicadas, visibilidad por candidata y catálogo por resultado | Parcial | Riesgo de consultas repetidas, sin medición | Medir antes de optimizar/paginar | Media |
| SC-001/002 | Sin ejecución | No verificable | Sin aceptación ni p95 acreditados | Verificar posteriormente | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `O/application/usecase/ConsultarTableroOportunidadesUseCase.java`, Service | Crear | Entrada propia | Aplicación | UC034 y puertos |
| `O/application/dto/ConsultarTableroQuery.java`, `OportunidadResult.java` | Crear | Entrada/salida independiente de HTTP | Aplicación | Filtros y detalle |
| `O/presentation/mapper/OportunidadPresentationMapper.java`, `O/infrastructure/config/OportunidadUseCaseConfig.java` | Crear | Conversión y transacción | Presentación/infraestructura | Controller |
| Controller y servicio agrupado | Modificar parcialmente | Enrutar interfaz | Varias | Preservar puerto compatibilidad |
| Proyección común | Crear/reutilizar | Evitar duplicación | Aplicación | UC037 |

## UC036 — Filtrar tablero

Mismo GET; parámetros opcionales categoria y urgencia.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Filtro no altera compatibilidad | `listar`: compatible antes de filtros | Cumple | Solo acota resultado | Conservar | — |
| Sin coincidencias | `resolverCategoria` y predicados | Cumple | Lista vacía sin error | Conservar | — |
| Criterios | Categoría UUID/nombre; urgencia ignorando mayúsculas | Cumple | Criterios opcionales; no reconocido produce vacío | Conservar contrato | — |
| UseCase | Filtros en listar; plan los agrupa en tablero | No cumple | AGENTS exige interfaz por CU | Diseñar contrato/delegación útil sin duplicar ni crear endpoint | Alta |
| Query/Result/mapper | Parámetros escalares y dominio al controller | No cumple | Falta DTO de filtros y mapper | Reutilizar con UC035 | Alta |
| Pruebas | Sin ejecución específica | No verificable | CompatibilidadTest no cubre filtro | Etapa autorizada | Media |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| Contrato/Service de filtrado bajo `O/application/` según descomposición acordada | Crear | CU propio | Aplicación | UC035 |
| Query/Result/mapper compartidos | Crear/reutilizar | Criterios y salida | Aplicación/presentación | UC035/037 |
| Controller y servicio agrupado | Modificar parcialmente | Interfaz | Varias | GET existente |
| Plan SPEC-06 | Proponer actualización autorizada | Granularidad UC036 omitida de interfaces | Documentación | AGENTS |

## UC037 — Consultar detalle

Endpoint: `GET /api/v1/oportunidades/{solicitudId}`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Compatible o 404 uniforme | `OportunidadService#detalle` | Cumple | Mismo código/mensaje inexistente o incompatible | Conservar | — |
| Rol e identidad | Controller#detalle | Cumple | PRESTADOR de sesión | Conservar | — |
| Datos sin campos privados explícitos | `aOportunidad`, OportunidadResponse | Cumple | Categoría, zona, descripción, urgencia, fecha y claves fotos; sin ID/contacto/dirección del Cliente | Conservar proyección | — |
| Fotos | Keys sin fotoId; descarga SPEC-04 permite cualquier autenticado si PUBLICADA | Parcial | Referencia no navegable directamente y descarga no exige compatibilidad | Acordar representación y autorización con UC016 | Alta |
| UseCase/Result/mapper | Controller y servicio agrupado | No cumple | Sin contrato propio | Extraer UseCase/Service y mapper | Alta |
| Errores | BusinessException.NotFound en aplicación | Parcial | Semántica HTTP en aplicación | Excepción propia y handler global | Media |
| Aceptación | Sin ejecución | No verificable | Sin matching/privacidad integral acreditados | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `O/application/usecase/ConsultarDetalleOportunidadUseCase.java`, Service y Query útil | Crear | Operación propia | Aplicación | UC034 y consultas |
| `O/application/exception/OportunidadNoDisponibleException.java`, handler global | Crear/modificar | 404 semántico | Aplicación/presentación | Formato vigente |
| Result/proyección/mapper/config tablero | Reutilizar/ampliar | Construcción común | Varias | UC035 |
| Controller y servicio agrupado | Modificar parcialmente | Integrar interfaz | Varias | Conservar CompatibilidadPort |
| DTO y permisos fotos | Solo tras autorización | Descarga autorizada | Varias | SPEC-04 UC016 y clientes API |

## UC038 — Detectar y notificar nueva oportunidad

Generación por evento; consulta `GET /api/v1/notificaciones/oportunidades`.

| Requisito o regla | Evidencia | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Publicación real | CreacionSolicitud, PublicarSolicitudService, NotificacionEventListener#alPublicarSolicitud | Cumple | Evento sin consultar tablero | Conservar | — |
| Destinatarios y registro | Listener usa CompatibilidadPort; NotificacionService#registrar | Cumple | NUEVA_OPORTUNIDAD con solicitud, destinatario y versión | Conservar, sujeto UC034 | — |
| Consulta propia/rol | NotificacionController#notificacionesOportunidades | Cumple | Filtra destinatario sesión y PRESTADOR | Conservar | — |
| Pendiente sin sesión | Worker, LogCanalEntregaAdapter | No cumple | Log marca ENTREGADA sin proveedor/recepción ni esperar conexión | Resolver entrega y estado con SPEC-09 | Alta |
| Entrega efectiva | Adaptador log devuelve true | Parcial | Bandeja existe; entrega externa no acreditada | Proveedor aprobado o canal simulado explícito | Alta |
| Idempotencia de evento | Preconsulta y UNIQUE V4 | Parcial | Carrera puede causar conflicto/rollback de publicación; convergencia no acreditada | Verificar y corregir transacción/concurrencia | Alta |
| Preferencias | ConsultaPreferenciasPort en registrar | Parcial | Deshabilitado impide registro frente SC-004 100% | Confirmar interpretación con SPEC-14 | Media |
| UseCases/mapeo | Listener llama puertos; controller usa puerto y mapper privado | No cumple | Sin DetectarNuevaOportunidadUseCase ni contrato consulta propio | Separar productor/consulta reutilizando módulo dueño | Alta |
| Aceptación | Sin ejecución | No verificable | Sin prueba de entrega/offline/unicidad | Etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias |
|---|---|---|---|---|
| `O/application/usecase/DetectarNuevaOportunidadUseCase.java`, Service y DTO evento | Crear | Productor propio | Aplicación | UC034, notificaciones |
| `O/application/port/RegistroNotificacionPort.java` y adaptador notificaciones | Crear | Puerto consumidor | Aplicación/infraestructura | Evitar ciclos |
| `N/infrastructure/adapter/NotificacionEventListener.java` | Modificar parcialmente | Delegar detección | Infraestructura | Mantener otros eventos |
| Contrato/Service consulta avisos, Result y mapper en dueño | Crear/reutilizar | Entrada explícita | Aplicación/presentación | Controller |
| `N/application/NotificacionService.java`, canal y worker | Solo alcance aprobado | Entrega/offline/concurrencia | Aplicación/infraestructura | SPEC-09/14 |
| V4 notificaciones | Reutilizar | Esquema existente | Persistencia | No editar migración |

## Decisiones y hallazgos compartidos

1. D-03: solicitud sin ventana temporal; disponibilidad es existencia de bloques y agenda no interviene. No afirmar matching horario completo ni inventar fecha/duración/migración.
2. NO_EVALUABLE está mezclado con incompatible; plan deja pendiente si estado debe persistirse. Un resultado calculado no exige nueva tabla por sí mismo.
3. Faltan cinco interfaces propias; plan omite contrato independiente UC036. Resolver descomposición sin duplicación.
4. Preservar CompatibilidadPort usado por propuestas/notificaciones; evitar ciclos y conservar lectura dedicada de visibilidad.
5. Log no demuestra entrega; coordinar con SPEC-09 y preferencias SPEC-14.
6. DTO no expone campos privados, pero otra ruta de descarga no exige compatibilidad; no afirmar privacidad de todas las rutas.
7. T002 pide tabla notificaciones_oportunidad, en contradicción con Data Model/T005/T006/Plan General y tabla unificada V4. Proponer corrección documental autorizada, no crear migración.
8. Plan dice 401 sin rol; código usa 401 sin sesión y 403 por rol, correcto según AGENTS.
9. OpenAPI local: los tres GET tienen parámetros correctos y sin body, solo documentan 200; faltan errores 401/403/404. No colisión de requests sin cuerpo.
10. Plan General propone p95 <=500 ms provisional en lecturas; plan local dice sin cifra. Sin medición, no afirmar incumplimiento.

## Tareas y secuencia

| Tarea | Estado comprobado |
|---|---|
| T001 | Parcial: módulo/modelo/controller existen; patrones y mapeos pendientes |
| T002 | Contradictoria: tabla puntual no debe crearse; V4 unificada existente |
| T003–T004 | No verificable por ejecución |
| T005–T006 | Parcial: modelo/tabla/registro presentes; contratos obligatorios pendientes |
| T007 | Parcial: tablero/cálculo booleano presentes; temporalidad/NO_EVALUABLE/patrón pendientes |
| T008 | Filtros presentes; aceptación y patrón pendientes |
| T009 | Parcial: evento/registro presentes; entrega/offline/patrón pendientes |
| T010 | Endpoints presentes; patrón y errores OpenAPI pendientes |
| T011–T012 | No verificable por ejecución |
| T013–T014 | Detalle presente; fotos/patrón/aceptación pendientes |
| F06-FE-* | No evaluadas |
| F06-QA-* | No acreditadas sin ejecución |

CompatibilidadTest cubre booleano, ausencia de disponibilidad, perfil oculto/privado, categoría distinta y ausencia de solicitud/perfil. CorreccionesVerificacionTest cubre indirectamente registro/preferencias de notificaciones. No ejecutadas.

Orden recomendado: UC034 → UC035 → UC036 → UC037 → UC038. Resolver D-03 y representación no evaluable antes de afirmar cumplimiento funcional. Compartir Result/mapper/configuración. No se propone eliminar archivos ahora; reducir/retirar servicio agrupado solo tras verificar consumidores y pruebas existentes.
