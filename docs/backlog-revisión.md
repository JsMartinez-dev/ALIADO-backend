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

Refactorización **finalizada por decisión del usuario**. Se conservan los pendientes funcionales y de verificación detallados en el [diagnóstico](reviews/auditoria-spec-05.md); no se acredita compilación Maven ni pruebas automatizadas.

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

## SPEC-06 — Compatibilidad y oportunidades

Refactorización **finalizada por decisión del usuario**. Permanecen pendientes temporalidad, entrega real y verificaciones. Detalle en [diagnóstico](reviews/auditoria-spec-06.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC034 | Calcular compatibilidad | Solicitudes, perfiles y decisión D-03 | Refactorizado — temporalidad y aceptación pendientes |
| 2 | UC035 | Consultar tablero | UC034 | Refactorizado — aceptación pendiente |
| 3 | UC036 | Filtrar tablero | UC035 | Refactorizado — aceptación pendiente |
| 4 | UC037 | Consultar detalle | UC034, UC035 y fotos de SPEC-04 | Refactorizado — fotos y aceptación pendientes |
| 5 | UC038 | Notificar oportunidad | UC034, notificaciones SPEC-09 y preferencias | Parcial — entrega real y aceptación pendientes |

## SPEC-07 — Creación y gestión de propuestas

Refactorización **completada por decisión del usuario**. Persisten pendientes de idempotencia, concurrencia, entrega real y aceptación integral; pasaron 24 pruebas unitarias. Detalle en [diagnóstico](reviews/auditoria-spec-07.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC039 | Crear propuesta | Solicitudes, compatibilidad, UC040/041/044 | Parcial — refactorizado; idempotencia y concurrencia pendientes |
| 2 | UC040 | Definir disponibilidad | UC039 | Refactorizado — aceptación integral pendiente |
| 3 | UC041 | Mensaje opcional | UC039 | Refactorizado — aceptación integral pendiente |
| 4 | UC044 | Notificar propuesta | UC039 y notificaciones | Parcial — entrega real y aceptación integral pendientes |
| 5 | UC042 | Editar propuesta | UC039 | Refactorizado — aceptación integral pendiente |
| 6 | UC043 | Retirar propuesta | UC039 | Refactorizado — aceptación integral pendiente |

## SPEC-08 — Contratación y ciclo de vida del servicio

Refactorización **finalizada por decisión del usuario**. Persisten pendientes de idempotencia, concurrencia y D-10; pasaron 29 pruebas unitarias. Detalle en [diagnóstico](reviews/auditoria-spec-08.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC045 | Consultar propuestas recibidas | Solicitudes y propuestas | Refactorizado — aceptación integral pendiente |
| 2 | UC046 | Aceptar propuesta | UC045 y UC047–UC049 | Parcial — idempotencia y concurrencia pendientes |
| 3 | UC047 | Cerrar propuestas no seleccionadas | UC046 y propuestas | Refactorizado — atomicidad pendiente de verificar |
| 4 | UC048 | Registrar servicio contratado | UC046 | Refactorizado — atomicidad pendiente de verificar |
| 5 | UC049 | Habilitar comunicación | UC046 y UC048 | Refactorizado — D-10 pendiente |
| 6 | UC050 | Consultar detalle | UC048 | Refactorizado — aceptación integral pendiente |
| 7 | UC051 | Marcar en ejecución | UC048 | Parcial — idempotencia y concurrencia pendientes |
| 8 | UC052 | Marcar terminado | UC051 | Parcial — idempotencia y concurrencia pendientes |
| 9 | UC053 | Confirmar finalización | UC052 | Parcial — idempotencia y concurrencia pendientes |
| 10 | UC054 | Cancelar servicio | Servicio no finalizado | Parcial — idempotencia y concurrencia pendientes |
| 11 | UC055 | Consultar historial | UC048 y transiciones | Refactorizado — concurrencia pendiente de verificar |
| 12 | UC056 | Usar canal de comunicación | UC049; política D-10 | Parcial — idempotencia y D-10 pendientes |


## SPEC-09 — Notificaciones de propuestas y servicios

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-09.md).

| ID | Caso de uso | Estado de revisión |
|---|---|---|
| UC057 | Notificar aceptación de propuesta | Refactorizado — proveedor de entrega y concurrencia pendientes |
| UC058 | Notificar cambio de estado | Refactorizado — proveedor de entrega y concurrencia pendientes |

## SPEC-10 — Calificaciones, reseñas y verificación

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-10.md).

| ID | Caso de uso | Estado de revisión |
|---|---|---|
| UC059 | Calificar servicio finalizado | Refactorizado; aceptación integral pendiente |
| UC060 | Agregar reseña textual | Refactorizado; aceptación integral pendiente |
| UC061 | Recalcular reputación | Refactorizado — concurrencia de reputación pendiente |
| UC062 | Mostrar confianza y verificación | Refactorizado; aceptación integral pendiente |
| UC063 | Solicitar verificación documental | Refactorizado — política documental e integración de almacenamiento pendientes |
| UC064 | Reportar reseña | Refactorizado; aceptación integral pendiente |

## SPEC-11 — Reportes y evidencias

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-11.md).

| ID | Caso de uso | Estado de revisión |
|---|---|---|
| UC065 | Reportar usuario | Refactorizado — consolidación concurrente pendiente |
| UC066 | Reportar servicio | Refactorizado — consolidación concurrente pendiente |
| UC067 | Adjuntar evidencia y consultar reporte propio | Refactorizado — bloqueo y rollback pendientes de verificar en PostgreSQL |

## SPEC-12 — Administración, moderación y riesgos

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-12.md).

| ID | Caso de uso | Estado de revisión |
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

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-13.md).

| ID | Caso de uso | Estado de revisión |
|---|---|---|
| UC077 | Consultar ruta orientativa | Refactorizado; aceptación integral pendiente |
| UC078 | Consultar enlaces oficiales | Refactorizado; aceptación integral pendiente |
| UC079 | Progreso orientativo | Refactorizado — fórmula D-13 pendiente |
| UC080 | Vinculación institucional futura | Refactorizado — Future deshabilitado |

## SPEC-14 — Preferencias, historial e indicadores

Refactorización implementada y suite unitaria aprobada. Ver [diagnóstico](reviews/auditoria-spec-14.md).

| ID | Caso de uso | Estado de revisión |
|---|---|---|
| UC081 | Configurar preferencias de notificación | Refactorizado; aceptación integral pendiente |
| UC082 | Consultar historial propio | Refactorizado; aceptación integral pendiente |
| UC083 | Exportar historial | Refactorizado — tamaño final del CSV e integración pendientes |
| UC084 | Consultar indicadores propios | Refactorizado — regla acordada implementada, resto de D-15 pendiente |
| UC085 | Consultar actividad reciente | Refactorizado; aceptación integral pendiente |
