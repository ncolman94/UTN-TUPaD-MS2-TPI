package ar.edu.utn.tupad.solicitudes.dominio;

/** No se pudieron guardar los datos. La operacion no quedo registrada en el almacenamiento. */
public class ErrorDePersistencia extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErrorDePersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
