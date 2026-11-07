package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.aplicacion.GeneradorDeReporte;
import ar.edu.utn.tupad.solicitudes.aplicacion.ServicioSolicitudes;
import ar.edu.utn.tupad.solicitudes.consola.MenuConsola;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioEstudiantesCsv;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioResponsablesCsv;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioSolicitudesCsv;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Punto de entrada y raiz de composicion: el unico lugar que elige las implementaciones concretas
 * (archivos CSV, reloj del sistema, consola) y las conecta.
 */
public final class App {

    private static final Path CARPETA_DE_DATOS = Path.of("datos");

    private App() {
    }

    // System.err es de la JVM: no se cierra (falso positivo de PMD, ver revision CR-06).
    @SuppressWarnings("PMD.CloseResource")
    public static void main(String[] args) {
        PrintStream errores = System.err;
        try {
            armarMenu(errores::println).iniciar();
        } catch (UncheckedIOException e) {
            errores.println("Error: " + e.getMessage());
        }
    }

    private static MenuConsola armarMenu(Consumer<String> avisos) {
        Clock reloj = Clock.systemDefaultZone();
        RepositorioSolicitudesCsv solicitudes =
                new RepositorioSolicitudesCsv(CARPETA_DE_DATOS.resolve("solicitudes.csv"), avisos);
        ServicioSolicitudes servicio = new ServicioSolicitudes(solicitudes,
                new RepositorioEstudiantesCsv(CARPETA_DE_DATOS.resolve("estudiantes.csv"), avisos),
                new RepositorioResponsablesCsv(CARPETA_DE_DATOS.resolve("responsables.csv"), avisos),
                reloj);
        GeneradorDeReporte generadorDeReporte = new GeneradorDeReporte(solicitudes, reloj);
        return new MenuConsola(new Scanner(System.in), System.out, servicio, generadorDeReporte);
    }
}
