import quindioexpress.GestorQuindioExpress;
import quindioexpress.Paquete;
import quindioexpress.Repartidor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Punto de entrada de la aplicación de gestión de paquetes QuindíoExpress. */
public class Main {
    public static void main(String[] args) {
        GestorQuindioExpress gestor = new GestorQuindioExpress();
        cargarPaquetesIniciales(gestor);
        cargarRepartidores(gestor);

        mostrarEncabezado();
        mostrarResumenOperativo(gestor);
        mostrarPaquetesPorDestino(gestor);
        mostrarDespachosPendientes(gestor);
        consultarPaquete(gestor, "PQ731");
        mostrarOrdenamientos(gestor);
        mostrarRecursividad(gestor);
        mostrarBusquedaBinaria(gestor);
        mostrarDivideYVenceras(gestor);
        mostrarValidaciones(gestor);
        gestionarEntrega(gestor, "PQ731", "R001");
        mostrarDisponibilidadYLlegada(gestor);
        mostrarHistorialEntregas(gestor);
    }

    private static void cargarPaquetesIniciales(GestorQuindioExpress gestor) {
        // Datos de operación de ejemplo: 30 paquetes distribuidos en cinco municipios.
        String[] codigos = {"PQ731","PQ105","PQ942","PQ318","PQ567","PQ829","PQ420","PQ214","PQ842","PQ101",
                "PQ203","PQ415","PQ730","PQ611","PQ522","PQ906","PQ117","PQ638","PQ274","PQ359",
                "PQ488","PQ763","PQ192","PQ845","PQ306","PQ574","PQ681","PQ933","PQ248","PQ790"};
        String[] destinos = {"Armenia","Salento","Armenia","Calarcá","Salento","Armenia","Montenegro","Circasia",
                "Armenia","Salento","Armenia","Armenia","Armenia","Calarcá","Montenegro","Circasia","Salento",
                "Armenia","Calarcá","Montenegro","Circasia","Salento","Armenia","Calarcá","Montenegro",
                "Circasia","Salento","Armenia","Calarcá","Montenegro"};
        double[] pesos = {4.5,3.0,12.0,3.2,7.4,5.1,18.7,6.3,2.8,4.1,7.2,2.3,9.4,5.6,8.1,
                1.8,6.9,11.2,3.9,13.4,4.7,6.1,10.3,2.5,14.2,5.8,7.7,3.6,9.1,12.6};
        int[] prioridades = {3,5,4,2,5,3,5,2,4,1,4,5,3,2,4,1,5,3,2,4,1,5,3,2,4,1,5,3,2,4};
        int[] tiempos = {30,25,20,40,35,28,18,32,22,45,26,15,24,38,21,50,19,27,42,23,48,17,29,37,16,44,31,34,39,25};

        for (int i = 0; i < codigos.length; i++) {
            Paquete paquete = new Paquete(codigos[i], destinos[i], pesos[i], prioridades[i], tiempos[i]);
            if (!gestor.registrarPaquete(paquete)) {
                System.err.println("No se pudo registrar el paquete " + paquete.getCodigo() + ": código duplicado.");
            }
        }
    }

    private static void cargarRepartidores(GestorQuindioExpress gestor) {
        gestor.registrarRepartidor(new Repartidor("R001", "Ana Gómez", "Armenia", true));
        gestor.registrarRepartidor(new Repartidor("R002", "Luis Pérez", "Salento", true));
        gestor.registrarRepartidor(new Repartidor("R003", "María López", "Calarcá", true));
        gestor.registrarRepartidor(new Repartidor("R004", "Carlos Ruiz", "Montenegro", true));
        gestor.registrarRepartidor(new Repartidor("R005", "Diana Torres", "Circasia", true));
        gestor.registrarRepartidor(new Repartidor("R006", "Jorge Díaz", "Armenia", false));
    }

    private static void mostrarEncabezado() {
        System.out.println("====================================================");
        System.out.println("                 QUINDÍOEXPRESS                     ");
        System.out.println("          Gestión de envíos y entregas              ");
        System.out.println("====================================================");
    }

    private static void mostrarResumenOperativo(GestorQuindioExpress gestor) {
        int entregados = 0;
        for (Paquete paquete : gestor.getPaquetesRegistrados()) {
            if (paquete.isEntregado()) entregados++;
        }
        int pendientes = gestor.getPaquetesRegistrados().size() - entregados;
        int repartidoresDisponibles = 0;
        for (Repartidor repartidor : gestor.getRepartidores()) {
            if (repartidor.isDisponible()) repartidoresDisponibles++;
        }

        System.out.println("\nRESUMEN DE OPERACIONES");
        System.out.println("Paquetes registrados : " + gestor.getPaquetesRegistrados().size());
        System.out.println("Entregas completadas : " + entregados);
        System.out.println("Paquetes pendientes  : " + pendientes);
        System.out.println("Municipios atendidos : " + String.join(", ", gestor.getMunicipios()));
        System.out.println("Repartidores activos : " + repartidoresDisponibles + " disponibles de " + gestor.getRepartidores().size());
    }

