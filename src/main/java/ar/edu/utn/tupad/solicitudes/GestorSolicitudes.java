package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.aplicacion.DatosNuevaSolicitud;
import ar.edu.utn.tupad.solicitudes.aplicacion.FiltroSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.DatosDeSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Estudiante;
import ar.edu.utn.tupad.solicitudes.dominio.Pedido;
import ar.edu.utn.tupad.solicitudes.dominio.Prioridad;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Gestiona las solicitudes academicas: carga y guarda los archivos, aplica las reglas de negocio
 * y muestra los resultados por consola.
 */
public class GestorSolicitudes {

    private static final String ARCHIVO_ESTUDIANTES = "datos/estudiantes.csv";
    private static final String ARCHIVO_RESPONSABLES = "datos/responsables.csv";
    private static final String ARCHIVO_SOLICITUDES = "datos/solicitudes.csv";
    private static final String ENCABEZADO_SOLICITUDES =
            "id;legajo;tipo;descripcion;prioridad;estado;responsable;fechaAlta;fechaVencimiento;fechaCierre";
    private static final String SEPARADOR = ";";
    private static final String SIN_VALOR = "";
    private static final int LARGO_MAXIMO_DESCRIPCION = 200;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<Solicitud> solicitudes = new ArrayList<>();
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();

    public void cargar() {
        cargarEstudiantes();
        cargarResponsables();
        cargarSolicitudes();
    }

    // ---------------------------------------------------------------- casos de uso

    public void registrarSolicitud(DatosNuevaSolicitud datos) {
        Optional<TipoSolicitud> tipo = TipoSolicitud.desde(datos.tipo());
        String error = validarNuevaSolicitud(datos.legajo(), tipo, datos.descripcion());
        if (error != null) {
            System.out.println("Error: " + error);
            return;
        }
        Pedido pedido = new Pedido(datos.legajo(), tipo.orElseThrow(), datos.descripcion().trim(), datos.urgente());
        Solicitud solicitud = Solicitud.registrar(proximoNumero(), pedido, LocalDate.now());
        solicitudes.add(solicitud);
        guardar();
        System.out.println("Solicitud #" + solicitud.numero() + " registrada. Prioridad: " + solicitud.prioridad()
                + ". Vence: " + solicitud.fechaDeVencimiento().format(FORMATO_FECHA));
    }

    public void asignarResponsable(int numero, String idResponsable) {
        Solicitud solicitud = buscarSolicitud(numero);
        if (solicitud == null) {
            System.out.println("Error: no existe la solicitud #" + numero);
            return;
        }
        Responsable responsable = buscarResponsable(idResponsable);
        if (responsable == null) {
            System.out.println("Error: no existe el responsable " + idResponsable);
            return;
        }
        if (solicitud.estaCerrada()) {
            System.out.println("Error: la solicitud #" + numero + " ya esta cerrada");
            return;
        }
        Area areaRequerida = solicitud.tipo().area();
        if (!responsable.perteneceA(areaRequerida)) {
            System.out.println("Error: " + responsable.nombre() + " no pertenece al area " + areaRequerida.etiqueta());
            return;
        }
        solicitud.asignar(responsable);
        guardar();
        System.out.println("Solicitud #" + numero + " asignada a " + responsable.nombre() + " ("
                + responsable.area().etiqueta() + ")");
    }

    public void cambiarEstado(int numero, String estadoIngresado) {
        Solicitud solicitud = buscarSolicitud(numero);
        if (solicitud == null) {
            System.out.println("Error: no existe la solicitud #" + numero);
            return;
        }
        if (solicitud.estaCerrada()) {
            System.out.println("Error: la solicitud #" + numero + " ya esta cerrada");
            return;
        }
        Optional<EstadoSolicitud> destino = EstadoSolicitud.desde(estadoIngresado);
        if (solicitud.estado() == EstadoSolicitud.PENDIENTE && destino.equals(Optional.of(EstadoSolicitud.EN_PROCESO))
                && !solicitud.tieneResponsable()) {
            System.out.println("Error: la solicitud debe tener un responsable asignado");
            return;
        }
        if (destino.isEmpty() || !solicitud.estado().puedePasarA(destino.get())) {
            System.out.println("Error: no se puede pasar de " + solicitud.estado() + " a "
                    + EstadoSolicitud.normalizar(estadoIngresado));
            return;
        }
        solicitud.cambiarEstado(destino.get(), LocalDate.now());
        guardar();
        System.out.println("Solicitud #" + numero + ": estado actualizado a " + solicitud.estado());
    }

    public void buscar(FiltroSolicitudes filtro) {
        LocalDate hoy = LocalDate.now();
        List<Solicitud> encontradas = solicitudes.stream().filter(s -> filtro.admite(s, hoy)).toList();
        if (encontradas.isEmpty()) {
            System.out.println("No se encontraron solicitudes");
            return;
        }
        System.out.println("Se encontraron " + encontradas.size() + " solicitudes:");
        imprimirLineas(encontradas);
    }

