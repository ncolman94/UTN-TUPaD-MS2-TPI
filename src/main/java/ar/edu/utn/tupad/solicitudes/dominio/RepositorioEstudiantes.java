package ar.edu.utn.tupad.solicitudes.dominio;

/** Consulta de estudiantes: el sistema solo necesita saber si un legajo existe. */
@FunctionalInterface
public interface RepositorioEstudiantes {

    boolean existe(String legajo);
}
