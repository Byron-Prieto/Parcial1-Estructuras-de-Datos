package quindioexpress;

import java.util.*;

/**
 * Centraliza las operaciones de gestión de QuindíoExpress.
 * Es el único punto que modifica el estado de paquetes y repartidores.
 */
public class GestorQuindioExpress {
    private final List<Paquete> paquetesRegistrados = new ArrayList<>();
    private final Map<String, Paquete> paquetesPorCodigo = new HashMap<>();
    // clave normalizada (sin tildes ni mayúsculas) -> nombre canónico del municipio
    private final Map<String, String> nombrePorClaveMunicipio = new HashMap<>();
    private final Map<String, List<Paquete>> paquetesPorMunicipio = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Queue<Paquete> colaLlegada = new ArrayDeque<>();
    private final PriorityQueue<Paquete> colaPrioridad = new PriorityQueue<>(Paquete.POR_PRIORIDAD);
    private final Map<String, Repartidor> repartidores = new HashMap<>();
    private final ListaEnlazada<Paquete> historialEntregas = new ListaEnlazada<>();

    private static String claveId(String identificacion) {
        return identificacion == null ? "" : identificacion.trim().toUpperCase(Locale.ROOT);
    }

    private String nombreCanonico(String destino) {
        return nombrePorClaveMunicipio.computeIfAbsent(Texto.clave(destino), k -> destino);
    }

    private boolean estaRegistrado(Paquete paquete) {
        return paquete != null && paquetesPorCodigo.get(paquete.getCodigo()) == paquete;
    }

    private boolean estaRegistrado(Repartidor repartidor) {
        return repartidor != null && repartidores.get(claveId(repartidor.getIdentificacion())) == repartidor;
    }

