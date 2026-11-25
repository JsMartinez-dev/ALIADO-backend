# Correcciones backend MVP — seguimiento BE-10/BE-13

**Estado:** rama de correcciones para revisión del equipo backend. No integrada.

## BE-13 — Idempotencia de evidencia

Implementado en `POST /api/v1/reportes/{id}/evidencias`:

- La huella es SHA-256 sobre nombre original, tipo de contenido y bytes del archivo.
- El ámbito de replay es la clave + reporte + reportante, sin aceptar un propietario arbitrario.
- Antes de comprobar o crear se adquiere un advisory lock transaccional de PostgreSQL para serializar solicitudes concurrentes con la misma clave.
- Misma clave, mismo reporte y mismo contenido: devuelve la evidencia original sin crear otra fila ni archivo.
- Misma clave, mismo reporte y contenido distinto: `409 IDEMPOTENCIA_CONFLICTO`.
- Otra cuenta u otro reporte no puede reproducir la respuesta de una clave ajena.
- Las claves nuevas guardan la huella; las claves antiguas con `huella = NULL` no se reproducen y responden `409 IDEMPOTENCIA_CONFLICTO`. Ver `be-13-idempotencia-evidencia.md`.
- OpenAPI documenta `201`, `409`, `401`, `403` y `404` para la operación.

## BE-10 — Recuperación de calificación y reseña

BE-10 permite la atomicidad o una recuperación suficiente. Esta rama mantiene la segunda alternativa, sin afirmar atomicidad:

1. Tras perder el contexto del `POST /api/v1/servicios/{id}/calificacion`, el Cliente propietario consulta `GET /api/v1/servicios/{id}/calificacion`.
2. La respuesta entrega `calificacionId`, `valor` y `tieneResena`.
3. Si `tieneResena=false`, el Cliente usa `calificacionId` para `POST /api/v1/calificaciones/{id}/resena`.
4. La consulta vuelve a mostrar `tieneResena=true` y el historial propio refleja la reseña.

Límites:

- No existe una operación atómica de calificación + reseña.
- La recuperación depende de conservar o volver a obtener el `servicioId`; el `calificacionId` se recupera desde el backend.
- La consulta exige rol `CLIENTE` y propiedad del servicio.
- El frontend aún debe integrar el flujo de recuperación al reabrir la pantalla después de perder el estado en memoria.

## Estado de otras brechas

- **BE-01:** funciona por la corrección JSONB heredada de `develop`; no es un cambio de esta rama.
- **BE-05:** resuelta con el contrato mínimo `descripcion`; ver `be-05-datos-generales.md`.
- **BE-06:** sigue parcial. OpenAPI de formalización mejora el `404` y los errores de autenticación/rol, pero no se declara un esquema de autorización por rol por operación.
- **BE-09/D-07:** el endpoint genérico de evidencia funciona; la política de archivos sigue pendiente de producto.
- **BE-17:** sin metadatos ni filtros nuevos; contrato actual sin total/cursor.
- **BE-18:** referencias estables añadidas; sin paginación.
- **BE-19:** exportación `201` e idempotente; indicadores sin fórmula/periodo por D-15.

## Verificación HTTP

Ejecutada contra una API aislada en `18081` con PostgreSQL desechable en `5545` y storage relativo temporal.

- Misma clave, mismo reporte y mismo archivo: `201` y mismo `id` de evidencia en el replay; sin fila ni archivo adicional.
- Misma clave, mismo reporte y archivo distinto: `409 IDEMPOTENCIA_CONFLICTO`.
- Dos solicitudes concurrentes con la misma clave y contenido: ambas `201`, mismo `id`; una sola fila y un solo archivo.
- Misma clave usada en otro reporte/usuario: crea evidencia independiente en su propio ámbito; no reproduce la evidencia ajena.
- Otro Cliente sobre el reporte ajeno: lectura `403` y carga `403`.
- Las claves nuevas persisten huella SHA-256 en `idempotency_keys`.
- BE-10: tras ignorar la respuesta del POST de calificación, `GET /servicios/{id}/calificacion` recuperó `calificacionId` y `tieneResena=false`; la reseña se creó con ese ID y la consulta posterior devolvió `tieneResena=true`; el historial incluyó la reseña.
- OpenAPI expone `409` para evidencia y los `404` de formalización con `ApiError`.

Validación automática: `bash ./mvnw test` → 140 tests, 0 fallos.
