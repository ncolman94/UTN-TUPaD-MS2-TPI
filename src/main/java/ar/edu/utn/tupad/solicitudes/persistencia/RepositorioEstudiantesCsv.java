package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.Estudiante;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioEstudiantes;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** Estudiantes leidos de un CSV con columnas legajo;nombre;carrera;email. */
public class RepositorioEstudiantesCsv implements RepositorioEstudiantes {

    private static final String ENCABEZADO = "legajo;nombre;carrera;email";

    private final List<Estudiante> estudiantes;

    public RepositorioEstudiantesCsv(Path ruta, Consumer<String> avisos) {
        this.estudiantes = new ArchivoCsv(ruta, ENCABEZADO, avisos)
                .leer(campos -> new Estudiante(campos[0], campos[1], campos[2], campos[3]));
    }

    @Override
    public boolean existe(String legajo) {
        return estudiantes.stream().anyMatch(estudiante -> estudiante.legajo().equals(legajo));
    }
}
