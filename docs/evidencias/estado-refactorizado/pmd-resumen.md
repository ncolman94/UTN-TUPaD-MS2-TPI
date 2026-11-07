Total de hallazgos PMD: 7

| Regla | Hallazgos |
|---|---:|
| TooManyMethods | 3 |
| CyclomaticComplexity | 2 |
| AvoidDuplicateLiterals | 1 |
| CloseResource | 1 |

| Archivo | Linea | Regla | Mensaje |
|---|---:|---|---|
| ar/edu/utn/tupad/solicitudes/App.java | 28 | CloseResource | Ensure that resources like this PrintStream object are closed after use |
| ar/edu/utn/tupad/solicitudes/consola/MenuConsola.java | 21 | TooManyMethods | This class has too many methods, consider refactoring it. |
| ar/edu/utn/tupad/solicitudes/consola/MenuConsola.java | 67 | CyclomaticComplexity | The method 'ejecutar(int)' has a cyclomatic complexity of 10. |
| ar/edu/utn/tupad/solicitudes/consola/MenuConsola.java | 93 | AvoidDuplicateLiterals | The String literal "Solicitud #" appears 3 times in this file; the first occurrence is on line 93 |
| ar/edu/utn/tupad/solicitudes/dominio/Solicitud.java | 9 | TooManyMethods | This class has too many methods, consider refactoring it. |
| ar/edu/utn/tupad/solicitudes/dominio/Solicitud.java | 60 | CyclomaticComplexity | The method 'cambiarEstado(EstadoSolicitud, LocalDate)' has a cyclomatic complexity of 8. |
| ar/edu/utn/tupad/solicitudes/persistencia/RepositorioSolicitudesCsv.java | 23 | TooManyMethods | This class has too many methods, consider refactoring it. |
