package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {

    private static final int OPCION_SALIR = 0;

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
    }

    private void registrarSolicitud() {
        String legajo = preguntar("Legajo: ");
        String tipo = preguntar("Tipo (" + opciones(TipoSolicitud.values()) + "): ");
        String descripcion = preguntar("Descripcion: ");
        String urgente = preguntar("Urgente (s/n): ");
        gestor.registrarSolicitud(legajo, tipo, descripcion, urgente.equals("s") || urgente.equals("S"));
    }

    private void asignarResponsable() {
        int numero = Integer.parseInt(preguntar("Numero de solicitud: "));
        String idResponsable = preguntar("Id del responsable: ");
        gestor.asignarResponsable(numero, idResponsable);
    }

    private void cambiarEstado() {
        int numero = Integer.parseInt(preguntar("Numero de solicitud: "));
        String estado = preguntar("Nuevo estado ("
                + opciones(EstadoSolicitud.destinosPosibles().toArray(EstadoSolicitud[]::new)) + "): ");
        gestor.cambiarEstado(numero, estado);
    }

    private void buscarSolicitudes() {
        String estado = preguntar("Estado (enter para omitir): ");
        String tipo = preguntar("Tipo (enter para omitir): ");
        String legajo = preguntar("Legajo (enter para omitir): ");
        String prioridad = preguntar("Prioridad (enter para omitir): ");
        String soloVencidas = preguntar("Solo vencidas (s/n): ");
        gestor.buscar(estado, tipo, legajo, prioridad, soloVencidas.equalsIgnoreCase("s"));
    }

    private static String opciones(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).collect(Collectors.joining("/"));
    }

    private String preguntar(String texto) {
        System.out.print(texto);
        return entrada.nextLine();
    }
}
