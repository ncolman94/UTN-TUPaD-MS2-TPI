package legacy;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        GestorSolicitudes g = new GestorSolicitudes();
        g.cargar();
        Scanner sc = new Scanner(System.in);
        int op = -1;
        while (op != 0) {
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
            op = Integer.parseInt(sc.nextLine());
            switch (op) {
                case 1:
                    System.out.print("Legajo: ");
                    String leg = sc.nextLine();
                    System.out.print("Tipo (CERTIFICADO/EXAMEN/EQUIVALENCIA/REVISION/BAJA): ");
                    String t = sc.nextLine();
                    System.out.print("Descripcion: ");
                    String d = sc.nextLine();
                    System.out.print("Urgente (s/n): ");
                    String u = sc.nextLine();
                    g.registrarSolicitud(leg, t, d, u.equals("s") || u.equals("S"));
                    break;
                case 2:
                    System.out.print("Numero de solicitud: ");
                    int id = Integer.parseInt(sc.nextLine());
                    System.out.print("Id del responsable: ");
                    String r = sc.nextLine();
                    g.asignarResponsable(id, r);
                    break;
                case 3:
                    System.out.print("Numero de solicitud: ");
                    int id2 = Integer.parseInt(sc.nextLine());
                    System.out.print("Nuevo estado (EN_PROCESO/RESUELTA/RECHAZADA): ");
                    String e = sc.nextLine();
                    g.cambiarEstado(id2, e);
                    break;
                case 4:
                    System.out.print("Estado (enter para omitir): ");
                    String es = sc.nextLine();
                    System.out.print("Tipo (enter para omitir): ");
                    String ti = sc.nextLine();
                    System.out.print("Legajo (enter para omitir): ");
                    String le = sc.nextLine();
                    System.out.print("Prioridad (enter para omitir): ");
                    String pr = sc.nextLine();
                    System.out.print("Solo vencidas (s/n): ");
                    String sv = sc.nextLine();
                    g.buscar(es, ti, le, pr, sv.equalsIgnoreCase("s"));
                    break;
                case 5:
                    g.reporte();
                    break;
                case 6:
                    g.listar();
                    break;
                // case 7:
                //     g.borrar(id);
                //     break;
                case 0:
                    System.out.println("Hasta luego");
                    break;
                default:
                    System.out.println("Opcion invalida");
            }
        }
    }
}
