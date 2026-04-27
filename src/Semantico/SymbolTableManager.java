package Semantico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

// Gestor de tablas de símbolos del compilador
public class SymbolTableManager {
    private final ScopeTable globalScope;
    private final Deque<ScopeTable> stack = new ArrayDeque<>();
    private final List<ScopeTable> allScopes = new ArrayList<>();
    private int scopeCounter = 0;

     /**
     * Constructor que inicializa el scope global y lo coloca en la pila.
     */
    public SymbolTableManager() {
        globalScope = new ScopeTable(scopeCounter++, "global", 0, null);
        stack.push(globalScope);
        allScopes.add(globalScope);
    }
    /**
     * Retorna el scope global del programa
     *
     * @return ScopeTable correspondiente al scope global
     */
    public ScopeTable getGlobalScope() {
        return globalScope;
    }
    /**
     * Retorna el scope actualmente activo 
     *
     * @return ScopeTable del scope actual
     */
    public ScopeTable currentScope() {
        return stack.peek();
    }

     /**
     * Crea un nuevo scope hijo del actual y lo coloca en el tope de la pila
     *
     * @param name Nombre identificador del nuevo scope 
     */
    public void enterScope(String name) {
        ScopeTable parent = currentScope();
        ScopeTable child = new ScopeTable(scopeCounter++, name, parent.getLevel() + 1, parent);
        stack.push(child);
        allScopes.add(child);
    }

     /**
     * Sale del scope actual eliminándolo de la pila.
     * No hace nada si solo queda el scope global.
     */
    public void exitScope() {
        if (stack.size() > 1) {
            stack.pop();
        }
    }

    /**
     * Inserta un simbolo en el scope actualmente activo.
     *
     * @param info Información del símbolo
     * @return true si se insertó correctamente, false si ya existia en el scope actual
     */
    public boolean insert(SymbolInfo info) {
        return currentScope().insert(info);
    }

    /**
     * Inserta un simbolo directamente en el scope global.
     * Se usa para registrar funciones y procedimientos.
     *
     * @param info Información del símbolo a insertar.
     * @return true si se insertó correctamente, false si ya existía en el scope global.
     */
    public boolean insertIntoGlobal(SymbolInfo info) {
        return globalScope.insert(info);
    }

     /**
     * Busca un símbolo recorriendo la pila de scopes desde el actual hacia el global
     *
     * @param lexeme Nombre del símbolo a buscar
     * @return SymbolInfo si se encontró, null si no existe 
     */
    public SymbolInfo lookup(String lexeme) {
        for (ScopeTable scope : stack) {
            SymbolInfo found = scope.lookupLocal(lexeme);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Busca un símbolo únicamente en el scope global.
     * Se usa para verificar existencia de funciones y procedimientos.
     *
     * @param lexeme Nombre del símbolo a buscar.
     * @return SymbolInfo si se encontró en el scope global, null en caso contrario.
     */
    public SymbolInfo lookupGlobal(String lexeme) {
        return globalScope.lookupLocal(lexeme);
    }

    /**
     * Retorna la lista de todos los scopes creados durante el análisis
     *
     * @return Lista de ScopeTable 
     */
    public List<ScopeTable> getAllScopes() {
        return allScopes;
    }

    /**
     * Genera una representación en texto formateado de toda la tabla de símbolos,
     * mostrando cada scope con sus símbolos y atributos en columnas alineadas.
     *
     * @return String con la tabla de símbolos lista para imprimir o escribir en archivo.
     */
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n================ TABLA DE SÍMBOLOS ================\n");

        for (ScopeTable scope : allScopes) {
            sb.append("\nScope: ").append(scope.getName())
              .append(" | nivel: ").append(scope.getLevel());

            if (scope.getParent() != null) {
                sb.append(" | padre: ").append(scope.getParent().getName());
            }

            sb.append("\n");
            sb.append(String.format(
                    "%-15s %-12s %-12s %-12s %-7s %-7s %-10s %-20s %-8s %-8s%n",
                    "Lexema", "Clase", "Tipo", "Scope", "Línea", "Col", "Init", "Parámetros", "Dims", "Usado"
            ));

            for (Map.Entry<String, SymbolInfo> entry : scope.getSymbols().entrySet()) {
                SymbolInfo s = entry.getValue();
                sb.append(String.format(
                        "%-15s %-12s %-12s %-12s %-7d %-7d %-10s %-20s %-8s %-8s%n",
                        s.getLexeme(),
                        s.getKind(),
                        s.getType(),
                        s.getScopeName(),
                        s.getLine(),
                        s.getColumn(),
                        s.isInitialized(),
                        s.getParamsAsText(),
                        s.getDimensionsAsText(),
                        s.isUsed()
                ));
            }
        }

        sb.append("===================================================\n");
        return sb.toString();
    }
}
