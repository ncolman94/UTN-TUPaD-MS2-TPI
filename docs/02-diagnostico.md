# Etapas 2 y 3 - Diagnóstico de calidad, deuda técnica y code smells

Versión analizada: tag `estado-inicial`. Las líneas citadas corresponden a ese tag.

Fuentes de evidencia:

- Lectura del código por el equipo.
- PMD 7 con las reglas de [config/pmd/reglas-diseno.xml](../config/pmd/reglas-diseno.xml): **154 hallazgos**
  ([detalle](evidencias/estado-inicial/pmd-resumen.md)).
- Métricas de [scripts/metricas.py](../scripts/metricas.py) ([resultado](evidencias/estado-inicial/metricas.md)).
- Ejecuciones guardadas en [evidencias/estado-inicial](evidencias/estado-inicial/).

---

## Etapa 2 - Calidad externa vs. calidad interna

La **calidad externa** es lo que percibe quien usa el sistema: que haga lo que debe, que no falle, que se
entienda. La **calidad interna** es cómo está construido: legibilidad, cohesión, acoplamiento y facilidad
para probarlo. El usuario no la ve, pero define cuánto cuesta cada cambio.

### Lo que funciona desde la mirada del usuario

| Característica | Evaluación | Evidencia |
|---|---|---|
| Adecuación funcional | **Buena**: las 6 operaciones hacen lo que se espera y aplican las reglas de prioridad, plazo, área y estados | [recorrido-funcional.txt](evidencias/estado-inicial/recorrido-funcional.txt) |
| Validaciones | **Buena**: legajo inexistente, tipo inválido, descripción vacía, área incorrecta y transiciones no permitidas se informan con mensajes claros | [validaciones.txt](evidencias/estado-inicial/validaciones.txt) |
| Persistencia | **Buena** en el uso normal: los datos sobreviven al cierre del programa | CSV final de cada evidencia |
| Usabilidad | **Aceptable**: menú numerado y textos simples | - |
| Fiabilidad | **Deficiente** ante entradas o archivos inesperados (ver E1 a E3) | [entrada-no-numerica.txt](evidencias/estado-inicial/entrada-no-numerica.txt) |

### Defectos externos encontrados

| Id | Defecto | Causa interna |
|---|---|---|
| E1 | Si el usuario escribe una letra donde se espera un número (menú o número de solicitud), el programa se cierra con `NumberFormatException` y código de salida 1 | `Integer.parseInt` sin control (`App.java` l. 23, 38, 45) |
| E2 | Si no se puede escribir el archivo, se muestra "Error al guardar" y a continuación "Solicitud #N registrada": dos mensajes contradictorios | `guardar()` atrapa la excepción y el método que lo llama sigue como si nada (l. 76-89) |
| E3 | Una línea mal formada en `solicitudes.csv` hace que se descarten **en silencio** esa línea y todas las siguientes | `catch (Exception e) {}` vacío alrededor de todo el ciclo de lectura (l. 52-73) |

### Lo que no se ve: problemas de la estructura interna

| Atributo interno | Situación | Por qué importa para la evolución |
|---|---|---|
| Cohesión | `GestorSolicitudes` (401 líneas) lee archivos, valida, calcula reglas, filtra, arma el reporte e imprime | Cualquier cambio, aunque sea de formato, toca la misma clase |
| Acoplamiento | La lógica depende de `System.out`, de rutas fijas (`"datos/..."`) y del reloj del sistema (`LocalDate.now()`) | No se puede probar una regla sin archivos reales, consola y fecha de hoy |
| Legibilidad | Métodos de hasta 101 líneas, variables `g`, `p`, `s`, `va`, `res` | Entender una regla exige leer el método entero |
| Modificabilidad | El tipo de solicitud se evalúa con textos en 8 lugares distintos | Agregar un tipo es una "cirugía de escopeta" |
| Capacidad de ser probado | 0 pruebas automatizadas; los métodos no devuelven resultados, imprimen | Cada cambio se verifica a mano, recorriendo el menú |

### El costo del cambio, con un ejemplo concreto

