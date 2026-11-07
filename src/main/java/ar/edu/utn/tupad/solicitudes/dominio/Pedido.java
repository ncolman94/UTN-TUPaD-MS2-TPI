package ar.edu.utn.tupad.solicitudes.dominio;

/**
 * Lo que pide el estudiante, ya validado: tipo interpretado y descripcion obligatoria,
 * de hasta 200 caracteres, sin punto y coma y sin espacios en los extremos.
 */
public record Pedido(String legajo, TipoSolicitud tipo, String descripcion, boolean urgente) {

    public static final int LARGO_MAXIMO_DESCRIPCION = 200;
    /** Reservado como separador de los archivos de datos (ver docs/decisiones/ADR-005). */
    public static final String CARACTER_NO_PERMITIDO = ";";

    public Pedido {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaDeNegocioException("la descripcion es obligatoria");
        }
        if (descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            throw new ReglaDeNegocioException(
                    "la descripcion no puede superar los " + LARGO_MAXIMO_DESCRIPCION + " caracteres");
        }
        if (descripcion.contains(CARACTER_NO_PERMITIDO)) {
            throw new ReglaDeNegocioException("la descripcion no puede contener '" + CARACTER_NO_PERMITIDO + "'");
        }
        descripcion = descripcion.trim();
    }
}
