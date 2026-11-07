# Etapa 5 - Refactoring seguro

## Cómo se trabajó

1. **Red de seguridad primero** (commit `7fd69f1`): 17 escenarios de caracterización que comparan la versión
   inicial congelada con la actual. Se validó que detectan un cambio (ver [05-pruebas.md](05-pruebas.md)).
2. **Un commit por técnica**, en pasos chicos. Después de cada uno: `./mvnw verify` y caracterización 17/17.
3. **Refactorizar y corregir van por separado.** Los refactorings no cambian el comportamiento; los defectos
   E1 a E3 se corrigieron en un commit `fix` aparte, con sus propias pruebas.
4. Se siguió el orden del plan del diagnóstico: primero lo barato y seguro (nombres, extraer métodos), después
   lo estructural (mover responsabilidades, separar capas).

## Resumen

Se aplicaron **8 refactorings** del catálogo de Fowler (R1 a R8) en 6 commits, y otros 3 surgieron de la
revisión (CR-01, CR-04 y CR-05, en un commit). Están los tres que pide la consigna: **Extract Method (R2)**,
**Move Method (R5)** e **Introduce Parameter Object (R6)**.

| Id | Refactoring | Smells que ataca | Principio | Commit | Caracterización |
|---|---|---|---|---|---|
| R1 | Rename Variable / Remove Dead Code | S11, S12 | Clean Code: nombres | `34c0f44` | 17/17 |
| R2 | **Extract Method** | S02, S06 | Clean Code: funciones | `34c0f44` | 17/17 |
| R3 | Replace Type Code with Enum (Replace Primitive with Object) | S03 | - | `5f1d26d` | 17/17 |
| R4 | Replace Conditional with Polymorphism | S04 | OCP | `5f1d26d` | 17/17 |
| R5 | **Move Method** + Encapsulate Field | S09, S10 | SRP | `fbe05ff` | 17/17 |
| R6 | **Introduce Parameter Object** | S08 | Clean Code: argumentos | `951ceef` | 17/17 |
| R7 | Replace Error Code with Exception | S05 | Clean Code: errores | `d609c88` | 17/17 |
| R8 | Extract Class + Extract Interface + inyección de dependencias | S01, S07 | SRP, ISP, DIP, LSP | `d75a628` | 17/17 |
| CR-04 | Replace Conditional with table (menú) | duplicación de la revisión | OCP, DRY | `23efc13` | 17/17 |

Para ver cada paso: `git show <commit>`. Para comparar inicio y fin: `git diff estado-inicial estado-refactorizado -- src/main`.

---

## R1 - Rename Variable y Remove Dead Code

**Problema (S11, S12).** Nombres de una letra y nombres engañosos; un método sin uso y código comentado.

```java
// Antes
GestorSolicitudes g = new GestorSolicitudes();
String p = "";            // prioridad
boolean va = true;        // "pasa el filtro"
int cerradas = 0;         // ...pero cuenta solo las RESUELTAS
// ya no se usa
private boolean esUrgente(String prioridad) { ... }
```

**Después.** `servicio`, `prioridad`, `encontradas`, `diasDeResolucion()`; se borraron `esUrgente` y el código
comentado (el historial de git los conserva). Se agregaron constantes con nombre para rutas, separador, largo
máximo y formato de fecha.

## R2 - Extract Method

**Problema (S02).** `registrarSolicitud` tenía 101 líneas y complejidad 29: validaba, calculaba prioridad,
calculaba plazo, buscaba el próximo número, armaba el objeto, guardaba e imprimía.

**Mecánica.** Cada bloque con un comentario del tipo "// calculo la prioridad" se convirtió en un método con ese
nombre, y el comentario sobró.

```java
// Después (commit 34c0f44): el método se lee como la secuencia de pasos de la regla
public void registrarSolicitud(String legajo, String tipoIngresado, String descripcion, boolean urgente) {
    String tipo = tipoIngresado.toUpperCase().trim();
    String error = validarNuevaSolicitud(legajo, tipo, descripcion);
    ...
    solicitud.id = proximoNumero();
    solicitud.prioridad = calcularPrioridad(tipo, urgente);
    solicitud.fechaVto = hoy.plusDays(calcularPlazoEnDias(tipo, urgente)).format(FORMATO_FECHA);
    ...
}
```

