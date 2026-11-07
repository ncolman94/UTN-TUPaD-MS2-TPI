package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Solicitudes en memoria, sin archivos. Cumple el mismo contrato que la version CSV. */
public class RepositorioSolicitudesEnMemoria implements RepositorioSolicitudes {

    private final List<Solicitud> solicitudes = new ArrayList<>();

    @Override
    public List<Solicitud> todas() {
        return ListaDeSolicitudes.copias(solicitudes);
    }

    @Override
    public Optional<Solicitud> buscarPorNumero(int numero) {
        return ListaDeSolicitudes.copiaDe(solicitudes, numero);
    }

    @Override
    public int proximoNumero() {
        return solicitudes.stream().mapToInt(Solicitud::numero).max().orElse(0) + 1;
    }

    @Override
    public void guardar(Solicitud solicitud) {
        ListaDeSolicitudes.agregarOReemplazar(solicitudes, solicitud);
    }
}
