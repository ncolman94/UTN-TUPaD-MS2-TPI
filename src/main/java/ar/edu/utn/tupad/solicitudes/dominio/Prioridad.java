package ar.edu.utn.tupad.solicitudes.dominio;

/** Prioridad de atencion de una solicitud, de menor a mayor. */
public enum Prioridad {
    BAJA,
    MEDIA,
    ALTA;

    /** La prioridad un nivel por encima; ALTA ya es la maxima. */
    public Prioridad elevada() {
        return this == ALTA ? ALTA : values()[ordinal() + 1];
    }
}
