# Auditoría estática SPEC-03 — Descubrimiento de prestadores

Alcance: UC014 y UC015. Fuentes: AGENTS.md, SPEC-03 y su plan, secciones pertinentes del plan general, SPEC-05, documentos relacionados de catálogo, compatibilidad, confianza y administración, código de descubrimiento y proveedores, seguridad y migración V3. El contrato cotejado es el del plan y las firmas del controlador; no se encontró OpenAPI estático ni se consultó una instancia. No se ejecutaron pruebas, compilación, aplicación ni migraciones; frontend no evaluado.

Abreviaturas: B = `src/main/java/com/sebastian/spring/mas/aliado/aliado/`, D = `B/modules/descubrimiento/`, P = `B/modules/perfiles/`.

**Avance posterior:** se extrajeron las operaciones de UC014 y UC015 a UseCase, Service, DTO y mapper, con proyección pública compartida. Las matrices siguientes conservan el diagnóstico inicial. Se preservaron las reglas existentes de búsqueda, disponibilidad y datos generales mientras se definen los cambios funcionales. La compilación no se acreditó: el wrapper falló antes de iniciar Maven. No se ejecutaron pruebas ni migraciones. Ambos CU siguen pendientes de verificaciones y de decisiones sobre catálogo, sugerencias, coherencia de disponibilidad y privacidad de datos anidados.

## UC014 — Buscar prestadores

Endpoint: `GET /api/v1/descubrimiento/prestadores?categoria=&zona=`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| FR-014: categoría y zona | `D/application/DescubrimientoService.java#buscar`; `P/infrastructure/persistence/jpa/PerfilProfesionalJpaRepository.java#findVisibles` | Cumple | Con ambos criterios resueltos se exige pertenencia a categoría y zona declaradas | Conservar semántica aprobada | — |
| Sin resultados | `buscar` devuelve lista vacía; controlador responde 200 | Cumple | No se convierte en error | Conservar | — |
| Solo Cliente | `DescubrimientoController#buscar`, `SecurityUtils.exigirRol`, `SecurityConfig` | Cumple | Otro rol 403; visitante 401 | Conservar | — |
| Sugerencias semejantes | `resolverCategoriaId`, `resolverZonaId`, `categoriasActivas`, `zonasActivas` | Parcial | Devuelve todo el catálogo activo, sin semejanza; dos errores solo sugieren el primero | Acordar política y formato | Alta |
| SC-005: catálogo y coincidencia | Parámetros opcionales; `CatalogoService` resuelve ID/nombre | Parcial | Política no definida; consultas por ID/nombre aceptan inactivos | Decidir obligatoriedad, vigencia y coincidencia | Alta |
| Coherencia lista/detalle | `buscar` y `detalle` | Parcial | Lista admite perfil sin disponibilidad; detalle lo rechaza | Acordar criterio de completitud y aplicar política común | Alta |
| Privacidad | `PrestadorPublico`, DTO HTTP, `PerfilProfesional.datosPublicos` | Parcial | No hay dirección explícita, pero valores `Object` anidados pueden contener datos privados | Definir esquema público seguro con SPEC-05 | Alta |
| UseCase y mapeos | `DescubrimientoController`, `DescubrimientoService#buscar` | No cumple | Inyecta servicio concreto; resultado anidado y mapper privado | Extraer UseCase, Service, Query, Result y mapper | Alta |
| Framework y puertos | `DescubrimientoService`; puertos de módulos proveedores | Parcial | Spring en aplicación y puertos definidos por proveedores, contrario a regla actual del consumidor | Composición y transacción en infraestructura; puertos del consumidor sin ciclos | Media |
| Persistencia/Flyway | Repositorio, adaptador y mapper de perfil; V3 | Cumple | Sin tablas propias ni entidades JPA en HTTP | Reutilizar | — |
| Aceptación y p95 | Sin pruebas específicas ni medición | No verificable | Múltiples consultas por prestador; rendimiento no medido | Medir antes de optimizar | Media |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `D/application/usecase/BuscarPrestadoresUseCase.java`, `D/application/service/BuscarPrestadoresService.java` | Crear | Contrato y orquestación | Aplicación | Controlador y puertos |
| `D/application/dto/BuscarPrestadoresQuery.java`, `BusquedaPrestadoresResult.java`, `PrestadorPublicoResult.java` | Crear | Entrada y resultados independientes de HTTP | Aplicación | UC015 y mapper |
| `D/presentation/mapper/DescubrimientoPresentationMapper.java`, `D/infrastructure/config/DescubrimientoUseCaseConfig.java` | Crear | Conversión y transacción de lectura | Presentación/infraestructura | DTO HTTP, puertos |
| `D/application/port/` y adaptadores en `B/modules/{perfiles,catalogo,confianza,perfil}/infrastructure/adapter/` | Crear según necesidad | Puertos del consumidor | Aplicación/infraestructura | Proveedores y UC015; evitar ciclos |
| `D/presentation/controller/DescubrimientoController.java`, `D/application/DescubrimientoService.java` | Modificar parcialmente | Extraer búsqueda | Presentación/aplicación | Preservar detalle |
| Política de sugerencias, catálogo y privacidad | Por determinar | Decisiones funcionales pendientes | Varias | SPEC-04/05/12 |

