package ar.edu.utn.tupad.solicitudes.persistencia;

import ar.edu.utn.tupad.solicitudes.dominio.RepositorioSolicitudes;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Contrato de RepositorioSolicitudes: implementacion en memoria")
class RepositorioSolicitudesEnMemoriaTest extends ContratoRepositorioSolicitudes {

    @Override
    protected RepositorioSolicitudes repositorioVacio() {
        return new RepositorioSolicitudesEnMemoria();
    }
}
