package Intermedio;

/**
 * Genera nombres de variables temporales para el código intermedio.
 * 
 * Produce identificadores únicos con formato t1, t2, t3, ...
 */
public class GenerateTemp {
    /** Contador global de temporales generados */
    private static int count = 0;

    /**
     * Crea un nuevo temporal único.
     */
    public static String newTemp() {
        return "t" + (++count);
    }

    /**
     * Reinicia el contador de temporales.
     * 
     * Se usa al iniciar una nueva ejecución del compilador
     * para que la numeración vuelva a comenzar desde t1.
     */
    public static void reset() {
        count = 0;
    }
}