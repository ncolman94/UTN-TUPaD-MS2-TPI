package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Prioridad;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.LocalDate;

public class Solicitud {
    public int id;
    public String legajo;
    public TipoSolicitud tipo;
    public String desc;
    public Prioridad prioridad;
    public EstadoSolicitud estado;
    public String responsable;
    public LocalDate fechaAlta;
    public LocalDate fechaVto;
    public LocalDate fechaCierre;
}
