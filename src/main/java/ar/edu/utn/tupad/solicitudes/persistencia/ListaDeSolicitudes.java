package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import java.util.List;
import java.util.Optional;

/** Operaciones comunes a los repositorios, que guardan y entregan copias de las solicitudes. */
final class ListaDeSolicitudes {

    private ListaDeSolicitudes() {
    }

    /** Agrega una copia de la solicitud o reemplaza la de igual numero. */
    static void agregarOReemplazar(List<Solicitud> solicitudes, Solicitud solicitud) {
        Solicitud copia = solicitud.copia();
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).numero() == solicitud.numero()) {
                solicitudes.set(i, copia);
                return;
            }
        }
        solicitudes.add(copia);
    }

    static List<Solicitud> copias(List<Solicitud> solicitudes) {
        return solicitudes.stream().map(Solicitud::copia).toList();
    }

    static Optional<Solicitud> copiaDe(List<Solicitud> solicitudes, int numero) {
        return solicitudes.stream().filter(s -> s.numero() == numero).findFirst().map(Solicitud::copia);
    }
}
