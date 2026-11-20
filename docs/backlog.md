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

## SPEC-07 — Creación y gestión de propuestas

Refactorización **completada por decisión del usuario**. Persisten pendientes de idempotencia compartida, concurrencia con el cierre de solicitudes, entrega real y aceptación integral; pasaron 24 pruebas unitarias, sin compilación Maven completa.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC039 | Crear propuesta | Parcial — refactorizado; idempotencia y concurrencia pendientes |
| UC040 | Definir disponibilidad | Refactorizado — aceptación integral pendiente |
| UC041 | Añadir mensaje opcional | Refactorizado — aceptación integral pendiente |
| UC042 | Editar propuesta | Refactorizado — concurrencia y aceptación integral pendientes |
| UC043 | Retirar propuesta | Refactorizado — reintentos y aceptación integral pendientes |
| UC044 | Notificar propuesta recibida | Parcial — entrega real y aceptación integral pendientes |

## SPEC-08 — Contratación y ciclo de vida del servicio

Refactorización **finalizada por decisión del usuario**. Persisten pendientes de idempotencia compartida, concurrencia y política D-10; pasaron 29 pruebas unitarias, sin compilación Maven completa.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC045 | Consultar propuestas recibidas | Refactorizado — aceptación integral pendiente |
| UC046 | Aceptar propuesta | Parcial — idempotencia y concurrencia pendientes |
| UC047 | Cerrar propuestas no seleccionadas | Refactorizado — atomicidad pendiente de verificar |
| UC048 | Registrar servicio contratado | Refactorizado — atomicidad pendiente de verificar |
| UC049 | Habilitar comunicación | Refactorizado — política D-10 pendiente |
| UC050 | Consultar detalle de servicio | Refactorizado — aceptación integral pendiente |
| UC051 | Marcar en ejecución | Parcial — idempotencia y concurrencia pendientes |
| UC052 | Marcar terminado | Parcial — idempotencia y concurrencia pendientes |
| UC053 | Confirmar finalización | Parcial — idempotencia y concurrencia pendientes |
| UC054 | Cancelar servicio | Parcial — idempotencia y concurrencia pendientes |
| UC055 | Consultar historial | Refactorizado — concurrencia pendiente de verificar |
| UC056 | Usar canal de comunicación | Parcial — idempotencia y política D-10 pendientes |


## SPEC-09 — Notificaciones de propuestas y servicios

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC057 | Notificar aceptación de propuesta | Refactorizado — proveedor de entrega y concurrencia pendientes |
| UC058 | Notificar cambio de estado | Refactorizado — proveedor de entrega y concurrencia pendientes |

## SPEC-10 — Calificaciones, reseñas y verificación

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC059 | Calificar servicio finalizado | Refactorizado; aceptación integral pendiente |
| UC060 | Agregar reseña textual | Refactorizado; aceptación integral pendiente |
| UC061 | Recalcular reputación | Refactorizado — concurrencia de reputación pendiente |
| UC062 | Mostrar confianza y verificación | Refactorizado; aceptación integral pendiente |
| UC063 | Solicitar verificación documental | Refactorizado — política documental e integración de almacenamiento pendientes |
| UC064 | Reportar reseña | Refactorizado; aceptación integral pendiente |

## SPEC-11 — Reportes y evidencias

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC065 | Reportar usuario | Refactorizado — consolidación concurrente pendiente |
| UC066 | Reportar servicio | Refactorizado — consolidación concurrente pendiente |
| UC067 | Adjuntar evidencia y consultar reporte propio | Refactorizado — bloqueo y rollback pendientes de verificar en PostgreSQL |

## SPEC-12 — Administración, moderación y riesgos

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC068 | Administrar categorías | Refactorizado; aceptación integral pendiente |
| UC069 | Administrar zonas | Refactorizado; aceptación integral pendiente |
| UC070 | Consultar cola de reportes | Refactorizado; aceptación integral pendiente |
| UC071 | Revisar detalle del reporte | Refactorizado; aceptación integral pendiente |
| UC072 | Resolver reporte | Refactorizado — concurrencia y política D-12 pendientes |
| UC073 | Bloquear preventivamente cuenta | Refactorizado — protección secuencial implementada, carrera con confirmación pendiente |
| UC074 | Moderar contenido | Refactorizado — granularidad de moderación pendiente |
| UC075 | Buscar cuentas y perfiles | Refactorizado — paginación pendiente |
| UC076 | Marcar servicio de alto riesgo | Refactorizado — criterios D-12 pendientes |

## SPEC-13 — Ruta de formalización

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC077 | Consultar ruta orientativa | Refactorizado; aceptación integral pendiente |
| UC078 | Consultar enlaces oficiales | Refactorizado; aceptación integral pendiente |
| UC079 | Progreso orientativo | Refactorizado — fórmula D-13 pendiente |
| UC080 | Vinculación institucional futura | Refactorizado — Future deshabilitado |

## SPEC-14 — Preferencias, historial e indicadores

Refactorización implementada. Compilación Maven y suite unitaria aprobadas. La aceptación integral sigue pendiente.

| ID | Caso de uso | Estado de implementación verificable |
|---|---|---|
| UC081 | Configurar preferencias de notificación | Refactorizado; aceptación integral pendiente |
| UC082 | Consultar historial propio | Refactorizado; aceptación integral pendiente |
| UC083 | Exportar historial | Refactorizado — tamaño final del CSV e integración pendientes |
| UC084 | Consultar indicadores propios | Refactorizado — regla acordada implementada, resto de D-15 pendiente |
| UC085 | Consultar actividad reciente | Refactorizado; aceptación integral pendiente |
