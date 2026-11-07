# Revisión técnica del refactoring

| Dato | Valor |
|---|---|
| Cambio revisado | `etapa-diagnostico..etapa-pruebas` (red de seguridad, R1 a R8, corrección de E1 a E3 y pruebas) |
| Autor de los cambios | Nicolás Colman |
| Revisores | Luis Cisneros (bloques A y B) y Santiago Caiciia Massello (bloques C, D y E); bloque F entre los tres |
| Checklist | [checklist.md](checklist.md) |
| Herramientas | `./mvnw verify` (JUnit 5 + JaCoCo), PMD 7 con [reglas de diseño](../../config/pmd/reglas-diseno.xml), lectura del diff |
| Estado | **Aprobada** después de las correcciones (ver "Cierre de la revisión") |

## Resultado del checklist

| Id | Resultado | Evidencia |
|---|---|---|
| A1 SRP | Cumple | Una razón de cambio por clase: `ServicioSolicitudes` (casos de uso), `GeneradorDeReporte` (cálculo), `FormatoConsola` (textos), `RepositorioSolicitudesCsv` (archivo) |
| A2 OCP | Cumple | Un tipo nuevo se agrega solo en `TipoSolicitud`: el menú y el reporte recorren `values()` |
| A3 LSP | Cumple | `ContratoRepositorioSolicitudes` pasa con la implementación CSV y con la de memoria |
| A4 ISP | Cumple | El reporte depende de `ConsultaSolicitudes` (un método); estudiantes y responsables, de interfaces de un método |
| A5 DIP | Cumple | `Clock`, repositorios y `PrintStream` se reciben por constructor; solo `App` crea implementaciones |
| A6 Dependencias | Cumple | El paquete `dominio` no importa ningún otro paquete del proyecto |
| B1 Nombres | Cumple | PMD `ShortVariable`: 0 (eran 31) |
| B2 Funciones pequeñas | Cumple | Método más largo: 20 líneas; complejidad máxima: 9 (eran 101 y 29) |
| B3 Literales | **No cumple** | PMD `AvoidDuplicateLiterals`: "Solicitud #" 3 veces → CR-01 |
| B4 Código muerto | Cumple | PMD `UnusedPrivateMethod`: 0 |
| B5 Comentarios | Cumple | Javadoc de contrato y del porqué (por ejemplo, formato de fecha y locale) |
| B6 DRY | **Parcial** | Las opciones del menú se escriben dos veces → CR-04 |
| C1 Catch | Cumple | PMD `EmptyCatchBlock` y `AvoidCatchingGenericException`: 0 (eran 7) |
| C2 Excepciones de dominio | Cumple | `ReglaDeNegocioException` y `ErrorDePersistencia`; textos solo en `consola` |
| C3 Entradas inválidas | Cumple | `CambiosIntencionalesTest` (E1) y `MenuConsolaTest` |
| C4 Fallos de E/S | **Parcial** | Registrar es correcto; en asignar y cambiar estado el cambio queda en memoria si falla el guardado → CR-08 |
| D1 Suite | Cumple | 128 pruebas, 0 fallas, 1 deshabilitada (CR-03) |
| D2 Caracterización | Cumple | 17/17 IDÉNTICOS |
| D3 Reglas probadas | Cumple | Ver [05-pruebas.md](../05-pruebas.md) |
| D4 Cobertura | Cumple | 95% de líneas, 98% de ramas |
| D5 Determinismo | Cumple | Reloj fijo y `@TempDir` |
| E1 Compatibilidad | Cumple | La caracterización compara los CSV byte a byte; prueba de ida y vuelta del repositorio |
| E2 Datos sintéticos | Cumple | Nombres y correos inventados (`@ejemplo.edu`) |
| E3 Validación de lo leído | **No cumple** | CR-02 (punto y coma en la descripción) y CR-03 (fechas imposibles) |
| F1 Commits | Cumple | Conventional commits con referencia a smell o refactoring |
| F2 Tags | **Parcial** | Faltan `etapa-docs` y `estado-refactorizado` → CR-09 |
| F3 README | **No cumple** | README de 6 líneas (deuda D02) → CR-09 |
| F4 ADR | **No cumple** | Decisiones sin registrar → CR-09 |
| F5 CI | Cumple | `.github/workflows/ci.yml` |

