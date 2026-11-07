package ar.edu.utn.tupad.solicitudes.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Lectura y escritura de un archivo CSV separado por punto y coma, con una linea de encabezado. */
final class ArchivoCsv {

    static final String SEPARADOR = ";";
    private static final String FIN_DE_LINEA = "\n";

    private final Path ruta;
    private final String encabezado;

    ArchivoCsv(Path ruta, String encabezado) {
        this.ruta = ruta;
        this.encabezado = encabezado;
    }

    /**
     * Convierte cada linea de datos con la funcion recibida. Si el archivo no existe devuelve una lista vacia.
     * Ante la primera linea que no se puede convertir deja de leer, como la version inicial (defecto E3).
     */
    <T> List<T> leer(Function<String[], T> convertir) {
        List<T> registros = new ArrayList<>();
        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            for (String linea : lineas.subList(Math.min(1, lineas.size()), lineas.size())) {
                registros.add(convertir.apply(linea.split(SEPARADOR, -1)));
            }
        } catch (IOException | IllegalArgumentException | DateTimeException | IndexOutOfBoundsException e) {
            return registros;
        }
        return registros;
    }

    void escribir(List<String[]> filas) throws IOException {
        StringBuilder contenido = new StringBuilder(encabezado).append(FIN_DE_LINEA);
        for (String[] fila : filas) {
            contenido.append(String.join(SEPARADOR, fila)).append(FIN_DE_LINEA);
        }
        Files.writeString(ruta, contenido.toString(), StandardCharsets.UTF_8);
    }
}
