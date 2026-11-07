package legacy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Clase principal del sistema. Maneja todo lo de las solicitudes.
 */
public class GestorSolicitudes {

    public ArrayList<Solicitud> lista = new ArrayList<>();
    public ArrayList<Estudiante> estudiantes = new ArrayList<>();
    public ArrayList<Responsable> resp = new ArrayList<>();
    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // carga los datos de los archivos
    public void cargar() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("datos/estudiantes.csv"));
            String linea = br.readLine(); // salteo encabezado
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(";");
                Estudiante e = new Estudiante();
                e.legajo = p[0];
                e.nombre = p[1];
                e.carrera = p[2];
                e.email = p[3];
                estudiantes.add(e);
            }
            br.close();
        } catch (Exception e) {
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("datos/responsables.csv"));
            String linea = br.readLine(); // salteo encabezado
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(";");
                Responsable r = new Responsable();
                r.id = p[0];
                r.nombre = p[1];
                r.area = p[2];
                resp.add(r);
            }
            br.close();
        } catch (Exception e) {
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("datos/solicitudes.csv"));
            String linea = br.readLine(); // salteo encabezado
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(";", -1);
                Solicitud s = new Solicitud();
                s.id = Integer.parseInt(p[0]);
                s.legajo = p[1];
                s.tipo = p[2];
                s.desc = p[3];
                s.prioridad = p[4];
                s.estado = p[5];
                s.responsable = p[6];
                s.fechaAlta = p[7];
                s.fechaVto = p[8];
                s.fechaCierre = p[9];
                lista.add(s);
            }
            br.close();
        } catch (Exception e) {
        }
    }

    // guarda las solicitudes en el archivo
    public void guardar() {
        try {
            FileWriter fw = new FileWriter("datos/solicitudes.csv");
            fw.write("id;legajo;tipo;descripcion;prioridad;estado;responsable;fechaAlta;fechaVencimiento;fechaCierre\n");
            for (int i = 0; i < lista.size(); i++) {
                Solicitud s = lista.get(i);
                fw.write(s.id + ";" + s.legajo + ";" + s.tipo + ";" + s.desc + ";" + s.prioridad + ";" + s.estado + ";"
                        + s.responsable + ";" + s.fechaAlta + ";" + s.fechaVto + ";" + s.fechaCierre + "\n");
            }
            fw.close();
        } catch (Exception e) {
            System.out.println("Error al guardar");
        }
    }

    public void registrarSolicitud(String legajo, String tipo, String desc, boolean urgente) {
        // valido que exista el estudiante
        boolean existe = false;
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).legajo.equals(legajo)) {
                existe = true;
            }
        }
        if (!existe) {
            System.out.println("Error: no existe un estudiante con legajo " + legajo);
            return;
        }
        tipo = tipo.toUpperCase().trim();
        if (!tipo.equals("CERTIFICADO") && !tipo.equals("EXAMEN") && !tipo.equals("EQUIVALENCIA")
                && !tipo.equals("REVISION") && !tipo.equals("BAJA")) {
            System.out.println("Error: tipo de solicitud invalido");
            return;
        }
        if (desc == null || desc.trim().equals("")) {
            System.out.println("Error: la descripcion es obligatoria");
            return;
        }
        if (desc.length() > 200) {
            System.out.println("Error: la descripcion no puede superar los 200 caracteres");
            return;
        }
        // calculo la prioridad segun el tipo
        String p = "";
        switch (tipo) {
            case "CERTIFICADO":
                p = "BAJA";
                break;
            case "EXAMEN":
                p = "MEDIA";
                break;
            case "EQUIVALENCIA":
                p = "MEDIA";
                break;
            case "REVISION":
                p = "ALTA";
                break;
            case "BAJA":
                p = "ALTA";
                break;
        }
        if (urgente) {
            if (p.equals("BAJA")) {
                p = "MEDIA";
            } else if (p.equals("MEDIA")) {
                p = "ALTA";
            }
        }
        // calculo el plazo en dias
        int dias = 0;
        switch (tipo) {
            case "CERTIFICADO":
                dias = 3;
                break;
            case "EXAMEN":
                dias = 5;
                break;
            case "EQUIVALENCIA":
                dias = 30;
                break;
            case "REVISION":
                dias = 10;
                break;
            case "BAJA":
                dias = 7;
                break;
        }
        if (urgente) {
            dias = dias / 2;
            if (dias < 1) {
                dias = 1;
            }
        }
        // busco el id mas alto
        int max = 0;
        for (Solicitud s : lista) {
            if (s.id > max) {
                max = s.id;
            }
        }
        Solicitud s = new Solicitud();
        s.id = max + 1;
        s.legajo = legajo;
        s.tipo = tipo;
        s.desc = desc.trim();
        s.prioridad = p;
        s.estado = "PENDIENTE";
        s.responsable = "";
        LocalDate hoy = LocalDate.now();
        s.fechaAlta = hoy.format(f);
        s.fechaVto = hoy.plusDays(dias).format(f);
        s.fechaCierre = "";
        // s.fechaVto = hoy.plusDays(5).format(f); // antes era fijo
        lista.add(s);
        guardar();
        System.out.println("Solicitud #" + s.id + " registrada. Prioridad: " + s.prioridad + ". Vence: " + s.fechaVto);
    }

    public void asignarResponsable(int id, String idResp) {
        Solicitud sol = null;
        for (Solicitud s : lista) {
            if (s.id == id) {
                sol = s;
            }
        }
        if (sol == null) {
            System.out.println("Error: no existe la solicitud #" + id);
            return;
        }
        Responsable r = null;
        for (Responsable x : resp) {
            if (x.id.equalsIgnoreCase(idResp)) {
                r = x;
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + idResp);
            return;
        }
        if (sol.estado.equals("RESUELTA") || sol.estado.equals("RECHAZADA")) {
            System.out.println("Error: la solicitud #" + id + " ya esta cerrada");
            return;
        }
        // el area depende del tipo
        String area = "";
        switch (sol.tipo) {
            case "CERTIFICADO":
                area = "BEDELIA";
                break;
            case "EXAMEN":
                area = "BEDELIA";
                break;
            case "BAJA":
                area = "BEDELIA";
                break;
            case "EQUIVALENCIA":
                area = "SECRETARIA ACADEMICA";
                break;
            case "REVISION":
                area = "SECRETARIA ACADEMICA";
                break;
        }
        if (!r.area.equals(area)) {
            System.out.println("Error: " + r.nombre + " no pertenece al area " + area);
            return;
        }
        sol.responsable = r.id;
        guardar();
        System.out.println("Solicitud #" + id + " asignada a " + r.nombre + " (" + r.area + ")");
    }

    public void cambiarEstado(int id, String nuevo) {
        Solicitud sol = null;
        for (Solicitud s : lista) {
            if (s.id == id) {
                sol = s;
            }
        }
        if (sol == null) {
            System.out.println("Error: no existe la solicitud #" + id);
            return;
        }
        nuevo = nuevo.toUpperCase().trim();
        boolean ok = false;
        if (sol.estado.equals("PENDIENTE")) {
            if (nuevo.equals("EN_PROCESO")) {
                if (sol.responsable == null || sol.responsable.equals("")) {
                    System.out.println("Error: la solicitud debe tener un responsable asignado");
                    return;
                } else {
                    ok = true;
                }
            } else if (nuevo.equals("RECHAZADA")) {
                ok = true;
            }
        } else if (sol.estado.equals("EN_PROCESO")) {
            if (nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA")) {
                ok = true;
            }
        } else {
            System.out.println("Error: la solicitud #" + id + " ya esta cerrada");
            return;
        }
        if (!ok) {
            System.out.println("Error: no se puede pasar de " + sol.estado + " a " + nuevo);
            return;
        }
        sol.estado = nuevo;
        if (nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA")) {
            sol.fechaCierre = LocalDate.now().format(f);
        }
        guardar();
        System.out.println("Solicitud #" + id + ": estado actualizado a " + nuevo);
    }

    public void buscar(String estado, String tipo, String legajo, String prioridad, boolean soloVencidas) {
        ArrayList<Solicitud> res = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (Solicitud s : lista) {
            boolean va = true;
            if (estado != null && !estado.equals("") && !s.estado.equalsIgnoreCase(estado)) {
                va = false;
            }
            if (tipo != null && !tipo.equals("") && !s.tipo.equalsIgnoreCase(tipo)) {
                va = false;
            }
            if (legajo != null && !legajo.equals("") && !s.legajo.equals(legajo)) {
                va = false;
            }
            if (prioridad != null && !prioridad.equals("") && !s.prioridad.equalsIgnoreCase(prioridad)) {
                va = false;
            }
            if (soloVencidas) {
                LocalDate vto = LocalDate.parse(s.fechaVto, f);
                if (!(vto.isBefore(hoy) && (s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO")))) {
                    va = false;
                }
            }
            if (va) {
                res.add(s);
            }
        }
        if (res.size() == 0) {
            System.out.println("No se encontraron solicitudes");
            return;
        }
        System.out.println("Se encontraron " + res.size() + " solicitudes:");
        for (Solicitud s : res) {
            System.out.println("#" + s.id + " | " + s.tipo + " | " + s.prioridad + " | " + s.estado + " | Leg: " + s.legajo
                    + " | Resp: " + (s.responsable.equals("") ? "-" : s.responsable) + " | Vence: " + s.fechaVto + " | " + s.desc);
        }
    }

    public void listar() {
        if (lista.size() == 0) {
            System.out.println("No hay solicitudes cargadas");
            return;
        }
        System.out.println("Solicitudes registradas: " + lista.size());
        for (int i = 0; i < lista.size(); i++) {
            Solicitud s = lista.get(i);
            System.out.println("#" + s.id + " | " + s.tipo + " | " + s.prioridad + " | " + s.estado + " | Leg: " + s.legajo
                    + " | Resp: " + (s.responsable.equals("") ? "-" : s.responsable) + " | Vence: " + s.fechaVto + " | " + s.desc);
        }
    }

    public void reporte() {
        int pend = 0, proc = 0, res = 0, rech = 0;
        int cert = 0, exa = 0, equi = 0, rev = 0, baja = 0;
        int bedelia = 0, secretaria = 0;
        int vencidas = 0;
        int cerradas = 0;
        long totalDias = 0;
        LocalDate hoy = LocalDate.now();
        for (Solicitud s : lista) {
            if (s.estado.equals("PENDIENTE")) {
                pend++;
            } else if (s.estado.equals("EN_PROCESO")) {
                proc++;
            } else if (s.estado.equals("RESUELTA")) {
                res++;
            } else if (s.estado.equals("RECHAZADA")) {
                rech++;
            }

            if (s.tipo.equals("CERTIFICADO")) {
                cert++;
            }
            if (s.tipo.equals("EXAMEN")) {
                exa++;
            }
            if (s.tipo.equals("EQUIVALENCIA")) {
                equi++;
            }
            if (s.tipo.equals("REVISION")) {
                rev++;
            }
            if (s.tipo.equals("BAJA")) {
                baja++;
            }

            if (s.tipo.equals("CERTIFICADO") || s.tipo.equals("EXAMEN") || s.tipo.equals("BAJA")) {
                bedelia++;
            } else {
                secretaria++;
            }

            // vencidas: no cerradas y con fecha de vencimiento pasada
            if (s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO")) {
                LocalDate vto = LocalDate.parse(s.fechaVto, f);
                if (vto.isBefore(hoy)) {
                    vencidas++;
                }
            }
            if (s.estado.equals("RESUELTA")) {
                LocalDate alta = LocalDate.parse(s.fechaAlta, f);
                LocalDate cierre = LocalDate.parse(s.fechaCierre, f);
                totalDias += ChronoUnit.DAYS.between(alta, cierre);
                cerradas++;
            }
        }
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total de solicitudes: " + lista.size());
        System.out.println("-- Por estado --");
        System.out.println("PENDIENTE: " + pend);
        System.out.println("EN_PROCESO: " + proc);
        System.out.println("RESUELTA: " + res);
        System.out.println("RECHAZADA: " + rech);
        System.out.println("-- Por tipo --");
        System.out.println("CERTIFICADO: " + cert);
        System.out.println("EXAMEN: " + exa);
        System.out.println("EQUIVALENCIA: " + equi);
        System.out.println("REVISION: " + rev);
        System.out.println("BAJA: " + baja);
        System.out.println("-- Por area --");
        System.out.println("BEDELIA: " + bedelia);
        System.out.println("SECRETARIA ACADEMICA: " + secretaria);
        System.out.println("Vencidas sin resolver: " + vencidas);
        if (cerradas > 0) {
            double prom = (double) totalDias / cerradas;
            System.out.println("Tiempo promedio de resolucion: " + String.format("%.1f", prom) + " dias");
        } else {
            System.out.println("Tiempo promedio de resolucion: sin datos");
        }
    }

    // ya no se usa
    private boolean esUrgente(String prioridad) {
        if (prioridad.equals("ALTA")) {
            return true;
        } else {
            return false;
        }
    }
}
