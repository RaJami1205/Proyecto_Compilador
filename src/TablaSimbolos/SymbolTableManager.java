package TablaSimbolos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class SymbolTableManager {
    private final ScopeTable globalScope;
    private final Deque<ScopeTable> stack = new ArrayDeque<>();
    private final List<ScopeTable> allScopes = new ArrayList<>();
    private int scopeCounter = 0;

    public SymbolTableManager() {
        globalScope = new ScopeTable(scopeCounter++, "global", 0, null);
        stack.push(globalScope);
        allScopes.add(globalScope);
    }

    public ScopeTable getGlobalScope() {
        return globalScope;
    }

    public ScopeTable currentScope() {
        return stack.peek();
    }

    public void enterScope(String name) {
        ScopeTable parent = currentScope();
        ScopeTable child = new ScopeTable(scopeCounter++, name, parent.getLevel() + 1, parent);
        stack.push(child);
        allScopes.add(child);
    }

    public void exitScope() {
        if (stack.size() > 1) {
            stack.pop();
        }
    }

    public boolean insert(SymbolInfo info) {
        return currentScope().insert(info);
    }

    public boolean insertIntoGlobal(SymbolInfo info) {
        return globalScope.insert(info);
    }

    public SymbolInfo lookup(String lexeme) {
        for (ScopeTable scope : stack) {
            SymbolInfo found = scope.lookupLocal(lexeme);
            if (found != null) return found;
        }
        return null;
    }

    public SymbolInfo lookupGlobal(String lexeme) {
        return globalScope.lookupLocal(lexeme);
    }

    public List<ScopeTable> getAllScopes() {
        return allScopes;
    }

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
