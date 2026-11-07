package ar.edu.utn.tupad.solicitudes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Prioridad, Area y Responsable")
class PrioridadYAreaTest {

    @ParameterizedTest(name = "{0} elevada = {1}")
    @CsvSource({"BAJA, MEDIA", "MEDIA, ALTA", "ALTA, ALTA"})
    void elevarSubeUnNivelHastaAlta(Prioridad actual, Prioridad esperada) {
        assertEquals(esperada, actual.elevada());
    }

    @Test
    void elAreaSeLeeYSeMuestraConSuEtiqueta() {
        assertEquals(Area.SECRETARIA_ACADEMICA, Area.desdeEtiqueta("SECRETARIA ACADEMICA"));
        assertEquals("SECRETARIA ACADEMICA", Area.SECRETARIA_ACADEMICA.etiqueta());
        assertThrows(IllegalArgumentException.class, () -> Area.desdeEtiqueta("TESORERIA"));
    }

    @Test
    void unResponsablePerteneceSoloASuArea() {
        Responsable responsable = new Responsable("R01", "Laura Medina", Area.BEDELIA);

        assertTrue(responsable.perteneceA(Area.BEDELIA));
        assertEquals(false, responsable.perteneceA(Area.SECRETARIA_ACADEMICA));
    }
}
