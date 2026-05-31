package Intermedio;

import java.util.HashMap;
import java.util.Map;

/**
 * Genera etiquetas únicas para el código intermedio.
 * 
 * Usa un contador por nombre base para producir etiquetas
 * como if_else_1, if_else_2, switch_end_1, etc.
 */
public class GenerateLabel {
    /** Contadores asociados a cada prefijo de etiqueta */
    private static final Map<String, Integer> counters = new HashMap<>();

    /**
     * Genera una nueva etiqueta a partir de un nombre base.
     * Si el nombre base es nulo o vacío, usa "label".
     */
    public static String newLabel(String base) {
        if (base == null || base.isBlank()) {
            base = "label";
        }

        int next = counters.getOrDefault(base, 0) + 1;
        counters.put(base, next);
        return base + "_" + next;
    }

    /**
     * Reinicia todos los contadores de etiquetas.
     * 
     * Se usa al comenzar una nueva corrida del compilador
     * para evitar que las etiquetas sigan numerándose
     * desde ejecuciones anteriores.
     */
    public static void reset() {
        counters.clear();
    }
}