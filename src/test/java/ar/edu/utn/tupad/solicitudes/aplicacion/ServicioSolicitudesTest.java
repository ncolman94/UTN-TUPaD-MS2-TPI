package ar.edu.utn.tupad.solicitudes.aplicacion;

import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_BEDELIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.HOY;
import static ar.edu.utn.tupad.solicitudes.Fixtures.RELOJ_FIJO;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import ar.edu.utn.tupad.solicitudes.dominio.ErrorDePersistencia;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Prioridad;
import ar.edu.utn.tupad.solicitudes.dominio.ReglaDeNegocioException;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioEstudiantes;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioResponsables;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Los repositorios se reemplazan por dobles en memoria y el reloj por uno fijo: no hay archivos ni consola. */
@DisplayName("ServicioSolicitudes: casos de uso")
class ServicioSolicitudesTest {

    private static final RepositorioEstudiantes SOLO_EXISTE_1001 = "1001"::equals;
    private static final RepositorioResponsables SOLO_EXISTE_R01 =
            id -> "R01".equalsIgnoreCase(id) ? Optional.of(DE_BEDELIA) : Optional.empty();

    private RepositorioSolicitudesEnMemoria repositorio;
    private ServicioSolicitudes servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioSolicitudesEnMemoria();
        servicio = new ServicioSolicitudes(repositorio, SOLO_EXISTE_1001, SOLO_EXISTE_R01, RELOJ_FIJO);
    }

    @Test
    void registraConNumeroConsecutivoYLaGuarda() {
        repositorio.guardar(Fixtures.certificadoDeHoy(7));

        Solicitud solicitud = servicio.registrar(new DatosNuevaSolicitud("1001", "examen", "Constancia", true));

        assertAll(
                () -> assertEquals(8, solicitud.numero()),
                () -> assertEquals(TipoSolicitud.EXAMEN, solicitud.tipo()),
                () -> assertEquals(Prioridad.ALTA, solicitud.prioridad()),
                () -> assertEquals(HOY.plusDays(2), solicitud.fechaDeVencimiento()),
                () -> assertEquals(solicitud.datos(), repositorio.buscarPorNumero(8).orElseThrow().datos()));
    }

    @Test
    @DisplayName("valida el legajo antes que el tipo y que la descripcion, como la version inicial")
    void validaEnElOrdenOriginal() {
        DatosNuevaSolicitud todoMal = new DatosNuevaSolicitud("9999", "PASAPORTE", "", false);
        DatosNuevaSolicitud tipoYDescripcionMal = new DatosNuevaSolicitud("1001", "PASAPORTE", "", false);

        assertEquals("no existe un estudiante con legajo 9999", mensajeDeError(() -> servicio.registrar(todoMal)));
        assertEquals("tipo de solicitud invalido", mensajeDeError(() -> servicio.registrar(tipoYDescripcionMal)));
        assertEquals(List.of(), repositorio.todas());
    }

    @Test
    void asignaUnResponsableExistente() {
        repositorio.guardar(Fixtures.certificadoDeHoy(1));

        servicio.asignarResponsable(1, "r01");

        assertEquals(Optional.of("R01"), repositorio.buscarPorNumero(1).orElseThrow().idResponsable());
    }

    @Test
    void informaSolicitudOResponsableInexistentes() {
        repositorio.guardar(Fixtures.certificadoDeHoy(1));

        assertEquals("no existe la solicitud #5", mensajeDeError(() -> servicio.asignarResponsable(5, "R01")));
        assertEquals("no existe el responsable R99", mensajeDeError(() -> servicio.asignarResponsable(1, "R99")));
    }

    @Test
    @DisplayName("CR-08: si el guardado falla, la solicitud guardada no cambia")
    void siFallaElGuardadoNoQuedaElCambio() {
        RepositorioSolicitudesEnMemoria queFallaAlGuardar = new RepositorioSolicitudesEnMemoria() {
            private boolean fallar;

            @Override
            public void guardar(Solicitud solicitud) {
                if (fallar) {
                    throw new ErrorDePersistencia("no se pudo escribir", null);
                }
                super.guardar(solicitud);
                fallar = true;
            }
        };
        queFallaAlGuardar.guardar(Fixtures.certificadoDeHoy(1));
        ServicioSolicitudes conFallas =
                new ServicioSolicitudes(queFallaAlGuardar, SOLO_EXISTE_1001, SOLO_EXISTE_R01, RELOJ_FIJO);

        assertThrows(ErrorDePersistencia.class, () -> conFallas.asignarResponsable(1, "R01"));
        assertThrows(ErrorDePersistencia.class, () -> conFallas.cambiarEstado(1, "RECHAZADA"));

        Solicitud guardada = queFallaAlGuardar.buscarPorNumero(1).orElseThrow();
        assertEquals(Optional.empty(), guardada.idResponsable());
        assertEquals(EstadoSolicitud.PENDIENTE, guardada.estado());
    }

    @Test
    void cambiaElEstadoConLaFechaDelReloj() {
        Solicitud solicitud = Fixtures.solicitud(1, TipoSolicitud.CERTIFICADO, HOY.minusDays(2));
        solicitud.asignar(DE_BEDELIA);
        repositorio.guardar(solicitud);

        servicio.cambiarEstado(1, "en_proceso");
        EstadoSolicitud nuevo = servicio.cambiarEstado(1, "RESUELTA");

        assertEquals(EstadoSolicitud.RESUELTA, nuevo);
        assertEquals(HOY, repositorio.buscarPorNumero(1).orElseThrow().datos().fechaDeCierre());
    }

    @Test
    @DisplayName("un estado desconocido se informa como transicion invalida, con el texto normalizado")
    void estadoDesconocido() {
        repositorio.guardar(Fixtures.certificadoDeHoy(1));

        assertEquals("no se puede pasar de PENDIENTE a ARCHIVADA",
                mensajeDeError(() -> servicio.cambiarEstado(1, " archivada ")));
    }

    @Test
    void enUnaSolicitudCerradaInformaQueEstaCerradaAntesQueElEstadoDesconocido() {
        Solicitud cerrada = Fixtures.certificadoDeHoy(1);
        cerrada.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);
        repositorio.guardar(cerrada);

        assertEquals("la solicitud #1 ya esta cerrada", mensajeDeError(() -> servicio.cambiarEstado(1, "XYZ")));
    }

    @Test
    void buscaAplicandoElFiltroConLaFechaDelReloj() {
        repositorio.guardar(Fixtures.solicitud(1, TipoSolicitud.CERTIFICADO, HOY.minusDays(10)));
        repositorio.guardar(Fixtures.certificadoDeHoy(2));

        List<Solicitud> vencidas = servicio.buscar(FiltroSolicitudes.sinCriterios().conSoloVencidas(true));

        assertEquals(List.of(1), vencidas.stream().map(Solicitud::numero).toList());
    }

    private static String mensajeDeError(Runnable operacion) {
        return assertThrows(ReglaDeNegocioException.class, operacion::run).getMessage();
    }
}