También se extrajeron `buscarSolicitud`, que estaba duplicado en dos métodos, `formatearLinea`, duplicado en
`buscar` y `listar`, y un método por opción del menú en `App`.

**Efecto lateral útil.** Al extraer el filtro apareció `cumpleFiltro` con **7 parámetros**: el Extract Method dejó
a la vista un grupo de datos que viajan juntos, y eso motivó R6.

## R3 - Replace Type Code with Enum

**Problema (S03).** Tipo, estado y prioridad eran `String` (58 literales repetidos). Un error de tipeo
(`"PENDENTE"`) compila y falla en silencio.

```java
// Antes
public String tipo;      // "CERTIFICADO", "EXAMEN", ...
public String estado;    // "PENDIENTE", "EN_PROCESO", ...
public String fechaVto;  // "dd/MM/yyyy", se vuelve a parsear para comparar
```

```java
// Después
private final TipoSolicitud tipo;
private EstadoSolicitud estado;
private final LocalDate fechaDeVencimiento;  // se formatea solo al mostrar o guardar
```

## R4 - Replace Conditional with Polymorphism

**Problema (S04, OCP).** Tres `switch` sobre el tipo (prioridad, plazo y área) más listas de tipos en el reporte.

```java
// Antes: tres switch como este, en dos métodos distintos
switch (tipo) {
    case "CERTIFICADO": dias = 3; break;
    case "EXAMEN": dias = 5; break;
    case "EQUIVALENCIA": dias = 30; break;
    ...
}
```

```java
// Después: cada constante trae sus reglas
CERTIFICADO(Prioridad.BAJA, 3, Area.BEDELIA),
EXAMEN(Prioridad.MEDIA, 5, Area.BEDELIA),
...
public int plazoEnDias(boolean urgente) {
    return urgente ? Math.max(PLAZO_MINIMO_EN_DIAS, plazoEnDias / 2) : plazoEnDias;
}
```

Las transiciones de estado pasaron a `EstadoSolicitud.puedePasarA`, que reemplazó un `if` anidado de tres
niveles.

## R5 - Move Method + Encapsulate Field

**Problema (S09 Feature Envy, S10 Data Class).** El gestor calculaba el vencimiento, los días de resolución y las
transiciones leyendo los campos públicos de `Solicitud`. La regla de "vencida" estaba escrita dos veces.

```java
// Antes, en GestorSolicitudes.buscar y otra vez en GestorSolicitudes.reporte
LocalDate vto = LocalDate.parse(s.fechaVto, f);
if (!(vto.isBefore(hoy) && (s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO")))) { ... }
```

```java
// Después, en Solicitud: el comportamiento está junto a los datos que usa
public boolean estaVencida(LocalDate hoy) {
    return estado.estaAbierta() && fechaDeVencimiento.isBefore(hoy);
}
```

También se movieron `asignar`, `cambiarEstado`, `diasDeResolucion`, el armado de una solicitud nueva
(`Solicitud.registrar`) y `Responsable.perteneceA`. Los campos pasaron a privados: ya no se puede poner un estado
inválido desde afuera.

## R6 - Introduce Parameter Object

**Problema (S08).** Cuatro `String` seguidos, fáciles de invertir sin que el compilador avise, y un `boolean`
que cambia el comportamiento (argumento bandera).

```java
// Antes
public void buscar(String estado, String tipo, String legajo, String prioridad, boolean soloVencidas)
gestor.buscar(es, ti, le, pr, sv.equalsIgnoreCase("s"));
```

