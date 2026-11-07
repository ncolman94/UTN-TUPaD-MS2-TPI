# ADR-006 - Refactorizar y corregir en commits separados

**Estado:** aceptada · **Fecha:** 07/11/2026

## Contexto
El diagnóstico encontró defectos externos (E1 a E3) además de problemas internos. Corregirlos durante el
refactoring haría fallar la caracterización y no se sabría si la diferencia era buscada o una regresión.

## Decisión
Los commits `refactor` no cambian el comportamiento (la caracterización debe quedar 17/17). Los cambios de
comportamiento van en commits `fix` separados, cada uno con una prueba que ejecuta la versión inicial y la actual y
documenta la diferencia.

## Consecuencias
- Toda diferencia observable está explicada y probada.
- Algunos defectos se mantuvieron a propósito durante varios pasos (por ejemplo, E2 hasta el commit `ae2ab70`).
- El historial permite revisar o revertir por separado una mejora interna y un cambio visible.
