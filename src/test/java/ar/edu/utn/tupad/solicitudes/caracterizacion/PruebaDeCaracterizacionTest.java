package ar.edu.utn.tupad.solicitudes.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.tupad.solicitudes.App;
import ar.edu.utn.tupad.solicitudes.caracterizacion.EjecucionDelPrograma.DatosIniciales;
import ar.edu.utn.tupad.solicitudes.caracterizacion.EjecucionDelPrograma.Resultado;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Prueba de caracterizacion (golden master) que sostiene el refactoring.
 *
 * <p>Ejecuta cada escenario dos veces, como procesos separados y con la misma entrada:
 * con la copia congelada de la version inicial (paquete {@code legacy}, tag estado-inicial)
 * y con la version actual. El comportamiento externo se conserva si la salida por pantalla,
 * el codigo de salida y los tres archivos CSV resultantes son identicos.
 *
 * <p>Las salidas quedan en target/caracterizacion/ como evidencia.
 */
@DisplayName("Caracterizacion: la version actual se comporta igual que la version inicial")
class PruebaDeCaracterizacionTest {

    private static final Path CARPETA_DE_EVIDENCIA = Path.of("target", "caracterizacion");

    @BeforeAll
    static void limpiarEvidenciaAnterior() throws IOException {
        Files.deleteIfExists(CARPETA_DE_EVIDENCIA.resolve("resumen.txt"));
    }

