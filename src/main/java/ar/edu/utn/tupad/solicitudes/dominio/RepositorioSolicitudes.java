package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.Optional;

/**
 * Almacenamiento de solicitudes. Las implementaciones deben poder usarse indistintamente.
 * Entregan y guardan copias: un cambio sobre una solicitud solo queda registrado al guardarla.
 */
public interface RepositorioSolicitudes extends ConsultaSolicitudes {

    Optional<Solicitud> buscarPorNumero(int numero);

    /** Numero para la proxima solicitud: el mayor existente mas uno (1 si no hay ninguna). */
    int proximoNumero();

    /**
     * Agrega la solicitud o reemplaza la que tenga su mismo numero, y la deja guardada.
     *
     * @throws ErrorDePersistencia si no se pudo guardar; en ese caso una solicitud nueva no se agrega
     */
    void guardar(Solicitud solicitud);
}
