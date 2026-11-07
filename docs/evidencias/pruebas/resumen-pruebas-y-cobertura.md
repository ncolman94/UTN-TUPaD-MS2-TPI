## Pruebas automatizadas (JUnit 5)

| Clase de prueba | Casos | Fallidos | Deshabilitados |
|---|---:|---:|---:|
| aplicacion.FiltroSolicitudesTest | 7 | 0 | 0 |
| aplicacion.GeneradorDeReporteTest | 2 | 0 | 0 |
| aplicacion.ServicioSolicitudesTest | 8 | 0 | 0 |
| caracterizacion.CambiosIntencionalesTest | 5 | 0 | 0 |
| caracterizacion.PruebaDeCaracterizacionTest | 17 | 0 | 0 |
| consola.MenuConsolaTest | 8 | 0 | 0 |
| dominio.EstadoSolicitudTest | 22 | 0 | 0 |
| dominio.PedidoTest | 7 | 0 | 0 |
| dominio.PrioridadYAreaTest | 5 | 0 | 0 |
| dominio.SolicitudTest | 13 | 0 | 0 |
| dominio.TipoSolicitudTest | 19 | 0 | 0 |
| persistencia.RepositorioSolicitudesCsvTest | 8 | 0 | 1 |
| persistencia.RepositorioSolicitudesEnMemoriaTest | 4 | 0 | 0 |
| persistencia.RepositoriosDeConsultaCsvTest | 3 | 0 | 0 |
| **Total** | **128** | **0** | **1** |

## Cobertura (JaCoCo)

| Paquete | Clase | Lineas | Ramas |
|---|---|---:|---:|
| dominio | Estudiante | 100% (1/1) | - |
| dominio | DatosDeSolicitud | 100% (1/1) | - |
| dominio | Pedido | 100% (7/7) | 100% (6/6) |
| dominio | ErrorDePersistencia | 100% (2/2) | - |
| dominio | Prioridad | 100% (5/5) | 100% (2/2) |
| dominio | Solicitud | 100% (51/51) | 100% (22/22) |
| dominio | EstadoSolicitud | 100% (15/15) | 100% (15/15) |
| dominio | Responsable | 100% (2/2) | 100% (2/2) |
| dominio | ReglaDeNegocioException | 100% (3/3) | - |
| dominio | Area | 100% (11/11) | - |
| dominio | TipoSolicitud | 100% (16/16) | 100% (4/4) |
| consola | FormatoConsola | 92% (23/25) | 88% (7/8) |
| consola | MenuConsola | 100% (94/94) | 95% (19/20) |
| (raiz) | App | 0% (0/15) | - |
| persistencia | ListaDeSolicitudes | 100% (6/6) | 100% (4/4) |
| persistencia | RepositorioSolicitudesEnMemoria | 100% (7/7) | 100% (2/2) |
| persistencia | RepositorioResponsablesCsv | 100% (5/5) | - |
| persistencia | RepositorioEstudiantesCsv | 100% (5/5) | - |
| persistencia | ArchivoCsv | 94% (34/36) | 100% (10/10) |
| persistencia | RepositorioSolicitudesCsv | 100% (30/30) | 100% (10/10) |
| aplicacion | DatosNuevaSolicitud | 100% (1/1) | - |
| aplicacion | FiltroSolicitudes | 100% (18/18) | 94% (17/18) |
| aplicacion | ServicioSolicitudes | 100% (34/34) | 100% (2/2) |
| aplicacion | ReporteSolicitudes | 100% (1/1) | - |
| aplicacion | GeneradorDeReporte | 100% (24/24) | 100% (4/4) |
| **Total** | | **95% (396/415)** | **98% (126/129)** |
