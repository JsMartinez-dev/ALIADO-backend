# Instrucciones para el agente de desarrollo

# 1. Objetivo y contexto obligatorio

Este proyecto es un monolito modular desarrollado con Clean Architecture.

Todas las tareas de desarrollo deben respetar las reglas arquitectónicas definidas en este documento.

## 1.1. Documentación del proyecto

La documentación se encuentra organizada de la siguiente manera:

- `docs/plans/plan-general-ALIADO.md`: visión global, alcance, organización y dependencias del sistema.
- `/docs/specs/`: especificaciones funcionales y no funcionales de cada funcionalidad.
- `/docs/plans/`: planes técnicos de implementación asociados a cada SPEC.
- `/docs/architecture/`: documentación arquitectónica complementaria, cuando exista.

## 1.2. Jerarquía y responsabilidad de la documentación

Utiliza cada documento según su responsabilidad:

1. Las instrucciones explícitas más recientes del usuario prevalecen sobre instrucciones anteriores.
2. `AGENTS.md` establece las reglas arquitectónicas y convenciones obligatorias de implementación.
3. Los SPEC son la fuente de verdad para los requisitos funcionales, reglas de negocio y criterios de aceptación.
4. El Plan General establece el contexto global, la organización del sistema y sus dependencias.
5. Los Implementation Plans establecen la secuencia técnica de implementación y las tareas necesarias para completar cada SPEC.

Un Implementation Plan no puede introducir requisitos funcionales que no estén respaldados por los SPEC ni contradecir las reglas arquitectónicas de `AGENTS.md`.

Si un plan contiene una estructura de paquetes, tecnología o decisión técnica incompatible con la arquitectura vigente, identifica el conflicto y propone actualizar el plan antes de implementarlo.

No resuelvas contradicciones importantes silenciosamente.

## 1.3. Documentación que debe consultarse antes de implementar

Cuando el usuario solicite trabajar en un SPEC específico:

1. Lee `AGENTS.md`.
2. Lee `docs/plans/plan-general-ALIADO.md`.
3. Localiza el SPEC correspondiente en `/docs/specs/`.
4. Localiza su Implementation Plan en `/docs/plans/`.
5. Inspecciona los SPEC y planes adicionales que tengan dependencias directas o afecten los módulos involucrados.
6. Consulta la documentación arquitectónica necesaria para la tarea.
7. Inspecciona el código existente antes de proponer modificaciones.

No es obligatorio volver a leer todos los SPEC del proyecto para cada tarea. Consulta los documentos relacionados con el alcance solicitado y amplía la lectura cuando existan dependencias entre funcionalidades, módulos o contratos compartidos.

Si no encuentras el SPEC o su Implementation Plan, indícalo expresamente.

## 1.4. Relación entre los SPEC y los Implementation Plans

Cada Implementation Plan debe corresponder a un único SPEC.

El plan es la guía técnica para desarrollar los casos de uso documentados en su especificación.

Al utilizar un plan:

- Conserva sus identificadores de tareas.
- Respeta el orden de ejecución y sus dependencias.
- Comprueba que las tareas correspondan a requisitos reales.
- Identifica las tareas que ya están implementadas.
- No vuelvas a implementar componentes que ya existen.
- No marques tareas como terminadas sin verificar su cumplimiento.
- Si la arquitectura cambia, identifica las tareas del plan que requieren actualización.
- No elimines tareas o dependencias sin justificar el cambio.

El plan puede incluir tareas técnicas derivadas de un requisito, como crear un adaptador, implementar una migración o desarrollar pruebas. Estas decisiones deben ser coherentes con la arquitectura y no deben alterar el comportamiento funcional especificado.

## 1.5. Contradicciones y documentación desactualizada

Si encuentras diferencias entre documentación y código:

1. Identifica el requisito y los archivos involucrados.
2. Explica la diferencia encontrada.
3. Determina si corresponde actualizar el plan, corregir la implementación o solicitar una decisión funcional.
4. No modifiques documentación ni código fuera del alcance autorizado.

Si `/docs` está incompleto o algún archivo no puede abrirse, informa exactamente qué información no estuvo disponible.

Antes de una tarea grande, presenta un resumen breve de los documentos consultados, las tareas del plan relacionadas y los módulos que podrían modificarse.

No afirmes haber leído documentos que no hayas podido consultar.

