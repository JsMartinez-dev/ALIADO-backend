# F06 — Visualización de fotos de una oportunidad

## Hallazgo

El backend ya tenía la carga multipart de fotos y la descarga binaria. El problema era el contrato del tablero: `OportunidadResponse.fotos` entregaba claves de almacenamiento, pero no los `fotoId` requeridos por la descarga. El frontend solo podía contar las fotos.

## Cambio

- `POST /api/v1/solicitudes/{solicitudId}/fotos` conserva la carga multipart exclusiva del Cliente.
- Las respuestas de tablero y detalle de `GET /api/v1/oportunidades` y `GET /api/v1/oportunidades/{solicitudId}` exponen `fotosDetalle: [{ id, orden }]` y dejan de publicar la lista `fotos` con claves internas de almacenamiento.
- `GET /api/v1/solicitudes/{solicitudId}/fotos/{fotoId}` responde bytes con el MIME detectado.
- La descarga autoriza al Cliente propietario o a un Prestador cuya compatibilidad con la solicitud siga vigente y el estado continúe `PUBLICADA`. Usuarios incompatibles, otras cuentas Cliente, Administrador, solicitud cerrada o foto ajena reciben el 404 existente, sin revelar el recurso.

## Validación

- Pruebas de autorización: propietario Cliente permitido; Prestador compatible permitido solo con solicitud publicada; Cliente ajeno, Prestador incompatible, Prestador con solicitud cerrada y Administrador rechazados sin leer el archivo.
- Prueba de proyección: `fotosDetalle` contiene ID y orden de cada foto, sin publicar la clave interna de almacenamiento.
- `bash ./mvnw test -q` → exit 0; 399 tests descubiertos, 0 fallos, 0 errores, 46 omitidos por condiciones de suite.
- `git diff --check` → 0.
- La prueba visual con una foto real desde Android requiere una instancia del backend con esta rama. Se documentará cuando se ejecute.
