# Etapa 6 - Pruebas y verificación del comportamiento

## Estrategia

La pregunta a responder es: **¿el refactoring alteró negativamente lo que el usuario ve?** Se respondió en
cuatro niveles, cada uno con una herramienta distinta:

| Nivel | Qué verifica | Herramienta | Casos |
|---|---|---|---:|
| 1. Caracterización (golden master) | Que la versión actual se comporta **igual** que la inicial: pantalla, código de salida y los tres CSV | `PruebaDeCaracterizacionTest`: ejecuta ambas versiones como procesos con la misma entrada | 17 |
| 2. Cambios intencionales | Que los defectos E1, E2 y E3 se corrigieron, y cómo fallaba la versión inicial | `CambiosIntencionalesTest`: ejecuta ambas versiones y documenta la diferencia | 5 |
| 3. Unitarias (patrón AAA) | Cada regla por separado, sin archivos ni consola, con fecha fija | JUnit 5 sobre dominio, aplicación, persistencia y consola | 105 (+1 deshabilitada) |
| 4. Contrato (LSP) | Que las dos implementaciones del repositorio son intercambiables | `ContratoRepositorioSolicitudes`, heredada por la prueba CSV y la de memoria | incluidas en 3 |

Además, la integración continua ([.github/workflows/ci.yml](../.github/workflows/ci.yml)) corre todo en cada
push y pull request.

## Antes de refactorizar: la red de seguridad

1. Se congeló una copia literal de la versión inicial en `src/test/java/legacy` (solo cambia la línea `package`).
2. Se escribieron 17 escenarios que recorren todas las operaciones, sus validaciones y casos de borde.
3. Se comprobó que la red **detecta** cambios: al alterar el plazo de EXAMEN de 5 a 6 días, fallaron 3
   escenarios ([validación](evidencias/red-de-seguridad/validacion-de-la-red.md)).

## Casos de prueba de caracterización

Resultado esperado en todos: **IDÉNTICO** entre la versión inicial y la actual. Resultado obtenido: 17/17
IDÉNTICOS ([resumen](evidencias/pruebas/caracterizacion/resumen.txt); cada escenario tiene su archivo con la
salida completa en [evidencias/pruebas/caracterizacion](evidencias/pruebas/caracterizacion/)).

| Id | Escenario | Qué ejercita |
|---|---|---|
| CP-01 | Listar todas | Formato de línea, responsable vacío como "-" |
| CP-02 | Registrar certificado | Número consecutivo, prioridad y vencimiento por tipo, escritura del CSV |
| CP-03 | Registrar urgente en minúsculas | Tipo sin distinguir mayúsculas, prioridad elevada, plazo a la mitad, recorte de espacios |
| CP-04 | Urgente con plazo mínimo; "si" no es urgente | Mínimo de 1 día; solo "s" o "S" marcan urgencia |
| CP-05 | Errores de validación | Legajo inexistente, tipo inválido, descripción vacía, 201 caracteres (error) y 200 (válido) |
| CP-06 | Asignar responsable y errores | Área correcta, otra área, solicitud o responsable inexistente, solicitud cerrada, id en minúsculas |
| CP-07 | Flujo de estados completo | EN_PROCESO sin responsable, asignar, EN_PROCESO, RESUELTA, intento sobre cerrada |
| CP-08 | Transiciones inválidas | PENDIENTE→RESUELTA, estado desconocido, EN_PROCESO→PENDIENTE, espacios, cerrada, inexistente |
| CP-09 | Buscar por estado y tipo | Criterios sin distinguir mayúsculas |
| CP-10 | Búsqueda combinada | Estado + prioridad, legajo, solo vencidas ("s" y "S"), sin resultados, criterio con espacio final |
| CP-11 | Reporte | Totales por estado, tipo y área, vencidas, promedio con coma decimal |
| CP-12 | Flujo completo con reporte | Registrar, asignar, procesar, resolver, rechazar y reportar |
| CP-13 | Opciones inexistentes | 9, 7 y -1 |
| CP-14 | Sin solicitudes | Listado vacío, reporte "sin datos", búsqueda sin resultados |
| CP-15 | Sin archivo de solicitudes | Arranca vacío, el primer número es 1 y crea el archivo |
| CP-16 | Reasignar y cerrar una solicitud nueva | Reasignación permitida mientras está abierta; no después de cerrarla |
| CP-17 | Reporte con resueltas hoy | Promedio con resoluciones de 0 días |

### Ejecución en cada paso del refactoring

| Commit | Paso | Caracterización |
|---|---|---|
| `7fd69f1` | Red de seguridad | 17/17 |
| `34c0f44` | R1, R2 - Rename, Remove Dead Code, Extract Method | 17/17 |
| `5f1d26d` | R3, R4 - Enums con sus reglas | 17/17 |
| `fbe05ff` | R5 - Move Method, Encapsulate Field | 17/17 |
| `951ceef` | R6 - Introduce Parameter Object | 17/17 |
| `d609c88` | R7 - Replace Error Code with Exception | 17/17 |
| `d75a628` | R8 - Extract Class, Extract Interface, inyección de dependencias | 17/17 |
| `ae2ab70` | Corrección de E1, E2 y E3 | 17/17 + 5/5 cambios intencionales |

