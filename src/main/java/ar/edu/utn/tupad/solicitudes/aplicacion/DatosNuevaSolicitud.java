package ar.edu.utn.tupad.solicitudes.aplicacion;

/**
 * Datos que ingresa el operador para registrar una solicitud (Introduce Parameter Object).
 * Llegan como texto, sin validar: el registro los valida en el orden de la regla de negocio.
 */
public record DatosNuevaSolicitud(String legajo, String tipo, String descripcion, boolean urgente) {
}
