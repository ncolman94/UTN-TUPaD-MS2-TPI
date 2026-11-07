package ar.edu.utn.tupad.solicitudes.aplicacion;

import static ar.edu.utn.tupad.solicitudes.Fixtures.HOY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FiltroSolicitudes: criterios de busqueda")
class FiltroSolicitudesTest {

    // Examen del legajo 1001, prioridad MEDIA, dado de alta hace 10 dias: vencido y pendiente.
    private final Solicitud examenVencido = Fixtures.solicitud(1, TipoSolicitud.EXAMEN, HOY.minusDays(10));

    @Test
    void sinCriteriosAdmiteTodo() {
        assertTrue(FiltroSolicitudes.sinCriterios().admite(examenVencido, HOY));
    }

    @Test
    void comparaEstadoTipoYPrioridadSinDistinguirMayusculas() {
        FiltroSolicitudes filtro = FiltroSolicitudes.sinCriterios()
                .conEstado("pendiente").conTipo("Examen").conPrioridad("media");

        assertTrue(filtro.admite(examenVencido, HOY));
    }

    @Test
    void noRecortaEspacios() {
        assertFalse(FiltroSolicitudes.sinCriterios().conEstado("PENDIENTE ").admite(examenVencido, HOY));
    }

    @Test
    void elLegajoDebeCoincidirExacto() {
        assertTrue(FiltroSolicitudes.sinCriterios().conLegajo("1001").admite(examenVencido, HOY));
        assertFalse(FiltroSolicitudes.sinCriterios().conLegajo("100").admite(examenVencido, HOY));
    }

    @Test
    void soloVencidasDescartaLasQueNoVencieron() {
        FiltroSolicitudes soloVencidas = FiltroSolicitudes.sinCriterios().conSoloVencidas(true);

        assertTrue(soloVencidas.admite(examenVencido, HOY));
        assertFalse(soloVencidas.admite(Fixtures.certificadoDeHoy(2), HOY));
    }

    @Test
    void todosLosCriteriosDebenCumplirse() {
        FiltroSolicitudes filtro = FiltroSolicitudes.sinCriterios().conEstado("PENDIENTE").conTipo("CERTIFICADO");

        assertFalse(filtro.admite(examenVencido, HOY));
    }

    @Test
    void unCriterioNuloEquivaleAVacio() {
        assertTrue(new FiltroSolicitudes(null, null, null, null, false).admite(examenVencido, HOY));
    }
}
