# ADR-002 - Tipos y estados como enums que contienen sus reglas

**Estado:** aceptada · **Fecha:** 07/11/2026

## Contexto
El tipo de solicitud se evaluaba con textos en 8 lugares (prioridad, plazo, área, validación, reporte y menú).
Agregar un tipo era una cirugía de escopeta y un error de tipeo compilaba sin aviso.

## Decisión
`TipoSolicitud`, `EstadoSolicitud`, `Prioridad` y `Area` son enums. Cada constante de `TipoSolicitud` declara su
prioridad base, su plazo y su área; las transiciones se definen en `EstadoSolicitud.puedePasarA`. El menú y el
reporte recorren `values()`.

## Alternativas consideradas
- **Una clase por tipo con polimorfismo (Strategy):** más flexible si cada tipo tuviera comportamiento distinto,
  pero hoy solo difieren en datos; serían 5 clases casi vacías.
- **Tabla de configuración en un archivo:** permitiría cambiar reglas sin compilar, pero agrega validación de
  configuración y no fue pedido.

## Consecuencias
- Agregar un tipo es agregar una línea (OCP); el compilador detecta nombres mal escritos.
- Los nombres de las constantes coinciden con los textos del archivo CSV, así el formato no cambió.
- Si un tipo llegara a necesitar lógica propia, el enum puede sobrescribir un método o migrar a Strategy.
