package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.util.List;

/** Operacion comun a los repositorios: agregar una solicitud o reemplazar la de igual numero. */
final class ListaDeSolicitudes {

    private ListaDeSolicitudes() {
    }

    static void agregarOReemplazar(List<Solicitud> solicitudes, Solicitud solicitud) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).numero() == solicitud.numero()) {
                solicitudes.set(i, solicitud);
                return;
            }
        }
        solicitudes.add(solicitud);
    }
}
