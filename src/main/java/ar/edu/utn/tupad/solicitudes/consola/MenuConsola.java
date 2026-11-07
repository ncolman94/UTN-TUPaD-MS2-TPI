package ar.edu.utn.tupad.solicitudes.consola;

import ar.edu.utn.tupad.solicitudes.aplicacion.DatosNuevaSolicitud;
import ar.edu.utn.tupad.solicitudes.aplicacion.FiltroSolicitudes;
import ar.edu.utn.tupad.solicitudes.aplicacion.GeneradorDeReporte;
import ar.edu.utn.tupad.solicitudes.aplicacion.ServicioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.ErrorDePersistencia;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.ReglaDeNegocioException;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Menu de consola: lee lo que escribe el operador, llama a los casos de uso y muestra el resultado. */
public class MenuConsola {

    private static final int OPCION_SALIR = 0;
    private static final int OPCION_INVALIDA = -1;
    private static final String RESPUESTA_SI = "s";

    private final Scanner entrada;
    private final PrintStream salida;
    private final ServicioSolicitudes servicio;
    private final GeneradorDeReporte generadorDeReporte;
    private final List<Opcion> opciones;

    /** Una opcion del menu: el mismo registro sirve para mostrarla y para ejecutarla. */
    private record Opcion(int numero, String texto, Runnable accion) {
    }

    public MenuConsola(Scanner entrada, PrintStream salida, ServicioSolicitudes servicio,
                       GeneradorDeReporte generadorDeReporte) {
        this.entrada = entrada;
        this.salida = salida;
        this.servicio = servicio;
        this.generadorDeReporte = generadorDeReporte;
        this.opciones = List.of(
                new Opcion(1, "Registrar solicitud", this::registrarSolicitud),
                new Opcion(2, "Asignar responsable", this::asignarResponsable),
                new Opcion(3, "Cambiar estado", this::cambiarEstado),
                new Opcion(4, "Buscar solicitudes", this::buscarSolicitudes),
                new Opcion(5, "Reporte", this::mostrarReporte),
                new Opcion(6, "Listar todas", this::listarTodas),
                new Opcion(OPCION_SALIR, "Salir", () -> salida.println("Hasta luego")));
    }

    /** Muestra el menu hasta que el operador elige salir o se termina la entrada. */
    public void iniciar() {
        try {
            int opcion;
            do {
                mostrarMenu();
                opcion = numeroEntero(leerLinea()).orElse(OPCION_INVALIDA);
                ejecutar(opcion);
            } while (opcion != OPCION_SALIR);
        } catch (FinDeEntrada e) {
            salida.println();
        }
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
        opciones.forEach(opcion -> salida.println(opcion.numero() + ". " + opcion.texto()));
        salida.print("Opcion: ");
    }

    private void ejecutar(int numero) {
        try {
            opciones.stream()
                    .filter(opcion -> opcion.numero() == numero)
                    .findFirst()
                    .ifPresentOrElse(opcion -> opcion.accion().run(), () -> salida.println("Opcion invalida"));
        } catch (ReglaDeNegocioException e) {
            salida.println("Error: " + e.getMessage());
        } catch (ErrorDePersistencia e) {
            salida.println("Error al guardar: " + e.getMessage() + ". El cambio no quedo guardado.");
        }
    }

    private void registrarSolicitud() {
        String legajo = preguntar("Legajo: ");
        String tipo = preguntar("Tipo (" + opciones(TipoSolicitud.values()) + "): ");
        String descripcion = preguntar("Descripcion: ");
        String urgente = preguntar("Urgente (s/n): ");
        Solicitud solicitud = servicio.registrar(new DatosNuevaSolicitud(legajo, tipo, descripcion,
                urgente.equals(RESPUESTA_SI) || urgente.equals("S")));
        salida.println(referencia(solicitud.numero()) + " registrada. Prioridad: " + solicitud.prioridad()
                + ". Vence: " + FormatoConsola.fecha(solicitud.fechaDeVencimiento()));
    }

    private void asignarResponsable() {
        int numero = numeroDeSolicitud(preguntar("Numero de solicitud: "));
        String idResponsable = preguntar("Id del responsable: ");
        Responsable responsable = servicio.asignarResponsable(numero, idResponsable);
        salida.println(referencia(numero) + " asignada a " + responsable.nombre() + " ("
                + responsable.area().etiqueta() + ")");
    }

    private void cambiarEstado() {
        int numero = numeroDeSolicitud(preguntar("Numero de solicitud: "));
        String estado = preguntar("Nuevo estado ("
                + opciones(EstadoSolicitud.destinosPosibles().toArray(EstadoSolicitud[]::new)) + "): ");
        EstadoSolicitud nuevoEstado = servicio.cambiarEstado(numero, estado);
        salida.println(referencia(numero) + ": estado actualizado a " + nuevoEstado);
    }

    private void buscarSolicitudes() {
        FiltroSolicitudes filtro = FiltroSolicitudes.sinCriterios()
                .conEstado(preguntar("Estado (enter para omitir): "))
                .conTipo(preguntar("Tipo (enter para omitir): "))
                .conLegajo(preguntar("Legajo (enter para omitir): "))
                .conPrioridad(preguntar("Prioridad (enter para omitir): "))
                .conSoloVencidas(preguntar("Solo vencidas (s/n): ").equalsIgnoreCase(RESPUESTA_SI));
        List<Solicitud> encontradas = servicio.buscar(filtro);
        if (encontradas.isEmpty()) {
            salida.println("No se encontraron solicitudes");
            return;
        }
        salida.println("Se encontraron " + encontradas.size() + " solicitudes:");
        mostrar(encontradas);
    }

    private void mostrarReporte() {
        FormatoConsola.reporte(generadorDeReporte.generar()).forEach(salida::println);
    }

    private void listarTodas() {
        List<Solicitud> todas = servicio.todas();
        if (todas.isEmpty()) {
            salida.println("No hay solicitudes cargadas");
            return;
        }
        salida.println("Solicitudes registradas: " + todas.size());
        mostrar(todas);
    }

    private void mostrar(List<Solicitud> solicitudes) {
        solicitudes.stream().map(FormatoConsola::linea).forEach(salida::println);
    }

    private String preguntar(String texto) {
        salida.print(texto);
        return leerLinea();
    }

    private String leerLinea() {
        if (!entrada.hasNextLine()) {
            throw new FinDeEntrada();
        }
        return entrada.nextLine();
    }

    private static int numeroDeSolicitud(String texto) {
        return numeroEntero(texto).orElseThrow(
                () -> new ReglaDeNegocioException("el numero de solicitud debe ser un numero entero"));
    }

    private static OptionalInt numeroEntero(String texto) {
        try {
            return OptionalInt.of(Integer.parseInt(texto.trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /** La entrada se termino (por ejemplo, fin de archivo) antes de elegir "Salir". */
    private static final class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static String referencia(int numeroDeSolicitud) {
        return "Solicitud #" + numeroDeSolicitud;
    }

    private static String opciones(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).collect(Collectors.joining("/"));
    }
}