```java
// Después
public List<Solicitud> buscar(FiltroSolicitudes filtro)

FiltroSolicitudes filtro = FiltroSolicitudes.sinCriterios()
        .conEstado(preguntar("Estado (enter para omitir): "))
        .conTipo(preguntar("Tipo (enter para omitir): "))
        .conLegajo(preguntar("Legajo (enter para omitir): "))
        .conPrioridad(preguntar("Prioridad (enter para omitir): "))
        .conSoloVencidas(preguntar("Solo vencidas (s/n): ").equalsIgnoreCase(RESPUESTA_SI));
```

El objeto parámetro no es solo un contenedor: absorbió la lógica de filtrado (`admite`), así que la regla de
cada criterio está en un único lugar. Con el mismo criterio, `registrarSolicitud` pasó a recibir
`DatosNuevaSolicitud`.

## R7 - Replace Error Code with Exception

**Problema (S05).** Cada error se informaba con `println` + `return`. Las reglas no se podían probar ni reutilizar
sin consola.

```java
// Antes (dentro de la lógica)
if (!r.area.equals(area)) {
    System.out.println("Error: " + r.nombre + " no pertenece al area " + area);
    return;
}
```

```java
// Después: el dominio protege su regla...
if (!responsable.perteneceA(areaRequerida)) {
    throw new ReglaDeNegocioException(responsable.nombre() + " no pertenece al area " + areaRequerida.etiqueta());
}
// ...y solo la consola decide cómo mostrarla
} catch (ReglaDeNegocioException e) {
    salida.println("Error: " + e.getMessage());
}
```

Los mensajes quedaron idénticos: la caracterización lo verificó.

## R8 - Extract Class + Extract Interface + inyección de dependencias

**Problema (S01, S07).** Una clase de 401 líneas con todas las responsabilidades, que además creaba sus
dependencias (archivos, reloj, consola).

**Mecánica.**
1. Extract Class de la persistencia (`ArchivoCsv` y repositorios), del reporte (`GeneradorDeReporte`) y de la
   consola (`MenuConsola`, `FormatoConsola`).
2. Rename Class: lo que quedó del gestor son los casos de uso, por eso pasó a llamarse `ServicioSolicitudes`.
3. Extract Interface: `RepositorioSolicitudes`, `ConsultaSolicitudes`, `RepositorioEstudiantes` y
   `RepositorioResponsables`.
4. Inyección por constructor de repositorios, `Clock` y `PrintStream`; `App` quedó como raíz de composición.

```java
// Antes
BufferedReader br = new BufferedReader(new FileReader("datos/solicitudes.csv"));
LocalDate hoy = LocalDate.now();
System.out.println(...);
```

```java
// Después: App es el único lugar que conoce las implementaciones concretas
Clock reloj = Clock.systemDefaultZone();
RepositorioSolicitudesCsv solicitudes =
        new RepositorioSolicitudesCsv(CARPETA_DE_DATOS.resolve("solicitudes.csv"), avisos);
ServicioSolicitudes servicio = new ServicioSolicitudes(solicitudes,
        new RepositorioEstudiantesCsv(CARPETA_DE_DATOS.resolve("estudiantes.csv"), avisos),
        new RepositorioResponsablesCsv(CARPETA_DE_DATOS.resolve("responsables.csv"), avisos),
        reloj);
```

PMD pasó de 154 a 6 hallazgos en este paso.

## Cambios de comportamiento hechos a propósito (no son refactorings)

| Commit | Qué cambió | Por qué |
|---|---|---|
| `ae2ab70` | E1: entradas no numéricas y fin de entrada; E2: fallo al guardar; E3: líneas mal formadas | Defectos externos del diagnóstico |
| `a09eaa7` | CR-02: se rechaza `;` en la descripción | Corrompía el archivo (revisión) |
| `2a8fe39` | CR-03: se rechazan fechas inexistentes en los archivos | `31/02` se ajustaba en silencio (lo detectaron las pruebas) |
| `7e4ed12` | CR-08: los repositorios entregan copias | Un cambio que no se guardó quedaba aplicado en memoria |

Cada uno tiene su prueba en `CambiosIntencionalesTest` o en las pruebas unitarias. En ningún caso cambió el
comportamiento de los 17 escenarios normales.
