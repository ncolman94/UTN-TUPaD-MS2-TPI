# Checklist técnico de code review

Se aplica a cada pull request antes de integrarlo a `main`. Cada ítem tiene un **criterio verificable**: se
marca como cumplido solo si hay evidencia (una prueba, un reporte de herramienta o una referencia al código).

Severidad de las observaciones:

| Severidad | Significado | ¿Bloquea la integración? |
|---|---|---|
| Bloqueante | Rompe el comportamiento, pierde datos o no compila | Sí |
| Mayor | Defecto o riesgo concreto de mantenimiento | Sí, salvo que se acepte como deuda con justificación |
| Menor | Mejora de claridad o diseño, sin riesgo inmediato | No; se corrige o se registra |
| Sugerencia | Alternativa a considerar | No |

## A. Diseño y principios SOLID

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| A1 | Responsabilidad única (SRP) | Cada clase tiene una sola razón de cambio, que se puede decir en una oración |
| A2 | Abierto/cerrado (OCP) | Agregar un tipo de solicitud requiere modificar un solo archivo |
| A3 | Sustitución de Liskov (LSP) | Toda implementación de una interfaz pasa la misma prueba de contrato |
| A4 | Segregación de interfaces (ISP) | Ningún cliente depende de métodos que no usa |
| A5 | Inversión de dependencias (DIP) | La lógica no crea archivos, relojes ni consolas: los recibe |
| A6 | Dirección de las dependencias | `consola → aplicacion → dominio ← persistencia`; el dominio no importa otros paquetes del proyecto |

## B. Clean Code

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| B1 | Nombres expresivos | PMD `ShortVariable` = 0; ningún nombre engañoso |
| B2 | Funciones pequeñas | Ningún método supera 20 líneas ni complejidad ciclomática 10 |
| B3 | Sin literales mágicos repetidos | PMD `AvoidDuplicateLiterals` = 0 |
| B4 | Sin código muerto ni comentado | PMD `UnusedPrivateMethod` = 0; sin bloques comentados |
| B5 | Comentarios útiles | Explican el porqué o el contrato, no repiten el código |
| B6 | Sin duplicación (DRY) | Ninguna regla de negocio escrita en dos lugares |

## C. Manejo de errores

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| C1 | Sin catch vacíos ni genéricos | PMD `EmptyCatchBlock` y `AvoidCatchingGenericException` = 0 |
| C2 | Errores del dominio con excepciones | Las reglas lanzan `ReglaDeNegocioException`; solo la consola decide el texto final |
| C3 | Entradas inválidas | Ninguna entrada del usuario cierra el programa con una excepción |
| C4 | Fallos de entrada/salida | No se ocultan ni se informa éxito si el guardado falló |

## D. Pruebas

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| D1 | Suite en verde | `./mvnw verify` termina con BUILD SUCCESS |
| D2 | Comportamiento externo conservado | Caracterización 17/17 IDÉNTICOS |
| D3 | Reglas nuevas o modificadas probadas | Cada cambio de regla trae su prueba unitaria |
| D4 | Cobertura | Líneas ≥ 80% (JaCoCo) |
| D5 | Pruebas deterministas | No dependen de la fecha real ni de archivos fuera de carpetas temporales |

## E. Datos y compatibilidad

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| E1 | Formato de archivos compatible | Los CSV de la versión inicial se leen y se escriben igual |
| E2 | Datos sintéticos | Ningún dato personal real en el repositorio |
| E3 | Validación de lo que se lee | Una línea dañada no se interpreta mal ni hace perder las demás |

## F. Repositorio y documentación

| Id | Ítem | Criterio de aceptación |
|---|---|---|
| F1 | Commits trazables | Mensaje convencional (`feat`, `refactor`, `fix`, `test`, `docs`) con referencia al smell, refactoring u observación |
| F2 | Tags de etapa | `estado-inicial`, `etapa-diagnostico`, `etapa-refactoring`, `etapa-pruebas`, `etapa-docs`, `estado-refactorizado` |
| F3 | README completo | Descripción, instalación, uso, funcionalidades, tecnologías y estructura |
| F4 | Decisiones registradas | Cada decisión de diseño relevante tiene su ADR |
| F5 | Integración continua | El pipeline corre pruebas, cobertura y PMD en cada push |

## Criterios de aceptación de la revisión (Definition of Done)

El cambio se integra cuando se cumplen **todos**:

1. No quedan observaciones **bloqueantes** ni **mayores** abiertas.
2. `./mvnw verify` en verde y caracterización 17/17.
3. Cobertura de líneas ≥ 80%.
4. Todo hallazgo de PMD está corregido o tiene una justificación escrita.
5. Las observaciones menores y las sugerencias están corregidas o registradas como deuda, con su motivo.

## Cómo se da el feedback

Cada observación sigue el mismo formato, para que sea objetiva y accionable:

> **Dónde** (archivo y línea) · **Qué se observa** (un hecho, no una opinión) · **Por qué importa** (riesgo
> concreto) · **Propuesta** (qué hacer y cómo verificarlo).

Se comenta el código, no a la persona. También se registra lo que está bien, para que se mantenga.
