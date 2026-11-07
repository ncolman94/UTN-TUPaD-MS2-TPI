package ar.edu.utn.tupad.solicitudes.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import ar.edu.utn.tupad.solicitudes.App;
import ar.edu.utn.tupad.solicitudes.caracterizacion.EjecucionDelPrograma.DatosIniciales;
import ar.edu.utn.tupad.solicitudes.caracterizacion.EjecucionDelPrograma.Resultado;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Cambios de comportamiento hechos a proposito (defectos E1, E2 y E3 del diagnostico).
 * Cada prueba ejecuta la version inicial y la actual con la misma entrada y verifica que la
 * version inicial falla como se documento y que la actual se comporta como se espera.
 * Las salidas quedan en target/cambios-intencionales/ como evidencia.
 */
@DisplayName("Cambios intencionales: defectos externos corregidos")
class CambiosIntencionalesTest {

    private static final Path CARPETA_DE_EVIDENCIA = Path.of("target", "cambios-intencionales");

    @TempDir
    Path carpetaTemporal;

    @Test
    @DisplayName("E1: una letra en el menu ya no cierra el programa")
    void letraEnElMenu() throws IOException {
        List<String> entrada = List.of("abc", "6", "0");

        Resultado inicial = ejecutarVersionInicial(entrada, DatosIniciales.EJEMPLO);
        Resultado actual = ejecutarVersionActual(entrada, DatosIniciales.EJEMPLO);

        guardar("E1-letra-en-el-menu", entrada, inicial, actual);
        assertAll(
                () -> assertEquals(1, inicial.codigoDeSalida()),
                () -> assertTrue(inicial.salida().contains("java.lang.NumberFormatException")),
                () -> assertEquals(0, actual.codigoDeSalida()),
                () -> assertTrue(actual.salida().contains("Opcion: Opcion invalida")),
                () -> assertTrue(actual.salida().contains("Solicitudes registradas: 8")),
                () -> assertTrue(actual.salida().contains("Hasta luego")));
    }

    @Test
    @DisplayName("E1: un numero de solicitud no numerico se informa como error")
    void numeroDeSolicitudNoNumerico() throws IOException {
        List<String> entrada = List.of("2", "cuatro", "3", "4x", "0");

        Resultado inicial = ejecutarVersionInicial(entrada, DatosIniciales.EJEMPLO);
        Resultado actual = ejecutarVersionActual(entrada, DatosIniciales.EJEMPLO);

        guardar("E1-numero-de-solicitud-no-numerico", entrada, inicial, actual);
        assertAll(
                () -> assertEquals(1, inicial.codigoDeSalida()),
                () -> assertEquals(0, actual.codigoDeSalida()),
                () -> assertEquals(2, ocurrencias(actual.salida(),
                        "Error: el numero de solicitud debe ser un numero entero")),
                () -> assertTrue(actual.salida().contains("Hasta luego")));
    }

    @Test
    @DisplayName("E1: si la entrada termina sin elegir Salir, el programa termina sin error")
    void finDeLaEntrada() throws IOException {
        List<String> entrada = List.of("6");

        Resultado inicial = ejecutarVersionInicial(entrada, DatosIniciales.EJEMPLO);
        Resultado actual = ejecutarVersionActual(entrada, DatosIniciales.EJEMPLO);

        guardar("E1-fin-de-la-entrada", entrada, inicial, actual);
        assertAll(
                () -> assertEquals(1, inicial.codigoDeSalida()),
                () -> assertTrue(inicial.salida().contains("java.util.NoSuchElementException")),
                () -> assertEquals(0, actual.codigoDeSalida()),
                () -> assertFalse(actual.salida().contains("Exception")));
    }

