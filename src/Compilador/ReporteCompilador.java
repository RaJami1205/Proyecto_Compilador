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

    /**
     * genera un reporte completo del analisis del compilador en un archivo TXT
     * el reporte incluye los tokens encontrados, la tabla de simbolos,
     * los errores lexicos y sintacticos, y el resultado final del analisis.
     *
     * @param archivoFuente      ruta del archivo fuente 
     * @param archivoSalida      ruta del archivo.txt donde se escribira el reporte
     * @param tokens             lista de tokens reconocidos durante el análisis léxico
     * @param tablaSimbolos      tabla de símbolos construida durante el análisis sintáctico
     * @param erroresLexicos     lista de errores léxicos encontrados durante el escaneo
     * @param erroresSintacticos lista de errores sintacticos 
     * @param esValido           true si el archivo fuente no contiene errores léxicos
     *                           ni sintácticos
     * @throws IOException 
     */
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
            
            //  SECCIÓN 2: TABLA DE SÍMBOLOS
            pw.println("==========================================");
            pw.println(" SECCIÓN 2: TABLA DE SÍMBOLOS");
            pw.println("==========================================");

            if (tablaSimbolos == null) {
                pw.println("  No disponible.");
            } else {
                pw.println(tablaSimbolos.toPrettyString());
            }

            pw.println();

            //  SECCIÓN 3: ERRORES 
            pw.println("==========================================");
            pw.println(" SECCIÓN 3: ERRORES ENCONTRADOS");
            pw.println("==========================================");

            pw.println();
            pw.println("--- Errores léxicos (" + erroresLexicos.size() + ") ---");
            if (erroresLexicos.isEmpty()) {
                pw.println("  Ninguno.");
            } else {
                erroresLexicos.forEach(e -> pw.println("  " + e));
            }

            pw.println();
            pw.println("--- Errores sintácticos (" + erroresSintacticos.size() + ") ---");
            if (erroresSintacticos.isEmpty()) {
                pw.println("  Ninguno.");
            } else {
                erroresSintacticos.forEach(e -> pw.println("  " + e));
            }

            pw.println();

             // ── SECCIÓN 4: RESULTADO ───────────────────────────
            pw.println("==========================================");
            pw.println(" SECCIÓN 4: RESULTADO");
            pw.println("==========================================");
            if (esValido) {
                pw.println("El archivo fuente SÍ puede ser generado por la gramática.");
            } else {
                pw.println("El archivo fuente NO puede ser generado por la gramática.");
                pw.println("Errores léxicos    : " + erroresLexicos.size());
                pw.println("Errores sintácticos: " + erroresSintacticos.size());
            }

            pw.println();
            pw.println("==========================================");
            pw.println("           FIN DEL REPORTE");
            pw.println("==========================================");

        }

        
    }


}
