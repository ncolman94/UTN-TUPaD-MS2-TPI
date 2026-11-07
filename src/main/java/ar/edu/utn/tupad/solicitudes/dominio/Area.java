package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.Arrays;

/** Area administrativa que atiende un tipo de solicitud. */
public enum Area {
    BEDELIA("BEDELIA"),
    SECRETARIA_ACADEMICA("SECRETARIA ACADEMICA");

    private final String etiqueta;

    Area(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Nombre con el que el area se muestra y se guarda en los archivos. */
    public String etiqueta() {
        return etiqueta;
    }

    public static Area desdeEtiqueta(String etiqueta) {
        return Arrays.stream(values())
                .filter(area -> area.etiqueta.equals(etiqueta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Area desconocida: " + etiqueta));
    }
}
