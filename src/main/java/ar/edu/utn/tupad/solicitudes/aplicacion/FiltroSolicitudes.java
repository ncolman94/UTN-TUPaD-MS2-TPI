package ar.edu.utn.tupad.solicitudes.aplicacion;

import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Criterios de busqueda de solicitudes (Introduce Parameter Object).
 *
 * <p>Un criterio vacio no filtra. Estado, tipo y prioridad se comparan con el nombre del valor sin
 * distinguir mayusculas; el legajo se compara exacto. Se arma de forma fluida:
 * {@code FiltroSolicitudes.sinCriterios().conEstado("PENDIENTE").conSoloVencidas(true)}.
 */
public record FiltroSolicitudes(String estado, String tipo, String legajo, String prioridad, boolean soloVencidas) {

    private static final String SIN_CRITERIO = "";

    public FiltroSolicitudes {
        estado = Objects.requireNonNullElse(estado, SIN_CRITERIO);
        tipo = Objects.requireNonNullElse(tipo, SIN_CRITERIO);
        legajo = Objects.requireNonNullElse(legajo, SIN_CRITERIO);
        prioridad = Objects.requireNonNullElse(prioridad, SIN_CRITERIO);
    }

    public static FiltroSolicitudes sinCriterios() {
        return new FiltroSolicitudes(SIN_CRITERIO, SIN_CRITERIO, SIN_CRITERIO, SIN_CRITERIO, false);
    }

    public FiltroSolicitudes conEstado(String valor) {
        return new FiltroSolicitudes(valor, tipo, legajo, prioridad, soloVencidas);
    }

    public FiltroSolicitudes conTipo(String valor) {
        return new FiltroSolicitudes(estado, valor, legajo, prioridad, soloVencidas);
    }

    public FiltroSolicitudes conLegajo(String valor) {
        return new FiltroSolicitudes(estado, tipo, valor, prioridad, soloVencidas);
    }

    public FiltroSolicitudes conPrioridad(String valor) {
        return new FiltroSolicitudes(estado, tipo, legajo, valor, soloVencidas);
    }

    public FiltroSolicitudes conSoloVencidas(boolean valor) {
        return new FiltroSolicitudes(estado, tipo, legajo, prioridad, valor);
    }

    public boolean admite(Solicitud solicitud, LocalDate hoy) {
        return coincideSinMayusculas(estado, solicitud.estado().name())
                && coincideSinMayusculas(tipo, solicitud.tipo().name())
                && (legajo.isEmpty() || legajo.equals(solicitud.legajo()))
                && coincideSinMayusculas(prioridad, solicitud.prioridad().name())
                && (!soloVencidas || solicitud.estaVencida(hoy));
    }

    private static boolean coincideSinMayusculas(String criterio, String valor) {
        return criterio.isEmpty() || valor.equalsIgnoreCase(criterio);
    }
}
