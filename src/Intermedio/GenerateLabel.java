package Intermedio;

import java.util.HashMap;
import java.util.Map;

public class GenerateLabel {
    private static final Map<String, Integer> counters = new HashMap<>();

    public static String newLabel(String base) {
        if (base == null || base.isBlank()) {
            base = "label";
        }

        int next = counters.getOrDefault(base, 0) + 1;
        counters.put(base, next);
        return base + "_" + next;
    }

    public static void reset() {
        counters.clear();
    }
}