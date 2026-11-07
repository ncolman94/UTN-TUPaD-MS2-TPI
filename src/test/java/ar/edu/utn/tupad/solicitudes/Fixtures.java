package ar.edu.utn.tupad.solicitudes;

import ar.edu.utn.tupad.solicitudes.dominio.Area;
import ar.edu.utn.tupad.solicitudes.dominio.Pedido;
import ar.edu.utn.tupad.solicitudes.dominio.Responsable;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.dominio.TipoSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Datos de prueba comunes. Todas las pruebas unitarias trabajan con una fecha fija. */
public final class Fixtures {

    public static final LocalDate HOY = LocalDate.of(2026, 11, 7);
    public static final Clock RELOJ_FIJO = Clock.fixed(HOY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    public static final Responsable DE_BEDELIA = new Responsable("R01", "Laura Medina", Area.BEDELIA);
    public static final Responsable DE_SECRETARIA =
            new Responsable("R03", "Sofia Herrera", Area.SECRETARIA_ACADEMICA);

    private Fixtures() {
    }

    public static Solicitud solicitud(int numero, TipoSolicitud tipo, LocalDate alta) {
        return Solicitud.registrar(numero, new Pedido("1001", tipo, "Descripcion de prueba", false), alta);
    }

    public static Solicitud certificadoDeHoy(int numero) {
        return solicitud(numero, TipoSolicitud.CERTIFICADO, HOY);
    }
}
