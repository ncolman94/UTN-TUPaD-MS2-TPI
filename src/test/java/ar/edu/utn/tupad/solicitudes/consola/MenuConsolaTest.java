package ar.edu.utn.tupad.solicitudes.consola;

import static ar.edu.utn.tupad.solicitudes.Fixtures.DE_BEDELIA;
import static ar.edu.utn.tupad.solicitudes.Fixtures.RELOJ_FIJO;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.tupad.solicitudes.aplicacion.GeneradorDeReporte;
import ar.edu.utn.tupad.solicitudes.aplicacion.ServicioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.ErrorDePersistencia;
import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import ar.edu.utn.tupad.solicitudes.dominio.Solicitud;
import ar.edu.utn.tupad.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** El menu se prueba en memoria: la entrada es un texto y la salida se captura en un buffer. */
@DisplayName("MenuConsola: entrada del operador y mensajes")
class MenuConsolaTest {

    @Test
    void registraYMuestraElResultado() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "1", "1001", "EXAMEN", "Constancia", "n", "0");

        assertTrue(salida.contains("Solicitud #1 registrada. Prioridad: MEDIA. Vence: 12/11/2026"), salida);
        assertTrue(salida.endsWith("Hasta luego\n"), salida);
    }

    @Test
    @DisplayName("flujo completo: registrar, asignar, cambiar estado, buscar y listar")
    void flujoCompleto() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(),
                "1", "1001", "certificado", "Certificado de regularidad", "s",
                "2", "1", "r01",
                "3", "1", "en_proceso",
                "4", "EN_PROCESO", "", "", "", "n",
                "4", "", "", "", "", "s",
                "6", "0");

        assertTrue(salida.contains("Tipo (CERTIFICADO/EXAMEN/EQUIVALENCIA/REVISION/BAJA): "), salida);
        assertTrue(salida.contains("Solicitud #1 registrada. Prioridad: MEDIA. Vence: 08/11/2026"), salida);
        assertTrue(salida.contains("Solicitud #1 asignada a Laura Medina (BEDELIA)"), salida);
        assertTrue(salida.contains("Nuevo estado (EN_PROCESO/RESUELTA/RECHAZADA): "), salida);
        assertTrue(salida.contains("Solicitud #1: estado actualizado a EN_PROCESO"), salida);
        assertTrue(salida.contains("Se encontraron 1 solicitudes:\n#1 | CERTIFICADO | MEDIA | EN_PROCESO | "
                + "Leg: 1001 | Resp: R01 | Vence: 08/11/2026 | Certificado de regularidad"), salida);
        assertTrue(salida.contains("No se encontraron solicitudes"), salida);
        assertTrue(salida.contains("Solicitudes registradas: 1"), salida);
    }

    @Test
    void muestraLosErroresDeReglaConElPrefijoError() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "2", "8", "R01", "0");

        assertTrue(salida.contains("Error: no existe la solicitud #8"), salida);
    }

    @Test
    void unaOpcionNoNumericaEsInvalidaYElMenuSigue() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "abc", " 6 ", "0");

        assertTrue(salida.contains("Opcion: Opcion invalida"), salida);
        assertTrue(salida.contains("No hay solicitudes cargadas"), salida);
    }

    @Test
    void unNumeroDeSolicitudNoNumericoSeInformaComoError() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "3", "uno", "0");

        assertTrue(salida.contains("Error: el numero de solicitud debe ser un numero entero"), salida);
    }

    @Test
    void siLaEntradaTerminaSaleSinError() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "6");

        assertTrue(salida.contains("No hay solicitudes cargadas"), salida);
        assertFalse(salida.contains("Hasta luego"), salida);
    }

    @Test
    void siFallaElGuardadoLoInformaYNoMuestraExito() {
        RepositorioSolicitudes queNoGuarda = new RepositorioSolicitudesEnMemoria() {
            @Override
            public void guardar(Solicitud solicitud) {
                throw new ErrorDePersistencia("no se pudo escribir datos/solicitudes.csv", null);
            }
        };

        String salida = ejecutar(queNoGuarda, "1", "1001", "EXAMEN", "Constancia", "n", "0");

        assertTrue(salida.contains(
                "Error al guardar: no se pudo escribir datos/solicitudes.csv. El cambio no quedo guardado."), salida);
        assertFalse(salida.contains("registrada"), salida);
    }

    @Test
    void elReporteSeMuestraConElFormatoDePantalla() {
        String salida = ejecutar(new RepositorioSolicitudesEnMemoria(), "5", "0");

        assertTrue(salida.contains("===== REPORTE DE SOLICITUDES =====\nTotal de solicitudes: 0\n"), salida);
        assertTrue(salida.contains("SECRETARIA ACADEMICA: 0\n"), salida);
        assertTrue(salida.contains("Tiempo promedio de resolucion: sin datos\n"), salida);
    }

    private static String ejecutar(RepositorioSolicitudes solicitudes, String... lineas) {
        ServicioSolicitudes servicio = new ServicioSolicitudes(solicitudes, "1001"::equals,
                id -> "R01".equalsIgnoreCase(id) ? Optional.of(DE_BEDELIA) : Optional.empty(), RELOJ_FIJO);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        Scanner entrada = new Scanner(String.join("\n", lineas) + "\n");

        new MenuConsola(entrada, salida, servicio, new GeneradorDeReporte(solicitudes, RELOJ_FIJO)).iniciar();

        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
