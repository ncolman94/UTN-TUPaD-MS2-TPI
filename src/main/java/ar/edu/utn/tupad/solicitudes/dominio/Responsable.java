package ar.edu.utn.tupad.solicitudes.dominio;

/** Persona de un area administrativa a la que se le asignan solicitudes. */
public record Responsable(String id, String nombre, Area area) {

    public boolean perteneceA(Area otraArea) {
        return area == otraArea;
    }
}