    public void listar() {
        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes cargadas");
            return;
        }
        System.out.println("Solicitudes registradas: " + solicitudes.size());
        imprimirLineas(solicitudes);
    }

    public void reporte() {
        LocalDate hoy = LocalDate.now();
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total de solicitudes: " + solicitudes.size());
        System.out.println("-- Por estado --");
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            System.out.println(estado + ": " + solicitudes.stream().filter(s -> s.estado() == estado).count());
        }
        System.out.println("-- Por tipo --");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            System.out.println(tipo + ": " + solicitudes.stream().filter(s -> s.tipo() == tipo).count());
        }
        System.out.println("-- Por area --");
        for (Area area : Area.values()) {
            System.out.println(area.etiqueta() + ": "
                    + solicitudes.stream().filter(s -> s.tipo().area() == area).count());
        }
        System.out.println("Vencidas sin resolver: " + solicitudes.stream().filter(s -> s.estaVencida(hoy)).count());
        System.out.println("Tiempo promedio de resolucion: " + promedioDeResolucionComoTexto());
    }

    // ---------------------------------------------------------------- reglas de negocio

    /** Devuelve el mensaje de error de la primera validacion que falla, o null si los datos son validos. */
    private String validarNuevaSolicitud(String legajo, Optional<TipoSolicitud> tipo, String descripcion) {
        if (!existeEstudiante(legajo)) {
            return "no existe un estudiante con legajo " + legajo;
        }
        if (tipo.isEmpty()) {
            return "tipo de solicitud invalido";
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return "la descripcion es obligatoria";
        }
        if (descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            return "la descripcion no puede superar los " + LARGO_MAXIMO_DESCRIPCION + " caracteres";
        }
        return null;
    }

    private String promedioDeResolucionComoTexto() {
        OptionalDouble promedio = solicitudes.stream()
                .map(Solicitud::diasDeResolucion)
                .filter(dias -> dias.isPresent())
                .mapToLong(dias -> dias.getAsLong())
                .average();
        return promedio.isPresent() ? String.format("%.1f", promedio.getAsDouble()) + " dias" : "sin datos";
    }

    // ---------------------------------------------------------------- busquedas

    private Solicitud buscarSolicitud(int numero) {
        return solicitudes.stream().filter(s -> s.numero() == numero).findFirst().orElse(null);
    }

    private Responsable buscarResponsable(String idResponsable) {
        return responsables.stream().filter(r -> r.id().equalsIgnoreCase(idResponsable)).findFirst().orElse(null);
    }

    private boolean existeEstudiante(String legajo) {
        return estudiantes.stream().anyMatch(e -> e.legajo().equals(legajo));
    }

    private int proximoNumero() {
        return solicitudes.stream().mapToInt(Solicitud::numero).max().orElse(0) + 1;
    }

    // ---------------------------------------------------------------- presentacion

    private void imprimirLineas(List<Solicitud> lista) {
        for (Solicitud solicitud : lista) {
            System.out.println(formatearLinea(solicitud));
        }
    }

    private String formatearLinea(Solicitud solicitud) {
        return "#" + solicitud.numero() + " | " + solicitud.tipo() + " | " + solicitud.prioridad() + " | "
                + solicitud.estado() + " | Leg: " + solicitud.legajo() + " | Resp: "
                + solicitud.idResponsable().orElse("-") + " | Vence: "
                + solicitud.fechaDeVencimiento().format(FORMATO_FECHA) + " | " + solicitud.descripcion();
    }

    // ---------------------------------------------------------------- archivos

    private void cargarEstudiantes() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_ESTUDIANTES)) {
                String[] campos = linea.split(SEPARADOR);
                estudiantes.add(new Estudiante(campos[0], campos[1], campos[2], campos[3]));
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private void cargarResponsables() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_RESPONSABLES)) {
                String[] campos = linea.split(SEPARADOR);
                responsables.add(new Responsable(campos[0], campos[1], Area.desdeEtiqueta(campos[2])));
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private void cargarSolicitudes() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_SOLICITUDES)) {
                solicitudes.add(Solicitud.restaurar(datosDesde(linea.split(SEPARADOR, -1))));
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private DatosDeSolicitud datosDesde(String[] campos) {
        return new DatosDeSolicitud(Integer.parseInt(campos[0]), campos[1], TipoSolicitud.valueOf(campos[2]),
                campos[3], Prioridad.valueOf(campos[4]), EstadoSolicitud.valueOf(campos[5]),
                campos[6].isEmpty() ? null : campos[6], fechaDesde(campos[7]), fechaDesde(campos[8]),
                fechaDesde(campos[9]));
    }

    private static LocalDate fechaDesde(String texto) {
        return texto.isEmpty() ? null : LocalDate.parse(texto, FORMATO_FECHA);
    }

    /**
     * Devuelve las lineas del archivo sin el encabezado. Si una linea no se puede convertir,
     * el error corta la carga en el metodo que la llama, como en la version inicial.
     */
    private List<String> leerLineasDeDatos(String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            lector.readLine();
            String linea = lector.readLine();
            while (linea != null) {
                lineas.add(linea);
                linea = lector.readLine();
            }
        }
        return lineas;
    }

    private void guardar() {
        try (FileWriter escritor = new FileWriter(ARCHIVO_SOLICITUDES)) {
            escritor.write(ENCABEZADO_SOLICITUDES + "\n");
            for (Solicitud solicitud : solicitudes) {
                escritor.write(lineaDeArchivo(solicitud.datos()) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar");
        }
    }

    private static String lineaDeArchivo(DatosDeSolicitud datos) {
        return String.join(SEPARADOR, String.valueOf(datos.numero()), datos.legajo(), datos.tipo().name(),
                datos.descripcion(), datos.prioridad().name(), datos.estado().name(),
                datos.idResponsable() == null ? SIN_VALOR : datos.idResponsable(),
                fechaComoTexto(datos.fechaDeAlta()), fechaComoTexto(datos.fechaDeVencimiento()),
                fechaComoTexto(datos.fechaDeCierre()));
    }

    private static String fechaComoTexto(LocalDate fecha) {
        return fecha == null ? SIN_VALOR : fecha.format(FORMATO_FECHA);
    }
}
