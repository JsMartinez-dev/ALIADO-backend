# BE-13 — Idempotencia de evidencias y registros legados

**Estado:** implementado en la rama de correcciones; no integrado.

## Contrato operativo

- La huella SHA-256 cubre nombre original, tipo de contenido y bytes del archivo.
- El ámbito es `clave + reporte + reportante`.
- Misma clave y contenido: replay de la evidencia original.
- Misma clave y contenido distinto: `409 IDEMPOTENCIA_CONFLICTO`.
- Una clave con huella `NULL` se considera **no verificable** y también responde `409 IDEMPOTENCIA_CONFLICTO`; no se reproduce ni se devuelve la evidencia antigua.
- Las claves concurrentes se serializan con un advisory lock transaccional.
- Una clave de otro reporte o usuario no devuelve evidencia ajena.

## Registros legados

Antes de guardar huellas, una clave pudo quedar registrada con `huella = NULL`. No es posible demostrar que el archivo entrante coincide con el original. Para evitar un replay inseguro:

1. El backend no devuelve la evidencia almacenada.
2. No crea una nueva fila ni un archivo nuevo.
3. Responde `409 IDEMPOTENCIA_CONFLICTO`.
4. El cliente debe iniciar una nueva operación con una clave nueva si el reintento seguro no es posible.
