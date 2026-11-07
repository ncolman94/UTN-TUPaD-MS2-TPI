package ar.edu.utn.tupad.solicitudes.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prueba de contrato (principio de sustitucion de Liskov): cualquier implementacion de
 * RepositorioSolicitudes debe pasar estas mismas pruebas para poder usarse en lugar de otra.
 */
abstract class ContratoRepositorioSolicitudes {

    /** Un repositorio vacio de la implementacion bajo prueba. */
    protected abstract RepositorioSolicitudes repositorioVacio();

    @Test
    void vacioNoTieneSolicitudesYElProximoNumeroEsUno() {
        RepositorioSolicitudes repositorio = repositorioVacio();

        assertEquals(List.of(), repositorio.todas());
        assertEquals(1, repositorio.proximoNumero());
        assertEquals(Optional.empty(), repositorio.buscarPorNumero(1));
    }

    @Test
    void guardaEnOrdenYCalculaElProximoNumeroDesdeElMayor() {
        RepositorioSolicitudes repositorio = repositorioVacio();

        repositorio.guardar(Fixtures.certificadoDeHoy(5));
        repositorio.guardar(Fixtures.certificadoDeHoy(2));

        assertEquals(List.of(5, 2), repositorio.todas().stream().map(Solicitud::numero).toList());
        assertEquals(6, repositorio.proximoNumero());
    }

    @Test
    void guardarConUnNumeroExistenteReemplazaSinDuplicar() {
        RepositorioSolicitudes repositorio = repositorioVacio();
        repositorio.guardar(Fixtures.certificadoDeHoy(1));
        Solicitud modificada = Fixtures.certificadoDeHoy(1);
        modificada.cambiarEstado(EstadoSolicitud.RECHAZADA, Fixtures.HOY);

        repositorio.guardar(modificada);

        assertEquals(1, repositorio.todas().size());
        assertEquals(EstadoSolicitud.RECHAZADA, repositorio.buscarPorNumero(1).orElseThrow().estado());
    }

    @Test
    @DisplayName("CR-08: un cambio sobre una solicitud obtenida no queda registrado hasta guardarla")
    void entregaCopias() {
        RepositorioSolicitudes repositorio = repositorioVacio();
        Solicitud guardada = Fixtures.certificadoDeHoy(1);
        repositorio.guardar(guardada);
        guardada.cambiarEstado(EstadoSolicitud.RECHAZADA, Fixtures.HOY);

        Solicitud obtenida = repositorio.buscarPorNumero(1).orElseThrow();
        obtenida.asignar(Fixtures.DE_BEDELIA);

        assertEquals(EstadoSolicitud.PENDIENTE, repositorio.buscarPorNumero(1).orElseThrow().estado());
        assertTrue(repositorio.todas().get(0).idResponsable().isEmpty());
    }

    @Test
    void laListaDevueltaNoSePuedeModificarDesdeAfuera() {
        RepositorioSolicitudes repositorio = repositorioVacio();
        repositorio.guardar(Fixtures.certificadoDeHoy(1));

        List<Solicitud> todas = repositorio.todas();

        assertThrows(UnsupportedOperationException.class, () -> todas.add(Fixtures.certificadoDeHoy(2)));
        assertTrue(repositorio.buscarPorNumero(2).isEmpty());
    }
}