## 2. Estructura arquitectónica requerida por módulo

Cada módulo de negocio debe respetar esta estructura base, adaptándola solo cuando una carpeta no sea necesaria para su funcionalidad:

```text
modulo/
├── domain/
│   ├── model/                 # Modelos de dominio y reglas/invariantes del negocio
│   ├── repository/            # Interfaces/puertos de repositorios requeridos por el núcleo
│   └── exception/             # Excepciones propias del dominio, cuando aplique
│
├── application/
│   ├── usecase/                # Contratos explícitos de casos de uso, si aportan claridad
|   ├── service/               # Implementacion de cada interfaz de caso de uso 
│   ├── dto/                   # Commands, queries y resultados de aplicación; no DTO HTTP
│   └── port/                  # Otros puertos de entrada/salida, especialmente integraciones externas
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/            # Entidades de persistencia JPA (@Entity, @Table, etc.)
│   │   ├── mapper/            # Conversión entre dominio y persistencia
│   │   ├── jpa/               # Interfaces que extienden JpaRepository y detalles Spring Data
│   │   └── adapter/           # Implementaciones concretas de los puertos de repositorio
│   ├── adapter/               # Adaptadores para correo, APIs u otros sistemas externos
│   └── config/                # Configuración técnica y composición de dependencias
│
└── presentation/
    ├── controller/            # Controladores HTTP/API REST
    ├── dto/                   # DTO de entrada y salida HTTP (Request/Response)
    ├── mapper/                # Conversión entre DTO HTTP y objetos de aplicación
    └── exception/             # Traducción de errores a respuestas HTTP
```

No crees carpetas, interfaces o clases vacías solo para que el árbol se vea completo. Añade componentes cuando la funcionalidad los necesite. Respeta los nombres y convenciones que ya use el repositorio, excepto cuando se solicite una refactorización explícita.

## 3. Responsabilidades de cada capa

### `domain/`

- Contiene el modelo del negocio, las reglas esenciales, invariantes y excepciones de dominio.
- Las clases del dominio deben ser Java puro y no deben importar Spring, JPA/Hibernate, DTO HTTP, controladores, clientes HTTP ni SDK de proveedores.
- No agregues `@Entity`, `@Table`, `JpaRepository`, `@RestController` ni anotaciones equivalentes a clases del dominio.
- `domain/repository/` contiene contratos/puertos de persistencia necesarios para el núcleo. Estas interfaces no deben extender `JpaRepository` ni exponer `Entity` de persistencia.
- Las interfaces deben declarar operaciones necesarias para el negocio; no añadas métodos especulativos o CRUD genérico sin una necesidad real.
- Coloca excepciones aquí solamente cuando representen una condición o regla del dominio. Los errores de red, configuración o persistencia no son automáticamente errores de dominio.

### `application/`

- Contiene los casos de uso y la orquestación de operaciones del sistema.
- Las clases `Service` implementan la interfaz `UseCase` correspondiente y su método `ejecutar()`. Coordinan el dominio y usan interfaces/puertos; no contienen consultas JPA ni detalles de HTTP.
- `application/dto/` contiene objetos de entrada/salida de los casos de uso, como `RegistrarClienteCommand` o `ClienteResultado`. No reutilices automáticamente DTO de presentación.
- `application/port/` define contratos para dependencias externas que la aplicación necesita, como correo, almacenamiento de archivos, APIs de terceros o publicación de eventos.
- Evita dependencias de framework en el núcleo de la aplicación. No introduzcas imports de Spring en `domain/` y evita introducirlos en `application/`; gestiona el registro de beans/configuración técnica desde infraestructura cuando sea viable y consistente con el proyecto.
- `usecase/` contiene obligatoriamente una interfaz `UseCase` propia para cada caso de uso. Cada interfaz declara `ejecutar()` y tiene una clase `Service` correspondiente que implementa la interfaz y el método.
- Implementa validaciones de negocio en el dominio o en el caso de uso según corresponda; la validación superficial de formato de una petición puede estar en presentación.

### `infrastructure/`

