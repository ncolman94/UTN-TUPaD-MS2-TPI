package ar.edu.utn.tupad.solicitudes.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Lectura y escritura de un archivo CSV separado por punto y coma, con una linea de encabezado. */
final class ArchivoCsv {

    static final String SEPARADOR = ";";
    private static final String FIN_DE_LINEA = "\n";
    private static final int PRIMERA_LINEA_DE_DATOS = 1;

    private final Path ruta;
    private final String encabezado;
    private final int cantidadDeColumnas;
    private final Consumer<String> avisos;

    /**
     * @param avisos recibe un aviso por cada linea que no se puede leer y se ignora
     */
    ArchivoCsv(Path ruta, String encabezado, Consumer<String> avisos) {
        this.ruta = ruta;
        this.encabezado = encabezado;
        this.cantidadDeColumnas = encabezado.split(SEPARADOR).length;
        this.avisos = avisos;
    }

    Path ruta() {
        return ruta;
    }

    /**
     * Convierte cada linea de datos con la funcion recibida. Si el archivo no existe devuelve una lista vacia.
     * Las lineas en blanco se saltean; las que no tienen la cantidad de columnas esperada o no se pueden
     * convertir se informan como aviso y se saltean, sin perder las demas.
     */
    <T> List<T> leer(Function<String[], T> convertir) {
        if (!Files.exists(ruta)) {
            return List.of();
        }
        List<String> lineas = leerLineas();
        List<T> registros = new ArrayList<>();
        for (int indice = PRIMERA_LINEA_DE_DATOS; indice < lineas.size(); indice++) {
            String linea = lineas.get(indice);
            if (!linea.isBlank()) {
                convertirLinea(linea, indice + 1, convertir, registros);
            }
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

    private List<String> leerLineas() {
        try {
            return Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + ruta, e);
        }
    }

    private <T> void convertirLinea(String linea, int numeroDeLinea, Function<String[], T> convertir,
                                    List<T> registros) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length != cantidadDeColumnas) {
            avisarLineaIgnorada(numeroDeLinea);
            return;
        }
        try {
            registros.add(convertir.apply(campos));
        } catch (IllegalArgumentException | DateTimeException e) {
            avisarLineaIgnorada(numeroDeLinea);
        }
    }

    private void avisarLineaIgnorada(int numeroDeLinea) {
        avisos.accept("Aviso: se ignoro la linea " + numeroDeLinea + " de " + ruta + " porque tiene un formato invalido");
    }
}
