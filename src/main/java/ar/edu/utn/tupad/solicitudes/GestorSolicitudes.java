package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Prioridad;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void registrarSolicitud(String legajo, String tipoIngresado, String descripcion, boolean urgente) {
        Optional<TipoSolicitud> tipo = TipoSolicitud.desde(tipoIngresado);
        String error = validarNuevaSolicitud(legajo, tipo, descripcion);
        if (error != null) {
            System.out.println("Error: " + error);
            return;
        }
        LocalDate hoy = LocalDate.now();
        Solicitud solicitud = new Solicitud();
        solicitud.id = proximoNumero();
        solicitud.legajo = legajo;
        solicitud.tipo = tipo.orElseThrow();
        solicitud.desc = descripcion.trim();
        solicitud.prioridad = solicitud.tipo.prioridad(urgente);
        solicitud.estado = EstadoSolicitud.PENDIENTE;
        solicitud.fechaAlta = hoy;
        solicitud.fechaVto = hoy.plusDays(solicitud.tipo.plazoEnDias(urgente));
        solicitudes.add(solicitud);
        guardar();
        System.out.println("Solicitud #" + solicitud.id + " registrada. Prioridad: " + solicitud.prioridad
                + ". Vence: " + solicitud.fechaVto.format(FORMATO_FECHA));
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
        if (estaCerrada(solicitud)) {
            System.out.println("Error: la solicitud #" + numero + " ya esta cerrada");
            return;
        }
        Area areaRequerida = solicitud.tipo.area();
        if (!responsable.area.equals(areaRequerida.etiqueta())) {
            System.out.println("Error: " + responsable.nombre + " no pertenece al area " + areaRequerida.etiqueta());
            return;
        }
        solicitud.responsable = responsable.id;
        guardar();
        System.out.println("Solicitud #" + numero + " asignada a " + responsable.nombre + " (" + responsable.area + ")");
    }

    public void cambiarEstado(int numero, String estadoIngresado) {
        Solicitud solicitud = buscarSolicitud(numero);
        if (solicitud == null) {
            System.out.println("Error: no existe la solicitud #" + numero);
            return;
        }
        if (estaCerrada(solicitud)) {
            System.out.println("Error: la solicitud #" + numero + " ya esta cerrada");
            return;
        }
        Optional<EstadoSolicitud> destino = EstadoSolicitud.desde(estadoIngresado);
        if (solicitud.estado == EstadoSolicitud.PENDIENTE && destino.equals(Optional.of(EstadoSolicitud.EN_PROCESO))
                && !tieneResponsable(solicitud)) {
            System.out.println("Error: la solicitud debe tener un responsable asignado");
            return;
        }
        if (destino.isEmpty() || !solicitud.estado.puedePasarA(destino.get())) {
            System.out.println("Error: no se puede pasar de " + solicitud.estado + " a "
                    + EstadoSolicitud.normalizar(estadoIngresado));
            return;
        }
        solicitud.estado = destino.get();
        if (solicitud.estado.estaCerrada()) {
            solicitud.fechaCierre = LocalDate.now();
        }
        guardar();
        System.out.println("Solicitud #" + numero + ": estado actualizado a " + solicitud.estado);
    }

    public void buscar(String estado, String tipo, String legajo, String prioridad, boolean soloVencidas) {
        LocalDate hoy = LocalDate.now();
        List<Solicitud> encontradas = new ArrayList<>();
        for (Solicitud solicitud : solicitudes) {
            if (cumpleFiltro(solicitud, estado, tipo, legajo, prioridad, soloVencidas, hoy)) {
                encontradas.add(solicitud);
            }
        }
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
            System.out.println(estado + ": " + contarPorEstado(estado));
        }
        System.out.println("-- Por tipo --");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            System.out.println(tipo + ": " + contarPorTipo(tipo));
        }
        System.out.println("-- Por area --");
        for (Area area : Area.values()) {
            System.out.println(area.etiqueta() + ": " + contarPorArea(area));
        }
        System.out.println("Vencidas sin resolver: " + contarVencidas(hoy));
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

    private boolean estaCerrada(Solicitud solicitud) {
        return solicitud.estado.estaCerrada();
    }

    private boolean estaVencida(Solicitud solicitud, LocalDate hoy) {
        return solicitud.estado.estaAbierta() && solicitud.fechaVto.isBefore(hoy);
    }

    private boolean tieneResponsable(Solicitud solicitud) {
        return solicitud.responsable != null;
    }

    private boolean cumpleFiltro(Solicitud solicitud, String estado, String tipo, String legajo, String prioridad,
                                 boolean soloVencidas, LocalDate hoy) {
        return coincideSinMayusculas(estado, solicitud.estado.name())
                && coincideSinMayusculas(tipo, solicitud.tipo.name())
                && (sinCriterio(legajo) || solicitud.legajo.equals(legajo))
                && coincideSinMayusculas(prioridad, solicitud.prioridad.name())
                && (!soloVencidas || estaVencida(solicitud, hoy));
    }

    private boolean coincideSinMayusculas(String criterio, String valor) {
        return sinCriterio(criterio) || valor.equalsIgnoreCase(criterio);
    }

    private boolean sinCriterio(String criterio) {
        return criterio == null || criterio.isEmpty();
    }

    // ---------------------------------------------------------------- reporte

    private long contarPorEstado(EstadoSolicitud estado) {
        return solicitudes.stream().filter(s -> s.estado == estado).count();
    }

    private long contarPorTipo(TipoSolicitud tipo) {
        return solicitudes.stream().filter(s -> s.tipo == tipo).count();
    }

    private long contarPorArea(Area area) {
        return solicitudes.stream().filter(s -> s.tipo.area() == area).count();
    }

    private long contarVencidas(LocalDate hoy) {
        return solicitudes.stream().filter(s -> estaVencida(s, hoy)).count();
    }

    private String promedioDeResolucionComoTexto() {
        long resueltas = 0;
        long diasTotales = 0;
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.estado == EstadoSolicitud.RESUELTA) {
                diasTotales += ChronoUnit.DAYS.between(solicitud.fechaAlta, solicitud.fechaCierre);
                resueltas++;
            }
        }
        if (resueltas == 0) {
            return "sin datos";
        }
        return String.format("%.1f", (double) diasTotales / resueltas) + " dias";
    }

    // ---------------------------------------------------------------- busquedas

    private Solicitud buscarSolicitud(int numero) {
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.id == numero) {
                return solicitud;
            }
        }
        return null;
    }

    private Responsable buscarResponsable(String idResponsable) {
        for (Responsable responsable : responsables) {
            if (responsable.id.equalsIgnoreCase(idResponsable)) {
                return responsable;
            }
        }
        return null;
    }

    private boolean existeEstudiante(String legajo) {
        return estudiantes.stream().anyMatch(e -> e.legajo.equals(legajo));
    }

    private int proximoNumero() {
        return solicitudes.stream().mapToInt(s -> s.id).max().orElse(0) + 1;
    }

    // ---------------------------------------------------------------- presentacion

    private void imprimirLineas(List<Solicitud> lista) {
        for (Solicitud solicitud : lista) {
            System.out.println(formatearLinea(solicitud));
        }
    }

    private String formatearLinea(Solicitud solicitud) {
        String responsable = tieneResponsable(solicitud) ? solicitud.responsable : "-";
        return "#" + solicitud.id + " | " + solicitud.tipo + " | " + solicitud.prioridad + " | " + solicitud.estado
                + " | Leg: " + solicitud.legajo + " | Resp: " + responsable + " | Vence: "
                + solicitud.fechaVto.format(FORMATO_FECHA) + " | " + solicitud.desc;
    }

    // ---------------------------------------------------------------- archivos

    private void cargarEstudiantes() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_ESTUDIANTES)) {
                String[] campos = linea.split(SEPARADOR);
                Estudiante estudiante = new Estudiante();
                estudiante.legajo = campos[0];
                estudiante.nombre = campos[1];
                estudiante.carrera = campos[2];
                estudiante.email = campos[3];
                estudiantes.add(estudiante);
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private void cargarResponsables() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_RESPONSABLES)) {
                String[] campos = linea.split(SEPARADOR);
                Responsable responsable = new Responsable();
                responsable.id = campos[0];
                responsable.nombre = campos[1];
                responsable.area = campos[2];
                responsables.add(responsable);
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private void cargarSolicitudes() {
        try {
            for (String linea : leerLineasDeDatos(ARCHIVO_SOLICITUDES)) {
                solicitudes.add(solicitudDesde(linea.split(SEPARADOR, -1)));
            }
        } catch (IOException | RuntimeException e) {
            // Igual que la version inicial: ante el primer error se deja de leer (defecto E3).
        }
    }

    private Solicitud solicitudDesde(String[] campos) {
        Solicitud solicitud = new Solicitud();
        solicitud.id = Integer.parseInt(campos[0]);
        solicitud.legajo = campos[1];
        solicitud.tipo = TipoSolicitud.valueOf(campos[2]);
        solicitud.desc = campos[3];
        solicitud.prioridad = Prioridad.valueOf(campos[4]);
        solicitud.estado = EstadoSolicitud.valueOf(campos[5]);
        solicitud.responsable = campos[6].isEmpty() ? null : campos[6];
        solicitud.fechaAlta = LocalDate.parse(campos[7], FORMATO_FECHA);
        solicitud.fechaVto = LocalDate.parse(campos[8], FORMATO_FECHA);
        solicitud.fechaCierre = campos[9].isEmpty() ? null : LocalDate.parse(campos[9], FORMATO_FECHA);
        return solicitud;
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
                escritor.write(String.join(SEPARADOR, String.valueOf(solicitud.id), solicitud.legajo,
                        solicitud.tipo.name(), solicitud.desc, solicitud.prioridad.name(), solicitud.estado.name(),
                        valorOVacio(solicitud.responsable), solicitud.fechaAlta.format(FORMATO_FECHA),
                        solicitud.fechaVto.format(FORMATO_FECHA), fechaOVacio(solicitud.fechaCierre)) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar");
        }
    }

    private static String valorOVacio(String valor) {
        return valor == null ? SIN_VALOR : valor;
    }

    private static String fechaOVacio(LocalDate fecha) {
        return fecha == null ? SIN_VALOR : fecha.format(FORMATO_FECHA);
    }
}
