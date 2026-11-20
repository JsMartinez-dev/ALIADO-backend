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

Refactorización **finalizada por validación del usuario**. Permanece pendiente el hallazgo crítico de idempotencia compartida y su verificación transversal; no se acredita compilación Maven ni ejecución de pruebas automatizadas.

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

## SPEC-05 — Creación y gestión del perfil profesional

Refactorización **finalizada por decisión del usuario**. Persisten pendientes de privacidad, concurrencia, temporalidad, igualdad de vista previa y aceptación; no se acredita compilación Maven ni pruebas automatizadas.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC024 | Crear perfil profesional | Refactorizado; aceptación pendiente |
| UC025 | Editar datos generales | Refactorizado; privacidad, concurrencia y aceptación pendientes |
| UC026 | Seleccionar y actualizar categorías | Refactorizado; concurrencia y aceptación pendientes |
| UC027 | Configurar zonas de atención | Refactorizado; concurrencia y aceptación pendientes |
| UC028 | Registrar experiencia profesional | Refactorizado; concurrencia y aceptación pendientes |
| UC029 | Agregar servicio al portafolio | Refactorizado; consistencia y aceptación pendientes de verificar |
| UC030 | Eliminar servicio del portafolio | Refactorizado; consistencia y aceptación pendientes de verificar |
| UC031 | Configurar disponibilidad | Refactorizado; reglas temporales y aceptación pendientes |
| UC032 | Gestionar agenda | Refactorizado; reglas temporales, concurrencia y aceptación pendientes |
| UC033 | Consultar vista previa | Parcial — diferencias de respuesta y aceptación pendientes |

## SPEC-06 — Compatibilidad y oportunidades

Refactorización **finalizada por decisión del usuario**. Continúan pendientes la regla temporal, la entrega real de notificaciones y las verificaciones de aceptación; no se acredita compilación Maven ni pruebas automatizadas.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC034 | Calcular compatibilidad | Refactorizado; regla temporal y aceptación pendientes |
| UC035 | Consultar tablero | Refactorizado; aceptación pendiente |
| UC036 | Filtrar tablero | Refactorizado; aceptación pendiente |
| UC037 | Consultar detalle de oportunidad | Refactorizado; acceso a fotos y aceptación pendientes |
| UC038 | Notificar nueva oportunidad | Parcial — entrega real, idempotencia y aceptación pendientes |
