package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.aplicacion.DatosNuevaSolicitud;
import ar.edu.utn.tupad.solicitudes.aplicacion.FiltroSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.ReglaDeNegocioException;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {

    private static final int OPCION_SALIR = 0;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GestorSolicitudes gestor;
    private final Scanner entrada;

    App(GestorSolicitudes gestor, Scanner entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        GestorSolicitudes gestor = new GestorSolicitudes();
        gestor.cargar();
        new App(gestor, new Scanner(System.in)).iniciar();
    }

    void iniciar() {
        int opcion;
        do {
            mostrarMenu();
            opcion = Integer.parseInt(entrada.nextLine());
            ejecutar(opcion);
        } while (opcion != OPCION_SALIR);
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
        System.out.println("1. Registrar solicitud");
        System.out.println("2. Asignar responsable");
        System.out.println("3. Cambiar estado");
        System.out.println("4. Buscar solicitudes");
        System.out.println("5. Reporte");
        System.out.println("6. Listar todas");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private void ejecutar(int opcion) {
        try {
            switch (opcion) {
                case 1 -> registrarSolicitud();
                case 2 -> asignarResponsable();
                case 3 -> cambiarEstado();
                case 4 -> buscarSolicitudes();
                case 5 -> gestor.reporte();
                case 6 -> gestor.listar();
                case OPCION_SALIR -> System.out.println("Hasta luego");
                default -> System.out.println("Opcion invalida");
            }
        } catch (ReglaDeNegocioException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registrarSolicitud() {
        String legajo = preguntar("Legajo: ");
        String tipo = preguntar("Tipo (" + opciones(TipoSolicitud.values()) + "): ");
        String descripcion = preguntar("Descripcion: ");
        String urgente = preguntar("Urgente (s/n): ");
        Solicitud solicitud = gestor.registrarSolicitud(new DatosNuevaSolicitud(legajo, tipo, descripcion,
                urgente.equals("s") || urgente.equals("S")));
        System.out.println("Solicitud #" + solicitud.numero() + " registrada. Prioridad: " + solicitud.prioridad()
                + ". Vence: " + solicitud.fechaDeVencimiento().format(FORMATO_FECHA));
    }

    private void asignarResponsable() {
        int numero = Integer.parseInt(preguntar("Numero de solicitud: "));
        String idResponsable = preguntar("Id del responsable: ");
        Responsable responsable = gestor.asignarResponsable(numero, idResponsable);
        System.out.println("Solicitud #" + numero + " asignada a " + responsable.nombre() + " ("
                + responsable.area().etiqueta() + ")");
    }

    private void cambiarEstado() {
        int numero = Integer.parseInt(preguntar("Numero de solicitud: "));
        String estado = preguntar("Nuevo estado ("
                + opciones(EstadoSolicitud.destinosPosibles().toArray(EstadoSolicitud[]::new)) + "): ");
        EstadoSolicitud nuevoEstado = gestor.cambiarEstado(numero, estado);
        System.out.println("Solicitud #" + numero + ": estado actualizado a " + nuevoEstado);
    }

    private void buscarSolicitudes() {
        FiltroSolicitudes filtro = FiltroSolicitudes.sinCriterios()
                .conEstado(preguntar("Estado (enter para omitir): "))
                .conTipo(preguntar("Tipo (enter para omitir): "))
                .conLegajo(preguntar("Legajo (enter para omitir): "))
                .conPrioridad(preguntar("Prioridad (enter para omitir): "))
                .conSoloVencidas(preguntar("Solo vencidas (s/n): ").equalsIgnoreCase("s"));
        gestor.buscar(filtro);
    }

    private static String opciones(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).collect(Collectors.joining("/"));
    }

    private String preguntar(String texto) {
        System.out.print(texto);
        return entrada.nextLine();
    }
}