    @Test
    @DisplayName("E2: si no se puede guardar, ya no se informa que la solicitud quedo registrada")
    void falloAlGuardar() throws IOException {
        List<String> entrada = List.of("1", "1001", "EXAMEN", "Constancia", "n", "6", "0");

        Resultado inicial = ejecutarVersionInicial(entrada, DatosIniciales.SOLICITUDES_DE_SOLO_LECTURA);
        Resultado actual = ejecutarVersionActual(entrada, DatosIniciales.SOLICITUDES_DE_SOLO_LECTURA);

        guardar("E2-fallo-al-guardar", entrada, inicial, actual);
        assertAll(
                () -> assertTrue(inicial.salida().contains("Error al guardar\nSolicitud #9 registrada")),
                () -> assertTrue(inicial.salida().contains("Solicitudes registradas: 9")),
                () -> assertTrue(actual.salida().contains("Error al guardar: no se pudo escribir")),
                () -> assertFalse(actual.salida().contains("Solicitud #9 registrada")),
                () -> assertTrue(actual.salida().contains("Solicitudes registradas: 8")));
    }

    @Test
    @DisplayName("E3: una linea mal formada se avisa y se ignora, sin perder las demas")
    void lineaMalFormada() throws IOException {
        List<String> entrada = List.of("6", "1", "1001", "EXAMEN", "Constancia", "n", "0");

        Resultado inicial = ejecutarVersionInicial(entrada, DatosIniciales.CON_LINEA_MAL_FORMADA);
        Resultado actual = ejecutarVersionActual(entrada, DatosIniciales.CON_LINEA_MAL_FORMADA);

        guardar("E3-linea-mal-formada", entrada, inicial, actual);
        assertAll(
                () -> assertTrue(inicial.salida().contains("Solicitudes registradas: 3")),
                () -> assertEquals(5, inicial.solicitudesCsv().lines().count(),
                        "la version inicial reescribe el archivo y pierde las solicitudes #4 a #8"),
                () -> assertTrue(actual.salida().contains("Aviso: se ignoro la linea 5")),
                () -> assertTrue(actual.salida().contains("Solicitudes registradas: 8")),
                () -> assertEquals(10, actual.solicitudesCsv().lines().count(),
                        "encabezado + 8 solicitudes + la nueva; la linea danada se descarta al reescribir"));
    }

    private Resultado ejecutarVersionInicial(List<String> entrada, DatosIniciales datos) {
        Path carpeta = carpetaTemporal.resolve("version-inicial");
        Resultado resultado = EjecucionDelPrograma.ejecutar(legacy.App.class, entrada, datos, carpeta);
        verificarSoloLectura(datos, carpeta);
        return resultado;
    }

    private Resultado ejecutarVersionActual(List<String> entrada, DatosIniciales datos) {
        return EjecucionDelPrograma.ejecutar(App.class, entrada, datos, carpetaTemporal.resolve("version-actual"));
    }

    /** Si las pruebas corren como administrador, el archivo de solo lectura se puede escribir igual. */
    private static void verificarSoloLectura(DatosIniciales datos, Path carpeta) {
        if (datos == DatosIniciales.SOLICITUDES_DE_SOLO_LECTURA) {
            assumeFalse(Files.isWritable(carpeta.resolve("datos/solicitudes.csv")),
                    "El usuario actual puede escribir archivos de solo lectura; no se puede simular el fallo");
        }
    }

    private static int ocurrencias(String texto, String buscado) {
        return texto.split(java.util.regex.Pattern.quote(buscado), -1).length - 1;
    }

    private static void guardar(String nombre, List<String> entrada, Resultado inicial, Resultado actual)
            throws IOException {
        Files.createDirectories(CARPETA_DE_EVIDENCIA);
        String informe = "Cambio intencional: " + nombre + "\n"
                + "Entrada: " + String.join(" | ", entrada) + "\n\n"
                + "===== VERSION INICIAL (codigo de salida " + inicial.codigoDeSalida() + ") =====\n"
                + inicial.salida() + "\n----- datos/solicitudes.csv -----\n" + inicial.solicitudesCsv() + "\n"
                + "===== VERSION ACTUAL (codigo de salida " + actual.codigoDeSalida() + ") =====\n"
                + actual.salida() + "\n----- datos/solicitudes.csv -----\n" + actual.solicitudesCsv();
        Files.writeString(CARPETA_DE_EVIDENCIA.resolve(nombre + ".txt"), informe, StandardCharsets.UTF_8);
    }
}
