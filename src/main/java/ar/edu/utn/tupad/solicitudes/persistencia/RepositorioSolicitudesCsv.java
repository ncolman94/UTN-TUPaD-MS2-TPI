package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.DatosDeSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.ErrorDePersistencia;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Prioridad;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Solicitudes guardadas en un CSV. Se leen una vez al crear el repositorio y el archivo se reescribe
 * completo en cada guardado. El formato del archivo es el mismo de la version inicial.
 */
public class RepositorioSolicitudesCsv implements RepositorioSolicitudes {

    private static final String ENCABEZADO =
            "id;legajo;tipo;descripcion;prioridad;estado;responsable;fechaAlta;fechaVencimiento;fechaCierre";
    private static final String SIN_VALOR = "";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ArchivoCsv archivo;
    private final List<Solicitud> solicitudes;

    /**
     * @param avisos recibe un aviso por cada linea del archivo que no se puede leer y se ignora
     */
    public RepositorioSolicitudesCsv(Path ruta, Consumer<String> avisos) {
        this.archivo = new ArchivoCsv(ruta, ENCABEZADO, avisos);
        this.solicitudes = new ArrayList<>(archivo.leer(RepositorioSolicitudesCsv::desdeCampos));
    }

    @Override
    public List<Solicitud> todas() {
        return List.copyOf(solicitudes);
    }

    @Override
    public Optional<Solicitud> buscarPorNumero(int numero) {
        return solicitudes.stream().filter(solicitud -> solicitud.numero() == numero).findFirst();
    }

    @Override
    public int proximoNumero() {
        return solicitudes.stream().mapToInt(Solicitud::numero).max().orElse(0) + 1;
    }

    /** Escribe primero el archivo y solo despues actualiza la lista en memoria. */
    @Override
    public void guardar(Solicitud solicitud) {
        List<Solicitud> actualizadas = new ArrayList<>(solicitudes);
        ListaDeSolicitudes.agregarOReemplazar(actualizadas, solicitud);
        try {
            archivo.escribir(actualizadas.stream().map(s -> aCampos(s.datos())).toList());
        } catch (IOException e) {
            throw new ErrorDePersistencia("no se pudo escribir " + archivo.ruta(), e);
        }
        solicitudes.clear();
        solicitudes.addAll(actualizadas);
    }

    private static Solicitud desdeCampos(String[] campos) {
        return Solicitud.restaurar(new DatosDeSolicitud(Integer.parseInt(campos[0]), campos[1],
                TipoSolicitud.valueOf(campos[2]), campos[3], Prioridad.valueOf(campos[4]),
                EstadoSolicitud.valueOf(campos[5]), textoOpcional(campos[6]), fechaOpcional(campos[7]),
                fechaOpcional(campos[8]), fechaOpcional(campos[9])));
    }

    private static String[] aCampos(DatosDeSolicitud datos) {
        return new String[] {
            String.valueOf(datos.numero()), datos.legajo(), datos.tipo().name(), datos.descripcion(),
            datos.prioridad().name(), datos.estado().name(), textoOVacio(datos.idResponsable()),
            fechaOVacio(datos.fechaDeAlta()), fechaOVacio(datos.fechaDeVencimiento()),
            fechaOVacio(datos.fechaDeCierre())
        };
    }

    private static String textoOpcional(String texto) {
        return texto.isEmpty() ? null : texto;
    }

    private static LocalDate fechaOpcional(String texto) {
        return texto.isEmpty() ? null : LocalDate.parse(texto, FORMATO_FECHA);
    }

    private static String textoOVacio(String texto) {
        return texto == null ? SIN_VALOR : texto;
    }

    private static String fechaOVacio(LocalDate fecha) {
        return fecha == null ? SIN_VALOR : fecha.format(FORMATO_FECHA);
    }
}
