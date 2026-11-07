package ar.edu.utn.tupad.solicitudes.aplicacion;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.ConsultaSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;

/** Calcula el reporte de solicitudes. Solo necesita consultar, por eso depende de ConsultaSolicitudes. */
public class GeneradorDeReporte {

    private final ConsultaSolicitudes solicitudes;
    private final Clock reloj;

    public GeneradorDeReporte(ConsultaSolicitudes solicitudes, Clock reloj) {
        this.solicitudes = solicitudes;
        this.reloj = reloj;
    }

    public ReporteSolicitudes generar() {
        List<Solicitud> todas = solicitudes.todas();
        LocalDate hoy = LocalDate.now(reloj);
        return new ReporteSolicitudes(
                todas.size(),
                contar(todas, Solicitud::estado, EstadoSolicitud.class),
                contar(todas, Solicitud::tipo, TipoSolicitud.class),
                contar(todas, solicitud -> solicitud.tipo().area(), Area.class),
                todas.stream().filter(solicitud -> solicitud.estaVencida(hoy)).count(),
                todas.stream()
                        .map(Solicitud::diasDeResolucion)
                        .filter(OptionalLong::isPresent)
                        .mapToLong(OptionalLong::getAsLong)
                        .average());
    }

    private static <E extends Enum<E>> Map<E, Long> contar(List<Solicitud> lista, Function<Solicitud, E> criterio,
                                                           Class<E> tipoDeEnum) {
        Map<E, Long> conteo = new EnumMap<>(tipoDeEnum);
        for (E valor : tipoDeEnum.getEnumConstants()) {
            conteo.put(valor, 0L);
        }
        for (Solicitud solicitud : lista) {
            conteo.merge(criterio.apply(solicitud), 1L, Long::sum);
        }
        return Collections.unmodifiableMap(conteo);
    }
}
