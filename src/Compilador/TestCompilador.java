package Compilador;

import AnalizadorSintactico.Lexer;
import AnalizadorSintactico.Parser;
import AnalizadorSintactico.sym;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java_cup.runtime.Symbol;

public class TestCompilador {

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            return;
        }

        String mode = args[0].trim().toLowerCase();
        Path sourcePath = Path.of(args[1]);

        if (!Files.exists(sourcePath)) {
            System.err.println("No existe el archivo: " + sourcePath.toAbsolutePath());
            return;
        }

        try {
            switch (mode) {
                case "lex":
                    runLexer(sourcePath);
                    break;
                case "parse":
                    runParser(sourcePath);
                    break;
                default:
                    System.err.println("Modo no válido: " + mode);
                    printUsage();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Error durante la prueba: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private static void runLexer(Path sourcePath) throws IOException {
        try (Reader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
            Lexer lexer = new Lexer(reader);
            Symbol token;

            System.out.println("=== ANALISIS LEXICO ===");
            System.out.println("Archivo: " + sourcePath.toAbsolutePath());

            do {
                token = lexer.next_token();
                String tokenName = sym.terminalNames[token.sym];
                String lexeme = token.value != null ? token.value.toString() : "";
                System.out.printf("[%d:%d] %-22s %s%n", token.left, token.right, tokenName, lexeme);
            } while (token.sym != sym.EOF);

            System.out.println("Analisis lexico completado sin errores.");
        }
    }

    private static void runParser(Path sourcePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            System.out.println("=== ANALISIS SINTACTICO ===");
            System.out.println("Archivo: " + sourcePath.toAbsolutePath());

            parser.parse();

            System.out.println("El programa es sintacticamente valido segun la gramatica definida.");
        }
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java Compilador.TestCompilador lex <ruta_archivo>");
        System.out.println("  java Compilador.TestCompilador parse <ruta_archivo>");
    }
}