    private static void mostrarPaquetesPorDestino(GestorQuindioExpress gestor) {
        System.out.println("\nPAQUETES POR MUNICIPIO");
        for (Map.Entry<String, List<Paquete>> entrada : gestor.getPaquetesPorMunicipio().entrySet()) {
            double pesoTotal = 0.0;
            int pendientes = 0;
            for (Paquete paquete : entrada.getValue()) {
                pesoTotal += paquete.getPeso();
                if (!paquete.isEntregado()) pendientes++;
            }
            System.out.printf(Locale.US, "%-12s %2d paquetes | %2d pendientes | %.1f kg%n",
                    entrada.getKey(), entrada.getValue().size(), pendientes, pesoTotal);
        }
    }

    private static void mostrarDespachosPendientes(GestorQuindioExpress gestor) {
        System.out.println("\nPRÓXIMOS PAQUETES PARA DESPACHO");
        Paquete siguiente = gestor.siguientePorPrioridad();
        if (siguiente == null) {
            System.out.println("No hay paquetes pendientes de despacho.");
            return;
        }
        System.out.println("Siguiente por prioridad : " + siguiente.getCodigo());
        System.out.println("Pendiente más antiguo   : " + gestor.siguientePorLlegada().getCodigo());
        int mostrados = 0;
        for (Paquete paquete : gestor.obtenerPaquetesPorPrioridad()) {
            if (paquete.isEntregado()) continue;
            System.out.println("- " + paquete);
            if (++mostrados == 5) break;
        }
    }

    private static void consultarPaquete(GestorQuindioExpress gestor, String codigo) {
        System.out.println("\nCONSULTA DE ENVÍO");
        Paquete paquete = gestor.buscarPaquete(codigo);
        if (paquete == null) {
            System.out.println("No se encontró el paquete " + codigo + ".");
            return;
        }
        System.out.println(paquete);
        System.out.println("Estado: " + (paquete.isEntregado() ? "Entregado" : "Pendiente de entrega"));
    }

    private static void mostrarOrdenamientos(GestorQuindioExpress gestor) {
        System.out.println("\nORDENAMIENTOS (Comparator; el orden natural sigue siendo por código)");
        imprimirPrimeros("Por peso (mayor a menor)", gestor.obtenerPaquetesPorPeso(), 3);
        imprimirPrimeros("Por tiempo estimado (menor a mayor)", gestor.obtenerPaquetesPorTiempo(), 3);
        imprimirPrimeros("Por prioridad (mayor, menor tiempo, código)", gestor.obtenerPaquetesPorPrioridad(), 3);
    }

    private static void imprimirPrimeros(String titulo, List<Paquete> lista, int cantidad) {
        System.out.println(titulo + ":");
        for (int i = 0; i < cantidad && i < lista.size(); i++) System.out.println("  - " + lista.get(i));
    }

    private static void mostrarRecursividad(GestorQuindioExpress gestor) {
        System.out.println("\nRECURSIVIDAD");
        System.out.printf(Locale.US, "Peso total hacia Armenia (recursivo): %.1f kg%n", gestor.pesoTotalPorMunicipio("Armenia"));
        System.out.printf(Locale.US, "Peso total hacia calarca (sin tilde): %.1f kg%n", gestor.pesoTotalPorMunicipio("calarca"));
        System.out.println("Paquetes con prioridad >= 4 (recursivo): " + gestor.contarPrioridadMinima(4));
    }

    private static void mostrarBusquedaBinaria(GestorQuindioExpress gestor) {
        System.out.println("\nBÚSQUEDA BINARIA (sobre una copia ordenada por código)");
        Paquete encontrado = gestor.buscarPorCodigoConBusquedaBinaria("PQ731");
        System.out.println("PQ731 -> " + (encontrado == null ? "no encontrado" : encontrado));
        System.out.println("PQ999 -> " + (gestor.buscarPorCodigoConBusquedaBinaria("PQ999") == null
                ? "no encontrado" : "encontrado (inesperado)"));
        List<Paquete> ordenados = new ArrayList<>(gestor.getPaquetesRegistrados());
        java.util.Collections.sort(ordenados);
        System.out.println("Primer código en orden natural: " + ordenados.get(0).getCodigo()
                + " | último: " + ordenados.get(ordenados.size() - 1).getCodigo());
    }

    private static void mostrarDivideYVenceras(GestorQuindioExpress gestor) {
        System.out.println("\nDIVIDE Y VENCERÁS");
        Paquete masPesado = GestorQuindioExpress.paqueteMasPesado(gestor.getPaquetesRegistrados());
        System.out.println("Paquete más pesado de la jornada: " + masPesado);
        List<Paquete> armenia = gestor.paquetesDeMunicipio("Armenia");
        System.out.println("Más pesado con destino Armenia: " + GestorQuindioExpress.paqueteMasPesado(armenia));
    }

