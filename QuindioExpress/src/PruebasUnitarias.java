import quindioexpress.GestorQuindioExpress;
import quindioexpress.ListaEnlazada;
import quindioexpress.Paquete;
import quindioexpress.Repartidor;

import java.util.*;

/**
 * Pruebas unitarias ejecutables sin librerías externas.
 * Ejecutar con: java -ea -cp out PruebasUnitarias (ver README).
 */
public class PruebasUnitarias {
    private static int pasaron = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        probar("Lista vacía", PruebasUnitarias::listaVacia);
        probar("Lista con un elemento", PruebasUnitarias::listaUnElemento);
        probar("Eliminar primero, medio y último", PruebasUnitarias::eliminaciones);
        probar("Índices inválidos", PruebasUnitarias::indicesInvalidos);
        probar("Iterador y agotamiento", PruebasUnitarias::iterador);
        probar("ArrayList mantiene orden y códigos únicos", PruebasUnitarias::registroYBusqueda);
        probar("Municipios sin duplicados ignorando mayúsculas", PruebasUnitarias::municipiosSinDuplicados);
        probar("Municipios ignoran tildes", PruebasUnitarias::municipiosConTildes);
        probar("El destino del paquete usa el nombre canónico", PruebasUnitarias::destinoCanonico);
        probar("Prioridad, tiempo y código", PruebasUnitarias::ordenPrioridad);
        probar("Recursividad: peso total = 14.0", PruebasUnitarias::pesoRecursivo);
        probar("Recursividad: 4 prioridades >= 4", PruebasUnitarias::conteoRecursivo);
        probar("Búsqueda binaria: PQ731 en índice 5", PruebasUnitarias::busquedaBinaria);
        probar("Búsqueda binaria con lista desordenada", PruebasUnitarias::busquedaBinariaListaDesordenada);
        probar("Divide y vencerás: máximo 18.7", PruebasUnitarias::maximoRecursivo);
        probar("Divide y vencerás rechaza lista vacía", PruebasUnitarias::maximoListaVacia);
        probar("Entrega, asignación e historial", PruebasUnitarias::entregaEHistorial);
        probar("El paquete conserva la referencia al objeto Repartidor", PruebasUnitarias::referenciaObjetoRepartidor);
        probar("No se entrega un paquete sin repartidor", PruebasUnitarias::entregaRequiereAsignacion);
        probar("No se asigna dos veces el mismo paquete", PruebasUnitarias::noReasignarPaquete);
        probar("La entrega libera al repartidor", PruebasUnitarias::entregaLiberaRepartidor);
        probar("Asignación rechazada: otra zona, no disponible", PruebasUnitarias::asignacionRechazada);
        probar("Asignación acepta zona con tildes distintas", PruebasUnitarias::asignacionZonaConTildes);
        probar("Disponibilidad: no libera con paquete pendiente", PruebasUnitarias::disponibilidad);
        probar("buscarRepartidor normaliza el identificador", PruebasUnitarias::buscarRepartidorNormaliza);
        probar("Objetos no registrados en el gestor son rechazados", PruebasUnitarias::objetosNoRegistrados);
        probar("siguientePorPrioridad salta entregados", PruebasUnitarias::siguientePorPrioridadSaltaEntregados);
        probar("siguientePorLlegada respeta FIFO y salta entregados", PruebasUnitarias::siguientePorLlegada);
        probar("El historial es de solo lectura", PruebasUnitarias::historialSoloLectura);
        probar("Igualdad y hashCode de Paquete por código", PruebasUnitarias::igualdadPaquete);
        probar("toString usa punto decimal", PruebasUnitarias::formatoDecimal);
        probar("Validaciones del paquete", PruebasUnitarias::validacionesPaquete);
        probar("Prioridad por defecto = 3", PruebasUnitarias::prioridadPorDefecto);
        probar("El siguiente por despacho salta paquetes ya asignados", PruebasUnitarias::siguienteSaltaAsignados);
        probar("paquetesDeMunicipio ignora tildes y mayúsculas", PruebasUnitarias::paquetesDeMunicipioConTildes);
        System.out.println("\nResultado: " + pasaron + "/" + (pasaron + fallidas) + " pruebas superadas.");
        if (fallidas > 0) System.exit(1);
    }

    private static void probar(String nombre, Runnable prueba) {
        try { prueba.run(); pasaron++; System.out.println("[OK] " + nombre); }
        catch (Throwable e) { fallidas++; System.err.println("[FALLO] " + nombre + " -> " + e); }
    }

    private static Paquete p(String codigo, String destino, double peso, int prioridad, int tiempo) {
        return new Paquete(codigo, destino, peso, prioridad, tiempo);
    }
    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    private static void igual(Object esperado, Object real) {
        if (!Objects.equals(esperado, real)) throw new AssertionError("Esperado=" + esperado + ", real=" + real);
    }
    private static void cerca(double esperado, double real) {
        if (Math.abs(esperado - real) > 0.000001) throw new AssertionError("Esperado=" + esperado + ", real=" + real);
    }

    // ------------------------------------------------------------ lista enlazada

    private static void listaVacia() {
        ListaEnlazada<String> l = new ListaEnlazada<>();
        exigir(l.estaVacia(), "Debe iniciar vacía"); igual(0, l.tamanio());
        exigir(!l.eliminar("x"), "No debe eliminar en lista vacía");
    }
    private static void listaUnElemento() {
        ListaEnlazada<String> l = new ListaEnlazada<>(); l.agregar("A");
        exigir(!l.estaVacia(), "No debe estar vacía"); igual(1, l.tamanio()); igual("A", l.obtener(0));
        exigir(l.eliminar("A"), "Debe eliminar el único elemento"); exigir(l.estaVacia(), "Debe quedar vacía");
        l.agregar("B"); igual("B", l.obtener(0));
    }
    private static void eliminaciones() {
        ListaEnlazada<Integer> l = new ListaEnlazada<>();
        for (int i = 1; i <= 5; i++) l.agregar(i);
        exigir(l.eliminar(1), "Eliminar cabeza"); exigir(l.eliminar(3), "Eliminar medio");
        exigir(l.eliminar(5), "Eliminar cola"); igual(Arrays.asList(2, 4), comoLista(l));
        igual(2, l.tamanio()); exigir(!l.eliminar(99), "No debe eliminar inexistentes");
        l.agregar(6); igual(Arrays.asList(2, 4, 6), comoLista(l));
    }
    private static void indicesInvalidos() {
        ListaEnlazada<String> l = new ListaEnlazada<>(); l.agregar("A");
        try { l.obtener(-1); throw new AssertionError("Debió lanzar IndexOutOfBoundsException"); }
        catch (IndexOutOfBoundsException esperado) { }
        try { l.obtener(1); throw new AssertionError("Debió lanzar IndexOutOfBoundsException"); }
        catch (IndexOutOfBoundsException esperado) { }
    }
    private static void iterador() {
        ListaEnlazada<String> l = new ListaEnlazada<>(); l.agregar("A"); l.agregar("B");
        Iterator<String> it = l.iterator(); igual("A", it.next()); igual("B", it.next());
        exigir(!it.hasNext(), "No debe haber más elementos");
        try { it.next(); throw new AssertionError("Debió lanzar NoSuchElementException"); }
        catch (NoSuchElementException esperado) { }
    }

    // ------------------------------------------------------------ registro y municipios

    private static void registroYBusqueda() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        exigir(g.registrarPaquete(p("PQ731", "Armenia", 1, 3, 20)), "Registro válido");
        exigir(g.registrarPaquete(p("PQ105", "Salento", 2, 4, 15)), "Registro válido");
        exigir(!g.registrarPaquete(p("pq731", "Calarcá", 3, 1, 30)), "Código duplicado");
        igual(Arrays.asList("PQ731", "PQ105"), codigos(g.getPaquetesRegistrados()));
        igual("PQ731", g.buscarPaquete("pq731").getCodigo());
        igual(null, g.buscarPaquete("NOEXISTE"));
    }
    private static void municipiosSinDuplicados() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        g.registrarPaquete(p("PQ001", "Armenia", 1, 3, 20));
        g.registrarPaquete(p("PQ002", " armenia ", 1, 3, 20));
        g.registrarPaquete(p("PQ003", "Salento", 1, 3, 20));
        igual(2, g.getMunicipios().size());
        cerca(2.0, g.pesoTotalPorMunicipio("ARMENIA"));
        igual(2, g.getPaquetesPorMunicipio().get("Armenia").size());
    }
    private static void municipiosConTildes() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        g.registrarPaquete(p("PQ001", "Calarcá", 1, 3, 20));
        g.registrarPaquete(p("PQ002", "Calarca", 2, 3, 20));
        g.registrarPaquete(p("PQ003", "CALARCÁ", 3, 3, 20));
        igual(1, g.getMunicipios().size());
        igual(3, g.getPaquetesPorMunicipio().get("Calarcá").size());
        cerca(6.0, g.pesoTotalPorMunicipio("calarca"));
    }
    private static void destinoCanonico() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        g.registrarPaquete(p("PQ001", "Armenia", 1, 3, 20));
        Paquete segundo = p("PQ002", "  armenia  ", 1, 3, 20);
        g.registrarPaquete(segundo);
        igual("Armenia", segundo.getDestino());
    }

    // ------------------------------------------------------------ orden, recursividad y búsqueda

    private static void ordenPrioridad() {
        List<Paquete> ps = new ArrayList<>(Arrays.asList(
            p("PQ420", "Armenia", 1, 3, 30), p("PQ731", "Armenia", 1, 5, 45),
            p("PQ105", "Armenia", 1, 5, 25), p("PQ942", "Armenia", 1, 4, 20)));
        ps.sort(Paquete.POR_PRIORIDAD);
        igual(Arrays.asList("PQ105", "PQ731", "PQ942", "PQ420"), codigos(ps));
    }
    private static void pesoRecursivo() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        g.registrarPaquete(p("PQ101", "Armenia", 4.5, 3, 30));
        g.registrarPaquete(p("PQ203", "Salento", 3.0, 2, 20));
        g.registrarPaquete(p("PQ415", "Armenia", 7.2, 5, 15));
        g.registrarPaquete(p("PQ730", "Armenia", 2.3, 4, 25));
        cerca(14.0, g.pesoTotalPorMunicipio("Armenia"));
    }
    private static void conteoRecursivo() {
        GestorQuindioExpress g = new GestorQuindioExpress(); int[] a = {5, 2, 4, 1, 5, 3, 4};
        for (int i = 0; i < a.length; i++) g.registrarPaquete(p("PR" + i, "Armenia", 1, a[i], i + 1));
        igual(4, g.contarPrioridadMinima(4));
    }
    private static void busquedaBinaria() {
        String[] codigos = {"PQ105", "PQ214", "PQ318", "PQ421", "PQ567", "PQ731", "PQ842", "PQ942"};
        List<Paquete> ps = new ArrayList<>();
        for (String c : codigos) ps.add(p(c, "Armenia", 1, 3, 20));
        Paquete encontrado = GestorQuindioExpress.busquedaBinariaPorCodigo(ps, "PQ731");
        igual(5, ps.indexOf(encontrado)); igual(null, GestorQuindioExpress.busquedaBinariaPorCodigo(ps, "PQ999"));
    }
    private static void busquedaBinariaListaDesordenada() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        String[] desordenados = {"PQ105", "PQ731", "PQ214", "PQ318", "PQ421", "PQ567", "PQ842", "PQ942"};
        List<Paquete> ps = new ArrayList<>();
        for (String c : desordenados) { Paquete p = p(c, "Armenia", 1, 3, 20); ps.add(p); g.registrarPaquete(p); }
        igual(null, GestorQuindioExpress.busquedaBinariaPorCodigo(ps, "PQ731"));
        igual("PQ731", g.buscarPorCodigoConBusquedaBinaria("PQ731").getCodigo());
        igual("PQ731", g.buscarPaquete("PQ731").getCodigo());
    }
    private static void maximoRecursivo() {
        double[] pesos = {4.5, 12.0, 3.2, 18.7, 7.4, 5.1}; List<Paquete> ps = new ArrayList<>();
        for (int i = 0; i < pesos.length; i++) ps.add(p("PW" + i, "Armenia", pesos[i], 3, 20));
        cerca(18.7, GestorQuindioExpress.paqueteMasPesado(ps).getPeso());
    }
    private static void maximoListaVacia() {
        try { GestorQuindioExpress.paqueteMasPesado(Collections.emptyList()); throw new AssertionError("Debió rechazar lista vacía"); }
        catch (IllegalArgumentException esperado) { }
    }

    // ------------------------------------------------------------ asignación, entrega y repartidores

    private static void entregaEHistorial() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(paquete); g.registrarRepartidor(ana);
        exigir(g.asignarPaquete(paquete, ana), "Asignación válida");
        exigir(g.entregarPaquete(paquete), "Entrega válida"); exigir(!g.entregarPaquete(paquete), "No entregar dos veces");
        igual(1, g.totalEntregas()); igual("PQ101", g.entregaEn(0).getCodigo());
        exigir(g.siguientePorLlegada() == null, "No debe devolver entregados");
    }
    private static void referenciaObjetoRepartidor() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor repartidor = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(paquete); g.registrarRepartidor(repartidor);
        exigir(g.asignarPaquete(paquete, repartidor), "Asignación válida");
        exigir(paquete.getRepartidorAsignado() == repartidor, "Debe conservar la misma referencia de objeto");
        igual("Ana", paquete.getRepartidorAsignado().getNombre());
    }
    private static void entregaRequiereAsignacion() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        g.registrarPaquete(paquete);
        exigir(!g.entregarPaquete(paquete), "No debe entregar sin repartidor asignado");
        igual(0, g.totalEntregas());
    }
    private static void noReasignarPaquete() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        g.registrarPaquete(paquete);
        Repartidor a = new Repartidor("R001", "Ana", "Armenia", true);
        Repartidor b = new Repartidor("R002", "Luis", "Armenia", true);
        g.registrarRepartidor(a); g.registrarRepartidor(b);
        exigir(g.asignarPaquete(paquete, a), "Primera asignación válida");
        exigir(!g.asignarPaquete(paquete, b), "No debe reemplazar silenciosamente el repartidor");
        exigir(paquete.getRepartidorAsignado() == a, "Debe conservar la asignación original");
        exigir(b.isDisponible(), "El segundo repartidor debe seguir disponible");
    }
    private static void entregaLiberaRepartidor() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(paquete); g.registrarRepartidor(ana);
        g.asignarPaquete(paquete, ana);
        exigir(!ana.isDisponible(), "Asignado: debe quedar ocupado");
        g.entregarPaquete(paquete);
        exigir(ana.isDisponible(), "Tras la entrega debe quedar disponible");
    }
    private static void asignacionRechazada() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor otraZona = new Repartidor("R001", "Luis", "Salento", true);
        Repartidor ocupado = new Repartidor("R002", "Ana", "Armenia", false);
        g.registrarPaquete(paquete); g.registrarRepartidor(otraZona); g.registrarRepartidor(ocupado);
        exigir(!g.asignarPaquete(paquete, otraZona), "Repartidor de otra zona");
        exigir(!g.asignarPaquete(paquete, ocupado), "Repartidor no disponible");
        exigir(paquete.getRepartidorAsignado() == null, "No debe quedar asignado");
    }
    private static void asignacionZonaConTildes() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Calarcá", 1, 3, 20);
        Repartidor r = new Repartidor("R001", "María", "calarca", true);
        g.registrarPaquete(paquete); g.registrarRepartidor(r);
        exigir(g.asignarPaquete(paquete, r), "Debe coincidir 'Calarcá' con 'calarca'");
    }
    private static void disponibilidad() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        Repartidor fuera = new Repartidor("R002", "Jorge", "Armenia", false);
        g.registrarPaquete(paquete); g.registrarRepartidor(ana); g.registrarRepartidor(fuera);
        g.asignarPaquete(paquete, ana);
        exigir(!g.actualizarDisponibilidad(ana, true), "No debe liberar con paquete pendiente");
        exigir(!ana.isDisponible(), "Sigue ocupada");
        exigir(g.actualizarDisponibilidad(fuera, true), "Quien no tiene pendientes puede volver a estar disponible");
        exigir(fuera.isDisponible(), "Debe quedar disponible");
        exigir(g.actualizarDisponibilidad(fuera, false), "Se puede marcar como no disponible");
        exigir(!fuera.isDisponible(), "Debe quedar no disponible");
    }
    private static void buscarRepartidorNormaliza() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        exigir(g.registrarRepartidor(ana), "Registro válido");
        exigir(!g.registrarRepartidor(new Repartidor(" r001 ", "Otra", "Armenia", true)), "Id duplicado ignorando formato");
        exigir(g.buscarRepartidor(" r001 ") == ana, "Debe encontrar al mismo objeto");
        igual(null, g.buscarRepartidor("R999")); igual(null, g.buscarRepartidor(null));
    }
    private static void objetosNoRegistrados() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        GestorQuindioExpress otro = new GestorQuindioExpress();
        Paquete registrado = p("PQ101", "Armenia", 1, 3, 20);
        Paquete ajeno = p("PQ101", "Armenia", 1, 3, 20); // mismo código, otro objeto
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(registrado); g.registrarRepartidor(ana);
        otro.registrarPaquete(ajeno);
        exigir(!g.asignarPaquete(ajeno, ana), "Paquete que no pertenece a este gestor");
        exigir(!g.asignarPaquete(registrado, new Repartidor("R001", "Falsa", "Armenia", true)), "Repartidor no registrado");
        exigir(!g.entregarPaquete(ajeno), "No debe entregar paquetes ajenos");
        exigir(!g.asignarPaquete(null, ana) && !g.asignarPaquete(registrado, null), "Nulos rechazados");
    }
    private static void siguientePorPrioridadSaltaEntregados() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete alta = p("PQ001", "Armenia", 1, 5, 20);
        Paquete baja = p("PQ002", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(baja); g.registrarPaquete(alta); g.registrarRepartidor(ana);
        exigir(g.siguientePorPrioridad() == alta, "Primero la de mayor prioridad");
        g.asignarPaquete(alta, ana); g.entregarPaquete(alta);
        exigir(g.siguientePorPrioridad() == baja, "Debe saltar el entregado");
        g.asignarPaquete(baja, ana); g.entregarPaquete(baja);
        igual(null, g.siguientePorPrioridad());
    }
    private static void siguientePorLlegada() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete primero = p("PQ900", "Armenia", 1, 1, 20);
        Paquete segundo = p("PQ100", "Armenia", 1, 5, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(primero); g.registrarPaquete(segundo); g.registrarRepartidor(ana);
        exigir(g.siguientePorLlegada() == primero, "FIFO: el primero registrado");
        exigir(g.siguientePorLlegada() == primero, "Consultar no extrae");
        g.asignarPaquete(primero, ana); g.entregarPaquete(primero);
        exigir(g.siguientePorLlegada() == segundo, "Debe saltar el entregado");
    }
    private static void historialSoloLectura() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete paquete = p("PQ101", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(paquete); g.registrarRepartidor(ana);
        g.asignarPaquete(paquete, ana); g.entregarPaquete(paquete);
        Iterator<Paquete> it = g.getHistorialEntregas().iterator();
        exigir(it.next() == paquete, "Debe recorrer el historial");
        try { it.remove(); throw new AssertionError("Debió rechazar remove()"); }
        catch (UnsupportedOperationException esperado) { }
        igual(1, g.totalEntregas());
    }

    // ------------------------------------------------------------ paquete

    private static void igualdadPaquete() {
        Paquete a = p("pq1", "Armenia", 1, 3, 20);
        Paquete b = p("PQ1", "Salento", 5, 1, 50);
        igual(a, b); igual(a.hashCode(), b.hashCode());
        exigir(!a.equals(p("PQ2", "Armenia", 1, 3, 20)), "Códigos distintos no son iguales");
        exigir(a.compareTo(b) == 0, "compareTo consistente con equals");
    }
    private static void formatoDecimal() {
        exigir(p("PQ1", "Armenia", 4.5, 3, 20).toString().contains("peso=4.5 kg"), "Debe usar punto decimal");
    }
    private static void validacionesPaquete() {
        try { p("", "Armenia", 1, 3, 20); throw new AssertionError("Código vacío inválido"); }
        catch (IllegalArgumentException esperado) { }
        try { p("PQ1", "Armenia", 0, 3, 20); throw new AssertionError("Peso cero inválido"); }
        catch (IllegalArgumentException esperado) { }
        try { p("PQ1", "Armenia", 1, 6, 20); throw new AssertionError("Prioridad 6 inválida"); }
        catch (IllegalArgumentException esperado) { }
        try { p("PQ2", "Armenia", Double.NaN, 3, 20); throw new AssertionError("Peso NaN inválido"); }
        catch (IllegalArgumentException esperado) { }
        try { p("PQ3", "Armenia", Double.POSITIVE_INFINITY, 3, 20); throw new AssertionError("Peso infinito inválido"); }
        catch (IllegalArgumentException esperado) { }
    }

    private static void prioridadPorDefecto() {
        Paquete paquete = new Paquete("PQ1", "Armenia", 2.0, 20);
        igual(3, paquete.getPrioridad());
        igual(Paquete.PRIORIDAD_POR_DEFECTO, paquete.getPrioridad());
    }
    private static void siguienteSaltaAsignados() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        Paquete alta = p("PQ001", "Armenia", 1, 5, 20);
        Paquete baja = p("PQ002", "Armenia", 1, 3, 20);
        Repartidor ana = new Repartidor("R001", "Ana", "Armenia", true);
        g.registrarPaquete(baja); g.registrarPaquete(alta); g.registrarRepartidor(ana);
        g.asignarPaquete(alta, ana); // asignado pero aún no entregado
        exigir(g.siguientePorPrioridad() == baja, "No debe ofrecer un paquete ya asignado");
        exigir(g.siguientePorLlegada() == baja, "No debe ofrecer un paquete ya asignado (FIFO)");
    }
    private static void paquetesDeMunicipioConTildes() {
        GestorQuindioExpress g = new GestorQuindioExpress();
        g.registrarPaquete(p("PQ001", "Calarcá", 1, 3, 20));
        g.registrarPaquete(p("PQ002", "calarca", 1, 3, 20));
        igual(2, g.paquetesDeMunicipio(" CALARCA ").size());
        igual(0, g.paquetesDeMunicipio("Inexistente").size());
    }

    private static <T> List<T> comoLista(Iterable<T> iterable) {
        List<T> r = new ArrayList<>(); for (T x : iterable) r.add(x); return r;
    }
    private static List<String> codigos(List<Paquete> ps) {
        List<String> r = new ArrayList<>(); for (Paquete p : ps) r.add(p.getCodigo()); return r;
    }
}
