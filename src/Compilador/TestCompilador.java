package Compilador;

import AnalizadorSintactico.Lexer;
import AnalizadorSintactico.Parser;
import AnalizadorSintactico.sym;
import java.io.FileReader;
import java_cup.runtime.Symbol;

public class TestCompilador {

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
            System.out.println("\n[ERROR FATAL]");
            System.out.println("No fue posible completar el análisis del archivo.");
            System.out.println("Detalle: " + e.getMessage());
        }

        System.out.println("==========================================");
    }

    private static void ejecutarCompleto(String archivo) {
        boolean lexicoOK = ejecutarLexer(archivo);

        if (!lexicoOK) {
            System.out.println("\n[RESUMEN]");
            System.out.println("El archivo tiene errores léxicos.");
            System.out.println("No se ejecutará el análisis sintáctico.");
            return;
        }

        boolean sintacticoOK = ejecutarParser(archivo);

        System.out.println("\n[RESUMEN FINAL]");
        if (lexicoOK && sintacticoOK) {
            System.out.println("El archivo es léxica y sintácticamente válido.");
            System.out.println("Puede ser generado por la gramática actual.");
        } else if (lexicoOK) {
            System.out.println("El archivo no tiene errores léxicos,");
            System.out.println("pero sí contiene errores sintácticos.");
        }
    }

    private static boolean ejecutarLexer(String archivo) {
        System.out.println("\n[1] ANÁLISIS LÉXICO");
        System.out.println("------------------------------------------");

        try (FileReader fr = new FileReader(archivo)) {
            Lexer lexer = new Lexer(fr);
            Symbol token;

            while ((token = lexer.next_token()).sym != sym.EOF) {
                String tokenName = symToString(token.sym);
                String lexema = lexer.yytext();

                System.out.printf("Línea %-4d Col %-4d %-22s -> %s%n",
                        token.left, token.right, tokenName, lexema);
            }

            System.out.println("\nResultado léxico: correcto.");
            System.out.println("No se encontraron errores léxicos.");
            return true;

        } catch (Exception e) {
            System.out.println("\nResultado léxico: incorrecto.");
            System.out.println("Se detectó un error léxico:");
            System.out.println("  " + e.getMessage());
            return false;
        }
    }

    private static boolean ejecutarParser(String archivo) {
        System.out.println("\n[2] ANÁLISIS SINTÁCTICO");
        System.out.println("------------------------------------------");

        try (FileReader fr = new FileReader(archivo)) {
            Lexer lexer = new Lexer(fr);
            Parser parser = new Parser(lexer);

            parser.parse();

            System.out.println("Resultado sintáctico: correcto.");
            System.out.println("El archivo cumple la gramática definida.");
            return true;

        } catch (Exception e) {
            System.out.println("Resultado sintáctico: incorrecto.");
            System.out.println("Se detectó un error sintáctico.");
            System.out.println("Detalle: " + e.getMessage());
            return false;
        }
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
        System.out.println("  java Compilador.TestCompilador <archivo>");
        System.out.println("  java Compilador.TestCompilador lex <archivo>");
        System.out.println("  java Compilador.TestCompilador parse <archivo>");
        System.out.println("  java Compilador.TestCompilador all <archivo>");
    }
}