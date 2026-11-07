# Cómo contribuir

## Flujo de trabajo

1. Crear una rama desde `main` con un nombre descriptivo: `refactor/extraer-reporte`, `fix/fechas-invalidas`.
2. Hacer commits chicos con [Conventional Commits](https://www.conventionalcommits.org/es/) y una referencia al
   motivo (smell, refactoring u observación de revisión):
   - `refactor(R6): ...` no cambia el comportamiento.
   - `fix: ...` cambia el comportamiento a propósito; incluye una prueba que lo documenta.
   - `test: ...`, `docs: ...`, `feat: ...`.
3. **No mezclar** un refactoring con un cambio de comportamiento en el mismo commit (ver ADR-006).
4. Antes de abrir el pull request, correr `./mvnw verify` y `./mvnw pmd:pmd`.
5. Abrir el pull request con la plantilla y pedir revisión a otro integrante, que aplica el
   [checklist](docs/06-code-review/checklist.md).

## Reglas que no se negocian

- La caracterización debe quedar **17/17 IDÉNTICOS**. Si un cambio la rompe a propósito, va en un commit `fix`
  con su prueba en `CambiosIntencionalesTest`.
- Toda regla nueva o modificada trae su prueba unitaria (patrón Arrange - Act - Assert).
- Cobertura de líneas ≥ 80%.
- Datos de prueba siempre sintéticos.

## Agregar un tipo de solicitud

Agregar una constante en `TipoSolicitud` con su prioridad, su plazo y su área, y una fila en
`TipoSolicitudTest`. No hace falta tocar nada más.
