package ar.edu.utn.tupad.solicitudes.dominio;

import java.util.List;

/** Acceso de solo lectura a las solicitudes (para quien solo necesita consultarlas, como el reporte). */
public interface ConsultaSolicitudes {

    /** Todas las solicitudes, en el orden en que se registraron. */
    List<Solicitud> todas();
}
