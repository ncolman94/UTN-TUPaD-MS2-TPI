# Etapa 1 - Sistema base

## Dominio y alcance

**Gestor de Solicitudes Académicas** es una aplicación de consola que usa el personal de Bedelía y de
Secretaría Académica para registrar y seguir los pedidos que hacen los estudiantes: certificados,
constancias de examen, equivalencias, revisiones de nota y bajas de materias.

El trabajo toma el sistema completo como **módulo acotado**: no se agregan funcionalidades nuevas. El
objetivo es diagnosticar su calidad interna y mejorarla sin alterar negativamente lo que el usuario ve.

Todos los datos son **sintéticos** (nombres, legajos y correos inventados).

## Actores

| Actor | Qué hace en el sistema |
|---|---|
| Operador administrativo (Bedelía o Secretaría Académica) | Usa la consola: registra solicitudes, asigna responsables, cambia estados, busca y saca el reporte |
| Estudiante | Origina la solicitud. No usa el sistema: el operador la carga con su legajo |
| Responsable de área | Persona a la que se asigna la solicitud; debe pertenecer al área que corresponde al tipo |

## Entidades

| Entidad | Datos principales |
|---|---|
| Solicitud | número, legajo, tipo, descripción, prioridad, estado, responsable, fecha de alta, vencimiento y cierre |
| Estudiante | legajo, nombre, carrera, correo |
| Responsable | identificador, nombre, área |
| Estado / Tipo / Prioridad | conceptos del dominio (en la versión inicial, simples textos) |

## Funcionalidades (operaciones verificables)

| # | Operación | Regla principal |
|---|---|---|
| 1 | Registrar solicitud | El legajo debe existir, el tipo ser válido y la descripción tener entre 1 y 200 caracteres. La prioridad y el vencimiento se calculan según el tipo |
| 2 | Asignar responsable | El responsable debe existir y pertenecer al área del tipo de solicitud; no se asignan solicitudes cerradas |
| 3 | Cambiar estado | Solo se permiten las transiciones del diagrama de estados; para pasar a EN_PROCESO hace falta responsable |
| 4 | Buscar solicitudes | Filtros combinables por estado, tipo, legajo, prioridad y "solo vencidas" |
| 5 | Reporte | Totales por estado, tipo y área, vencidas sin resolver y tiempo promedio de resolución |
| 6 | Listar todas | Una línea por solicitud |

### Reglas por tipo de solicitud

| Tipo | Prioridad base | Plazo (días) | Área |
|---|---|---:|---|
| CERTIFICADO | BAJA | 3 | BEDELIA |
| EXAMEN | MEDIA | 5 | BEDELIA |
| EQUIVALENCIA | MEDIA | 30 | SECRETARIA ACADEMICA |
| REVISION | ALTA | 10 | SECRETARIA ACADEMICA |
| BAJA | ALTA | 7 | BEDELIA |

Si la solicitud es **urgente**, la prioridad sube un nivel (BAJA → MEDIA → ALTA) y el plazo se reduce a la
mitad, con un mínimo de 1 día.

### Estados

```
PENDIENTE ──(con responsable)──> EN_PROCESO ──> RESUELTA
    │                                │
    └──────────> RECHAZADA <─────────┘
```

RESUELTA y RECHAZADA son estados finales. Una solicitud está **vencida** si sigue abierta (PENDIENTE o
EN_PROCESO) y su fecha de vencimiento ya pasó.

## Tecnología

| Elemento | Uso |
|---|---|
| Java 17 (probado con OpenJDK 21) | Lenguaje |
| Maven 3.9 con Maven Wrapper (`./mvnw`) | Compilación y empaquetado; no hace falta instalar Maven |
| Archivos CSV en `datos/` | Persistencia (`estudiantes.csv`, `responsables.csv`, `solicitudes.csv`) |
| Git y GitHub | Versionado, tags por etapa |
| PMD 7 (maven-pmd-plugin) | Análisis estático para respaldar el diagnóstico |

## Requisitos mínimos de la consigna

| Requisito | Cómo se cumple |
|---|---|
| Al menos 3 entidades | Solicitud, Estudiante, Responsable (más Estado, Tipo y Prioridad como conceptos) |
| Al menos 4 operaciones verificables | 6 operaciones |
| Persistencia | Archivos CSV |
| Versión inicial con problemas reales | Tag `estado-inicial`; diagnóstico en [02-diagnostico.md](02-diagnostico.md) |
| Demostrar que se conserva el comportamiento | Pruebas de caracterización (etapa 6) |

## Evidencia de funcionamiento (versión inicial)

Las ejecuciones se generan con `scripts/ejecutar_demo.sh`, que corre el programa con una entrada fija
sobre una copia de los datos:

- [Recorrido funcional](evidencias/estado-inicial/recorrido-funcional.txt): listar, registrar urgente,
  asignar, pasar a EN_PROCESO, buscar, reporte.
- [Validaciones](evidencias/estado-inicial/validaciones.txt): legajo inexistente, tipo inválido,
  descripción vacía, responsable de otra área, transiciones no permitidas, opción inexistente.
- [Entrada no numérica](evidencias/estado-inicial/entrada-no-numerica.txt): defecto externo, el programa
  se cierra con una excepción.

Para reproducirlas sobre la versión inicial sin cambiar de rama:

```bash
git worktree add ../inicial estado-inicial
(cd ../inicial && ./mvnw -q package)
scripts/ejecutar_demo.sh docs/evidencias/entradas/recorrido-funcional.txt /tmp/salida.txt ../inicial/target/gestor-solicitudes.jar
```
