package ar.edu.utn.tupad.solicitudes.caracterizacion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Ejecuta un programa de consola como proceso separado, igual que lo usaria una persona:
 * en una carpeta de trabajo propia, con una copia de los datos y una entrada fija.
 * Devuelve todo lo observable desde afuera: salida, codigo de salida y archivos CSV.
 */
final class EjecucionDelPrograma {

    private static final long SEGUNDOS_MAXIMOS = 30;

    private EjecucionDelPrograma() {
    }

    /** Lo que se observa del programa al terminar. */
    record Resultado(String salida, int codigoDeSalida, String estudiantesCsv, String responsablesCsv,
                     String solicitudesCsv) {
    }

    static Resultado ejecutar(Class<?> clasePrincipal, List<String> lineasDeEntrada, DatosIniciales datos,
                              Path carpetaDeTrabajo) {
        try {
            datos.prepararEn(carpetaDeTrabajo);
            Path entrada = carpetaDeTrabajo.resolve("entrada.txt");
            Files.write(entrada, lineasDeEntrada, StandardCharsets.UTF_8);

            Process proceso = new ProcessBuilder(rutaDelJava(), "-Duser.language=es", "-Duser.country=AR",
                    "-cp", classpathDe(clasePrincipal), clasePrincipal.getName())
                    .directory(carpetaDeTrabajo.toFile())
                    .redirectInput(entrada.toFile())
                    .redirectErrorStream(true)
                    .start();
            String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!proceso.waitFor(SEGUNDOS_MAXIMOS, TimeUnit.SECONDS)) {
                proceso.destroyForcibly();
                throw new IllegalStateException("El programa no termino en " + SEGUNDOS_MAXIMOS + " segundos");
            }
            return new Resultado(normalizar(salida), proceso.exitValue(),
                    leerSiExiste(carpetaDeTrabajo.resolve("datos/estudiantes.csv")),
                    leerSiExiste(carpetaDeTrabajo.resolve("datos/responsables.csv")),
                    leerSiExiste(carpetaDeTrabajo.resolve("datos/solicitudes.csv")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static String rutaDelJava() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String classpathDe(Class<?> clase) {
        try {
            return Path.of(clase.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String leerSiExiste(Path archivo) throws IOException {
        return Files.exists(archivo) ? Files.readString(archivo, StandardCharsets.UTF_8) : "<no existe>";
    }

    /** Unifica los saltos de linea para que la comparacion no dependa del sistema operativo. */
    private static String normalizar(String texto) {
        return texto.replace("\r\n", "\n");
    }

    /** Conjuntos de datos con los que puede arrancar un escenario. */
    enum DatosIniciales {
        /** Los archivos de ejemplo de la carpeta datos/ del proyecto. */
        EJEMPLO,
        /** Estudiantes y responsables de ejemplo, solicitudes.csv solo con el encabezado. */
        SIN_SOLICITUDES,
        /** Estudiantes y responsables de ejemplo, sin el archivo solicitudes.csv. */
        SIN_ARCHIVO_DE_SOLICITUDES,
        /** Datos de ejemplo con solicitudes.csv de solo lectura: cualquier guardado falla. */
        SOLICITUDES_DE_SOLO_LECTURA,
        /** Datos de ejemplo con una linea mal formada despues de la solicitud #3. */
        CON_LINEA_MAL_FORMADA;

        static final String LINEA_MAL_FORMADA = "XX;linea;danada";

        private static final Path DATOS_DEL_PROYECTO = Path.of("datos");

        void prepararEn(Path carpetaDeTrabajo) throws IOException {
            Path destino = Files.createDirectories(carpetaDeTrabajo.resolve("datos"));
            try (Stream<Path> archivos = Files.list(DATOS_DEL_PROYECTO)) {
                for (Path archivo : archivos.toList()) {
                    Files.copy(archivo, destino.resolve(archivo.getFileName()));
                }
            }
            Path solicitudes = destino.resolve("solicitudes.csv");
            if (this == SIN_SOLICITUDES) {
                String encabezado = Files.readAllLines(solicitudes, StandardCharsets.UTF_8).get(0);
                Files.writeString(solicitudes, encabezado + "\n", StandardCharsets.UTF_8);
            } else if (this == SIN_ARCHIVO_DE_SOLICITUDES) {
                Files.delete(solicitudes);
            } else if (this == SOLICITUDES_DE_SOLO_LECTURA) {
                solicitudes.toFile().setWritable(false);
            } else if (this == CON_LINEA_MAL_FORMADA) {
                List<String> lineas = new java.util.ArrayList<>(Files.readAllLines(solicitudes, StandardCharsets.UTF_8));
                lineas.add(4, LINEA_MAL_FORMADA);
                Files.write(solicitudes, lineas, StandardCharsets.UTF_8);
            }
        }
    }
}
