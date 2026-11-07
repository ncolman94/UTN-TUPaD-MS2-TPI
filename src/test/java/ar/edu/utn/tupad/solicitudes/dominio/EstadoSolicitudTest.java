package ar.edu.utn.tupad.solicitudes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("EstadoSolicitud: transiciones permitidas")
class EstadoSolicitudTest {

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
        "PENDIENTE,  PENDIENTE,  false",
        "PENDIENTE,  EN_PROCESO, true",
        "PENDIENTE,  RESUELTA,   false",
        "PENDIENTE,  RECHAZADA,  true",
        "EN_PROCESO, PENDIENTE,  false",
        "EN_PROCESO, EN_PROCESO, false",
        "EN_PROCESO, RESUELTA,   true",
        "EN_PROCESO, RECHAZADA,  true",
        "RESUELTA,   PENDIENTE,  false",
        "RESUELTA,   EN_PROCESO, false",
        "RESUELTA,   RESUELTA,   false",
        "RESUELTA,   RECHAZADA,  false",
        "RECHAZADA,  PENDIENTE,  false",
        "RECHAZADA,  EN_PROCESO, false",
        "RECHAZADA,  RESUELTA,   false",
        "RECHAZADA,  RECHAZADA,  false",
    })
    void respetaElDiagramaDeEstados(EstadoSolicitud origen, EstadoSolicitud destino, boolean permitida) {
        assertEquals(permitida, origen.puedePasarA(destino));
    }

    @ParameterizedTest(name = "{0} abierta={1}")
    @CsvSource({"PENDIENTE, true", "EN_PROCESO, true", "RESUELTA, false", "RECHAZADA, false"})
    void distingueAbiertasDeCerradas(EstadoSolicitud estado, boolean abierta) {
        assertEquals(abierta, estado.estaAbierta());
        assertEquals(!abierta, estado.estaCerrada());
    }

    @Test
    void losDestinosPosiblesSonTodosMenosPendiente() {
        assertEquals(List.of(EstadoSolicitud.EN_PROCESO, EstadoSolicitud.RESUELTA, EstadoSolicitud.RECHAZADA),
                EstadoSolicitud.destinosPosibles());
    }

    @Test
    void interpretaElTextoIngresado() {
        assertEquals(Optional.of(EstadoSolicitud.EN_PROCESO), EstadoSolicitud.desde(" en_proceso "));
        assertEquals(Optional.empty(), EstadoSolicitud.desde("ARCHIVADA"));
        assertEquals("ARCHIVADA", EstadoSolicitud.normalizar(" archivada "));
    }
}