PMD sobre el código revisado: **6 hallazgos** (eran 154). Se tratan en CR-01, CR-04, CR-05 y CR-06.

## Observaciones

| Id | Severidad | Ítem | Dónde | Observación y por qué importa | Propuesta | Estado |
|---|---|---|---|---|---|---|
| CR-01 | Menor | B3 | `MenuConsola` l. 93, 101, 110 | El texto "Solicitud #" está repetido 3 veces (PMD). Si cambia el formato del número, hay que acordarse de los tres | Extraer un método que arme la referencia a la solicitud | Corregida en `23efc13` |
| CR-02 | **Mayor** | E3 | `ArchivoCsv.escribir` l. 63 | Los campos se unen con `;` sin escapar. Una descripción con `;` genera una línea de 11 columnas. La versión inicial la vuelve a leer corrida (prioridad "b", estado "MEDIA", responsable "PENDIENTE") y la actual la descarta: en los dos casos **se pierde la solicitud**. El defecto viene de la versión inicial | Rechazar `;` en la descripción con un mensaje claro, como regla de `Pedido`. Se prefiere a escapar el campo porque no cambia el formato del archivo | Corregida en `a09eaa7` |
| CR-03 | **Mayor** | E3 | `RepositorioSolicitudesCsv` l. 28 | `dd/MM/yyyy` usa el modo SMART de Java: `31/02/2026` se lee como `28/02/2026` sin aviso. Lo detectó la prueba `rechazaFechasQueNoExisten`, que quedó deshabilitada | Formato `dd/MM/uuuu` con `ResolverStyle.STRICT`; habilitar la prueba | Corregida en `2a8fe39` |
| CR-04 | Menor | B6 | `MenuConsola.mostrarMenu` l. 54 y `ejecutar` l. 67 | El texto del menú y el `switch` son dos listas que deben coincidir; una opción nueva obliga a tocar las dos. Además, `ejecutar` tiene complejidad 9 (PMD) | Una tabla de opciones (número, texto, acción) que genere el menú y resuelva la opción elegida | Corregida en `23efc13` |
| CR-05 | Sugerencia | B2 | `Solicitud.cambiarEstado` l. 62 | La condición `PENDIENTE && EN_PROCESO && !tieneResponsable()` se entiende, pero no dice qué regla expresa (complejidad 8, PMD) | Extraer `requiereResponsable(destino)` | Corregida en `23efc13` |
| CR-06 | Menor | PMD | `Solicitud`, `MenuConsola`, `RepositorioSolicitudesCsv` | `TooManyMethods` (límite configurado: 8) | Revisar si alguna clase tiene más de una responsabilidad | Aceptada (ver justificación) |
| CR-07 | Sugerencia | - | `MenuConsola` l. 92 | "si" o "SI" no marcan una solicitud como urgente: solo "s" o "S". Se conservó a propósito (escenario CP-04) | Decidirlo con quien usa el sistema; cambiarlo altera el comportamiento | Aceptada como deuda |
| CR-08 | **Mayor** | C4 | `ServicioSolicitudes` l. 53 y 65 | La solicitud se modifica antes de guardar. Si el guardado falla, la consola informa el error pero el cambio queda aplicado en memoria y se ve en los listados hasta reiniciar | Que los repositorios devuelvan copias: los cambios se aplican recién cuando el guardado tiene éxito. Agregarlo al contrato (LSP) | Corregida en `7e4ed12` |
| CR-09 | Menor | F2, F3, F4 | `README.md`, `docs/` | README sin instalación, uso ni estructura; decisiones sin ADR; faltan tags finales | Completar README, ADR y tags en la etapa de documentación | Corregida en el commit del tag `etapa-docs` |
| CR-10 | Sugerencia | - | `FormatoConsola` l. 58 | El promedio usa el idioma de la máquina: "3,0" en es-AR y "3.0" en en-US. Se conservó porque así funciona la versión inicial | Definir un idioma fijo es una decisión de producto | Aceptada como deuda |
| CR-11 | Sugerencia | D4 | `App` | La raíz de composición no tiene cobertura medida (se ejecuta solo en la caracterización, en otro proceso) | Pasar el agente de JaCoCo a los procesos de la caracterización | Aceptada como deuda |

