# Gestor de Solicitudes Académicas

Trabajo Práctico Integrador de **Metodología de Sistemas II** (Tecnicatura Universitaria en Programación a
Distancia, UTN): *evolución profesional de un sistema existente: calidad, deuda técnica, refactoring y revisión
técnica*.

| | |
|---|---|
| Comisión | 11 |
| Docente | Naveda, Claudia |
| Integrantes | Cisneros, Luis ([luiscisneros356](https://github.com/luiscisneros356)) · Caiciia Massello, Santiago ([scaiciia](https://github.com/scaiciia)) · Colman, Nicolás Alberto ([ncolman94](https://github.com/ncolman94)) |
| Forma de trabajo | Sesión conjunta en la PC de Nicolás. El autor de cada commit es el integrante a cargo de esa parte y los otros dos figuran como coautores (`Co-authored-by`) |
| Video | [agregar enlace al video] |

## Descripción

Aplicación de consola que usa el personal de Bedelía y de Secretaría Académica para registrar y seguir las
solicitudes de los estudiantes (certificados, constancias de examen, equivalencias, revisiones de nota y bajas).
El repositorio contiene **la versión inicial**, con deuda técnica real, y **la versión refactorizada**, que conserva el
comportamiento externo. Entre las dos, cada paso queda registrado en el historial.

Todos los datos son **sintéticos**.

## Funcionalidades

1. **Registrar solicitud**: valida el legajo, el tipo y la descripción; calcula prioridad y vencimiento según el
   tipo (las urgentes suben un nivel de prioridad y tienen la mitad del plazo).
2. **Asignar responsable**: solo de su área (Bedelía o Secretaría Académica) y en solicitudes abiertas.
3. **Cambiar estado**: `PENDIENTE → EN_PROCESO → RESUELTA`, o `RECHAZADA` desde los dos primeros; para pasar a
   EN_PROCESO hace falta responsable.
4. **Buscar**: por estado, tipo, legajo, prioridad y "solo vencidas", combinables.
5. **Reporte**: totales por estado, tipo y área, vencidas sin resolver y promedio de días de resolución.
6. **Listar todas**.

## Tecnologías

Java 17 · Maven 3.9 (con Maven Wrapper) · JUnit 5 · JaCoCo · PMD 7 · Git y GitHub · GitHub Actions ·
Python 3 (scripts de evidencias, sin dependencias externas). Persistencia en archivos CSV (`datos/`).

## Instalación y uso

Requisito: **Java 17 o superior**. No hace falta instalar Maven: `./mvnw` lo descarga la primera vez
(en Windows, `mvnw.cmd`).

```bash
./mvnw package                        # compila y genera target/gestor-solicitudes.jar
java -jar target/gestor-solicitudes.jar
```

El programa trabaja con la carpeta `datos/` del directorio actual. Para volver a los datos de ejemplo:
`git checkout datos/`.

## Pruebas y evidencias

```bash
./mvnw verify                         # 133 pruebas + cobertura en target/site/jacoco/index.html
./mvnw pmd:pmd                        # análisis estático en target/reports/pmd.html
python3 scripts/metricas.py --ref estado-inicial   # métricas de cualquier versión
scripts/generar_evidencias.sh         # regenera todo docs/evidencias
```

| Indicador | `estado-inicial` | `estado-refactorizado` |
|---|---:|---:|
| Hallazgos PMD (reglas de diseño) | 154 | 3 (justificados) |
| Método más largo (líneas) | 101 | 20 |
| Complejidad ciclomática máxima | 29 | 7 |
| Lugares a tocar para agregar un tipo | 8 | 1 |
| Pruebas automatizadas | 0 | 133 |
| Cobertura de líneas | 0% | 95% |
| Escenarios de caracterización idénticos | - | 17/17 |

## Versiones (tags)

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base con deuda técnica (versión 1.0.0) |
| `etapa-diagnostico` | Diagnóstico de calidad, smells, PMD y métricas |
| `etapa-refactoring` | Red de seguridad, refactorings R1 a R8 y corrección de E1 a E3 |
| `etapa-pruebas` | Pruebas unitarias, de contrato y evidencias |
| `etapa-docs` | Revisión de código aplicada y documentación completa |
| `estado-refactorizado` | Estado final (versión 1.0.1). También existe el alias `estadorefactorizado`, como figura escrito en la consigna |

```bash
git log --oneline --decorate          # historia completa, un commit por técnica
git diff estado-inicial estado-refactorizado -- src/main
```

## Estructura

```
src/main/java/ar/edu/utn/tupad/solicitudes/
├── App.java                  raíz de composición (arma las dependencias)
├── dominio/                  reglas: Solicitud, TipoSolicitud, EstadoSolicitud, Pedido, interfaces de repositorio
├── aplicacion/               casos de uso (ServicioSolicitudes), reporte, filtro
├── persistencia/             archivos CSV y repositorio en memoria
└── consola/                  menú y textos de pantalla
src/test/java/
├── legacy/                   copia congelada de la versión inicial (para comparar)
└── ar/edu/utn/tupad/solicitudes/
    ├── caracterizacion/      golden master y cambios intencionales
    └── dominio/ aplicacion/ persistencia/ consola/   pruebas unitarias y de contrato
config/pmd/                   reglas de diseño de PMD
scripts/                      métricas, resúmenes y generación de evidencias
datos/                        archivos CSV de ejemplo (sintéticos)
docs/                         documentación del TPI
```

## Documentación

| Etapa | Documento |
|---|---|
| 1. Sistema base | [docs/01-sistema-base.md](docs/01-sistema-base.md) |
| 2 y 3. Calidad, deuda técnica y code smells | [docs/02-diagnostico.md](docs/02-diagnostico.md) |
| 4. SOLID y Clean Code | [docs/03-solid-clean-code.md](docs/03-solid-clean-code.md) |
| 5. Refactoring seguro | [docs/04-refactorings.md](docs/04-refactorings.md) |
| 6. Pruebas y verificación | [docs/05-pruebas.md](docs/05-pruebas.md) |
| 7. Code review | [checklist](docs/06-code-review/checklist.md) · [revisión](docs/06-code-review/revision.md) |
| 8. Conclusiones | [docs/07-conclusiones.md](docs/07-conclusiones.md) |
| Decisiones técnicas | [docs/decisiones](docs/decisiones/) (ADR-001 a ADR-006) |
| Evidencias | [docs/evidencias](docs/evidencias/) |
| Uso de IA | [docs/uso-de-ia.md](docs/uso-de-ia.md) |
| Cómo contribuir | [CONTRIBUTING.md](CONTRIBUTING.md) |

## Uso de inteligencia artificial

Se usó Claude Code (Anthropic) como asistente, con declaración, prompts y errores detectados en
[docs/uso-de-ia.md](docs/uso-de-ia.md). El equipo es responsable de todo lo entregado.
