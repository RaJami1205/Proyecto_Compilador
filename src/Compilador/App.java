package Compilador;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import Sintactico.Lexer;
import Sintactico.Parser;
import Sintactico.sym;

import Intermedio.GenerateLabel;
import Intermedio.GenerateTemp;

/**
 * Clase principal de prueba del compilador.
 * Organiza la ejecución por etapas: léxica, sintáctica, semántica,
 * tabla de símbolos y código intermedio.
 */
public class App {

    /** Carpeta única de salida del proyecto */
    private static final String OUTPUT_DIR = "src/Output";

    /**
     * Resultado del análisis léxico.
     * Guarda los tokens producidos y si hubo errores.
     */
    private static class LexResult {
        private final List<Symbol> tokens;
        private final List<String> erroresLexicos;
        private final boolean exito;

        public LexResult(List<Symbol> tokens, List<String> erroresLexicos, boolean exito) {
            this.tokens = tokens;
            this.erroresLexicos = erroresLexicos;
            this.exito = exito;
        }

        public List<Symbol> getTokens() {
            return tokens;
        }

        public List<String> getErroresLexicos() {
            return erroresLexicos;
        }

        public boolean isExito() {
            return exito;
        }
    }

    /**
     * Resultado del análisis del parser.
     * Guarda el parser ya ejecutado y si hubo errores.
     */
    private static class ParseResult {
        private final Parser parser;
        private final boolean exito;

        public ParseResult(Parser parser, boolean exito) {
            this.parser = parser;
            this.exito = exito;
        }

        public Parser getParser() {
            return parser;
        }

        public boolean isExito() {
            return exito;
        }
    }

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
            prepararCarpetaOutput();

