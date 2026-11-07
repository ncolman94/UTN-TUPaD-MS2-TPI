package ar.edu.utn.tupad.solicitudes.aplicacion;

import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Pedido;
import ar.edu.utn.tupad.solicitudes.dominio.ReglaDeNegocioException;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioEstudiantes;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioResponsables;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Casos de uso de las solicitudes. Traduce lo que ingresa el usuario a objetos del dominio,
 * delega las reglas en ellos y guarda el resultado. No sabe como se muestran ni donde se guardan los datos.
 */
public class ServicioSolicitudes {

    private final RepositorioSolicitudes solicitudes;
    private final RepositorioEstudiantes estudiantes;
    private final RepositorioResponsables responsables;
    private final Clock reloj;

    public ServicioSolicitudes(RepositorioSolicitudes solicitudes, RepositorioEstudiantes estudiantes,
                               RepositorioResponsables responsables, Clock reloj) {
        this.solicitudes = solicitudes;
        this.estudiantes = estudiantes;
        this.responsables = responsables;
        this.reloj = reloj;
    }

    /** Registra una solicitud nueva. Las validaciones siguen el orden: legajo, tipo, descripcion. */
    public Solicitud registrar(DatosNuevaSolicitud datos) {
        if (!estudiantes.existe(datos.legajo())) {
            throw new ReglaDeNegocioException("no existe un estudiante con legajo " + datos.legajo());
        }
        TipoSolicitud tipo = TipoSolicitud.desde(datos.tipo())
                .orElseThrow(() -> new ReglaDeNegocioException("tipo de solicitud invalido"));
        Pedido pedido = new Pedido(datos.legajo(), tipo, datos.descripcion(), datos.urgente());
        Solicitud solicitud = Solicitud.registrar(solicitudes.proximoNumero(), pedido, hoy());
        solicitudes.guardar(solicitud);
        return solicitud;
    }

    /** Asigna un responsable y lo devuelve, para informar a quien se asigno. */
    public Responsable asignarResponsable(int numero, String idResponsable) {
        Solicitud solicitud = buscarExistente(numero);
        Responsable responsable = responsables.buscarPorId(idResponsable)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe el responsable " + idResponsable));
        solicitud.asignar(responsable);
        solicitudes.guardar(solicitud);
        return responsable;
    }

    /** Cambia el estado y devuelve el estado nuevo. */
    public EstadoSolicitud cambiarEstado(int numero, String estadoIngresado) {
        Solicitud solicitud = buscarExistente(numero);
        solicitud.verificarQueEsteAbierta();
        EstadoSolicitud destino = EstadoSolicitud.desde(estadoIngresado)
                .orElseThrow(() -> ReglaDeNegocioException.transicionInvalida(solicitud.estado(),
                        EstadoSolicitud.normalizar(estadoIngresado)));
        solicitud.cambiarEstado(destino, hoy());
        solicitudes.guardar(solicitud);
        return destino;
    }

    public List<Solicitud> buscar(FiltroSolicitudes filtro) {
        LocalDate hoy = hoy();
        return solicitudes.todas().stream().filter(solicitud -> filtro.admite(solicitud, hoy)).toList();
    }

    public List<Solicitud> todas() {
        return solicitudes.todas();
    }

    private Solicitud buscarExistente(int numero) {
        return solicitudes.buscarPorNumero(numero)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe la solicitud #" + numero));
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}