- Contiene implementaciones tecnológicas concretas y detalles reemplazables.
- `persistence/entity/` contiene modelos de persistencia, por ejemplo `ClienteEntity`, con anotaciones JPA.
- `persistence/jpa/` contiene interfaces Spring Data, por ejemplo `ClienteJpaRepository extends JpaRepository<ClienteEntity, Long>`.
- `persistence/mapper/` convierte explícitamente entre modelos de dominio y entidades de persistencia. No filtren entidades JPA hacia el dominio o hacia los casos de uso.
- `persistence/adapter/` implementa los puertos de repositorio definidos en el núcleo. El adaptador coordina el mapper y el repositorio JPA; no debe apropiarse de las reglas de negocio.
- `infrastructure/adapter/` implementa puertos de integración, por ejemplo un adaptador de correo que implemente una interfaz definida por la aplicación.
- `config/` mantiene configuración técnica y composición de dependencias cuando se necesite.
- Mantén credenciales, secretos y datos sensibles fuera del código fuente. Utiliza la configuración segura ya adoptada por el proyecto.

### `presentation/`

- Los controladores reciben peticiones, convierten Request DTO a Command mediante un mapper, invocan la interfaz `UseCase` (nunca el `Service` concreto), convierten el Result a Response DTO mediante un mapper y devuelven respuestas HTTP.
- `presentation/dto/` contiene contratos de la API como `CrearClienteRequest` y `ClienteResponse`. No los confundas con `Cliente`, `ClienteEntity` ni con los DTO de aplicación.
- `presentation/mapper/` convierte entre DTO HTTP y los objetos de entrada/salida de la aplicación.
- Los controladores no deben acceder directamente a `JpaRepository`, entidades JPA ni la base de datos, ni implementar reglas de negocio complejas.
- `presentation/exception/` o un manejador global traduce errores a códigos y cuerpos HTTP consistentes. No dupliques manejadores si ya existe uno global.
- No expongas directamente entidades JPA ni objetos internos del dominio como contrato público de la API, salvo que una decisión de diseño existente y explícita lo justifique.

## 4. Dirección de dependencias e inversión de dependencias

La regla obligatoria es que las dependencias del código fuente apunten hacia el núcleo:

- `domain` no depende de ninguna otra capa del módulo.
- `application` puede depender de `domain`.
- `presentation` puede depender de `application` y de contratos públicos del núcleo cuando corresponda, pero no de implementaciones de persistencia.
- `infrastructure` implementa contratos definidos hacia el interior y puede depender de `domain`/`application` para cumplirlos.
- El dominio y los casos de uso no deben conocer `ClienteEntity`, `ClienteJpaRepository`, los controladores, ni la implementación concreta de proveedores externos.

Distingue **dirección de dependencias en el código** del **flujo de ejecución**: en tiempo de ejecución una petición puede viajar del controlador al caso de uso y luego al adaptador, pero el caso de uso llama a una abstracción, no a una clase concreta de infraestructura.

Para persistencia, el patrón esperado es:

1. `domain/model/Cliente.java`: modelo de negocio sin dependencia JPA.
2. `domain/repository/ClienteRepository.java`: contrato de persistencia definido hacia el núcleo.
3. `application/usecase/RegistrarClienteUseCase.java`: contrato explícito del caso de uso. Su implementación `RegistrarClienteService` orquesta el caso de uso a través de los puertos.
4. `infrastructure/persistence/entity/ClienteEntity.java`: representación JPA de la tabla.
5. `infrastructure/persistence/mapper/ClientePersistenceMapper.java`: conversión explícita entre `Cliente` y `ClienteEntity`.
6. `infrastructure/persistence/jpa/ClienteJpaRepository.java`: interfaz Spring Data JPA.
7. `infrastructure/persistence/adapter/ClientePersistenceAdapter.java`: implementa `ClienteRepository` usando el mapper y `ClienteJpaRepository`.

El contrato de repositorio puede vivir en `domain/repository/` según la estructura establecida en este proyecto. No crees una segunda interfaz equivalente en `application/port/` por duplicación. Para otras integraciones, usa `application/port/` cuando sea apropiado y consistente con el módulo.


## 4.1. Convenciones y decisiones vigentes específicas de ALIADO

Estas reglas reflejan decisiones explícitas del responsable del proyecto y deben respetarse mientras no sean modificadas expresamente:

### Estructura real de paquetes
- La estructura de paquetes existente del backend es la fuente para crear clases nuevas.
- La ruta base actualmente indicada para el proyecto es `src/main/java/com/sebastian/spring/mas/aliado/aliado`.
- No utilices la estructura propuesta anteriormente `backend/src/main/java/com/aliado`.
- Antes de crear clases, inspecciona el árbol real y confirma el `package` de clases equivalentes. Respeta el package base y las convenciones reales del repositorio.
- No cambies el package base ni hagas una reorganización global sin autorización explícita.

