# BE-05 — Contrato mínimo de datos generales

**Estado:** resuelto en la rama de correcciones como contrato mínimo MVP. No modifica SPEC-05; concreta la responsabilidad que el plan asigna a la UI.

## Campo aprobado para el MVP

| Clave | Tipo | Obligatorio | Validación | Visibilidad pública |
|---|---|---|---|---|
| `descripcion` | string | No | Se recorta; vacío/`null` elimina el campo; máximo 1000 caracteres UTF-16 | Sí, en la vista pública |

No se incluye `anosExperiencia`: la experiencia profesional tiene su propia operación y estructura.

## Justificación respecto a F05

- `descripcion` es una presentación libre del Prestador. No duplica categorías, zonas, experiencia, portafolio, disponibilidad ni agenda, que ya tienen apartados y operaciones propias.
- La edición de este campo permite cumplir FR-025 sin convertir la lista blanca técnica en un catálogo de producto.
- El campo no representa ningún dato de contacto ni documento.

## Request y response

Request de creación o edición:

```json
{
  "datosGenerales": {
    "descripcion": "Plomero con experiencia en reparaciones domiciliarias."
  }
}
```

Response:

```json
{
  "datosGenerales": {
    "descripcion": "Plomero con experiencia en reparaciones domiciliarias."
  }
}
```

Si no hay descripción, la respuesta devuelve `descripcion: null`. En creación o edición se acepta `datosGenerales` ausente o vacío.

## Claves desconocidas y compatibilidad

- El request tipado ignora claves desconocidas, incluidas `anosExperiencia`, teléfono, correo, dirección exacta y documentos. No se persisten en altas nuevas.
- En edición, las claves heredadas ya guardadas se conservan para no perder datos de perfiles existentes. No forman parte del contrato ni se publican.
- La respuesta propia y la proyección pública solo exponen `descripcion`.
- Los perfiles existentes sin `descripcion` siguen siendo legibles; el campo queda `null`.
- La lista blanca técnica anterior deja de determinar la proyección pública: la vista pública solo incluye `descripcion`.

## Proyección pública

`PerfilProfesional.datosPublicos` solo devuelve `descripcion` cuando es un string no vacío. Quedan excluidos teléfono, correo, dirección exacta, documentos y cualquier otra clave heredada.

## Pruebas

- Creación con descripción normalizada y descarte de claves desconocidas.
- Edición que conserva claves heredadas.
- Descripción vacía que elimina el campo sin borrar claves heredadas.
- Descripción mayor a 1000 caracteres → `400` de validación del request (el helper de dominio usa `DATOS_GENERALES_INVALIDOS` si se invoca directamente).
- Lectura de JSON con `anosExperiencia`, teléfono y correo ignorando esos campos.
- Proyección pública limitada a `descripcion`.
- Verificación HTTP contra PostgreSQL efímero de creación, edición, validación y compatibilidad con claves heredadas.
- Proyección pública cubierta por prueba de dominio: solo se expone `descripcion`; teléfono, correo, dirección, documentos y claves heredadas quedan fuera. La vista previa HTTP depende además del perfil base de F02, por lo que no se usó como evidencia de BE-05.
