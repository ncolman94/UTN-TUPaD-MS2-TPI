# Validación de la red de seguridad

Una prueba que nunca falla no protege nada. Antes de empezar a refactorizar se verificó que la prueba de
caracterización detecta un cambio de comportamiento real.

| Paso | Acción | Resultado |
|---|---|---|
| 1 | `./mvnw verify` con el código original | 17 de 17 escenarios IDÉNTICOS |
| 2 | Se introdujo un error a propósito en `GestorSolicitudes.registrarSolicitud`: plazo de EXAMEN de 5 a 6 días | 3 de 17 escenarios DISTINTOS (04, 05 y 15): difieren la fecha de vencimiento en pantalla y en `datos/solicitudes.csv` |
| 3 | Se deshizo el cambio | 17 de 17 IDÉNTICOS |

Escenarios que detectaron el error:

```
04-registrar-urgente-plazo-minimo-y-si-no-es-urgente: DISTINTO
05-registrar-con-errores-de-validacion: DISTINTO
15-sin-archivo-y-primer-registro: DISTINTO
```

Mensaje de JUnit (resumido):

```
[ERROR] Tests run: 17, Failures: 3, Errors: 0, Skipped: 0
  AssertionFailedError: salida por pantalla ==> expected: <... Vence: ...> but was: <...>
  AssertionFailedError: datos/solicitudes.csv ==> expected: <...> but was: <...>
```

Conclusión: la red compara lo que el usuario ve (pantalla) y lo que queda guardado (archivos), y alcanza
para detectar un cambio de un solo número en una regla de negocio.
