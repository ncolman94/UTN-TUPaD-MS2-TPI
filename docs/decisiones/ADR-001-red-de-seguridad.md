# ADR-001 - Pruebas de caracterización como red de seguridad

**Estado:** aceptada · **Fecha:** 07/11/2026

## Contexto
La versión inicial no tenía pruebas y su lógica imprimía directamente en consola, leía archivos con rutas fijas y
usaba la fecha del sistema. No se podían escribir pruebas unitarias sin cambiar antes el código, y cambiarlo sin
pruebas era justamente el riesgo a evitar.

## Decisión
Antes de refactorizar, congelar una copia literal de la versión inicial (`src/test/java/legacy`) y comparar ambas
versiones **como procesos**: misma entrada, carpetas de trabajo separadas, y comparación de la salida, el código
de salida y los tres CSV (golden master). Validar la red inyectando un error a propósito.

## Alternativas consideradas
- **Escribir primero pruebas unitarias:** obligaba a modificar el código para poder probarlo, sin red.
- **Guardar salidas esperadas fijas en archivos:** dependen de la fecha (vencimientos); comparar contra la versión
  inicial en la misma ejecución evita ese problema.
- **Probar a mano el menú:** no es repetible ni escala a 8 refactorings.

## Consecuencias
- Cada commit de refactoring se verificó con 17/17 escenarios idénticos.
- La red mide lo que el usuario ve, no la implementación: permite cambiar toda la estructura interna.
- Los cambios de comportamiento intencionales necesitan su propia prueba (`CambiosIntencionalesTest`).
- La copia `legacy` queda en el repositorio mientras se quiera seguir comparando.