            switch (mode) {
                case "lex":
                    ejecutarSoloLexico(archivo);
                    break;
                case "parse":
                    ejecutarSoloParser(archivo);
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
     * Crea la carpeta Output si no existe.
     * Si ya existe, elimina todo su contenido para evitar acumulación
     * de reportes y futuras salidas del compilador.
     */
    private static void prepararCarpetaOutput() throws Exception {
        File outputDir = new File(OUTPUT_DIR);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
            return;
        }

        File[] archivos = outputDir.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                eliminarRecursivo(archivo);
            }
        }
    }

    /**
     * Elimina archivos y subcarpetas de forma recursiva.
     */
    private static void eliminarRecursivo(File archivo) throws Exception {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    eliminarRecursivo(hijo);
                }
            }
        }

        if (!archivo.delete()) {
            throw new Exception("No se pudo eliminar: " + archivo.getAbsolutePath());
        }
    }

    /**
     * Reinicia los generadores de temporales y etiquetas del código intermedio.
     */
    private static void reiniciarGeneradoresIntermedios() {
        GenerateTemp.reset();
        GenerateLabel.reset();
    }

    /**
     * Ejecuta todas las etapas del compilador en orden:
     * 1. Léxico
     * 2. Sintáctico
     * 3. Semántico
     * 4. Tabla de símbolos
     * 5. Código intermedio
     */
    private static void ejecutarCompleto(String archivo) throws Exception {
        String archivoReporte = OUTPUT_DIR + "/" + "Reporte.txt";
        String archivoIntermedio = OUTPUT_DIR + "/" + "codigo_Intermedio.txt";

        LexResult lexResult = analizarLexico(archivo, true);

        ParseResult parseResult = null;
        if (lexResult.isExito()) {
            parseResult = analizarParser(archivo);
            imprimirSeccionSintactica(parseResult.getParser());
            imprimirSeccionSemantica(parseResult.getParser());
            imprimirSeccionTablaSimbolos(parseResult.getParser());
            imprimirSeccionCodigoIntermedio(parseResult.getParser(), archivoIntermedio);
        } else {
            imprimirTituloSeccion("[2] ANÁLISIS SINTÁCTICO");
            System.out.println("Análisis sintáctico omitido por errores léxicos.");

            imprimirTituloSeccion("[3] ANÁLISIS SEMÁNTICO");
            System.out.println("Análisis semántico omitido por errores léxicos.");

            imprimirTituloSeccion("[4] TABLA DE SÍMBOLOS");
            System.out.println("Tabla de símbolos no disponible porque el parser no fue ejecutado.");

            imprimirTituloSeccion("[5] CÓDIGO INTERMEDIO");
            System.out.println("Código intermedio no generado por errores léxicos.");
        }

        Parser parserParaReporte = (parseResult != null) ? parseResult.getParser() : null;

        ReporteCompilador.generarReporte(
                archivo,
                archivoReporte,
                lexResult.getTokens(),
                parserParaReporte != null ? parserParaReporte.getTablaSimbolos() : null,
                lexResult.getErroresLexicos(),
                parserParaReporte != null ? parserParaReporte.getErroresSintacticos() : new ArrayList<>(),
                lexResult.isExito() && parseResult != null && parseResult.isExito()
        );

        System.out.println("\nReporte generado en: " + archivoReporte);

        System.out.println("\n[RESUMEN FINAL]");
        System.out.println("------------------------------------------");

        if (lexResult.isExito() && parseResult != null && parseResult.isExito()) {
            System.out.println("El archivo es léxica, sintáctica y semánticamente válido.");
            System.out.println("Puede pasar a la siguiente etapa de traducción.");
        } else if (!lexResult.isExito()) {
            System.out.println("Léxico: contiene errores.");
            System.out.println("Las etapas posteriores fueron omitidas.");
        } else {
            System.out.println("Léxico: OK");
            System.out.println("Sintáctico/Semántico: contiene errores.");
        }
    }

    /**
     * Ejecuta únicamente el análisis léxico y muestra sus resultados.
     */
    private static void ejecutarSoloLexico(String archivo) throws Exception {
        analizarLexico(archivo, true);
    }

    /**
     * Ejecuta parser, semántica, tabla de símbolos y código intermedio.
     * Si hay errores léxicos previos, el parser no se ejecuta.
     */
    private static void ejecutarSoloParser(String archivo) throws Exception {
        LexResult lexResult = analizarLexico(archivo, false);

        if (!lexResult.isExito()) {
            imprimirTituloSeccion("[2] ANÁLISIS SINTÁCTICO");
            System.out.println("No se ejecutó el parser porque el archivo contiene errores léxicos.");
            return;
        }

        ParseResult parseResult = analizarParser(archivo);
        imprimirSeccionSintactica(parseResult.getParser());
        imprimirSeccionSemantica(parseResult.getParser());
        imprimirSeccionTablaSimbolos(parseResult.getParser());
        imprimirSeccionCodigoIntermedio(parseResult.getParser(), OUTPUT_DIR + "/codigo_Intermedio.txt");
    }

    /**
     * Ejecuta el análisis léxico.
     * Puede imprimir los tokens en consola según el modo de uso.
     *
     * @param archivo Ruta del archivo fuente
     * @param imprimirTokens true si se desea mostrar cada token en consola
     * @return Resultado del análisis léxico
     */
    private static LexResult analizarLexico(String archivo, boolean imprimirTokens) throws Exception {
        imprimirTituloSeccion("[1] ANÁLISIS LÉXICO");

        Lexer lexer = new Lexer(new FileReader(archivo));
        List<Symbol> tokens = new ArrayList<>();
        Symbol token;

        while ((token = lexer.next_token()).sym != sym.EOF) {
            if (token.value == null) {
                token.value = lexer.yytext();
            }
            tokens.add(token);

            if (imprimirTokens) {
                String tokenName = symToString(token.sym);
                String lexema = (token.value != null) ? token.value.toString() : lexer.yytext();

                System.out.printf("Línea %-4d Col %-4d %-22s -> %s%n",
                        token.left, token.right, tokenName, lexema);
            }
        }

        List<String> erroresLexicos = new ArrayList<>(lexer.getErroresLexicos());

        if (erroresLexicos.isEmpty()) {
            System.out.println("\nResultado léxico: correcto.");
        } else {
            System.out.println("--- Errores léxicos encontrados ---");
            for (String error : erroresLexicos) {
                System.out.println("  " + error);
            }
            System.out.println("\nResultado léxico: incorrecto.");
        }

        return new LexResult(tokens, erroresLexicos, erroresLexicos.isEmpty());
    }

    /**
     * Ejecuta el parser y devuelve el resultado del análisis sintáctico-semántico.
     *
     * @param archivo Ruta del archivo fuente
     * @return Resultado del parser ya ejecutado
     */
    private static ParseResult analizarParser(String archivo) throws Exception {
        reiniciarGeneradoresIntermedios();

        Lexer lexer = new Lexer(new FileReader(archivo));
        Parser parser = new Parser(lexer);
        parser.parse();

        return new ParseResult(parser, !parser.tieneErrores());
    }

    /**
     * Imprime la sección de errores sintácticos.
     *
     * @param parser Parser ya ejecutado
     */
    private static void imprimirSeccionSintactica(Parser parser) {
        imprimirTituloSeccion("[2] ANÁLISIS SINTÁCTICO");

        if (parser.getErroresSintacticos().isEmpty()) {
            System.out.println("No se encontraron errores sintácticos.");
            return;
        }

        System.out.println("--- Errores sintácticos encontrados ---");
        int i = 1;
        for (String error : parser.getErroresSintacticos()) {
            System.out.println("  [S" + i + "] " + error);
            i++;
        }
    }

    /**
     * Imprime la sección de errores semánticos.
     *
     * @param parser Parser ya ejecutado
     */
    private static void imprimirSeccionSemantica(Parser parser) {
        imprimirTituloSeccion("[3] ANÁLISIS SEMÁNTICO");

        if (parser.getErroresSemanticos().isEmpty()) {
            System.out.println("No se encontraron errores semánticos.");
            return;
        }

        System.out.println("--- Errores semánticos encontrados ---");
        int i = 1;
        for (String error : parser.getErroresSemanticos()) {
            System.out.println("  [M" + i + "] " + error);
            i++;
        }
    }

    /**
     * Imprime la tabla de símbolos generada por el parser.
     *
     * @param parser Parser ya ejecutado
     */
    private static void imprimirSeccionTablaSimbolos(Parser parser) {
        imprimirTituloSeccion("[4] TABLA DE SÍMBOLOS");
        System.out.println(parser.getTablaSimbolos().toPrettyString());
    }

    /**
     * Imprime y exporta el código intermedio si no hubo errores.
     *
     * @param parser Parser ya ejecutado
     * @param archivoIntermedio Ruta de salida del archivo de código intermedio
     */
    private static void imprimirSeccionCodigoIntermedio(Parser parser, String archivoIntermedio) {
        imprimirTituloSeccion("[5] CÓDIGO INTERMEDIO");

        if (parser.tieneErrores()) {
            System.out.println("No se generó código intermedio porque existen errores.");
            return;
        }

        parser.getCodigoIntermedio().printCode();
        parser.getCodigoIntermedio().exportToFile(archivoIntermedio);
        System.out.println("\nCódigo intermedio generado en: " + archivoIntermedio);
    }

    /**
     * Imprime un encabezado uniforme para cada etapa del compilador.
     *
     * @param titulo Título de la sección
     */
    private static void imprimirTituloSeccion(String titulo) {
        System.out.println();
        System.out.println(titulo);
        System.out.println("------------------------------------------");
    }

    /**
     * Convierte el código numérico de un token al nombre declarado en sym.java.
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