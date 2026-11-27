# D-05 — Eliminación definitiva de cuenta (MVP académico)

Fecha de decisión: 2026-11-26

Política aprobada por producto para este prototipo. No es una declaración de cumplimiento legal ni de retención definitiva para producción.

## Regla operativa

Al confirmar la eliminación:

1. Se revocan todas las sesiones y se impide login/recuperación.
2. Se eliminan credenciales, datos de contacto, ubicación exacta, perfil y preferencias.
3. Se elimina el contenido de archivos personales que no sea necesario para una obligación operativa.
4. El historial compartido de servicios se conserva sin asociación visible a la identidad; el actor se presenta como “Cuenta eliminada”.
5. La transacción de base de datos registra las claves en `archivos_pendientes_eliminacion` asociadas a `operacion_id` (la solicitud de eliminación); el borrado físico ocurre después del commit, de forma idempotente. Si el storage falla, la clave permanece pendiente y un reintento programado la procesa. La respuesta cuenta solo pendientes de esa operación.
6. No se hace borrado en cascada del historial compartido.
7. Una cuenta con servicio `CONTRATADO`, `EN_EJECUCION` o `TERMINADO` no puede solicitar ni confirmar la eliminación.
8. La solicitud conserva la expiración de 24 h de SPEC-02 y la confirmación explícita.
9. Las respuestas incluyen `politicaEliminacion: "DEFINITIVA_MVP_V1"`; el cliente rechaza respuestas antiguas o incompletas.

## Matriz de datos

| Entidad / campo | Clasificación | Tratamiento |
|---|---|---|
| `usuarios.email`, `password_hash` | Personal / credencial | Se ponen en `NULL`. El índice único admite múltiples `NULL`. |
| `usuarios.estado_cuenta` | Control de acceso | Pasa a `ELIMINADA`; login y sesión rechazados. |
| `usuarios.rol`, `fecha_creacion` | Integridad histórica | Se conservan sin contacto. |
| `consentimientos` | Auditoría de aceptación | Se conservan versión y fecha; no contienen contacto. |
| `sesiones`, `tokens_recuperacion` | Credenciales y acceso | Se eliminan. |
| `perfiles` (`nombre`, `telefono`, visibilidad) | Personal | Se elimina la fila. |
| `ubicaciones` | Personal (zona/dirección) | Se elimina la fila y la dirección cifrada. |
| `solicitudes_eliminacion` | Auditoría/idempotencia | Se conserva el registro confirmado. |
| `idempotency_keys` | Datos de operación con posible PII | Se eliminan las filas del usuario. |
| `solicitudes.descripcion` | Texto libre potencialmente identificable | Se pone en `NULL`; se conservan categoría, zona, urgencia, estado y fechas. |
| `fotos_solicitud` | Archivo personal | Se eliminan filas y contenido del archivo. |
| `historial_estado_solicitud` | Historial | Se conserva. |
| `perfiles_profesionales`, `perfil_categoria`, `perfil_zona_atencion`, `experiencias_profesionales` | Perfil personal | Se eliminan. |
| `servicios_portafolio` | Perfil y archivo | Se eliminan filas y archivos. |
| `disponibilidades`, `compromisos_agenda` | Perfil operativo | Se eliminan. |
| `propuestas.disponibilidad`, `propuestas.mensaje` | Texto libre | Se neutralizan; se conservan IDs, estado, tarifa y fechas. |
| `servicios_contratados` | Historial compartido | Se conserva íntegro. |
| `historial_estado_servicio` | Historial/auditoría | Se conserva; el actor se muestra como “Cuenta eliminada”. |
| `mensajes_servicio.contenido` | Texto libre del actor eliminado | Se neutraliza; se conservan fila, remitente, servicio y fecha. |
| `notificaciones` | Comunicación personal | Se eliminan las del destinatario. |
| `calificaciones` | Historial/reputación | Se conservan valores y relaciones. |
| `resenas.texto` | Texto libre potencialmente identificable | Se neutraliza; se conserva la fila/calificación. |
| `reputaciones_prestador` | Agregado histórico | Se conserva; no contiene contacto. |
| `solicitudes_verificacion` | Proceso personal | Se eliminan. |
| `documentos_verificacion` | Documento personal | Se eliminan filas y archivos. |
| `reportes.motivo`, `justificacion_resolucion` | Texto libre | Se neutralizan cuando el usuario es reportante u objetivo; se conserva estado y referencias. |
| `evidencias_reporte` | Archivo de reporte/auditoría | Se elimina el contenido; la fila queda como referencia mínima `ELIMINADO`. |
| `bloqueos_preventivos.motivo`, `acciones_moderacion.motivo` | Auditoría con posible PII | Se neutraliza el texto cuando el usuario es objetivo; se conserva la auditoría. |
| `auditoria_administrativa` | Auditoría append-only | No se modifica; ya está sanitizada y sin contacto. |
| `progreso_formalizacion` | Perfil operativo | Se elimina. |
| `preferencias_notificacion` | Preferencias personales | Se eliminan. |
| `descargas_historial` | Archivo personal | Se eliminan filas y archivos. |
| `archivos_pendientes_eliminacion` | Cola durable de limpieza | Se encola la clave con `operacion_id` en la transacción; se elimina la fila solo cuando el storage confirma el borrado. |
| Catálogo, zonas, configuración global | Datos compartidos | No se tocan. |

## Límites explícitos

- No se define una política de retención legal más allá de conservar la estructura del historial compartido.
- Los textos libres del actor eliminado se neutralizan para no conservar contenido identificable; el historial conserva la traza, estados, relaciones y fechas.
- La confirmación repetida a nivel de caso de uso es idempotente. Un reintento HTTP después de revocar la sesión queda no autenticado (`401`), que es un efecto esperado de la política y no restaura datos.
- `LocalArchivoStorageAdapter` ya no silencia `IOException`; el fallo se registra en la cola pendiente y se reintenta.
- La confirmación devuelve el pendiente de la operación actual, no el global; el scheduler global puede limpiar operaciones de varias cuentas sin alterar respuestas ya emitidas.
- La confirmación responde `archivosPendientes`; si es mayor que cero, el frontend no afirma que todos los archivos fueron eliminados.
- Un proveedor S3 productivo deberá implementar la misma semántica de borrado idempotente y reintentos.
