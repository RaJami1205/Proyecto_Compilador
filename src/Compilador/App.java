package Compilador;

import java.io.FileReader;
import java_cup.runtime.Symbol;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import Sintactico.Lexer;
import Sintactico.Parser;
import Sintactico.sym;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal de prueba del compilador.
 * Permite ejecutar el análisis léxico, sintáctico y semántico
 * sobre un archivo fuente del lenguaje definido.
 */
public class App {

    /**
     * Punto de entrada del programa.
     * Acepta:
     * - 1 argumento: ejecuta modo "all"
     * - 2 argumentos: ejecuta el modo indicado ("lex", "parse", "all")
     */
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
        System.out.println("            PROYECTO COMPILADOR           ");
        System.out.println("==========================================");
        System.out.println("Archivo: " + archivo);
        System.out.println("Modo: " + mode);
        System.out.println("------------------------------------------");

        try {
            switch (mode) {
                case "lex":
                    //ejecutarLexer(archivo);
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

    /**
     * Ejecuta el análisis completo:
     * 1. Léxico
     * 2. Sintáctico + semántico
     * Luego imprime un resumen general.
     */
    private static void ejecutarCompleto(String archivo) throws Exception {
        String nombreBase = new File(archivo).getName().replaceAll("\\.[^.]+$", "");
        new File("src/Reporte").mkdirs();
        String archivoReporte = "src/Reporte/" + nombreBase + "_reporte.txt";
        List<Symbol> tokens = new ArrayList<>(); // Acumular tokens durante el analisis

        boolean lexicoOK = ejecutarLexer(archivo,tokens);
        boolean analisisOK = ejecutarParser(archivo);
        
        // Nueva instancia del parser
        Lexer lexer2 = new Lexer(new FileReader(archivo));
        Parser parser2 = new Parser(lexer2);
        parser2.parse();

        boolean esValido = lexicoOK && analisisOK;

        ReporteCompilador.generarReporte(
            archivo,
            archivoReporte,
            tokens,
            parser2.getTablaSimbolos(),
            lexer2.getErroresLexicos(),
            parser2.getErroresSintacticos(),
            esValido
        );

         System.out.println("\nReporte generado en: " + archivoReporte);

        System.out.println("\n[RESUMEN FINAL]");
        System.out.println("------------------------------------------");

        if (esValido) {
            System.out.println("El archivo es léxica, sintáctica y semánticamente válido.");
            System.out.println("Puede pasar a la siguiente etapa de traducción.");
        } else if (lexicoOK) {
            System.out.println("Léxico: OK");
            System.out.println("Sintáctico/Semántico: contiene errores (ver arriba).");
        } else {
            System.out.println("El archivo contiene errores léxicos y/o semánticos (ver arriba).");
        }
    }

    /**
     * Ejecuta únicamente el análisis léxico.
     * Recorre todos los tokens del archivo y muestra sus datos en consola.
     * También imprime los errores léxicos acumulados por el lexer.
     *
     * @return true si no hubo errores léxicos, false en caso contrario
     */
    private static boolean ejecutarLexer(String archivo, List<Symbol> tokens) throws Exception {
        System.out.println("\n[1] ANALISIS LÉXICO");
        System.out.println("------------------------------------------");

        Lexer lexer = new Lexer(new FileReader(archivo));
        Symbol token;
        boolean hayErrores = false;

        // Se consumen todos los tokens válidos hasta EOF.
        // El lexer acumula errores léxicos en lugar de detenerse al primero.
        while ((token = lexer.next_token()).sym != sym.EOF) {
            if (token.value == null) {
                    token.value = lexer.yytext();
            }
            tokens.add(token);
            String tokenName = symToString(token.sym);
            String lexema = (token.value != null) ? token.value.toString() : lexer.yytext();

            System.out.printf("Línea %-4d Col %-4d %-22s -> %s%n",
                    token.left, token.right, tokenName, lexema);
        }

        // Mostrar errores léxicos reportados durante el escaneo
        if (!lexer.getErroresLexicos().isEmpty()) {
            hayErrores = true;
            System.out.println("\n--- Errores léxicos encontrados ---");
            lexer.getErroresLexicos().forEach(e -> System.out.println("  " + e));
        }

        System.out.println("\nResultado léxico: " + (hayErrores ? "incorrecto." : "correcto."));
        return !hayErrores;
    }

    /**
     * Ejecuta el análisis sintáctico y semántico.
     * El parser también construye la tabla de símbolos y la imprime al final.
     *
     * @return true si no hubo errores sintácticos ni semánticos, false en caso contrario
     */
    private static boolean ejecutarParser(String archivo) throws Exception {
        System.out.println("\n[2] ANALISIS SINTACTICO + TABLA DE SÍMBOLOS");
        System.out.println("---------------------------------------------");

        Lexer lexer = new Lexer(new FileReader(archivo));
        Parser parser = new Parser(lexer);

        // Inicia el proceso de parsing
        parser.parse();

        // Mostrar errores sintácticos si existen
        if (!parser.getErroresSintacticos().isEmpty()) {
            System.out.println("--- Errores sintácticos encontrados ---");
            int i = 1;
            for (String e : parser.getErroresSintacticos()) {
                System.out.println("  [S" + i + "] " + e);
                i++;
            }
        }

        // Mostrar errores semánticos si existen
        if (!parser.getErroresSemanticos().isEmpty()) {
            System.out.println("--- Errores semánticos encontrados ---");
            int i = 1;
            for (String e : parser.getErroresSemanticos()) {
                System.out.println("  [M" + i + "] " + e);
                i++;
            }
        }

        // Imprimir la tabla de símbolos generada durante el análisis
        System.out.println(parser.getTablaSimbolos().toPrettyString());

        System.out.println("\nResultado global: " +
                (parser.tieneErrores() ? "incorrecto." : "correcto."));

        return !parser.tieneErrores();
    }

    /**
     * Convierte el código numérico de un token al nombre declarado en sym.java.
     * Esto hace la salida del lexer más legible.
     */
    private static String symToString(int symCode) {
        try {
            return sym.terminalNames[symCode];
        } catch (Exception e) {
            return "SYM(" + symCode + ")";
        }
    }

    /**
     * Muestra la forma correcta de ejecutar la aplicación desde consola.
     */
    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java Compilador.App <archivo>");
        System.out.println("  java Compilador.App lex <archivo>");
        System.out.println("  java Compilador.App parse <archivo>");
        System.out.println("  java Compilador.App all <archivo>");
    }
}