    private static void mostrarValidaciones(GestorQuindioExpress gestor) {
        System.out.println("\nREGLAS Y VALIDACIONES");
        boolean duplicado = gestor.registrarPaquete(new Paquete("pq731", "Salento", 1.0, 3, 10));
        System.out.println("Registrar código duplicado (pq731): " + (duplicado ? "aceptado" : "rechazado"));
        boolean repDuplicado = gestor.registrarRepartidor(new Repartidor(" r001 ", "Otra Persona", "Armenia", true));
        System.out.println("Registrar identificación duplicada (r001): " + (repDuplicado ? "aceptado" : "rechazado"));
        Paquete salento = gestor.buscarPaquete("PQ105");
        System.out.println("Asignar PQ105 (Salento) a R001 (Armenia): "
                + (gestor.asignarPaquete(salento, gestor.buscarRepartidor("R001")) ? "aceptado" : "rechazado por zona"));
        Paquete armenia = gestor.buscarPaquete("PQ942");
        System.out.println("Asignar PQ942 (Armenia) a R006 (no disponible): "
                + (gestor.asignarPaquete(armenia, gestor.buscarRepartidor("R006")) ? "aceptado" : "rechazado por no disponible"));
        System.out.println("Entregar PQ942 sin repartidor asignado: "
                + (gestor.entregarPaquete(armenia) ? "aceptado" : "rechazado"));
        try {
            new Paquete("PQ000", "Armenia", 0, 3, 10);
        } catch (IllegalArgumentException e) {
            System.out.println("Crear paquete con peso 0: rechazado (" + e.getMessage() + ")");
        }
        System.out.println("Prioridad por defecto de un paquete sin prioridad: "
                + new Paquete("PQ001", "Armenia", 1.0, 10).getPrioridad());
    }

    private static void mostrarDisponibilidadYLlegada(GestorQuindioExpress gestor) {
        System.out.println("\nDISPONIBILIDAD Y ORDEN DE LLEGADA");
        Repartidor ana = gestor.buscarRepartidor("R001");
        Paquete otro = gestor.buscarPaquete("PQ942");
        System.out.println("Tras entregar PQ731, R001 disponible: " + ana.isDisponible());
        gestor.asignarPaquete(otro, ana);
        System.out.println("Se asigna PQ942 a R001; disponible: " + ana.isDisponible());
        System.out.println("Marcar R001 disponible con paquete pendiente: "
                + (gestor.actualizarDisponibilidad(ana, true) ? "aceptado" : "rechazado"));
        System.out.println("Siguiente por prioridad (ignora asignados y entregados): " + gestor.siguientePorPrioridad().getCodigo());
        gestor.entregarPaquete(otro);
        System.out.println("Se entrega PQ942; R001 disponible: " + ana.isDisponible());
        System.out.println("Pendiente más antiguo ahora: " + gestor.siguientePorLlegada().getCodigo());
    }

    private static void gestionarEntrega(GestorQuindioExpress gestor, String codigo, String idRepartidor) {
        System.out.println("\nASIGNACIÓN Y ENTREGA");
        Paquete paquete = gestor.buscarPaquete(codigo);
        if (paquete == null) {
            System.out.println("No se puede procesar la entrega: paquete inexistente.");
            return;
        }
        Repartidor repartidor = gestor.buscarRepartidor(idRepartidor);
        if (repartidor == null) {
            System.out.println("No se puede procesar la entrega: repartidor inexistente (" + idRepartidor + ").");
            return;
        }
        if (!gestor.asignarPaquete(paquete, repartidor)) {
            System.out.println("No se pudo asignar " + paquete.getCodigo() + " a " + repartidor.getNombre() + ".");
            return;
        }
        System.out.println("Paquete " + paquete.getCodigo() + " asignado a " + repartidor.getNombre()
                + " (" + repartidor.getIdentificacion() + ").");
        if (gestor.entregarPaquete(paquete)) {
            System.out.println("Entrega registrada correctamente: " + paquete.getCodigo() + ".");
        } else {
            System.out.println("No se pudo completar la entrega de " + paquete.getCodigo() + ".");
        }
    }

    private static void mostrarHistorialEntregas(GestorQuindioExpress gestor) {
        System.out.println("\nHISTORIAL DE ENTREGAS");
        for (Paquete paquete : gestor.getHistorialEntregas()) {
            Repartidor repartidor = paquete.getRepartidorAsignado();
            System.out.println("- " + paquete.getCodigo() + " | " + paquete.getDestino()
                    + " | " + (repartidor == null ? "Sin repartidor"
                    : repartidor.getNombre() + " (" + repartidor.getIdentificacion() + ")"));
        }
        if (gestor.totalEntregas() == 0) System.out.println("Todavía no hay entregas registradas.");
        System.out.println("Total de entregas en el historial: " + gestor.totalEntregas());
    }
}
