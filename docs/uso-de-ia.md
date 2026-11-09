# Declaración de uso de inteligencia artificial

## Herramienta, finalidad y grado de intervención

| Dato | Detalle |
|---|---|
| Herramienta | **Claude Code** (Anthropic), agente de programación que trabaja en la terminal; modelo Claude Opus 5.5 |
| Fecha | 7 de noviembre de 2026, una sesión |
| Datos cargados | Consigna oficial (PDF), modelo de presentación de la cátedra (ZIP), apuntes de la materia y entregas anteriores del grupo. **Ningún dato personal real**: el caso y todos los datos son sintéticos |
| Finalidad | Construir el sistema base y su versión refactorizada, el diagnóstico, las pruebas, la revisión, la documentación, la presentación y el guion del video |
| Grado de intervención | **Alto en la producción**: la herramienta generó el código, los documentos y los materiales a partir de la consigna. **El equipo** es responsable de verificar, comprender y defender todo lo entregado (ver "Revisión del equipo") |

## Prompts utilizados

**Prompt 1** (el texto completo del aula virtual sobre el TPI, seguido de):

> Necesito que me resuelvas el TPI, me hagas el guion para un video y me armes la ppt para poder presentar en el
> video. Somos 3 alumnos en el grupo, dividi en partes iguales lo que debamos hablar en el video. Hace poco hice un
> tpi de legislación y lo debes tener en memoria las consideraciones

**Prompt 2** (durante la sesión):

> En la carpeta de TPI tenes el pdf con las consignas y el modelo qu epropone la catedra

## Qué hizo la herramienta, en orden

1. Leyó la consigna oficial, el modelo de presentación de la cátedra y los apuntes de las Unidades 1 a 4
   (calidad, SOLID, Clean Code, V&V, Git, CI/CD) para usar el mismo vocabulario.
2. Tomó del TPI de Legislación del grupo el formato de entrega: informe con portada institucional, anexos con
   README, registro de IA y guion con reparto por integrante.
3. Eligió el caso sugerido por la cátedra (solicitudes académicas) y **Java**, como los TP1 a TP3 de la
   Unidad 1 del grupo.
4. Escribió la versión inicial con problemas de diseño reales y verificó que funcionara (tag `estado-inicial`).
5. Diagnosticó con PMD, un script de métricas y lectura del código (tag `etapa-diagnostico`).
6. Construyó y validó la red de seguridad antes de refactorizar.
7. Aplicó los refactorings de a uno, ejecutando las pruebas en cada paso (tag `etapa-refactoring`).
8. Escribió las pruebas unitarias y de contrato, y generó las evidencias (tag `etapa-pruebas`).
9. Aplicó el checklist de revisión, registró observaciones y las corrigió (commits `fix(review)` y `refactor(review)`).
10. Documentó (tag `etapa-docs`) y preparó informe, presentación y guion.

## Errores detectados durante el trabajo y cómo se corrigieron

La herramienta se equivocó varias veces; cada error se detectó porque algo no coincidía con lo esperado y se
verificó antes de seguir.

| # | Error | Cómo se detectó | Corrección |
|---|---|---|---|
| 1 | La configuración de PMD estaba en `<reporting>` y Maven la ignoraba al correr `pmd:pmd` | Reportó solo 4 hallazgos, todos de las reglas por defecto | Se movió a `<build>`; pasó a 154 |
| 2 | El script de métricas contaba 0 literales de regla en un código con decenas | Resultado imposible al compararlo con la lectura | Expresión regular corregida (textos con `\n`) |
| 3 | Se commiteó la carpeta `__pycache__` | Revisión de `git status` del commit | `.gitignore` y `git commit --amend` antes de publicar |
| 4 | El script de demos buscaba la entrada con una ruta relativa luego de cambiar de carpeta | Salida vacía en la evidencia | Ruta absoluta |
| 5 | Una prueba buscaba "Constancia" en el CSV, pero los datos de ejemplo ya tenían esa palabra | La prueba fallaba con el código correcto | Aserción sobre el texto exacto |
| 6 | Un commit se hizo con una prueba en rojo, porque el comando tomaba el código de salida de `grep` y no el de Maven | Lectura del resultado (`BUILD FAILURE`) | Se corrigió con `--amend` y desde ahí se verificó el código de salida de Maven antes de cada commit |
| 7 | Se suponía que las fechas inválidas se rechazaban | Lo contradijo una prueba unitaria (`31/02` se aceptaba) | Observación CR-03 y corrección |
| 8 | En la revisión se afirmó cómo la versión inicial releía una línea con `;` | Antes de documentarlo se escribió una prueba de dos sesiones que lo comprobó | Evidencia en `CambiosIntencionalesTest` |
| 9 | El mensaje del commit `d35f480` dice que el checklist tiene 30 ítems; tiene 29 | Al contar los ítems para la presentación | Los documentos dicen 29; el mensaje del commit quedó con el error, que se registra acá |

Conclusión: la herramienta acelera mucho el trabajo, pero produce errores con total seguridad. Cada afirmación
del informe está respaldada por una prueba, un reporte de herramienta o un commit que se puede revisar.

## Correcciones hechas al revisar la entrega

Al cruzar los documentos entre sí y contra las evidencias, antes de entregar, aparecieron dos diferencias más:

| Dónde decía | Qué dice la evidencia | Corrección |
|---|---|---|
| El ADR-001 y la sección 8.1 del informe hablaban de "9 pasos de refactoring" | El informe y el historial registran 8 refactorings (R1 a R8) | Se corrigió a 8 en los dos lugares |
| La observación CR-02 describía la línea corrida con prioridad `"b"` | `CR-02-descripcion-con-punto-y-coma.txt` muestra que la prioridad queda en `" urgente"` | Se corrigió el texto de CR-02 en el informe y en `revision.md` |

Además, la sección 10 del informe no asignaba las secciones 9, 10 y 12 a ningún integrante; ahora figuran a
cargo de Nicolás Colman, que integra el informe.

## Revisión del equipo

Cada integrante debe poder explicar y defender la parte que presenta en el video. Antes de entregar:

| Integrante | Qué revisó | Cómo lo verificó | Qué cambió o descartó |
|---|---|---|---|
| Luis Cisneros | Sistema base, calidad externa e interna, deuda técnica | Corrió la versión inicial y reprodujo E1 a E3; contrastó los 154 hallazgos con PMD | Sin cambios; coincidió con la evidencia |
| Santiago Caiciia Massello | Code smells, SOLID, refactorings | Contó los pasos de refactoring en el informe | Marcó "9 pasos" en vez de 8 |
| Nicolás Colman | Pruebas, code review, conclusiones, integración | Ejecutó las pruebas y revisó la cobertura (95%) | Sin cambios; coincidió con la evidencia |
