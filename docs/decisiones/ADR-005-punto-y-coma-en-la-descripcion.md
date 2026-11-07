# ADR-005 - Rechazar el punto y coma en la descripción

**Estado:** aceptada · **Fecha:** 07/11/2026 · **Origen:** observación CR-02 del code review

## Contexto
Los archivos usan `;` como separador y no escapan los campos. Una descripción con `;` generaba una línea con una
columna de más: la versión inicial la releía con los campos corridos y la refactorizada la descartaba. En ambos
casos se perdía la solicitud.

## Decisión
`Pedido` rechaza descripciones que contengan `;` con el mensaje "la descripcion no puede contener ';'".

## Alternativas consideradas
- **Escapar el campo (comillas, como el CSV estándar):** resuelve el caso sin limitar al usuario, pero cambia el
  formato del archivo y obliga a migrar o a leer dos formatos.
- **Reemplazar `;` por `,` en silencio:** evita el error, pero altera lo que escribió el usuario sin avisar.

## Consecuencias
- El archivo mantiene su formato y no se corrompe.
- El dominio conoce una restricción que nace del almacenamiento; está documentada en `Pedido`. Si se migra a un
  formato con escape o a una base de datos, la restricción se puede quitar.