    private boolean tienePendientes(Repartidor repartidor) {
        for (Paquete paquete : paquetesRegistrados) {
            if (!paquete.isEntregado() && paquete.getRepartidorAsignado() == repartidor) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- registro

    public boolean registrarPaquete(Paquete paquete) {
        Objects.requireNonNull(paquete, "El paquete no puede ser nulo.");
        if (paquetesPorCodigo.containsKey(paquete.getCodigo())) return false;

        String municipio = nombreCanonico(paquete.getDestino());
        paquete.normalizarDestino(municipio);
        paquetesRegistrados.add(paquete);
        paquetesPorCodigo.put(paquete.getCodigo(), paquete);
        paquetesPorMunicipio.computeIfAbsent(municipio, k -> new ArrayList<>()).add(paquete);
        colaLlegada.offer(paquete);
        colaPrioridad.offer(paquete);
        return true;
    }

    public boolean registrarRepartidor(Repartidor repartidor) {
        Objects.requireNonNull(repartidor, "El repartidor no puede ser nulo.");
        String clave = claveId(repartidor.getIdentificacion());
        if (repartidores.containsKey(clave)) return false;
        repartidores.put(clave, repartidor);
        return true;
    }

    // ---------------------------------------------------------------- consultas

    public Paquete buscarPaquete(String codigo) {
        if (codigo == null) return null;
        return paquetesPorCodigo.get(codigo.trim().toUpperCase(Locale.ROOT));
    }

    public Repartidor buscarRepartidor(String identificacion) {
        if (identificacion == null) return null;
        return repartidores.get(claveId(identificacion));
    }

    /** Municipios atendidos, sin duplicados y en orden alfabético. */
    public Set<String> getMunicipios() {
        Set<String> resultado = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        resultado.addAll(paquetesPorMunicipio.keySet());
        return resultado;
    }

    public Map<String, List<Paquete>> getPaquetesPorMunicipio() {
        Map<String, List<Paquete>> copia = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, List<Paquete>> e : paquetesPorMunicipio.entrySet()) {
            copia.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return copia;
    }

    public List<Paquete> obtenerPaquetesPorPrioridad() {
        List<Paquete> copia = new ArrayList<>(paquetesRegistrados);
        copia.sort(Paquete.POR_PRIORIDAD);
        return copia;
    }
    public List<Paquete> obtenerPaquetesPorPeso() {
        List<Paquete> copia = new ArrayList<>(paquetesRegistrados);
        copia.sort(Paquete.POR_PESO);
        return copia;
    }
    public List<Paquete> obtenerPaquetesPorTiempo() {
        List<Paquete> copia = new ArrayList<>(paquetesRegistrados);
        copia.sort(Paquete.POR_TIEMPO);
        return copia;
    }

    /** Paquetes de un municipio (sin importar mayúsculas, tildes ni espacios), en orden de registro. */
    public List<Paquete> paquetesDeMunicipio(String municipio) {
        String canonico = nombrePorClaveMunicipio.get(Texto.clave(municipio));
        List<Paquete> lista = canonico == null ? null : paquetesPorMunicipio.get(canonico);
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    /**
     * Consulta el siguiente paquete por despachar según prioridad (mayor prioridad, menor tiempo, código).
     * Solo considera paquetes pendientes y sin repartidor; descarta de la cola los ya asignados o entregados
     * (una asignación nunca se deshace, por lo que no se pierden paquetes).
     */
    public Paquete siguientePorPrioridad() {
        while (!colaPrioridad.isEmpty() && !esDespachable(colaPrioridad.peek())) colaPrioridad.poll();
        return colaPrioridad.peek();
    }

    /** Consulta el paquete más antiguo (FIFO) que sigue pendiente y sin repartidor asignado. */
    public Paquete siguientePorLlegada() {
        while (!colaLlegada.isEmpty() && !esDespachable(colaLlegada.peek())) colaLlegada.poll();
        return colaLlegada.peek();
    }

    private static boolean esDespachable(Paquete paquete) {
        return !paquete.isEntregado() && paquete.getRepartidorAsignado() == null;
    }

    // ---------------------------------------------------------------- asignación y entrega

    /**
     * Asigna el paquete al repartidor si ambos están registrados en este gestor, el paquete sigue
     * pendiente y sin asignar, y el repartidor está disponible y atiende el municipio de destino.
     */
    public boolean asignarPaquete(Paquete paquete, Repartidor repartidor) {
        if (!estaRegistrado(paquete) || !estaRegistrado(repartidor)
                || paquete.isEntregado() || paquete.getRepartidorAsignado() != null
                || !repartidor.isDisponible()
                || !Texto.clave(repartidor.getZona()).equals(Texto.clave(paquete.getDestino()))) return false;
        paquete.setRepartidorAsignado(repartidor);
        repartidor.setDisponible(false);
        return true;
    }

    /** Registra la entrega de un paquete asignado y libera automáticamente a su repartidor. */
    public boolean entregarPaquete(Paquete paquete) {
        if (!estaRegistrado(paquete) || paquete.isEntregado() || paquete.getRepartidorAsignado() == null) return false;
        Repartidor repartidor = paquete.getRepartidorAsignado();
        paquete.marcarEntregado();
        historialEntregas.agregar(paquete);
        repartidor.setDisponible(true);
        return true;
    }

    /**
     * Cambia la disponibilidad de un repartidor (por ejemplo, fin de turno).
     * No permite marcarlo disponible mientras tenga un paquete pendiente asignado.
     */
    public boolean actualizarDisponibilidad(Repartidor repartidor, boolean disponible) {
        if (!estaRegistrado(repartidor)) return false;
        if (disponible && tienePendientes(repartidor)) return false;
        repartidor.setDisponible(disponible);
        return true;
    }

    // ---------------------------------------------------------------- historial y listados (solo lectura)

    /** Historial de entregas en orden cronológico; solo se puede recorrer. */
    public Iterable<Paquete> getHistorialEntregas() { return historialEntregas::iterator; }
    public int totalEntregas() { return historialEntregas.tamanio(); }
    public Paquete entregaEn(int indice) { return historialEntregas.obtener(indice); }

    public List<Paquete> getPaquetesRegistrados() { return Collections.unmodifiableList(paquetesRegistrados); }
    public Collection<Repartidor> getRepartidores() { return Collections.unmodifiableCollection(repartidores.values()); }

    // ---------------------------------------------------------------- recursividad y búsqueda
    // Nota: la recursión usa una llamada por elemento (profundidad = n); con decenas de miles
    // de paquetes podría producir StackOverflowError. Es una decisión didáctica.

    public double pesoTotalPorMunicipio(String municipio) {
        return pesoTotalRecursivo(paquetesRegistrados, Texto.clave(municipio), 0);
    }
    private double pesoTotalRecursivo(List<Paquete> lista, String municipioClave, int indice) {
        if (indice >= lista.size()) return 0.0;
        Paquete actual = lista.get(indice);
        double peso = Texto.clave(actual.getDestino()).equals(municipioClave) ? actual.getPeso() : 0.0;
        return peso + pesoTotalRecursivo(lista, municipioClave, indice + 1);
    }

    public int contarPrioridadMinima(int prioridadMinima) {
        return contarPrioridadRecursivo(paquetesRegistrados, prioridadMinima, 0);
    }
    private int contarPrioridadRecursivo(List<Paquete> lista, int minimo, int indice) {
        if (indice >= lista.size()) return 0;
        int actual = lista.get(indice).getPrioridad() >= minimo ? 1 : 0;
        return actual + contarPrioridadRecursivo(lista, minimo, indice + 1);
    }

    /** Búsqueda binaria iterativa. La lista debe estar ordenada por código ascendente. */
    public static Paquete busquedaBinariaPorCodigo(List<Paquete> ordenados, String codigo) {
        Objects.requireNonNull(ordenados, "La lista no puede ser nula.");
        if (codigo == null) return null;
        String objetivo = codigo.trim().toUpperCase(Locale.ROOT);
        int izquierda = 0, derecha = ordenados.size() - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            int cmp = ordenados.get(medio).getCodigo().compareTo(objetivo);
            if (cmp == 0) return ordenados.get(medio);
            if (cmp < 0) izquierda = medio + 1; else derecha = medio - 1;
        }
        return null;
    }

    /** Ordena una copia para que la búsqueda binaria no dependa del orden de registro. */
    public Paquete buscarPorCodigoConBusquedaBinaria(String codigo) {
        List<Paquete> ordenados = new ArrayList<>(paquetesRegistrados);
        Collections.sort(ordenados);
        return busquedaBinariaPorCodigo(ordenados, codigo);
    }

    public static Paquete paqueteMasPesado(List<Paquete> paquetes) {
        if (paquetes == null || paquetes.isEmpty())
            throw new IllegalArgumentException("La lista debe contener al menos un paquete.");
        return masPesadoRecursivo(paquetes, 0, paquetes.size() - 1);
    }
    private static Paquete masPesadoRecursivo(List<Paquete> paquetes, int inicio, int fin) {
        if (inicio == fin) return paquetes.get(inicio);
        int medio = inicio + (fin - inicio) / 2;
        Paquete izq = masPesadoRecursivo(paquetes, inicio, medio);
        Paquete der = masPesadoRecursivo(paquetes, medio + 1, fin);
        return izq.getPeso() >= der.getPeso() ? izq : der;
    }
}
