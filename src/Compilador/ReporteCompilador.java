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
}