### Patrón obligatorio UseCase + Service
Para cada caso de uso:
1. Debe existir una interfaz propia `UseCase` en `application/usecase/` (adaptando únicamente el prefijo del paquete a la estructura real).
2. La interfaz debe declarar el método `ejecutar()`.
3. Debe existir una clase `Service` correspondiente.
4. La clase `Service` debe implementar la interfaz `UseCase` y el método `ejecutar()`.
5. Los controladores deben depender de la interfaz `UseCase`, nunca de la clase `Service` concreta.

Esta regla es obligatoria, no opcional. No crees una interfaz genérica compartida para sustituir la interfaz propia de cada CU.

### Flujo de mapeo entre presentación y aplicación
Cuando el caso de uso reciba y devuelva datos HTTP, el flujo esperado es:

`Request DTO -> Presentation Mapper -> Command -> UseCase -> Service.ejecutar() -> Result -> Presentation Mapper -> Response DTO`

- Request y Response pertenecen a presentación y no deben atravesar hacia dominio/aplicación.
- Command y Result pertenecen a aplicación y no deben depender de HTTP, Spring MVC ni JPA.
- Los mappers HTTP convierten DTOs de presentación a objetos de aplicación y viceversa.
- Los mappers de persistencia son independientes y convierten entre modelos de dominio y entidades JPA.
- Los mappers no deben contener reglas de negocio ni acceso a repositorios.
- No expongas entidades JPA ni modelos internos directamente como respuesta HTTP.
- No añadas Commands o Results artificiales si no representan una entrada/salida útil del caso de uso; justifica cualquier excepción y conserva el límite entre capas.

### Migraciones Flyway ya realizadas
La etapa de creación de las migraciones iniciales de base de datos ya fue realizada.
- Inspecciona las migraciones y el esquema existentes antes de implementar persistencia.
- No recrees tablas, columnas, índices, restricciones u objetos que ya existan.
- No modifiques migraciones Flyway existentes ni alteres su historial.
- No ejecutes migraciones como parte de una refactorización de código.
- No agregues nuevas migraciones sin autorización explícita.
- Si una implementación no coincide con el esquema actual, documenta la discrepancia y solicita autorización antes de cambiar esquema o migraciones.
- No marques tareas del Implementation Plan relacionadas con migraciones como completadas solo por esta instrucción: verifica el estado real y distingue las migraciones existentes de la tarea funcional que pudiera depender de ellas.

### Registro público y rol administrador
- El registro público solo puede crear usuarios con rol `CLIENTE` o `PRESTADOR`, conforme al SPEC-01.
- El rol `ADMINISTRADOR` se aprovisionará mediante un procedimiento privado e independiente.
- Ningún Request DTO, Command o parámetro controlado por el cliente del endpoint público puede permitir asignar `ADMINISTRADOR`.
- No implementes el aprovisionamiento privado de administradores salvo que el usuario lo solicite y el alcance esté documentado.
- Los endpoints administrativos deben protegerse mediante autenticación y autorización apropiadas; no basta con ocultar su URL o confiar en el frontend.

### Manejo de excepciones y errores HTTP
- Mantén una estrategia coherente de excepciones de dominio, aplicación e infraestructura, respetando las responsabilidades de cada capa.
- Las excepciones de dominio representan reglas o invariantes incumplidas; las excepciones de aplicación representan condiciones del caso de uso; los fallos técnicos deben traducirse sin filtrar detalles internos.
- Centraliza la traducción a HTTP en el mecanismo global existente (por ejemplo, `@RestControllerAdvice` si aplica). No dupliques manejadores.
- Usa códigos HTTP semánticamente correctos: 400 para solicitudes inválidas, 401 para falta de autenticación, 403 para falta de autorización, 404 para recursos inexistentes, 409 para conflictos, 422 cuando el contrato lo justifique y 500 para errores inesperados.
- No conviertas todos los errores en 400 o 500 indiscriminadamente.
- No expongas stack traces, consultas SQL, nombres internos sensibles, credenciales ni detalles de infraestructura.
- No captures `Exception` de forma genérica para ocultar errores ni devuelvas respuestas exitosas cuando una operación falló.
- Conserva el formato de error que defina el contrato vigente; si se requiere cambiarlo, identifica el impacto y solicita autorización.

