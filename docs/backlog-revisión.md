# Backlog de revisión y refactorización

## SPEC-01 — Autenticación, registro y control de acceso

La refactorización caso por caso de UC001–UC007 se cierra por aprobación del usuario. La fila de base técnica y la verificación funcional integral permanecen pendientes; este cierre no afirma que las pruebas de aceptación de SPEC-01 estén ejecutadas.

| Orden | ID | Caso de uso | Dependencias | Estado de la revisión |
|---|---|---|---|---|
| 1 | Base técnica de SPEC-01 | Base técnica de autenticación, registro y control de acceso | Sin dependencias | Pendiente |
| 2 | UC001 | Registro exitoso como Cliente | Base técnica de SPEC-01 | Finalizado |
| 3 | UC002 | Registro exitoso como Prestador | Base técnica de SPEC-01 | Finalizado |
| 4 | UC007 | Aceptación obligatoria de términos y tratamiento de datos | Base técnica de SPEC-01 | Finalizado |
| 5 | UC003 | Inicio de sesión exitoso | UC001, UC002, UC007 | Finalizado |
| 6 | UC004 | Cierre de sesión | UC001, UC002, UC007 | Finalizado |
| 7 | UC005 | Recuperación de acceso | UC001, UC002, UC007 | Finalizado |
| 8 | UC006 | Acceso restringido según rol | UC001, UC002, UC007 | Finalizado |

## SPEC-02 — Gestión de perfil y privacidad

Estado de seguimiento: **Refactorizado por decisión del usuario**. Permanece pendiente el filtro definitivo de servicios de UC012, además de compilación y aceptación. Detalle en [diagnóstico](reviews/auditoria-spec-02.md).

| Orden recomendado | ID | Caso de uso | Dependencias | Estado de la revisión |
|---|---|---|---|---|
| 1 | UC011 | Consultar datos propios | Autenticación | Revisado y finalizado por decisión del usuario — compilación y aceptación pendientes |
| 2 | UC008 | Editar datos personales | Autenticación, resultado compartido | Refactorizado — compilación y aceptación pendientes |
| 3 | UC009 | Configurar zona aproximada | Autenticación, cifrado | Refactorizado — compilación y aceptación pendientes |
| 4 | UC010 | Configurar visibilidad | Autenticación, política de privacidad | Refactorizado — compilación y aceptación pendientes |
| 5 | UC013 | Separar zona y dirección exacta | UC009, UC010 | Refactorizado — compilación y aceptación pendientes |
| 6 | UC012 | Solicitar eliminación | Perfil, servicios de SPEC-08 | Parcial — filtro de servicios y verificaciones pendientes |

## SPEC-03 — Descubrimiento de prestadores

Estado de seguimiento: **Refactorizado por decisión del usuario**. Permanecen pendientes decisiones funcionales, compilación y aceptación. Ver [diagnóstico](reviews/auditoria-spec-03.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC014 | Buscar prestadores | SPEC-01, perfil profesional, catálogo, confianza y visibilidad | Arquitectura refactorizada — decisiones funcionales y verificación pendientes |
| 2 | UC015 | Consultar perfil público | UC014 y proyección pública compartida | Arquitectura refactorizada — privacidad y verificación pendientes |

## SPEC-04 — Gestión de solicitudes de servicio

Refactorización **finalizada por validación del usuario**. La idempotencia compartida permanece pendiente; no se acredita compilación Maven ni pruebas automatizadas. Detalle en [diagnóstico](reviews/auditoria-spec-04.md).

| Orden recomendado | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC017 | Definir zona y urgencia | Catálogo y creación | Refactorizado — aceptación pendiente |
| 2 | UC018 | Publicar solicitud | UC017, idempotencia, catálogo | Parcial — idempotencia crítica y aceptación pendientes |
| 3 | UC019 | Guardar borrador | UC018, creación | Parcial — idempotencia crítica y aceptación pendientes |
| 4 | UC016 | Adjuntar fotos | Creación, storage | Refactorizado — consistencia y aceptación pendientes de verificar |
| 5 | UC020 | Editar solicitud abierta | Creación, UC017 | Refactorizado — aceptación pendiente |
| 6 | UC021 | Cancelar solicitud abierta | Creación e historial | Refactorizado — deuda de excepciones y aceptación pendientes |
| 7 | UC022 | Listar solicitudes propias | Creación, repositorio | Refactorizado — aceptación pendiente |
| 8 | UC023 | Consultar detalle propio | Creación, repositorio | Refactorizado — aceptación pendiente |

## SPEC-05 — Creación y gestión del perfil profesional

Auditoría estática completa; revisado no significa refactorizado ni probado. Ver [diagnóstico](reviews/auditoria-spec-05.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC024 | Crear perfil profesional | Autenticación, catálogo; incluye UC026/027/028 | Refactorizado — aceptación pendiente |
| 2 | UC026 | Categorías | UC024, catálogo | Refactorizado — concurrencia y aceptación pendientes |
| 3 | UC027 | Zonas de atención | UC024, catálogo | Refactorizado — concurrencia y aceptación pendientes |
| 4 | UC028 | Experiencia | UC024 | Refactorizado — aceptación pendiente |
| 5 | UC025 | Datos generales | UC024 | Refactorizado — privacidad y aceptación pendientes |
| 6 | UC029 | Agregar portafolio | UC024, storage | Refactorizado — consistencia y aceptación pendientes |
| 7 | UC030 | Eliminar portafolio | UC029, storage | Refactorizado — consistencia y aceptación pendientes |
| 8 | UC031 | Disponibilidad | UC024, agenda | Refactorizado — temporalidad y aceptación pendientes |
| 9 | UC032 | Agenda | UC024, disponibilidad | Refactorizado — temporalidad y aceptación pendientes |
| 10 | UC033 | Vista previa | Perfil, portafolio, disponibilidad, SPEC-03 | Parcial — diferencias de respuesta y aceptación pendientes |