## UC015 — Consultar perfil público del prestador

Endpoint: `GET /api/v1/descubrimiento/prestadores/{id}/perfil`.

| Requisito o regla | Evidencia en el código | Estado | Hallazgo | Corrección propuesta | Prioridad |
|---|---|---|---|---|---|
| Acceso desde lista y directo | `DescubrimientoService#detalle` y `aPublico` | Cumple | Mismo endpoint y mapeo sin depender de procedencia | Conservar | — |
| Datos públicos requeridos | `aPublico` integra categorías, zonas, reputación y disponibilidad | Cumple | Usa datos reales de módulos propietarios | Conservar integración | — |
| 404 genérico | `detalle`: visibilidad, exposición y disponibilidad | Cumple | Oculto, inexistente e incompleto producen `PERFIL_NO_DISPONIBLE` | Mantener formato; aclarar completitud | — |
| Solo Cliente | `DescubrimientoController#detalle`, seguridad | Cumple | Otros roles 403; sin sesión 401 | Conservar | — |
| Coherencia con búsqueda | `buscar` no exige disponibilidad y `detalle` sí | Parcial | Un resultado puede conducir a 404 | Acordar y compartir política de disponibilidad | Alta |
| Privacidad de datos generales | `PerfilProfesional.datosPublicos` | Parcial | Filtra clave superior pero no estructura de valores anidados | Esquema público explícito con SPEC-05 | Alta |
| Nulos en datos generales | `PerfilProfesional.datosPublicos`, `Map.copyOf` | No cumple | Un valor null bajo clave admitida puede producir NPE; inferencia estática | Definir tratamiento de nulos y corregir | Media |
| UseCase y mapper | Controller inyecta `DescubrimientoService`; mapper privado | No cumple | Falta contrato propio y resultado de aplicación | Extraer UseCase/Service, Result y mapper | Alta |
| Excepciones | `BusinessException.NotFound` en aplicación | Parcial | Semántica HTTP en excepción de aplicación | Excepción propia y traducción global, manteniendo formato | Media |
| Vista previa SPEC-05 | `PerfilProfesionalController#vistaPrevia`; `aPublico` | Parcial | Construcción separada; disponibilidad incluye ID en preview y no en descubrimiento | Acordar proyección canónica con UC033, sin ampliar alcance sin autorización | Alta |
| Persistencia | Puertos, adaptador y mapper de perfil profesional | Cumple | Sin JPA externo directo | Preservar límites | — |
| SC-002/003/004 | Sin pruebas específicas identificadas | No verificable | Sin ejecución HTTP | Verificar en etapa autorizada | Alta |

