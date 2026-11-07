package ar.edu.utn.tupad.solicitudes.dominio;

import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_BEDELIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_SECRETARIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.HOY;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Solicitud: reglas de la entidad")
class SolicitudTest {

    @Nested
    @DisplayName("al registrarse")
    class AlRegistrarse {

        @Test
        void calculaPrioridadYVencimientoSegunElTipo() {
            Pedido pedido = new Pedido("1002", TipoSolicitud.REVISION, "Revision de parcial", true);

            Solicitud solicitud = Solicitud.registrar(9, pedido, HOY);

            assertAll(
                    () -> assertEquals(9, solicitud.numero()),
                    () -> assertEquals(Prioridad.ALTA, solicitud.prioridad()),
                    () -> assertEquals(HOY.plusDays(5), solicitud.fechaDeVencimiento()),
                    () -> assertEquals(EstadoSolicitud.PENDIENTE, solicitud.estado()),
                    () -> assertEquals(Optional.empty(), solicitud.idResponsable()),
                    () -> assertEquals("Revision de parcial", solicitud.descripcion()));
        }

        @Test
        void seRestauraConLosMismosDatosConLosQueSeGuarda() {
            Solicitud original = Fixtures.certificadoDeHoy(3);
            original.asignar(DE_BEDELIA);

            Solicitud restaurada = Solicitud.restaurar(original.datos());

            assertEquals(original.datos(), restaurada.datos());
        }
    }

    @Nested
    @DisplayName("al asignar un responsable")
    class AlAsignar {

        @Test
        void aceptaUnResponsableDelAreaDelTipo() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(1);

            solicitud.asignar(DE_BEDELIA);

            assertEquals(Optional.of("R01"), solicitud.idResponsable());
        }

        @Test
        void rechazaUnResponsableDeOtraArea() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(1);

            ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                    () -> solicitud.asignar(DE_SECRETARIA));

            assertEquals("Sofia Herrera no pertenece al area BEDELIA", error.getMessage());
            assertFalse(solicitud.tieneResponsable());
        }

        @Test
        void rechazaAsignarUnaSolicitudCerrada() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(4);
            solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);

            ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                    () -> solicitud.asignar(DE_BEDELIA));

            assertEquals("la solicitud #4 ya esta cerrada", error.getMessage());
        }
    }

    @Nested
    @DisplayName("al cambiar de estado")
    class AlCambiarDeEstado {

        @Test
        void exigeResponsableParaPasarAEnProceso() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(1);

            ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                    () -> solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO, HOY));

            assertEquals("la solicitud debe tener un responsable asignado", error.getMessage());
            assertEquals(EstadoSolicitud.PENDIENTE, solicitud.estado());
        }

        @Test
        void rechazaUnaTransicionNoPermitida() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(1);

            ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                    () -> solicitud.cambiarEstado(EstadoSolicitud.RESUELTA, HOY));

            assertEquals("no se puede pasar de PENDIENTE a RESUELTA", error.getMessage());
        }

        @Test
        void alResolverseRegistraLaFechaDeCierreYLosDiasDeResolucion() {
            Solicitud solicitud = Fixtures.solicitud(1, TipoSolicitud.EXAMEN, HOY.minusDays(4));
            solicitud.asignar(DE_BEDELIA);
            solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO, HOY.minusDays(3));

            solicitud.cambiarEstado(EstadoSolicitud.RESUELTA, HOY);

            assertEquals(EstadoSolicitud.RESUELTA, solicitud.estado());
            assertEquals(HOY, solicitud.datos().fechaDeCierre());
            assertEquals(OptionalLong.of(4), solicitud.diasDeResolucion());
        }

        @Test
        void unaRechazadaNoTieneDiasDeResolucion() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(1);

            solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);

            assertEquals(HOY, solicitud.datos().fechaDeCierre());
            assertEquals(OptionalLong.empty(), solicitud.diasDeResolucion());
        }

        @Test
        void unaSolicitudCerradaNoCambiaMas() {
            Solicitud solicitud = Fixtures.certificadoDeHoy(2);
            solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);

            ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                    () -> solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO, HOY));

            assertEquals("la solicitud #2 ya esta cerrada", error.getMessage());
        }
    }

    @Nested
    @DisplayName("vencimiento")
    class Vencimiento {

        // Un certificado dado de alta hace 3 dias vence hoy.
        private final Solicitud queVenceHoy = Fixtures.solicitud(1, TipoSolicitud.CERTIFICADO, HOY.minusDays(3));

        @Test
        void elDiaDelVencimientoTodaviaNoEstaVencida() {
            assertFalse(queVenceHoy.estaVencida(HOY));
        }

        @Test
        void alDiaSiguienteEstaVencida() {
            assertTrue(queVenceHoy.estaVencida(HOY.plusDays(1)));
        }

        @Test
        void unaSolicitudCerradaNuncaEstaVencida() {
            queVenceHoy.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);

            assertFalse(queVenceHoy.estaVencida(HOY.plusDays(30)));
        }
    }
}
