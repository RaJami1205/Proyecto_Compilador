package Compilador;

import AnalizadorSintactico.Lexer;
import AnalizadorSintactico.Parser;
import AnalizadorSintactico.sym;
import java.io.FileReader;
import java_cup.runtime.Symbol;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class App {

    public static void main(String[] args) {
        String mode;
        String archivo;

        if (args.length == 1) {
            mode = "all";
            archivo = args[0];
        } else if (args.length == 2) {
            mode = args[0].trim().toLowerCase();
            archivo = args[1];
        } else {
            printUsage();
            return;
        }

        System.out.println("==========================================");
        System.out.println("      PROYECTO 1 - ANALIZADOR");
        System.out.println("==========================================");
        System.out.println("Archivo: " + archivo);
        System.out.println("Modo: " + mode);
        System.out.println("------------------------------------------");

        try {
            switch (mode) {
                case "lex":
                    ejecutarLexer(archivo);
                    break;
                case "parse":
                    ejecutarParser(archivo);
                    break;
                case "all":
                    ejecutarCompleto(archivo);
                    break;
                default:
                    System.out.println("Modo no válido: " + mode);
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            System.out.println("\n[ERROR FcATAL]");
            System.out.println("No fue posible completar el análisis del archivo.");
            System.out.println("Detalle: " + e.getMessage());
        }

        System.out.println("==========================================");
    }

    private static void ejecutarCompleto(String archivo) throws Exception{
        boolean lexicoOK = ejecutarLexer(archivo);
        boolean sintacticoOK = ejecutarParser(archivo);

        System.out.println("\n[RESUMEN FINAL]");
        System.out.println("------------------------------------------");
        if (lexicoOK && sintacticoOK) {
            System.out.println("El archivo es léxica y sintácticamente válido.");
            System.out.println("Puede ser generado por la gramática actual.");
        } else if (lexicoOK) {
            System.out.println("Léxico: OK");
            System.out.println("Sintáctico: contiene errores (ver arriba).");
        } else if (sintacticoOK) {
            System.out.println("Léxico: contiene errores (ver arriba).");
            System.out.println("Sintáctico: OK");
        } else {
            System.out.println("El archivo contiene errores léxicos y sintácticos (ver arriba).");
        }
    }

    private static boolean ejecutarLexer(String archivo) throws Exception{
        System.out.println("\n[1] ANALISIS LÉXICO");
        System.out.println("------------------------------------------");

        Lexer lexer = new Lexer(new FileReader(archivo));
        Symbol token;
        boolean hayErrores = false;

        // next_token() ya no lanza excepción — simplemente imprime el error
        // y retorna el siguiente token válido
        while ((token = lexer.next_token()).sym != sym.EOF) {
            String tokenName = symToString(token.sym);
            String lexema   = (token.value != null) ? token.value.toString() : lexer.yytext();

            System.out.printf("Línea %-4d Col %-4d %-22s -> %s%n",
                    token.left, token.right, tokenName, lexema);
        }

        // Consultar errores acumulados en el lexer
        if (!lexer.getErroresLexicos().isEmpty()) {
            hayErrores = true;
            System.out.println("\n--- Errores léxicos encontrados ---");
            lexer.getErroresLexicos().forEach(e -> System.out.println("  " + e));
        }

        System.out.println("\nResultado léxico: " + (hayErrores ? "incorrecto." : "correcto."));
        return !hayErrores;
    }

    private static boolean ejecutarParser(String archivo) throws Exception{
        System.out.println("\n[2] ANALISIS SINTACTICO");
        System.out.println("------------------------------------------");

        Lexer lexer   = new Lexer(new FileReader(archivo));
        Parser parser = new Parser(lexer);

        parser.parse();

        if (!parser.getErroresSintacticos().isEmpty()) {
            System.out.println("--- Errores sintácticos encontrados ---");
            parser.getErroresSintacticos().forEach(e -> System.out.println("  " + e));
        }

        System.out.println("\nResultado sintáctico: " +
                (parser.tieneErrores() ? "incorrecto." : "correcto."));
        return !parser.tieneErrores();
    }

    private static String symToString(int symCode) {
        try {
            return sym.terminalNames[symCode];
        } catch (Exception e) {
            return "SYM(" + symCode + ")";
        }
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java Compilador.App <archivo>");
        System.out.println("  java Compilador.App lex <archivo>");
        System.out.println("  java Compilador.App parse <archivo>");
        System.out.println("  java Compilador.App all <archivo>");
    }
}