## Cambios de comportamiento intencionales

Son los únicos casos en que la salida cambia. Cada uno tiene su evidencia en
[evidencias/pruebas/cambios-intencionales](evidencias/pruebas/cambios-intencionales/).

| Id | Situación | Versión inicial | Versión actual |
|---|---|---|---|
| CI-1 | Letra en el menú (`abc`) | `NumberFormatException`, el programa se cierra (código 1) | "Opcion invalida" y el menú sigue |
| CI-2 | Letra como número de solicitud | Se cierra con excepción | "Error: el numero de solicitud debe ser un numero entero" |
| CI-3 | La entrada termina sin elegir "Salir" | `NoSuchElementException` (código 1) | Termina sin error |
| CI-4 | No se puede escribir `solicitudes.csv` | "Error al guardar" **y** "Solicitud #9 registrada"; la solicitud aparece en el listado aunque no se guardó | "Error al guardar: no se pudo escribir datos/solicitudes.csv. El cambio no quedo guardado." y no aparece |
| CI-5 | Línea mal formada en `solicitudes.csv` | Lista 3 de 8 solicitudes; al registrar otra **reescribe el archivo, borra las #4 a #8 y reutiliza el número #4** | Avisa la línea ignorada, lista las 8 y asigna el #9 |

### Comparación de las demos antes y después

Las tres ejecuciones de [entradas](evidencias/entradas/) se corrieron sobre ambas versiones:

| Demo | Diferencias |
|---|---|
| Recorrido funcional | Ninguna |
| Validaciones | Ninguna |
| Entrada no numérica | Solo la esperada: antes excepción y código 1; ahora "Opcion invalida" y salida normal |

## Pruebas unitarias

Escritas con el patrón **Arrange - Act - Assert** y nombres que describen la regla. Las reglas de fecha usan
un `Clock` fijo (7/11/2026), así que el resultado no depende del día en que se corren.

| Clase de prueba | Qué verifica |
|---|---|
| `TipoSolicitudTest` | Prioridad, plazo y área de los 5 tipos, normal y urgente (tabla de 10 filas); plazo mínimo; interpretación del texto |
| `EstadoSolicitudTest` | Las 16 combinaciones de transición; abiertas y cerradas; destinos posibles |
| `PrioridadYAreaTest` | Elevar prioridad; etiquetas de área; pertenencia del responsable |
| `PedidoTest` | Descripción obligatoria (nula, vacía, espacios), límite de 200, recorte |
| `SolicitudTest` | Registro, restauración, asignación (área y cerrada), cambio de estado, fecha de cierre, días de resolución, bordes del vencimiento (vence hoy / ayer) |
| `FiltroSolicitudesTest` | Cada criterio, combinación, legajo exacto, criterio nulo |
| `GeneradorDeReporteTest` | Ceros sin datos; conteos por estado, tipo y área; promedio solo de resueltas |
| `ServicioSolicitudesTest` | Número consecutivo, orden de validaciones, errores de inexistencia, estado desconocido, búsqueda con la fecha del reloj |
| `ContratoRepositorioSolicitudes` (CSV y memoria) | Vacío, orden, próximo número, reemplazo sin duplicar, lista no modificable |
| `RepositorioSolicitudesCsvTest` | Ida y vuelta con el formato original, líneas mal formadas, fallo de escritura |
| `RepositoriosDeConsultaCsvTest` | Legajo exacto, id sin mayúsculas, área desconocida, archivo inexistente |
| `MenuConsolaTest` | Flujo completo, prefijo "Error:", entrada no numérica, fin de entrada, fallo de guardado, reporte |

## Resultados

Detalle por clase y cobertura por clase en
[resumen-pruebas-y-cobertura.md](evidencias/pruebas/resumen-pruebas-y-cobertura.md).

| Indicador | Versión inicial | Versión actual |
|---|---:|---:|
| Pruebas automatizadas | 0 | 128 (127 pasan, 1 deshabilitada) |
| Cobertura de líneas | 0% | 95% |
| Cobertura de ramas | 0% | 98% |

La clase sin cobertura medida es `App` (raíz de composición, 15 líneas): se ejecuta en la caracterización, pero
como proceso separado, al que JaCoCo no llega.

### Hallazgo de las pruebas unitarias

Al probar líneas mal formadas, la fecha `31/02/2026` **no** se rechazó: el formato `dd/MM/yyyy` de Java la
ajusta en silencio a `28/02/2026`. La versión inicial hace lo mismo. Se registró como observación **CR-03** del
code review y la prueba `rechazaFechasQueNoExisten` quedó deshabilitada con esa referencia hasta su corrección
(ver [06-code-review](06-code-review/revision.md)).

## Cómo reproducir

```bash
./mvnw verify                    # 128 pruebas + reporte de cobertura en target/site/jacoco/index.html
scripts/generar_evidencias.sh    # regenera todo docs/evidencias
```
