package ar.edu.utn.tupad.solicitudes.consola;

import ar.edu.utn.tupad.solicitudes.aplicacion.ReporteSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Textos que el sistema muestra por consola. Es el unico lugar que conoce el formato de pantalla. */
final class FormatoConsola {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SEPARADOR = " | ";
    private static final String SIN_RESPONSABLE = "-";

    private FormatoConsola() {
    }

    static String fecha(LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    static String linea(Solicitud solicitud) {
        return "#" + solicitud.numero() + SEPARADOR + solicitud.tipo() + SEPARADOR + solicitud.prioridad()
                + SEPARADOR + solicitud.estado() + SEPARADOR + "Leg: " + solicitud.legajo() + SEPARADOR
                + "Resp: " + solicitud.idResponsable().orElse(SIN_RESPONSABLE) + SEPARADOR
                + "Vence: " + fecha(solicitud.fechaDeVencimiento()) + SEPARADOR + solicitud.descripcion();
    }

    static List<String> reporte(ReporteSolicitudes reporte) {
        List<String> lineas = new ArrayList<>();
        lineas.add("===== REPORTE DE SOLICITUDES =====");
        lineas.add("Total de solicitudes: " + reporte.total());
        lineas.add("-- Por estado --");
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            lineas.add(estado + ": " + reporte.porEstado().get(estado));
        }
        lineas.add("-- Por tipo --");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            lineas.add(tipo + ": " + reporte.porTipo().get(tipo));
        }
        lineas.add("-- Por area --");
        for (Area area : Area.values()) {
            lineas.add(area.etiqueta() + ": " + reporte.porArea().get(area));
        }
        lineas.add("Vencidas sin resolver: " + reporte.vencidasSinResolver());
        lineas.add("Tiempo promedio de resolucion: " + promedio(reporte));
        return lineas;
    }

    /** Usa el idioma por defecto de la maquina, igual que la version inicial (3,0 en es-AR; 3.0 en en-US). */
    private static String promedio(ReporteSolicitudes reporte) {
        return reporte.promedioDeDiasDeResolucion().isPresent()
                ? String.format("%.1f", reporte.promedioDeDiasDeResolucion().getAsDouble())
                        + " dias"
                : "sin datos";
    }
}
