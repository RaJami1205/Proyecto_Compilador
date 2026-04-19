package TablaSimbolos;

import java.util.ArrayList;
import java.util.List;

public class SymbolInfo {
    private final String lexeme;
    private final SymbolKind kind;
    private final DataType type; // para funciones, este es el return type
    private final int line;
    private final int column;

    private final String scopeName;
    private final int scopeLevel;

    private boolean initialized;
    private boolean used;

    private Integer dim1;
    private Integer dim2;

    private final List<String> parameterNames = new ArrayList<>();
    private final List<DataType> parameterTypes = new ArrayList<>();

    public SymbolInfo(
            String lexeme,
            SymbolKind kind,
            DataType type,
            int line,
            int column,
            String scopeName,
            int scopeLevel
    ) {
        this.lexeme = lexeme;
        this.kind = kind;
        this.type = type;
        this.line = line;
        this.column = column;
        this.scopeName = scopeName;
        this.scopeLevel = scopeLevel;
        this.initialized = false;
        this.used = false;
    }

    public String getLexeme() {
        return lexeme;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public DataType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getScopeName() {
        return scopeName;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Integer getDim1() {
        return dim1;
    }

    public Integer getDim2() {
        return dim2;
    }

    public void setDimensions(Integer dim1, Integer dim2) {
        this.dim1 = dim1;
        this.dim2 = dim2;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public List<DataType> getParameterTypes() {
        return parameterTypes;
    }

    public void addParameter(String name, DataType type) {
        parameterNames.add(name);
        parameterTypes.add(type);
    }

    public String getDimensionsAsText() {
        if (dim1 == null || dim2 == null) return "-";
        return "[" + dim1 + "," + dim2 + "]";
    }

    public String getParamsAsText() {
        if (parameterTypes.isEmpty()) return "-";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameterTypes.get(i)).append(" ").append(parameterNames.get(i));
        }
        return sb.toString();
    }
}