| Ruta propuesta | Acción | Motivo | Capa | Dependencias afectadas |
|---|---|---|---|---|
| `D/application/usecase/ConsultarPerfilPublicoUseCase.java`, `D/application/service/ConsultarPerfilPublicoService.java` | Crear | Operación propia | Aplicación | Perfiles, catálogo, confianza, visibilidad |
| `D/application/dto/PrestadorPublicoResult.java` y subresultados | Crear/reutilizar | Proyección compartida | Aplicación | UC014 |
| `D/application/exception/PerfilPublicoNoDisponibleException.java`, `B/common/web/GlobalExceptionHandler.java` | Crear/modificar | Error semántico y 404 genérico | Aplicación/presentación | Formato vigente |
| Mapper, configuración y puertos de UC014 | Reutilizar/ampliar | Conversión y composición | Varias | UC014 |
| `D/presentation/controller/DescubrimientoController.java`, `D/application/DescubrimientoService.java` | Modificar | Extraer detalle | Presentación/aplicación | Verificar consumidores antes de retirar servicio |
| `P/domain/model/PerfilProfesional.java` y construcción pública | Proponer modificación coordinada | Nulos y valores públicos arbitrarios | Dominio/aplicación | UC014/015, SPEC-05 UC033 |
| `P/presentation/controller/PerfilProfesionalController.java` | Solo con autorización adicional | Coherencia con preview | Presentación | SPEC-05 UC033 |

## Hallazgos compartidos y decisiones

- SC-005 deja pendiente la política de coincidencia. El código usa ID o nombre exacto sin distinguir mayúsculas y permite criterios omitidos. La búsqueda de catálogo vigente y sugerencias semejantes requieren decisión.
- Un perfil sin disponibilidad puede salir en lista y ser rechazado en detalle; SPEC-05 permite crear perfil antes de horarios. No se debe inventar una definición de completitud.
- El mapa de datos generales requiere revisar valores anidados y nulos sin ampliar silenciosamente UC033. Es una prioridad de privacidad para ambos CU.
- El plan describe una lista para éxito y una variante con sugerencias; el código devuelve siempre `{prestadores, sugerencias}`. No cambiar el contrato sin autorización.
- Descubrimiento limita SOLO_ZONA a búsquedas acotadas por zona y PUBLICO permite también búsquedas sin zona; SPEC-02 expone el mismo DTO en ambos. Acordar política transversal antes de cambiarla.
- La documentación alude a 403 para visitantes; seguridad devuelve 401 sin sesión, coherente con AGENTS. El plan usa package obsoleto y menciona planes de dependencias inexistentes que hoy sí existen.
- No se propone eliminar archivos ni agregar migraciones. El servicio agrupado solo podría retirarse tras extraer ambos CU y verificar consumidores. Riesgo de múltiples consultas no demuestra incumplimiento de p95.

## Tareas del plan y estado

| Tarea | Estado comprobado |
|---|---|
| T001 | Parcial: módulo y controlador existen; faltan UseCases, servicios, DTO aplicación y mapper |
| T002 | No verificable: sin fixtures específicos identificados; integración productiva usa datos reales |
| T003–T004 | No verificable: pruebas específicas y ejecución no acreditadas |
| T005 | Parcial: modelo público existe; faltan criterio y resultado de aplicación |
| T006 | Parcial: búsqueda real con decisiones y arquitectura pendientes |
| T007 | Parcial: detalle y mapeo común existen; falta contrato y coherencia integral |
| T008 | Cumple por inspección para rol Cliente; discrepancia documental 401/403 |
| T009 | Endpoints presentes; cumplimiento parcial por arquitectura |
| F03-FE-* | No evaluadas |
| F03-QA-01 | Parcial: construcción duplicada y diferencias con preview |
| F03-QA-02/03/04 | No verificable sin ejecución ni medición |

UC014 y UC015 se extrajeron arquitectónicamente en ese orden; permanecen pendientes las decisiones funcionales y verificaciones indicadas. No se consideran completadas las tareas ni los criterios de aceptación sin evidencia ejecutable.
