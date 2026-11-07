package ar.edu.utn.tupad.solicitudes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TipoSolicitud: reglas de prioridad, plazo y area")
class TipoSolicitudTest {

    @ParameterizedTest(name = "{0} urgente={1} -> prioridad {2}, {3} dias, {4}")
    @CsvSource({
        "CERTIFICADO,  false, BAJA,  3,  BEDELIA",
        "CERTIFICADO,  true,  MEDIA, 1,  BEDELIA",
        "EXAMEN,       false, MEDIA, 5,  BEDELIA",
        "EXAMEN,       true,  ALTA,  2,  BEDELIA",
        "EQUIVALENCIA, false, MEDIA, 30, SECRETARIA_ACADEMICA",
        "EQUIVALENCIA, true,  ALTA,  15, SECRETARIA_ACADEMICA",
        "REVISION,     false, ALTA,  10, SECRETARIA_ACADEMICA",
        "REVISION,     true,  ALTA,  5,  SECRETARIA_ACADEMICA",
        "BAJA,         false, ALTA,  7,  BEDELIA",
        "BAJA,         true,  ALTA,  3,  BEDELIA",
    })
    void aplicaLasReglasDeCadaTipo(TipoSolicitud tipo, boolean urgente, Prioridad prioridad, int plazo, Area area) {
        assertEquals(prioridad, tipo.prioridad(urgente));
        assertEquals(plazo, tipo.plazoEnDias(urgente));
        assertEquals(area, tipo.area());
    }

    @Test
    @DisplayName("el plazo urgente nunca baja de un dia")
    void plazoMinimoDeUnDia() {
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            assertTrue(tipo.plazoEnDias(true) >= 1, tipo.name());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"revision", "REVISION", "  Revision  ", "rEvIsIoN"})
    @DisplayName("interpreta el texto sin distinguir mayusculas ni espacios")
    void interpretaElTexto(String texto) {
        assertEquals(Optional.of(TipoSolicitud.REVISION), TipoSolicitud.desde(texto));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASAPORTE", "", "REVISIONES", "REVI SION"})
    @DisplayName("un texto que no es un tipo devuelve vacio")
    void textoInvalido(String texto) {
        assertEquals(Optional.empty(), TipoSolicitud.desde(texto));
    }
}
