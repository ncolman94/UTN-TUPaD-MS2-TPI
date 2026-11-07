package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Tipos de solicitud con sus reglas: prioridad base, plazo de atencion y area responsable.
 * Para agregar un tipo nuevo alcanza con agregar una constante aqui (principio abierto/cerrado).
 */
public enum TipoSolicitud {
    CERTIFICADO(Prioridad.BAJA, 3, Area.BEDELIA),
    EXAMEN(Prioridad.MEDIA, 5, Area.BEDELIA),
    EQUIVALENCIA(Prioridad.MEDIA, 30, Area.SECRETARIA_ACADEMICA),
    REVISION(Prioridad.ALTA, 10, Area.SECRETARIA_ACADEMICA),
    BAJA(Prioridad.ALTA, 7, Area.BEDELIA);

    private static final int PLAZO_MINIMO_EN_DIAS = 1;

    private final Prioridad prioridadBase;
    private final int plazoEnDias;
    private final Area area;

    TipoSolicitud(Prioridad prioridadBase, int plazoEnDias, Area area) {
        this.prioridadBase = prioridadBase;
        this.plazoEnDias = plazoEnDias;
        this.area = area;
    }

    /** Una solicitud urgente sube un nivel de prioridad. */
    public Prioridad prioridad(boolean urgente) {
        return urgente ? prioridadBase.elevada() : prioridadBase;
    }

    /** Una solicitud urgente tiene la mitad del plazo, con un minimo de un dia. */
    public int plazoEnDias(boolean urgente) {
        return urgente ? Math.max(PLAZO_MINIMO_EN_DIAS, plazoEnDias / 2) : plazoEnDias;
    }

    public Area area() {
        return area;
    }

    /** Interpreta el texto ingresado por el usuario, sin distinguir mayusculas ni espacios en los extremos. */
    public static Optional<TipoSolicitud> desde(String texto) {
        String normalizado = texto.toUpperCase(Locale.ROOT).trim();
        return Arrays.stream(values()).filter(tipo -> tipo.name().equals(normalizado)).findFirst();
    }
}
