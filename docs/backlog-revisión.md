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

Auditoría estática completa; revisado no implica refactorizado ni probado. Detalle en [diagnóstico](reviews/auditoria-spec-08.md).

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
