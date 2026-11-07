package ar.edu.utn.tupad.solicitudes.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.Fixtures;
import ar.edu.utn.tupad.solicitudes.dominio.ErrorDePersistencia;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Contrato de RepositorioSolicitudes: implementacion CSV")
class RepositorioSolicitudesCsvTest extends ContratoRepositorioSolicitudes {

    private static final String ENCABEZADO =
            "id;legajo;tipo;descripcion;prioridad;estado;responsable;fechaAlta;fechaVencimiento;fechaCierre";

    @TempDir
    Path carpeta;

    private final List<String> avisos = new ArrayList<>();

    @Override
    protected RepositorioSolicitudes repositorioVacio() {
        return new RepositorioSolicitudesCsv(carpeta.resolve("solicitudes.csv"), avisos::add);
    }

    @Test
    void loGuardadoSeVuelveALeerIgualYConElFormatoOriginal() throws IOException {
        Path archivo = carpeta.resolve("solicitudes.csv");
        Solicitud solicitud = Fixtures.certificadoDeHoy(1);
        solicitud.asignar(Fixtures.DE_BEDELIA);
        new RepositorioSolicitudesCsv(archivo, avisos::add).guardar(solicitud);

        RepositorioSolicitudesCsv releido = new RepositorioSolicitudesCsv(archivo, avisos::add);

        assertEquals(solicitud.datos(), releido.buscarPorNumero(1).orElseThrow().datos());
        assertEquals(List.of(ENCABEZADO,
                        "1;1001;CERTIFICADO;Descripcion de prueba;BAJA;PENDIENTE;R01;07/11/2026;10/11/2026;"),
                Files.readAllLines(archivo, StandardCharsets.UTF_8));
    }

    @Test
    void ignoraLineasMalFormadasYLasInformaSinPerderLasDemas() throws IOException {
        Path archivo = carpeta.resolve("solicitudes.csv");
        Files.write(archivo, List.of(ENCABEZADO,
                "1;1001;CERTIFICADO;Uno;BAJA;PENDIENTE;;07/11/2026;10/11/2026;",
                "dos;1001;CERTIFICADO;Numero invalido;BAJA;PENDIENTE;;07/11/2026;10/11/2026;",
                "3;1001;CERTIFICADO;Faltan columnas",
                "",
                "4;1001;PASAPORTE;Tipo invalido;BAJA;PENDIENTE;;07/11/2026;10/11/2026;",
                "6;1001;CERTIFICADO;Seis;BAJA;PENDIENTE;;07/11/2026;10/11/2026;"), StandardCharsets.UTF_8);

        RepositorioSolicitudesCsv repositorio = new RepositorioSolicitudesCsv(archivo, avisos::add);

        assertEquals(List.of(1, 6), repositorio.todas().stream().map(Solicitud::numero).toList());
        assertEquals(3, avisos.size(), avisos.toString());
        assertTrue(avisos.get(0).startsWith("Aviso: se ignoro la linea 3 de "));
    }

    @Test
    @DisplayName("CR-03: una fecha que no existe hace que la linea se ignore con aviso")
    void rechazaFechasQueNoExisten() throws IOException {
        Path archivo = carpeta.resolve("solicitudes.csv");
        Files.write(archivo, List.of(ENCABEZADO,
                "5;1001;CERTIFICADO;Fecha imposible;BAJA;PENDIENTE;;31/02/2026;10/11/2026;"), StandardCharsets.UTF_8);

        RepositorioSolicitudesCsv repositorio = new RepositorioSolicitudesCsv(archivo, avisos::add);

        assertEquals(List.of(), repositorio.todas());
        assertEquals(1, avisos.size(), avisos.toString());
    }

    @Test
    @DisplayName("si no puede escribir, lanza ErrorDePersistencia y no agrega la solicitud")
    void siNoPuedeEscribirNoAgrega() {
        RepositorioSolicitudesCsv repositorio =
                new RepositorioSolicitudesCsv(carpeta.resolve("no-existe/solicitudes.csv"), avisos::add);

        assertThrows(ErrorDePersistencia.class, () -> repositorio.guardar(Fixtures.certificadoDeHoy(1)));
        assertEquals(List.of(), repositorio.todas());
    }
}
