package ar.edu.utn.tupad.solicitudes.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Repositorios CSV de estudiantes y responsables")
class RepositoriosDeConsultaCsvTest {

    @TempDir
    Path carpeta;

    private final List<String> avisos = new ArrayList<>();

    @Test
    void encuentraEstudiantesPorLegajoExacto() throws IOException {
        Path archivo = escribir("estudiantes.csv", "legajo;nombre;carrera;email", "1001;Ana Gomez;TUP;ana@ejemplo.edu");

        RepositorioEstudiantesCsv estudiantes = new RepositorioEstudiantesCsv(archivo, avisos::add);

        assertTrue(estudiantes.existe("1001"));
        assertFalse(estudiantes.existe("100"));
    }

    @Test
    void encuentraResponsablesSinDistinguirMayusculas() throws IOException {
        Path archivo = escribir("responsables.csv", "id;nombre;area", "R03;Sofia Herrera;SECRETARIA ACADEMICA",
                "R09;Area desconocida;TESORERIA");

        RepositorioResponsablesCsv responsables = new RepositorioResponsablesCsv(archivo, avisos::add);

        assertEquals(Optional.of(new Responsable("R03", "Sofia Herrera", Area.SECRETARIA_ACADEMICA)),
                responsables.buscarPorId("r03"));
        assertEquals(Optional.empty(), responsables.buscarPorId("R09"));
        assertEquals(1, avisos.size());
    }

    @Test
    void siElArchivoNoExisteNoHayDatosNiAvisos() {
        RepositorioEstudiantesCsv estudiantes =
                new RepositorioEstudiantesCsv(carpeta.resolve("no-existe.csv"), avisos::add);

        assertFalse(estudiantes.existe("1001"));
        assertEquals(List.of(), avisos);
    }

    private Path escribir(String nombre, String... lineas) throws IOException {
        return Files.write(carpeta.resolve(nombre), List.of(lineas), StandardCharsets.UTF_8);
    }
}
