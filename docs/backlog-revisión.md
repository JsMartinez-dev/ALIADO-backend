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

Auditoría estática completa; revisado no significa refactorizado ni probado. Ver [diagnóstico](reviews/auditoria-spec-03.md).

| Orden | ID | Caso de uso | Dependencias | Estado de revisión |
|---|---|---|---|---|
| 1 | UC014 | Buscar prestadores | SPEC-01, perfil profesional, catálogo, confianza y visibilidad | Arquitectura refactorizada — decisiones funcionales y verificación pendientes |
| 2 | UC015 | Consultar perfil público | UC014 y proyección pública compartida | Arquitectura refactorizada — privacidad y verificación pendientes |
