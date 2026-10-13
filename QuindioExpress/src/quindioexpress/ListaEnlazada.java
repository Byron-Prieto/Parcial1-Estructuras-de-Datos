package quindioexpress;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Lista simplemente enlazada propia.
 * No utiliza colecciones del Java Collections Framework para almacenar nodos.
 * El iterador es de solo lectura: {@code remove()} lanza UnsupportedOperationException.
 */
public class ListaEnlazada<T> implements Coleccion<T> {
    private static class Nodo<E> {
        E valor;
        Nodo<E> siguiente;
        Nodo(E valor) { this.valor = valor; }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamanio;

    public ListaEnlazada() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    @Override
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamanio++;
    }

    @Override
    public boolean eliminar(T elemento) {
        if (cabeza == null) return false;

        if (Objects.equals(cabeza.valor, elemento)) {
            cabeza = cabeza.siguiente;
            tamanio--;
            if (cabeza == null) cola = null;
            return true;
        }

        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (Objects.equals(actual.siguiente.valor, elemento)) {
                if (actual.siguiente == cola) cola = actual;
                actual.siguiente = actual.siguiente.siguiente;
                tamanio--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    @Override
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        return actual.valor;
    }

    @Override
    public int tamanio() { return tamanio; }

    @Override
    public boolean estaVacia() { return tamanio == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() { return actual != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }
}
