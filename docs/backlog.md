# Backlog de SPEC-01 — Autenticación, registro y control de acceso

Fuente: `docs/specs/spec-01-autenticacion-registro-control-acceso.md`. El estado indica verificación del código existente, no solo la presencia de tareas o clases.

| ID | Caso de uso / requisito | Estado de implementación verificable |
|---|---|---|
| UC001 | Registro exitoso como Cliente | Implementado y revisado previamente (backlog de revisión) |
| UC002 | Registro exitoso como Prestador | Implementado y revisado previamente (backlog de revisión) |
| UC007 | Aceptación obligatoria de términos y tratamiento de datos | Implementado y revisado previamente (backlog de revisión) |
| UC003 | Inicio de sesión exitoso y rechazo genérico de credenciales inválidas | Finalizado (verificado y aprobado por el usuario) |
| UC004 | Cierre de sesión verificable | Finalizado (verificado y aprobado por el usuario) |
| UC005 | Recuperación de acceso con respuesta no reveladora | Refactorización parcial; pendiente extraer lógica de AuthService, compilación y verificación funcional |
| UC006 | Restricción de capacidades según rol | Pendiente de auditoría |

La inspección inicial confirma el flujo de registro con `RegistrarUsuarioUseCase`, `RegistrarUsuarioService` y `AuthPresentationMapper`. UC003 tiene ruta `/login` en `AuthController` y delega actualmente en `AuthService.login`; aún no se ha confirmado el cumplimiento integral de requisitos ni se ha auditado su seguridad/persistencia. Los estados de UC001/UC002/UC007 reflejan la revisión previa conservada en `docs/backlog-revisión.md`.
