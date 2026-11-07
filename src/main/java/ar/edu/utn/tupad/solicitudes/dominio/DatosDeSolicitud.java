package ar.edu.utn.tupad.solicitudes.dominio;

import java.time.LocalDate;

/**
 * Estado completo de una solicitud, tal como se guarda. Permite volver a crearla
 * (por ejemplo, al leerla de un archivo) sin recalcular sus reglas.
 * idResponsable y fechaDeCierre son null cuando todavia no tienen valor.
 */
public record DatosDeSolicitud(int numero, String legajo, TipoSolicitud tipo, String descripcion,
                               Prioridad prioridad, EstadoSolicitud estado, String idResponsable,
                               LocalDate fechaDeAlta, LocalDate fechaDeVencimiento, LocalDate fechaDeCierre) {
}
