package ar.edu.utn.tupad.solicitudes.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;

/** Solicitud academica: conoce sus reglas de vencimiento, asignacion y cambio de estado. */
public final class Solicitud {

    private final int numero;
    private final String legajo;
    private final TipoSolicitud tipo;
    private final String descripcion;
    private final Prioridad prioridad;
    private final LocalDate fechaDeAlta;
    private final LocalDate fechaDeVencimiento;
    private EstadoSolicitud estado;
    private String idResponsable;
    private LocalDate fechaDeCierre;

    private Solicitud(DatosDeSolicitud datos) {
        this.numero = datos.numero();
        this.legajo = datos.legajo();
        this.tipo = datos.tipo();
        this.descripcion = datos.descripcion();
        this.prioridad = datos.prioridad();
        this.estado = datos.estado();
        this.idResponsable = datos.idResponsable();
        this.fechaDeAlta = datos.fechaDeAlta();
        this.fechaDeVencimiento = datos.fechaDeVencimiento();
        this.fechaDeCierre = datos.fechaDeCierre();
    }

    /** Crea una solicitud nueva: la prioridad y el vencimiento salen de las reglas de su tipo. */
    public static Solicitud registrar(int numero, Pedido pedido, LocalDate hoy) {
        TipoSolicitud tipo = pedido.tipo();
        LocalDate vencimiento = hoy.plusDays(tipo.plazoEnDias(pedido.urgente()));
        return new Solicitud(new DatosDeSolicitud(numero, pedido.legajo(), tipo, pedido.descripcion(),
                tipo.prioridad(pedido.urgente()), EstadoSolicitud.PENDIENTE, null, hoy, vencimiento, null));
    }

    /** Vuelve a crear una solicitud ya existente a partir de sus datos guardados. */
    public static Solicitud restaurar(DatosDeSolicitud datos) {
        return new Solicitud(datos);
    }

    public void asignar(Responsable responsable) {
        this.idResponsable = responsable.id();
    }

    public void cambiarEstado(EstadoSolicitud destino, LocalDate hoy) {
        this.estado = destino;
        if (destino.estaCerrada()) {
            this.fechaDeCierre = hoy;
        }
    }

    public boolean estaVencida(LocalDate hoy) {
        return estado.estaAbierta() && fechaDeVencimiento.isBefore(hoy);
    }

    public boolean estaCerrada() {
        return estado.estaCerrada();
    }

    public boolean tieneResponsable() {
        return idResponsable != null;
    }

    /** Dias entre el alta y el cierre; solo tiene valor para las solicitudes RESUELTAS. */
    public OptionalLong diasDeResolucion() {
        return estado == EstadoSolicitud.RESUELTA
                ? OptionalLong.of(ChronoUnit.DAYS.between(fechaDeAlta, fechaDeCierre))
                : OptionalLong.empty();
    }

    public DatosDeSolicitud datos() {
        return new DatosDeSolicitud(numero, legajo, tipo, descripcion, prioridad, estado, idResponsable,
                fechaDeAlta, fechaDeVencimiento, fechaDeCierre);
    }

    public int numero() {
        return numero;
    }

    public String legajo() {
        return legajo;
    }

    public TipoSolicitud tipo() {
        return tipo;
    }

    public String descripcion() {
        return descripcion;
    }

    public Prioridad prioridad() {
        return prioridad;
    }

    public EstadoSolicitud estado() {
        return estado;
    }

    public Optional<String> idResponsable() {
        return Optional.ofNullable(idResponsable);
    }

    public LocalDate fechaDeVencimiento() {
        return fechaDeVencimiento;
    }
}