## Lo que está bien (para mantener)

- **P1.** Las capas tienen límites claros y las dependencias apuntan al dominio: se puede probar cada regla sin
  archivos, consola ni fecha real.
- **P2.** La red de seguridad se validó antes de usarla (se le inyectó un error y lo detectó).
- **P3.** Commits chicos, uno por técnica, con la caracterización en verde en cada uno: el historial cuenta la
  historia del refactoring.
- **P4.** Los cambios de comportamiento (E1 a E3) están separados de los refactorings y tienen su propia prueba.

## Cierre de la revisión

### Ítems que no cumplían, después de las correcciones

| Id | Antes | Después | Evidencia |
|---|---|---|---|
| B3 Literales | No cumple | Cumple | PMD `AvoidDuplicateLiterals`: 0 |
| B6 DRY | Parcial | Cumple | El menú se genera desde una tabla de opciones |
| C4 Fallos de E/S | Parcial | Cumple | Contrato del repositorio: entrega copias; `ServicioSolicitudesTest.siFallaElGuardadoNoQuedaElCambio` |
| E3 Validación de lo leído | No cumple | Cumple | `PedidoTest.rechazaPuntoYComa`, `RepositorioSolicitudesCsvTest.rechazaFechasQueNoExisten`, `CambiosIntencionalesTest.descripcionConPuntoYComa` |
| F2 Tags | Parcial | Cumple | `etapa-docs` y `estado-refactorizado` |
| F3 README | No cumple | Cumple | README con descripción, instalación, uso, funcionalidades, tecnologías y estructura |
| F4 ADR | No cumple | Cumple | `docs/decisiones/ADR-001` a `ADR-006` |

### Justificación de las observaciones aceptadas

- **CR-06 (`TooManyMethods`).** `Solicitud` tiene 21 métodos: 9 son accesos de lectura (`numero()`, `estado()`,
  ...) y el resto son reglas de la misma entidad; separarlos dispersaría lógica cohesiva. `MenuConsola` tiene un
  método privado por opción del menú y `RepositorioSolicitudesCsv`, funciones privadas de conversión de una
  línea. Se mantiene la regla en PMD con límite 8 como **alarma**: un método nuevo en estas clases obliga a
  repensar si sigue siendo una sola responsabilidad.
- **CR-07 y CR-10.** Corregirlas cambia lo que ve el usuario sin que nadie lo haya pedido. Quedan en "Mejoras
  futuras" de las conclusiones, para decidirlas con quien usa el sistema.
- **CR-11.** `App` solo conecta piezas y se ejecuta en los 17 escenarios de caracterización; medir su cobertura
  requiere instrumentar procesos externos. Queda como mejora futura.

### Criterios de aceptación (Definition of Done)

| Criterio | Resultado |
|---|---|
| Sin observaciones bloqueantes ni mayores abiertas | Cumple: las 3 mayores (CR-02, CR-03, CR-08) están corregidas |
| `./mvnw verify` en verde y caracterización 17/17 | Cumple: 133 pruebas, 0 fallas, 0 deshabilitadas |
| Cobertura de líneas ≥ 80% | Cumple: 95% |
| Hallazgos de PMD corregidos o justificados | Cumple: 154 → 3, los 3 justificados (CR-06) |
| Menores y sugerencias corregidas o registradas | Cumple: 4 corregidas, 4 aceptadas con motivo |