### Comunicación entre módulos y estado actual

- Patrones que ya se usan y debes seguir:
  - Auditoría: el puerto vive en `common/auditoria` (concepto transversal); los módulos lo consumen; ningún módulo depende de `administracion` para auditar.
  - Lecturas entre módulos: el puerto lo define el módulo CONSUMIDOR (p. ej. `preferencias` define `ActividadNotificacionesPort` y `notificaciones` lo implementa). Nunca hagas que dos módulos se importen mutuamente.
  - Integraciones (correo, storage, cifrado, idempotencia): el puerto vive en `common/` y la implementación concreta lo implementa (`CifradoPort`, `IdempotenciaPort`, `ArchivoStorageService`); el código depende del puerto, jamás de la clase concreta.
- Estado de la migración al patrón UseCase: el registro público (CU001, `auth`) es la referencia — `RegistrarUsuarioUseCase` + `RegistrarUsuarioService` + `Command`/`Result` en `application/dto/` + `AuthPresentationMapper` + excepciones de dominio. Los demás módulos aún usan servicios de aplicación directos y mappers privados (`aRespuesta`) en el controlador; se migran caso de uso por caso de uso, no en un cambio global.
- Los puertos entre módulos pueden devolver el agregado del módulo dueño; los read-models dedicados son una mejora futura, no un cambio ad hoc.
- Flyway: si una base de desarrollo tiene un checksum distinto al de la migración, se recrea (`docker compose down -v` + `up -d`); nunca se edita la migración.
- No hay tests HTTP/integración (MockMvc no está en el classpath de test): los tests son unitarios de dominio/aplicación con H2 y Flyway apagado.

## 5. Reglas para el monolito modular

- Cada módulo debe ser propietario de su lógica de negocio, entidades de persistencia, repositorios concretos, casos de uso y controladores.
- No importes desde un módulo las clases internas de infraestructura de otro módulo. En particular, un módulo no debe depender del `Entity`, `JpaRepository` o adaptador de persistencia de otro.
- La comunicación entre módulos debe realizarse a través de interfaces/fachadas/casos de uso públicos claramente definidos o eventos de dominio/aplicación cuando el diseño lo requiera.
- Evita dependencias circulares entre módulos. Si detectas una, informa del problema y propone una dirección de dependencia adecuada antes de crear más acoplamiento.
- No muevas lógica específica de un módulo a un paquete compartido solo para facilitar imports. Un módulo `shared`/`common` debe contener únicamente conceptos realmente genéricos y estables.
- No conviertas automáticamente el monolito modular en microservicios ni agregues mensajería, complejidad distribuida o nuevas tecnologías sin un requisito documentado.
- Respeta los límites y contratos existentes incluso cuando otra implementación parezca más rápida.

## 6. Reglas de implementación

- Antes de crear una clase, busca si ya existe una clase que cumpla esa responsabilidad.
- Reutiliza las convenciones, versiones, dependencias, estilo de nombres, manejo de errores y formato de respuestas ya presentes en el proyecto.
- Evita refactorizaciones masivas o cambios fuera del alcance solicitado. No reemplaces implementaciones funcionales por una arquitectura nueva completa sin autorización explícita.
- No agregues librerías nuevas si las dependencias existentes resuelven el problema. Si una dependencia nueva es necesaria, explica por qué.
- No añadas métodos, endpoints, roles, permisos, campos, tablas ni reglas que no estén respaldados por los `spec`, el Plan General o la solicitud del usuario.
- Mantén los mappers explícitos y fáciles de probar; no reutilices automáticamente un mapper de HTTP para persistencia ni a la inversa.
- Asegura que el flujo de errores sea coherente y que no se filtren trazas, secretos o información interna en respuestas al usuario.
- Para operaciones de escritura, piensa en transacciones y consistencia. Implementa el límite transaccional siguiendo las convenciones existentes sin introducir dependencias de framework innecesarias en el dominio.
- No marques una tarea como completada si no ejecutaste las verificaciones indicadas o si quedan errores conocidos. Explica qué comandos y pruebas sí se ejecutaron.

## 7. Pruebas y verificación

