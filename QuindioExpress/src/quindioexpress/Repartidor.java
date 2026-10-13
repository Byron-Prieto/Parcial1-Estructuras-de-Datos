package quindioexpress;

/**
 * Representa a una persona encargada de entregar paquetes.
 * La disponibilidad solo se modifica desde el paquete (a través del gestor).
 */
public class Repartidor {
    private final String identificacion;
    private final String nombre;
    private final String zona;
    private boolean disponible;

    public Repartidor(String identificacion, String nombre, String zona, boolean disponible) {
        if (identificacion == null || identificacion.trim().isEmpty()
                || nombre == null || nombre.trim().isEmpty()
                || zona == null || zona.trim().isEmpty()) {
            throw new IllegalArgumentException("Identificación, nombre y zona son obligatorios.");
        }
        this.identificacion = identificacion.trim();
        this.nombre = nombre.trim();
        this.zona = Texto.limpiar(zona);
        this.disponible = disponible;
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public String getZona() { return zona; }
    public boolean isDisponible() { return disponible; }
    void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return identificacion + " | " + nombre + " | zona=" + zona + " | disponible=" + disponible;
    }
}
