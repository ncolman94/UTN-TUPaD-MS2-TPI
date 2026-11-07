package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.Optional;

/** Consulta de responsables por su identificador, sin distinguir mayusculas. */
@FunctionalInterface
public interface RepositorioResponsables {

    Optional<Responsable> buscarPorId(String id);
}
