package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Estados de una solicitud y transiciones permitidas:
 * PENDIENTE -> EN_PROCESO | RECHAZADA; EN_PROCESO -> RESUELTA | RECHAZADA.
 * RESUELTA y RECHAZADA son finales.
 */
public enum EstadoSolicitud {
    PENDIENTE,
    EN_PROCESO,
    RESUELTA,
    RECHAZADA;

    public boolean estaAbierta() {
        return this == PENDIENTE || this == EN_PROCESO;
    }

    public boolean estaCerrada() {
        return !estaAbierta();
    }

    public boolean puedePasarA(EstadoSolicitud destino) {
        return switch (this) {
            case PENDIENTE -> destino == EN_PROCESO || destino == RECHAZADA;
            case EN_PROCESO -> destino.estaCerrada();
            case RESUELTA, RECHAZADA -> false;
        };
    }

    /** Estados a los que se puede llegar con un cambio de estado (para mostrar al usuario). */
    public static List<EstadoSolicitud> destinosPosibles() {
        return Arrays.stream(values()).filter(destino -> destino != PENDIENTE).toList();
    }

    /** Texto ingresado por el usuario llevado a la forma en que se nombran los estados. */
    public static String normalizar(String texto) {
        return texto.toUpperCase(Locale.ROOT).trim();
    }

    public static Optional<EstadoSolicitud> desde(String texto) {
        String normalizado = normalizar(texto);
        return Arrays.stream(values()).filter(estado -> estado.name().equals(normalizado)).findFirst();
    }
}
