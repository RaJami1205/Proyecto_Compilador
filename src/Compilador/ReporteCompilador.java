package Compilador;


import Sintactico.Lexer;
import Sintactico.Parser;
import Sintactico.sym;
import Semantico.SymbolTableManager;
import Semantico.ScopeTable;
import Semantico.SymbolInfo;
import Semantico.SymbolKind;
import java_cup.runtime.Symbol;
import java.io.*;
import java.util.*;

public class ReporteCompilador {
    public static void generarReporte(
            String archivoFuente,
            String archivoSalida,
            List<Symbol> tokens,
            SymbolTableManager tablaSimbolos,
            List<String> erroresLexicos,
            List<String> erroresSintacticos,
            boolean esValido
    ) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(archivoSalida), "UTF-8"))) {

            pw.println("REPORTE DE ANÁLISIS - COMPILADOR");
            pw.println();
            pw.println("Archivo fuente : " + archivoFuente);
            pw.println();

            // SECCIÓN 1: TOKENS
            pw.println("==========================================");
            pw.println(" SECCIÓN 1: TOKENS ENCONTRADOS");
            pw.println("==========================================");
            pw.printf("%-6s %-6s %-25s %-25s%n",
                    "Línea", "Col", "Token", "Lexema");
            pw.println("-".repeat(65));

            for (Symbol token : tokens) {
                String tokenName = sym.terminalNames[token.sym];
                String lexema    = token.value != null ? token.value.toString() : tokenName;
                pw.printf("%-6d %-6d %-25s %-25s%n",
                        token.left, token.right, tokenName, lexema);
            }

            pw.println();
        }
    }
}