    static Stream<Arguments> escenarios() {
        return Stream.of(
                escenario("01-listar-todas", DatosIniciales.EJEMPLO,
                        "6", "0"),
                escenario("02-registrar-certificado", DatosIniciales.EJEMPLO,
                        "1", "1005", "CERTIFICADO", "Certificado de alumno regular", "n", "6", "0"),
                escenario("03-registrar-urgente-en-minusculas", DatosIniciales.EJEMPLO,
                        "1", "1002", "revision", "  Revision del final de Algoritmos  ", "s", "6", "0"),
                escenario("04-registrar-urgente-plazo-minimo-y-si-no-es-urgente", DatosIniciales.EJEMPLO,
                        "1", "1003", "certificado", "Certificado urgente", "S",
                        "1", "1004", "EXAMEN", "Constancia de examen", "si", "6", "0"),
                escenario("05-registrar-con-errores-de-validacion", DatosIniciales.EJEMPLO,
                        "1", "9999", "CERTIFICADO", "Legajo inexistente", "n",
                        "1", "1001", "PASAPORTE", "Tipo inexistente", "n",
                        "1", "1001", "EXAMEN", "   ", "n",
                        "1", "1001", "EXAMEN", "x".repeat(201), "n",
                        "1", "1001", "EXAMEN", "y".repeat(200), "n", "6", "0"),
                escenario("06-asignar-responsable-y-errores", DatosIniciales.EJEMPLO,
                        "2", "4", "R01",
                        "2", "4", "R03",
                        "2", "99", "R01",
                        "2", "4", "R99",
                        "2", "1", "R01",
                        "2", "8", "r04", "6", "0"),
                escenario("07-cambiar-estado-flujo-completo", DatosIniciales.EJEMPLO,
                        "3", "4", "EN_PROCESO",
                        "2", "4", "R02",
                        "3", "4", "en_proceso",
                        "3", "4", "RESUELTA",
                        "3", "4", "EN_PROCESO", "6", "0"),
                escenario("08-cambiar-estado-transiciones-invalidas", DatosIniciales.EJEMPLO,
                        "3", "6", "RESUELTA",
                        "3", "6", "ARCHIVADA",
                        "3", "2", "PENDIENTE",
                        "3", "2", " rechazada ",
                        "3", "5", "EN_PROCESO",
                        "3", "6", "RECHAZADA",
                        "3", "99", "RESUELTA", "6", "0"),
                escenario("09-buscar-por-estado-y-por-tipo", DatosIniciales.EJEMPLO,
                        "4", "pendiente", "", "", "", "n",
                        "4", "", "EQUIVALENCIA", "", "", "N",
                        "4", "", "", "", "media", "n", "0"),
                escenario("10-buscar-combinado-vencidas-y-sin-resultados", DatosIniciales.EJEMPLO,
                        "4", "EN_PROCESO", "", "", "ALTA", "n",
                        "4", "", "", "1001", "", "n",
                        "4", "", "", "", "", "s",
                        "4", "", "", "", "", "S",
                        "4", "RESUELTA", "BAJA", "1003", "", "n",
                        "4", "PENDIENTE ", "", "", "", "n", "0"),
                escenario("11-reporte", DatosIniciales.EJEMPLO,
                        "5", "0"),
                escenario("12-flujo-completo-con-reporte", DatosIniciales.EJEMPLO,
                        "1", "1005", "BAJA", "Baja de Programacion III", "n",
                        "2", "9", "R02",
                        "3", "9", "EN_PROCESO",
                        "3", "9", "RESUELTA",
                        "3", "8", "RECHAZADA",
                        "5", "6", "0"),
                escenario("13-opciones-inexistentes", DatosIniciales.EJEMPLO,
                        "9", "7", "-1", "0"),
                escenario("14-sin-solicitudes", DatosIniciales.SIN_SOLICITUDES,
                        "6", "5", "4", "", "", "", "", "n", "0"),
                escenario("15-sin-archivo-y-primer-registro", DatosIniciales.SIN_ARCHIVO_DE_SOLICITUDES,
                        "6", "1", "1001", "EXAMEN", "Constancia de examen final", "n", "6", "0"),
                escenario("16-asignar-y-cambiar-en-solicitud-nueva", DatosIniciales.EJEMPLO,
                        "1", "1001", "EQUIVALENCIA", "Equivalencia de Fisica", "n",
                        "2", "9", "R01",
                        "2", "9", "R03",
                        "2", "9", "R04",
                        "3", "9", "RECHAZADA",
                        "2", "9", "R03", "5", "0"),
                escenario("17-reporte-con-solicitudes-nuevas-resueltas-hoy", DatosIniciales.EJEMPLO,
                        "1", "1002", "CERTIFICADO", "Certificado de regularidad", "s",
                        "2", "9", "R01",
                        "3", "9", "EN_PROCESO",
                        "3", "9", "RESUELTA",
                        "5", "4", "RESUELTA", "", "", "", "n", "0"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("escenarios")
    void conservaElComportamientoExterno(String nombre, DatosIniciales datos, List<String> entrada,
                                         @TempDir Path carpetaTemporal) throws IOException {
        Resultado esperado = EjecucionDelPrograma.ejecutar(legacy.App.class, entrada, datos,
                carpetaTemporal.resolve("version-inicial"));
        Resultado obtenido = EjecucionDelPrograma.ejecutar(App.class, entrada, datos,
                carpetaTemporal.resolve("version-actual"));

        guardarEvidencia(nombre, entrada, esperado, obtenido);
        assertAll(nombre,
                () -> assertEquals(esperado.salida(), obtenido.salida(), "salida por pantalla"),
                () -> assertEquals(esperado.codigoDeSalida(), obtenido.codigoDeSalida(), "codigo de salida"),
                () -> assertEquals(esperado.solicitudesCsv(), obtenido.solicitudesCsv(), "datos/solicitudes.csv"),
                () -> assertEquals(esperado.estudiantesCsv(), obtenido.estudiantesCsv(), "datos/estudiantes.csv"),
                () -> assertEquals(esperado.responsablesCsv(), obtenido.responsablesCsv(), "datos/responsables.csv"));
    }

    private static Arguments escenario(String nombre, DatosIniciales datos, String... entrada) {
        return Arguments.of(nombre, datos, List.of(entrada));
    }

    private static void guardarEvidencia(String nombre, List<String> entrada, Resultado esperado,
                                         Resultado obtenido) throws IOException {
        Files.createDirectories(CARPETA_DE_EVIDENCIA);
        boolean identicos = esperado.equals(obtenido);
        String informe = "Escenario: " + nombre + "\n"
                + "Resultado: " + (identicos ? "IDENTICO" : "DISTINTO") + "\n"
                + "Entrada: " + String.join(" | ", entrada) + "\n"
                + "Codigo de salida (inicial / actual): " + esperado.codigoDeSalida() + " / "
                + obtenido.codigoDeSalida() + "\n\n"
                + "----- Salida de la version actual -----\n" + obtenido.salida() + "\n"
                + "----- datos/solicitudes.csv de la version actual -----\n" + obtenido.solicitudesCsv();
        Files.writeString(CARPETA_DE_EVIDENCIA.resolve(nombre + ".txt"), informe, StandardCharsets.UTF_8);
        Files.writeString(CARPETA_DE_EVIDENCIA.resolve("resumen.txt"),
                nombre + ": " + (identicos ? "IDENTICO" : "DISTINTO") + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
