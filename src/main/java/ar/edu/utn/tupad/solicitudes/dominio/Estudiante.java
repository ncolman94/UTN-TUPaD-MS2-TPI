package ar.edu.utn.tupad.solicitudes.dominio;

/** Estudiante que origina las solicitudes. */
public record Estudiante(String legajo, String nombre, String carrera, String email) {
}
