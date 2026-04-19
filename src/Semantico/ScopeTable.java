package Semantico;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScopeTable {
    private final int id;
    private final String name;
    private final int level;
    private final ScopeTable parent;
    private final Map<String, SymbolInfo> symbols = new LinkedHashMap<>();

    public ScopeTable(int id, String name, int level, ScopeTable parent) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public ScopeTable getParent() {
        return parent;
    }

    public boolean containsLocal(String lexeme) {
        return symbols.containsKey(lexeme);
    }

    public boolean insert(SymbolInfo info) {
        if (containsLocal(info.getLexeme())) {
            return false;
        }
        symbols.put(info.getLexeme(), info);
        return true;
    }

    public SymbolInfo lookupLocal(String lexeme) {
        return symbols.get(lexeme);
    }

    public Map<String, SymbolInfo> getSymbols() {
        return symbols;
    }
}
