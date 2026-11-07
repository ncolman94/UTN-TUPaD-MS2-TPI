# ADR-004 - Reloj inyectado (java.time.Clock)

**Estado:** aceptada · **Fecha:** 07/11/2026

## Contexto
El vencimiento, la fecha de cierre y el reporte dependían de `LocalDate.now()`, llamado en 4 lugares. Una prueba
del vencimiento daba resultados distintos según el día en que se corriera.

## Decisión
`ServicioSolicitudes` y `GeneradorDeReporte` reciben un `java.time.Clock`. `App` usa `Clock.systemDefaultZone()`;
las pruebas usan un reloj fijo (7/11/2026). Las entidades reciben la fecha como parámetro (`estaVencida(hoy)`).

## Alternativas consideradas
- **Una interfaz propia `Reloj`:** equivalente, pero `Clock` ya es estándar en Java y lo conocen otros desarrolladores.
- **Pasar la fecha desde la consola:** mezclaría la interfaz de usuario con una regla de negocio.

## Consecuencias
- Pruebas deterministas de bordes ("vence hoy" no está vencida; "venció ayer" sí).
- El dominio no depende del sistema operativo.
