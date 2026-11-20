# Backlog de SPEC-01 — Autenticación, registro y control de acceso

Fuente: `docs/specs/spec-01-autenticacion-registro-control-acceso.md`. El estado indica verificación del código existente, no solo la presencia de tareas o clases.

| ID | Caso de uso / requisito | Estado de implementación verificable |
|---|---|---|
| UC001 | Registro exitoso como Cliente | Finalizado  |
| UC002 | Registro exitoso como Prestador | Finalizado  |
| UC007 | Aceptación obligatoria de términos y tratamiento de datos | Finalizado  |
| UC003 | Inicio de sesión exitoso y rechazo genérico de credenciales inválidas | Finalizado |
| UC004 | Cierre de sesión verificable | Finalizado |
| UC005 | Recuperación de acceso con respuesta no reveladora | Finalizado  |
| UC006 | Restricción de capacidades según rol | Finalizado |

La refactorización caso por caso de UC001–UC007 se cierra por aprobación.

## SPEC-02 — Gestión de perfil y privacidad

Estado de seguimiento: **Refactorizado por decisión del usuario**. UC012 conserva pendiente la definición y ajuste del filtro de servicios; la compilación y las pruebas de aceptación no están acreditadas. Este estado no implica verificación funcional completa.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC008 | Editar datos personales | Refactorizado; compilación y aceptación pendientes |
| UC009 | Configurar zona aproximada | Refactorizado; compilación y aceptación pendientes |
| UC010 | Configurar visibilidad | Refactorizado; compilación y aceptación pendientes |
| UC011 | Consultar datos propios | Revisado y finalizado por decisión del usuario — compilación y aceptación pendientes |
| UC012 | Solicitar eliminación de cuenta | Parcial — filtro de servicios y verificaciones pendientes |
| UC013 | Separar zona y dirección exacta | Refactorizado; compilación y aceptación pendientes |

## SPEC-03 — Descubrimiento de prestadores

Estado de seguimiento: **Refactorizado por decisión del usuario**. Permanecen pendientes decisiones funcionales sobre búsqueda y privacidad, compilación y pruebas de aceptación; este estado no acredita verificación funcional completa.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC014 | Buscar prestadores | Refactorización arquitectónica implementada; decisiones funcionales y verificación pendientes |
| UC015 | Consultar perfil público del prestador | Refactorización arquitectónica implementada; privacidad y verificación pendientes |

## SPEC-04 — Gestión de solicitudes de servicio

Estado basado en auditoría estática; no acredita pruebas de aceptación.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC016 | Adjuntar fotos opcionales | Refactorizado; consistencia y aceptación pendientes de verificar |
| UC017 | Definir zona y urgencia | Refactorizado; aceptación pendiente de verificar |
| UC018 | Publicar solicitud | Parcial — idempotencia crítica y aceptación pendientes |
| UC019 | Guardar borrador | Parcial — idempotencia crítica y aceptación pendientes |
| UC020 | Editar solicitud abierta | Refactorizado; aceptación pendiente de verificar |
| UC021 | Cancelar solicitud abierta | Refactorizado; deuda de excepciones y aceptación pendientes |
| UC022 | Listar solicitudes propias | Refactorizado; aceptación pendiente de verificar |
| UC023 | Consultar detalle de solicitud propia | Refactorizado; aceptación pendiente de verificar |