Pedido hipotético: *"agregar el tipo CONSTANCIA_TITULO, prioridad MEDIA, 15 días, área BEDELIA"*. En la versión
inicial hay que modificar **8 puntos en 2 archivos**:

1. `App.java` l. 28: el texto del menú con los tipos.
2. `GestorSolicitudes.java` l. 104: la condición que valida el tipo.
3. l. 119: el `switch` de prioridad.
4. l. 145: el `switch` de plazo.
5. l. 220: el `switch` de área.
6. l. 343 y 360-374: el contador del reporte.
7. l. 376: la clasificación por área del reporte (con una lista propia de tipos).
8. l. 403-408: la línea impresa del reporte.

Si se olvida uno (por ejemplo el punto 7), el sistema compila y funciona, pero el reporte por área queda mal
sin que nadie lo note. Ese es el **interés** de la deuda técnica: el tiempo extra y el riesgo que se pagan en
cada cambio futuro.

---

## Etapa 3 - Deuda técnica y code smells

### Criterio de priorización

Cada problema se puntúa de 1 a 5 en **impacto** (cuánto afecta la mantenibilidad o la fiabilidad) y en
**riesgo de mantenimiento** (qué tan probable es que provoque errores o demoras en los próximos cambios).
Prioridad = impacto × riesgo: **Alta** de 16 a 25, **Media** de 9 a 15, **Baja** hasta 8.

### Inventario

| Id | Smell o debilidad | Ubicación (estado-inicial) | Evidencia | Impacto | Riesgo | Prioridad |
|---|---|---|---|:-:|:-:|:-:|
| D01 | **Sin pruebas automatizadas** (deuda de proceso) | Todo el proyecto | 0 pruebas; la única verificación es manual por menú | 5 | 5 | **Alta (25)** |
| S01 | **Clase gigante** (Large Class / God Class) | `GestorSolicitudes` | 401 LOC, 9 métodos, complejidad ciclomática total 116 (PMD `NcssCount`, `CyclomaticComplexity`, `TooManyMethods`); mezcla persistencia, reglas, consultas y presentación | 5 | 5 | **Alta (25)** |
| S02 | **Método largo** (Long Method) | `registrarSolicitud` l. 91-191; `reporte` l. 341-419; `App.main` l. 7-79 | 101, 79 y 73 líneas; complejidad 29, 19 y 9; complejidad cognitiva 21 y 30 (PMD) | 4 | 5 | **Alta (20)** |
| S03 | **Obsesión por primitivos** (Primitive Obsession) | `Solicitud`: `tipo`, `estado`, `prioridad`, fechas como `String` | 58 literales de tipo, estado o prioridad repetidos (`AvoidDuplicateLiterals`: "BAJA" 8 veces, "CERTIFICADO" 6); las fechas se vuelven a convertir con `LocalDate.parse` en l. 308, 384, 390 y 391 | 4 | 5 | **Alta (20)** |
| S04 | **Switch repetidos / cirugía de escopeta** (Switch Statements / Shotgun Surgery) | l. 104, 119, 145, 220, 360-378, 403-408 y `App` l. 28 | 8 puntos de cambio para agregar un tipo (ver ejemplo arriba). Viola OCP | 5 | 4 | **Alta (20)** |
| S05 | **Manejo de errores deficiente** | `cargar` l. 36, 50, 71; `guardar` l. 86; `App` l. 23, 38, 45; errores como `println` + `return` en todos los métodos | 3 `EmptyCatchBlock`, 4 `AvoidCatchingGenericException`; provoca los defectos E1, E2 y E3 | 5 | 4 | **Alta (20)** |
| S06 | **Código duplicado** (Duplicated Code) | Búsqueda por número l. 194-199 y 247-252; lectura de CSV l. 23-73 (3 veces); formato de línea l. 323 y 336; criterio "abierta" l. 309 y 383 | Bloques casi idénticos; viola DRY | 4 | 4 | **Alta (16)** |
| S07 | **Acoplamiento a infraestructura / dependencias ocultas** | Rutas fijas l. 24, 39, 53, 78; `LocalDate.now()` l. 183, 284, 292, 348; 66 `System.out.println` en la lógica | PMD `SystemPrintln`: 66. Viola DIP: la regla depende de detalles concretos | 4 | 4 | **Alta (16)** |
| S08 | **Demasiados parámetros + argumento bandera** (Long Parameter List / Flag Argument) | `buscar(String, String, String, String, boolean)` l. 290 | 4 `String` seguidos (fácil invertir el orden sin que el compilador avise) y un `boolean` que cambia el comportamiento (PMD `ExcessiveParameterList`) | 3 | 4 | **Media (12)** |
| S09 | **Envidia de características** (Feature Envy) | Vencimiento l. 307-312 y 382-388; transiciones l. 259-280; días de resolución l. 389-394 | El gestor calcula todo leyendo campos de `Solicitud`; la regla de "vencida" está escrita dos veces | 3 | 4 | **Media (12)** |
| S10 | **Clase de datos** (Data Class) | `Solicitud`, `Estudiante`, `Responsable` | Solo campos públicos, sin comportamiento (PMD `DataClass` en 2 de 3). Nada impide `s.estado = "CUALQUIERA"` | 3 | 4 | **Media (12)** |
| S11 | **Nombres poco expresivos o engañosos** (Mysterious Name) | `App`: `g`, `sc`, `op`, `t`, `d`, `u`; gestor: `p`, `s`, `f`, `va`, `res`, `resp`, `desc` | 31 hallazgos `ShortVariable`. `cerradas` (l. 346) cuenta solo las RESUELTA; `res` significa "resultado" en `buscar` y "resueltas" en `reporte` | 3 | 3 | **Media (9)** |
| S12 | **Comentarios redundantes y código muerto** | `esUrgente` l. 421-428 sin uso; código comentado l. 187 y `App` l. 69-71; comentarios que repiten el código (l. 92, 117, 143, 168) | PMD `UnusedPrivateMethod`, `SimplifyBooleanReturns` | 2 | 2 | **Baja (4)** |
| D02 | **Documentación mínima** (deuda de proceso) | `README.md` | 6 líneas; no explica reglas, estructura ni decisiones | 2 | 3 | **Baja (6)** |

