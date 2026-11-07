package ar.edu.utn.tupad.solicitudes.dominio;

/** Lo que pide el estudiante, con el tipo ya interpretado. */
public record Pedido(String legajo, TipoSolicitud tipo, String descripcion, boolean urgente) {
}
