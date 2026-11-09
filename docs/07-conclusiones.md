# Etapa 8 - Conclusiones y aprendizaje

## Conclusiones grupales

**1. Un sistema que funciona puede estar en mala salud.** La versión inicial hacía todo lo que el usuario
necesitaba y pasaba sus validaciones. Sin embargo, PMD le encontró 154 problemas de diseño, un método de 101 líneas
y 0 pruebas. Para agregar un tipo de solicitud había que tocar 8 lugares. La calidad interna no se ve desde el
menú, pero define cuánto cuesta y cuánto riesgo tiene cada cambio.

**2. Sin red de seguridad no hay refactoring, solo cambios.** La decisión más importante fue escribir primero la
prueba de caracterización y comprobar que detectaba un error inyectado. Con esa red se hicieron 8 pasos de
refactoring con 17 de 17 escenarios idénticos en cada uno. Sin ella, cada paso habría sido una apuesta.

**3. Refactorizar y corregir son trabajos distintos.** Separarlos (ADR-006) permitió afirmar con evidencia que
el comportamiento se conservó, y a la vez corregir los defectos visibles (E1 a E3) con su propia prueba.

**4. Las herramientas encuentran lo que la lectura no ve, y al revés.** Las pruebas unitarias encontraron que
`31/02/2026` se aceptaba en silencio. La revisión humana encontró que un `;` corrompía el archivo, defecto que
venía de la versión inicial. PMD encontró literales repetidos y complejidad. Ninguna fuente alcanzaba sola.

**5. SOLID no es un fin, es una forma de bajar el costo del cambio.** Cada principio se aplicó donde había un
problema concreto: SRP para la clase gigante, OCP para la cirugía de escopeta, DIP para poder probar sin archivos ni
fecha real, ISP para que el reporte solo vea lo que usa y LSP verificado con una prueba de contrato.

**6. Mejor diseño no significa menos código.** El total de líneas pasó de 501 a 943. Lo que bajó fue el tamaño
de cada pieza (archivo más grande: 401 → 158 líneas; método más largo: 101 → 20) y el acoplamiento entre ellas.

## Dificultades

- Conservar **exactamente** los mismos mensajes y el mismo orden de validaciones mientras se movía la lógica
  entre clases (por ejemplo, un estado desconocido debía seguir diciendo "no se puede pasar de PENDIENTE a X").
- Comparar salidas que dependen de la fecha: se resolvió comparando contra la versión inicial en la misma
  ejecución, en lugar de salidas fijas guardadas.
- Decidir qué no corregir: cambiar "si" como respuesta válida o fijar el formato decimal habría alterado el
  comportamiento sin un pedido del usuario (CR-07 y CR-10).

## Límites del trabajo

- El sistema es de consola y de un solo usuario; no se analizaron concurrencia, seguridad ni rendimiento.
- La caracterización cubre 17 escenarios elegidos por el equipo: reduce el riesgo, no prueba la ausencia total de
  regresiones.
- Las métricas propias son aproximadas (expresiones regulares); para decisiones finas conviene una herramienta
  como SonarQube.
- Persistencia en CSV reescrito completo en cada guardado: suficiente para el volumen del caso.

## Mejoras futuras

| Mejora | Deuda que paga | Facilitada por |
|---|---|---|
| Base de datos (por ejemplo, SQLite) en lugar de CSV | Robustez y concurrencia | `RepositorioSolicitudes` y su prueba de contrato |
| Aceptar "si" y "no" como respuestas (CR-07) | Usabilidad | `MenuConsola`, con decisión del usuario |
| Idioma de formato fijo (CR-10) | Salidas iguales en cualquier máquina | `FormatoConsola` |
| Agente de JaCoCo en las pruebas de caracterización (CR-11) | Medir la cobertura de `App` | `EjecucionDelPrograma` |
| Interfaz web o API | Más canales de uso | Los casos de uso ya no dependen de la consola |
| Retirar la copia `legacy` cuando el equipo decida dejar de compararse con la versión inicial | Mantenimiento de pruebas | Las pruebas unitarias ya cubren las reglas |

## Relación con los contenidos de la asignatura

| Unidad / tema | Dónde se aplicó |
|---|---|
| Calidad interna y externa, deuda técnica, code smells | [02-diagnostico.md](02-diagnostico.md) |
| SOLID y Clean Code | [03-solid-clean-code.md](03-solid-clean-code.md) |
| Refactoring | [04-refactorings.md](04-refactorings.md) |
| Verificación y validación: patrón AAA, dobles de prueba, cobertura | [05-pruebas.md](05-pruebas.md) |
| Definition of Done, code review | [06-code-review](06-code-review/checklist.md) |
| Git avanzado: commits atómicos, tags, `--amend` antes de publicar | Historial del repositorio |
| Trabajo colaborativo: ADR, CONTRIBUTING, plantilla de PR | [decisiones](decisiones/), [CONTRIBUTING.md](../CONTRIBUTING.md) |
| CI/CD y SemVer | [ci.yml](../.github/workflows/ci.yml); versión 1.0.0 → 1.0.1 (solo correcciones compatibles) |
| IA como copiloto, con trazabilidad y verificación | [uso-de-ia.md](uso-de-ia.md) |

## Conclusiones individuales

**Luis Cisneros** - *Sistema base, calidad y deuda técnica.* Lo que más me sorprendió fue que un programa que
funcionaba bien tuviera tantos problemas por dentro. Medir con PMD y con el ejemplo de "agregar un tipo" me
permitió explicar la deuda técnica con números y no con opiniones: los intereses de la deuda son las 8
modificaciones que hacía falta para un cambio chico.

**Santiago Caiciia Massello** - *Code smells, SOLID y refactorings.* Aprendí que refactorizar es una técnica con
pasos, no "reescribir prolijo". Hacer un refactoring por commit, con la caracterización en verde cada vez, me dio
la confianza para mover responsabilidades grandes. Lo más difícil fue mantener los mismos mensajes y el mismo
orden de validaciones mientras la lógica cambiaba de clase.

**Nicolás Colman** - *Pruebas, code review y conclusiones.* Me quedo con que una red de seguridad hay que
probarla antes de confiar en ella. También con que las pruebas y la revisión encuentran cosas distintas: las
pruebas detectaron la fecha imposible y la revisión, el punto y coma que corrompía el archivo. La IA ayudó a
avanzar rápido, pero cada afirmación del informe tuvo que quedar respaldada por una prueba o un reporte.
