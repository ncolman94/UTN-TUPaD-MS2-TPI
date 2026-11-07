package ar.edu.utn.tupad.solicitudes.aplicacion;

import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_BEDELIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_SECRETARIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.HOY;
import static ar.edu.utn.tupad.solicitudes.Fixtures.RELOJ_FIJO;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GeneradorDeReporte: totales, vencidas y promedio")
class GeneradorDeReporteTest {

    @Test
    void sinSolicitudesTodoEnCeroYSinPromedio() {
        GeneradorDeReporte generador = new GeneradorDeReporte(List::of, RELOJ_FIJO);

        ReporteSolicitudes reporte = generador.generar();

        assertAll(
                () -> assertEquals(0, reporte.total()),
                () -> assertEquals(0L, reporte.porEstado().get(EstadoSolicitud.PENDIENTE)),
                () -> assertEquals(0L, reporte.porTipo().get(TipoSolicitud.BAJA)),
                () -> assertEquals(0L, reporte.porArea().get(Area.BEDELIA)),
                () -> assertEquals(0, reporte.vencidasSinResolver()),
                () -> assertEquals(OptionalDouble.empty(), reporte.promedioDeDiasDeResolucion()));
    }

    @Test
    void cuentaPorEstadoTipoYAreaYPromediaSoloLasResueltas() {
        Solicitud pendienteVencida = Fixtures.solicitud(1, TipoSolicitud.CERTIFICADO, HOY.minusDays(10));
        Solicitud resueltaEnDosDias = resuelta(2, TipoSolicitud.EXAMEN, 2);
        Solicitud resueltaEnCuatroDias = resuelta(3, TipoSolicitud.EXAMEN, 4);
        Solicitud rechazada = Fixtures.solicitud(4, TipoSolicitud.EQUIVALENCIA, HOY.minusDays(40));
        rechazada.asignar(DE_SECRETARIA);
        rechazada.cambiarEstado(EstadoSolicitud.RECHAZADA, HOY);
        List<Solicitud> solicitudes = List.of(pendienteVencida, resueltaEnDosDias, resueltaEnCuatroDias, rechazada);

        ReporteSolicitudes reporte = new GeneradorDeReporte(() -> solicitudes, RELOJ_FIJO).generar();

        assertAll(
                () -> assertEquals(4, reporte.total()),
                () -> assertEquals(1L, reporte.porEstado().get(EstadoSolicitud.PENDIENTE)),
                () -> assertEquals(2L, reporte.porEstado().get(EstadoSolicitud.RESUELTA)),
                () -> assertEquals(1L, reporte.porEstado().get(EstadoSolicitud.RECHAZADA)),
                () -> assertEquals(2L, reporte.porTipo().get(TipoSolicitud.EXAMEN)),
                () -> assertEquals(3L, reporte.porArea().get(Area.BEDELIA)),
                () -> assertEquals(1L, reporte.porArea().get(Area.SECRETARIA_ACADEMICA)),
                () -> assertEquals(1, reporte.vencidasSinResolver()),
                () -> assertEquals(OptionalDouble.of(3.0), reporte.promedioDeDiasDeResolucion()));
    }

    private static Solicitud resuelta(int numero, TipoSolicitud tipo, int diasDeResolucion) {
        Solicitud solicitud = Fixtures.solicitud(numero, tipo, HOY.minusDays(diasDeResolucion));
        solicitud.asignar(DE_BEDELIA);
        solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO, HOY);
        solicitud.cambiarEstado(EstadoSolicitud.RESUELTA, HOY);
        return solicitud;
    }
}
