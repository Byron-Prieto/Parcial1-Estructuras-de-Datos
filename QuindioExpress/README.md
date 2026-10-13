# QuindíoExpress

Aplicación de consola para gestionar paquetes, destinos, prioridades, repartidores y entregas.

## Requisitos
- Java JDK 8 o posterior.

## Estructura
```
README.md
src/
    Main.java
    PruebasUnitarias.java
    quindioexpress/
        Coleccion.java              (interfaz genérica)
        ListaEnlazada.java          (lista simplemente enlazada propia, Iterable/Iterator)
        Texto.java                  (utilidad interna, no pública)
        Paquete.java                (Comparable por código + Comparator por prioridad, peso y tiempo)
        Repartidor.java
        GestorQuindioExpress.java
```
Los métodos que cambian el estado (`marcarEntregado`, `setRepartidorAsignado`, `setDisponible`) no son públicos: solo el gestor los usa.

## Compilar
Desde la carpeta raíz del repositorio:

```bash
javac -encoding UTF-8 -d out src/quindioexpress/*.java src/Main.java src/PruebasUnitarias.java
```

## Ejecutar la aplicación

```bash
java -cp out Main
```

El programa carga 30 paquetes (5 municipios) y 6 repartidores y demuestra, en orden: resumen operativo, paquetes por municipio, siguiente despacho por prioridad y por llegada, consulta de un envío, ordenamientos con `Comparator` (peso, tiempo, prioridad), recursividad (peso total por municipio y conteo por prioridad), búsqueda binaria, divide y vencerás (paquete más pesado), reglas y validaciones (duplicados, zona, repartidor no disponible, entrega sin asignar, peso inválido, prioridad por defecto), asignación y entrega, disponibilidad del repartidor y el historial de entregas.

## Ejecutar pruebas unitarias

```bash
java -ea -cp out PruebasUnitarias
```

Las 35 pruebas cubren la lista enlazada propia, la normalización de municipios (mayúsculas, espacios y tildes), las reglas de prioridad, la recursividad, la búsqueda binaria, divide y vencerás, las validaciones de `Paquete` (incluida la prioridad por defecto) y las reglas de asignación, entrega y disponibilidad entre `Paquete` y `Repartidor`.

## Notas de diseño
- **Estructuras:** `ArrayList` (orden de registro), `HashMap<String, Paquete>` (consulta por código), `TreeMap<String, List<Paquete>>` (municipio → paquetes; sus claves dan los municipios sin repetidos y en orden alfabético, y `getMunicipios()` los entrega en un `TreeSet`), `ArrayDeque` como `Queue` (atención por orden de llegada), `PriorityQueue` (despacho por prioridad) y `ListaEnlazada<Paquete>` propia (historial de entregas).
- **Prioridad:** valores válidos de 1 a 5 (regla del enunciado); si no se indica, el paquete usa la prioridad por defecto 3.
- **Despacho:** `siguientePorPrioridad` y `siguientePorLlegada` solo consultan (no extraen) y omiten paquetes ya asignados o entregados.
- **Búsqueda binaria:** exige lista ordenada por código; `buscarPorCodigoConBusquedaBinaria` ordena una copia (O(n log n) por consulta). Para consultas frecuentes se usa `buscarPaquete` (HashMap, O(1) promedio).
- **Municipios:** el nombre canónico es el de la primera escritura registrada; las comparaciones ignoran mayúsculas, tildes y espacios (`paquetesDeMunicipio`, `pesoTotalPorMunicipio`, asignación por zona).
- Los repartidores se manejan como objetos `Repartidor` (`buscarRepartidor`, `asignarPaquete(Paquete, Repartidor)`, `entregarPaquete(Paquete)`).
- La entrega libera automáticamente al repartidor; `actualizarDisponibilidad` no permite liberarlo con un paquete pendiente.
- La recursión usa una llamada por elemento; con decenas de miles de paquetes podría desbordar la pila.