- Para reglas de negocio, agrega pruebas unitarias del dominio.
- Para casos de uso, prueba el comportamiento usando contratos/mocks o dobles de prueba en vez de depender obligatoriamente de una base de datos real.
- Para adaptadores de persistencia, agrega pruebas de integración cuando sean pertinentes para comprobar el mapeo y las consultas.
- Para controladores, prueba códigos HTTP, validaciones y forma de la respuesta cuando corresponda.
- Usa el sistema de construcción que ya tenga el repositorio (Maven o Gradle); no supongas uno sin inspeccionar los archivos.
- Ejecuta las pruebas pertinentes y, si el tiempo y el entorno lo permiten, la compilación del módulo afectado o del proyecto. Si algún comando falla por problemas preexistentes o por falta de entorno/servicios, informa el error sin ocultarlo.
- No elimines pruebas para hacer que la compilación pase. Corrige la causa cuando esté dentro del alcance solicitado.

# 8. Flujo de trabajo esperado en cada tarea

El backend existente puede contener funcionalidades completas, parciales o generadas previamente. El objetivo no es reescribirlo desde cero: se debe conservar lo que cumple y refactorizar lo que incumple los SPEC, los Implementation Plans o estas reglas arquitectónicas.

## 8.1. Al iniciar un SPEC o una etapa de recuperación

Cuando el usuario solicite trabajar un SPEC:
1. Lee `AGENTS.md`.
2. Consulta `docs/plans/plan-general-ALIADO.md`, el SPEC seleccionado y su Implementation Plan.
3. Identifica los casos de uso documentados y las tareas relacionadas.
4. Inspecciona la implementación existente antes de proponer desarrollo o refactorización.
5. Determina qué partes están completas, parciales, ausentes o incumplen la arquitectura.
6. Identifica dependencias con otros SPEC y componentes compartidos.
7. Presenta el orden de trabajo y los riesgos.

No asumas que una funcionalidad está ausente solo porque el plan la marque pendiente. Tampoco asumas que está terminada porque exista código: contrasta el comportamiento y la arquitectura con los documentos.

## 8.2. Trabajo caso de uso por caso de uso

El trabajo se realiza sobre un único CU cada vez:
1. Localiza el CU en el SPEC.
2. Localiza las tareas correspondientes en el Implementation Plan.
3. Identifica endpoints, controladores, DTO, mappers, Commands, Results, UseCase, Service, dominio, puertos, adaptadores y persistencia relacionados.
4. Traza el flujo completo desde la petición HTTP hasta la persistencia y la respuesta.
5. Comprueba las dependencias y prerrequisitos.
6. Define el alcance exacto de los cambios.

No implementes ni refactorices otros casos de uso sin autorización.

## 8.3. Diagnóstico previo a la refactorización

Antes de editar, presenta una matriz que incluya:
- Requisito, regla o tarea.
- Implementación encontrada y evidencia (archivos, clases y métodos).
- Estado: cumple, parcial, incumple o no verificable.
- Severidad: crítica, alta, media o baja.
- Corrección propuesta.
- Archivos que se crearían o modificarían.

Evalúa al menos:
- Cumplimiento funcional del SPEC y su Implementation Plan.
- Clean Architecture y dirección de dependencias.
- Patrón obligatorio UseCase + Service + ejecutar().
- Mapeo Request -> Command y Result -> Response.
- Mappers de persistencia y separación de entidades JPA.
- Excepciones y respuestas HTTP.
- Autenticación, autorización y propiedad de recursos.
- Transacciones, idempotencia y persistencia cuando apliquen.
- Compatibilidad con Flyway y el esquema existente.

Si el usuario solicita únicamente diagnóstico, no modifiques archivos. Si solicita diagnóstico y refactorización, puedes corregir los hallazgos dentro del alcance autorizado. Detente y consulta cuando exista una decisión funcional no documentada, un cambio de contrato público, una modificación de esquema, una dependencia nueva o una ampliación del alcance.

## 8.4. Refactorización e implementación

