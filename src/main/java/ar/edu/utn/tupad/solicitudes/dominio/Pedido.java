package ar.edu.utn.tupad.solicitudes.dominio;

/**
 * Lo que pide el estudiante, ya validado: tipo interpretado y descripcion obligatoria,
 * de hasta 200 caracteres y sin espacios en los extremos.
 */
public record Pedido(String legajo, TipoSolicitud tipo, String descripcion, boolean urgente) {

    public static final int LARGO_MAXIMO_DESCRIPCION = 200;

    public Pedido {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaDeNegocioException("la descripcion es obligatoria");
        }
        if (descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            throw new ReglaDeNegocioException(
                    "la descripcion no puede superar los " + LARGO_MAXIMO_DESCRIPCION + " caracteres");
        }
        descripcion = descripcion.trim();
    }
}
