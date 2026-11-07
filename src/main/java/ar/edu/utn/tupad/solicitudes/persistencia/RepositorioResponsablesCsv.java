package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioResponsables;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Responsables leidos de un CSV con columnas id;nombre;area. */
public class RepositorioResponsablesCsv implements RepositorioResponsables {

    private static final String ENCABEZADO = "id;nombre;area";

    private final List<Responsable> responsables;

    public RepositorioResponsablesCsv(Path ruta, Consumer<String> avisos) {
        this.responsables = new ArchivoCsv(ruta, ENCABEZADO, avisos)
                .leer(campos -> new Responsable(campos[0], campos[1], Area.desdeEtiqueta(campos[2])));
    }

    @Override
    public Optional<Responsable> buscarPorId(String id) {
        return responsables.stream().filter(responsable -> responsable.id().equalsIgnoreCase(id)).findFirst();
    }
}
