package quindioexpress;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades internas para comparar textos sin importar mayúsculas, tildes ni espacios extra. */
final class Texto {
    private Texto() { }

    /** Quita espacios sobrantes (inicio, fin y repetidos). */
    static String limpiar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    /** Clave de comparación: sin tildes, en minúsculas y con espacios normalizados. */
    static String clave(String texto) {
        String sinTildes = Normalizer.normalize(limpiar(texto), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}
