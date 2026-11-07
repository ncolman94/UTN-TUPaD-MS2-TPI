package ar.edu.utn.tupad.solicitudes.aplicacion;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Resultado del reporte, sin formato: cada conteo tiene una entrada por cada valor posible (aunque sea 0).
 * El promedio de dias de resolucion considera solo las solicitudes RESUELTAS.
 */
public record ReporteSolicitudes(int total,
                                 Map<EstadoSolicitud, Long> porEstado,
                                 Map<TipoSolicitud, Long> porTipo,
                                 Map<Area, Long> porArea,
                                 long vencidasSinResolver,
                                 OptionalDouble promedioDeDiasDeResolucion) {
}
