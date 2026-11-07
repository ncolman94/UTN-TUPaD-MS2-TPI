package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.aplicacion.GeneradorDeReporte;
import ar.edu.utn.tupad.solicitudes.aplicacion.ServicioSolicitudes;
import ar.edu.utn.tupad.solicitudes.consola.MenuConsola;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioEstudiantesCsv;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioResponsablesCsv;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioSolicitudesCsv;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;

/**
 * Punto de entrada y raiz de composicion: el unico lugar que elige las implementaciones concretas
 * (archivos CSV, reloj del sistema, consola) y las conecta.
 */
public final class App {

    private static final Path CARPETA_DE_DATOS = Path.of("datos");

    private App() {
    }

    public static void main(String[] args) {
        Clock reloj = Clock.systemDefaultZone();
        RepositorioSolicitudesCsv solicitudes = new RepositorioSolicitudesCsv(
                CARPETA_DE_DATOS.resolve("solicitudes.csv"), System.out::println);
        ServicioSolicitudes servicio = new ServicioSolicitudes(solicitudes,
                new RepositorioEstudiantesCsv(CARPETA_DE_DATOS.resolve("estudiantes.csv")),
                new RepositorioResponsablesCsv(CARPETA_DE_DATOS.resolve("responsables.csv")),
                reloj);
        GeneradorDeReporte generadorDeReporte = new GeneradorDeReporte(solicitudes, reloj);
        new MenuConsola(new Scanner(System.in), System.out, servicio, generadorDeReporte).iniciar();
    }
}
