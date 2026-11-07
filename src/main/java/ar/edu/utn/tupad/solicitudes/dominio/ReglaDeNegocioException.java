package ar.edu.utn.tupad.solicitudes.dominio;

/** Se lanza cuando una operacion no cumple una regla de negocio. El mensaje se muestra al usuario. */
public class ReglaDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }

    public static ReglaDeNegocioException transicionInvalida(EstadoSolicitud origen, String destino) {
        return new ReglaDeNegocioException("no se puede pasar de " + origen + " a " + destino);
    }
}