Son **12 code smells y 2 deudas de proceso** (la consigna pide al menos 8).

### Mapa de calor del gestor

| Método | Líneas | Complejidad ciclomática | Smells que concentra |
|---|---:|---:|---|
| `registrarSolicitud` | 101 | 29 | S02, S03, S04, S05, S07, S11, S12 |
| `reporte` | 79 | 19 | S02, S03, S04, S07, S09, S11 |
| `asignarResponsable` | 52 | 15 | S04, S05, S06 |
| `cambiarEstado` | 43 | 15 | S03, S05, S06, S09 |
| `buscar` | 37 | 22 | S03, S06, S08, S09 |
| `cargar` / `guardar` | 53 / 14 | - | S05, S06, S07 |

### Plan de corrección (orden y justificación)

| Paso | Qué se paga | Por qué en ese orden |
|---|---|---|
| 1 | D01: red de seguridad con pruebas de caracterización | Sin pruebas, ningún refactoring es seguro |
| 2 | S03 y S04: tipos del dominio (enums) con sus reglas | Elimina 58 literales y deja la regla en un único lugar |
| 3 | S02: Extract Method sobre `registrarSolicitud`, `reporte` y `main` | Hace visible cada paso antes de moverlo |
| 4 | S09 y S10: Move Method a `Solicitud` y a los enums | Cada regla queda junto a los datos que usa |
| 5 | S08: Introduce Parameter Object en `buscar` y `registrarSolicitud` | Firma más clara y fácil de extender |
| 6 | S01 y S07: Extract Class + interfaces de repositorio + reloj inyectado | Separa capas y permite probar sin archivos ni consola |
| 7 | S05: excepciones de dominio en lugar de `println` + `return` | Corrige E1, E2 y E3 (cambios de comportamiento intencionales y documentados) |
| 8 | S06, S11, S12: en cada paso | Regla del Boy Scout: dejar el código mejor de lo que estaba |
| 9 | D02: README, decisiones (ADR) y documentación | Cierra la trazabilidad del trabajo |
