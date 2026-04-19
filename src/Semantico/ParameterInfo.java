package Semantico;

public class ParameterInfo {
    private final String name;
    private final DataType type;
    private final int line;
    private final int column;

    public ParameterInfo(String name, DataType type, int line, int column) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
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
}