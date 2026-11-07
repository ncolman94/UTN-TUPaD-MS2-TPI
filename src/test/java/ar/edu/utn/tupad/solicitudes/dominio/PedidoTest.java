package ar.edu.utn.tupad.solicitudes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Pedido: validacion de la descripcion")
class PedidoTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void laDescripcionEsObligatoria(String descripcion) {
        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> new Pedido("1001", TipoSolicitud.EXAMEN, descripcion, false));

        assertEquals("la descripcion es obligatoria", error.getMessage());
    }

    @Test
    void aceptaHastaDoscientosCaracteres() {
        Pedido pedido = new Pedido("1001", TipoSolicitud.EXAMEN, "x".repeat(200), false);

        assertEquals(200, pedido.descripcion().length());
    }

    @Test
    void rechazaMasDeDoscientosCaracteres() {
        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> new Pedido("1001", TipoSolicitud.EXAMEN, "x".repeat(201), false));

        assertEquals("la descripcion no puede superar los 200 caracteres", error.getMessage());
    }

    @Test
    @DisplayName("CR-02: rechaza el punto y coma, que es el separador del archivo")
    void rechazaPuntoYComa() {
        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> new Pedido("1001", TipoSolicitud.EXAMEN, "Constancia; urgente", false));

        assertEquals("la descripcion no puede contener ';'", error.getMessage());
    }

    @Test
    void quitaLosEspaciosDeLosExtremos() {
        Pedido pedido = new Pedido("1001", TipoSolicitud.EXAMEN, "  Constancia  ", false);

        assertEquals("Constancia", pedido.descripcion());
    }
}
