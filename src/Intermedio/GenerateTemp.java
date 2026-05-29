package Intermedio;

public class GenerateTemp {
    private static int count = 0;

    public static String newTemp() {
        return "t" + (++count);
    }

    public static void reset() {
        count = 0;
    }
}