- Conserva el comportamiento que ya cumple el SPEC.
- Corrige las responsabilidades de capa sin trasladar reglas de negocio a controladores, DTO o entidades JPA.
- Implementa o corrige el patrón obligatorio UseCase + Service + ejecutar().
- Corrige los mapeos entre Request/Command y Result/Response.
- Corrige el manejo de excepciones y la traducción HTTP.
- Reutiliza componentes existentes cuando sean correctos.
- Evita duplicar clases, repositorios, validaciones o lógica.
- Haz cambios pequeños, coherentes y limitados al CU.
- No hagas refactorizaciones cosméticas o globales no relacionadas.
- No cambies contratos públicos ni comportamiento funcional salvo que el SPEC lo exija y el cambio esté autorizado.
- No modifiques migraciones Flyway existentes ni agregues migraciones sin autorización.
- No modifiques SPEC, planes, `AGENTS.md` ni `pom.xml` sin autorización.

## 8.5. Pruebas y verificaciones por etapas

La refactorización y la etapa formal de pruebas pueden ser actividades separadas, según lo indique el usuario.

- No crees ni modifiques pruebas cuando el usuario haya reservado expresamente esa etapa para después.
- Cuando se autoricen pruebas, cubre reglas de dominio, casos de uso, adaptadores de persistencia y controladores según corresponda.
- Usa PostgreSQL/Testcontainers cuando se requiera validar comportamiento específico de PostgreSQL y así lo establezca el proyecto.
- No uses H2 como sustituto para validar comportamiento específico de PostgreSQL.
- Puedes ejecutar verificaciones técnicas no destructivas, como compilación Maven, si están dentro del alcance.
- No ejecutes migraciones Flyway durante una refactorización de código.
- No afirmes que una prueba o verificación pasó si no fue ejecutada.
- Informa comandos, resultados y fallos, diferenciando errores preexistentes de errores introducidos por los cambios.

## 8.6. Uso de agentes especializados

Cuando el proyecto tenga agentes personalizados configurados:
- `aliado-dev` implementa funcionalidades nuevas o cambios de desarrollo dentro del alcance asignado.
- `aliado-reviewer` audita y puede refactorizar código cuando el usuario le autorice explícitamente. No es un agente necesariamente de solo lectura.
- `aliado-test` diseña, crea y ejecuta pruebas cuando el usuario autorice la etapa de pruebas.

No simules la invocación de un agente. Si no puedes invocarlo realmente, informa esa limitación. No invoques agentes adicionales de forma rutinaria si el usuario no lo ha solicitado o si no aportan valor a la tarea.

Los subagentes heredan el sandbox y permisos de la sesión principal. Las instrucciones del agente delimitan su comportamiento y alcance, pero no constituyen por sí solas un aislamiento técnico de permisos.

## 8.7. Al finalizar un caso de uso

1. Resume los hallazgos iniciales y las correcciones.
2. Lista archivos creados y modificados.
3. Explica los cambios por capa.
4. Demuestra el flujo Request -> Command -> UseCase -> Service.ejecutar() -> Result -> Response cuando corresponda.
5. Explica el manejo de excepciones y los códigos HTTP relevantes.
6. Informa verificaciones realmente ejecutadas.
7. Identifica criterios de aceptación cumplidos, parciales y pendientes.
8. Relaciona las tareas del Implementation Plan y su estado comprobado.
9. Identifica riesgos y decisiones que requieren autorización.
10. Espera autorización antes de continuar con el siguiente CU.

No marques tareas como completadas si solo existe código sin evidencia suficiente de cumplimiento.

## 8.8. Revisión global del SPEC

La revisión global debe realizarse cuando el usuario indique que los casos de uso del SPEC están implementados o refactorizados y que corresponde cerrar esa etapa.

En esa revisión:
- Evalúa coherencia entre los casos de uso y los componentes compartidos.
- Comprueba contratos API y seguridad transversal.
- Identifica duplicaciones, inconsistencias y dependencias entre CU.
- Verifica cumplimiento integral del SPEC y del Implementation Plan.
- Separa hallazgos globales de pendientes propios de cada CU.
- No reescribas ni modifiques documentos de planificación sin autorización.

## 8.9. Informe de resultados

Al finalizar, informa:
- SPEC y caso de uso trabajado.
- Estado inicial y hallazgos.
- Tareas relacionadas del Implementation Plan.
- Archivos creados y modificados.
- Componentes arquitectónicos involucrados.
- Mapeos entre capas.
- Manejo de excepciones y seguridad revisados.
- Verificaciones ejecutadas y resultados reales.
- Criterios de aceptación cumplidos, parciales y pendientes.
- Riesgos, dependencias y decisiones pendientes.

No marques una funcionalidad como completada si sus verificaciones no han sido satisfactorias.
