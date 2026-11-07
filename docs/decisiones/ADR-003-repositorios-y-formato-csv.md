# ADR-003 - Repositorios detrás de interfaces, conservando el formato CSV

**Estado:** aceptada · **Fecha:** 07/11/2026

## Contexto
La lectura y escritura de archivos estaba mezclada con las reglas. Además, los archivos `datos/*.csv` ya existen y
pueden tener datos reales de uso: cambiar su formato rompería la compatibilidad.

## Decisión
Definir en el dominio las interfaces `RepositorioSolicitudes` (con `ConsultaSolicitudes` de solo lectura),
`RepositorioEstudiantes` y `RepositorioResponsables`, e implementarlas en `persistencia` con el **mismo formato
de archivo** de la versión inicial. Agregar `RepositorioSolicitudesEnMemoria` y una prueba de contrato común.
Los repositorios entregan y guardan copias (CR-08).

## Alternativas consideradas
- **Pasar a una base de datos (SQLite):** mejora la robustez, pero cambia el comportamiento externo y la
  instalación; queda como mejora futura, facilitada por las interfaces.
- **Una sola interfaz con todos los métodos:** obligaría al reporte a depender de métodos de escritura (ISP).

## Consecuencias
- Las reglas se prueban con la implementación en memoria, sin archivos.
- Cambiar de almacenamiento solo requiere una implementación nueva que pase el contrato.
- El archivo se reescribe completo en cada guardado, igual que antes; con muchos datos convendría otro formato.
