package quindioexpress;

import java.util.Comparator;
import java.util.Locale;

/**
 * Representa un paquete registrado en QuindíoExpress.
 * El orden natural y la igualdad se determinan por el código (único).
 * Los cambios de estado (entrega, asignación, destino canónico) solo los hace el gestor.
 */
public class Paquete implements Comparable<Paquete> {
    /** Prioridad asignada cuando no se indica una (el enunciado fija 3 por defecto). */
    public static final int PRIORIDAD_POR_DEFECTO = 3;

    private final String codigo;
    private String destino;
    private final double peso;
    private final int prioridad;
    private final int tiempoEstimado;
    private boolean entregado;
    private Repartidor repartidorAsignado;

    /** Crea un paquete con la prioridad por defecto (3). */
    public Paquete(String codigo, String destino, double peso, int tiempoEstimado) {
        this(codigo, destino, peso, PRIORIDAD_POR_DEFECTO, tiempoEstimado);
    }

    public Paquete(String codigo, String destino, double peso, int prioridad, int tiempoEstimado) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede ser nulo ni vacío.");
        }
        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El destino no puede ser nulo ni vacío.");
        }
        if (!Double.isFinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero.");
        }
        if (prioridad < 1 || prioridad > 5) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 y 5.");
        }
        if (tiempoEstimado <= 0) {
            throw new IllegalArgumentException("El tiempo estimado debe ser mayor que cero.");
        }
        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.destino = Texto.limpiar(destino);
        this.peso = peso;
        this.prioridad = prioridad;
        this.tiempoEstimado = tiempoEstimado;
        this.entregado = false;
        this.repartidorAsignado = null;
    }

    public String getCodigo() { return codigo; }
    public String getDestino() { return destino; }
    public double getPeso() { return peso; }
    public int getPrioridad() { return prioridad; }
    public int getTiempoEstimado() { return tiempoEstimado; }
    public boolean isEntregado() { return entregado; }
    /** Devuelve el objeto repartidor asociado, no solamente su identificador. */
    public Repartidor getRepartidorAsignado() { return repartidorAsignado; }

    void marcarEntregado() { this.entregado = true; }
    void setRepartidorAsignado(Repartidor repartidor) { this.repartidorAsignado = repartidor; }
    /** Unifica la escritura del destino con el nombre del municipio ya registrado. */
    void normalizarDestino(String nombreCanonico) { this.destino = nombreCanonico; }

    @Override
    public int compareTo(Paquete otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paquete)) return false;
        return codigo.equals(((Paquete) o).codigo);
    }

    @Override
    public int hashCode() { return codigo.hashCode(); }

    /** Mayor prioridad primero; en empate, menor tiempo y luego código ascendente. */
    public static final Comparator<Paquete> POR_PRIORIDAD =
        Comparator.comparingInt(Paquete::getPrioridad).reversed()
                  .thenComparingInt(Paquete::getTiempoEstimado)
                  .thenComparing(Paquete::getCodigo);

    public static final Comparator<Paquete> POR_PESO =
        Comparator.comparingDouble(Paquete::getPeso).reversed()
                  .thenComparing(Paquete::getCodigo);

    public static final Comparator<Paquete> POR_TIEMPO =
        Comparator.comparingInt(Paquete::getTiempoEstimado)
                  .thenComparing(Paquete::getCodigo);

    @Override
    public String toString() {
        return String.format(Locale.US, "%s | destino=%s | peso=%.1f kg | prioridad=%d | tiempo=%d min%s",
                codigo, destino, peso, prioridad, tiempoEstimado,
                repartidorAsignado == null ? "" : " | repartidor=" + repartidorAsignado.getIdentificacion()
                        + " (" + repartidorAsignado.getNombre() + ")");
    }
}
