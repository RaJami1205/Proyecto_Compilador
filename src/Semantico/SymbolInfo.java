package Semantico;

import java.util.ArrayList;
import java.util.List;

//Representa la información de un símbolo almacenado en la tabla de símbolos.
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

    /**
     * Constructor principal inicializa el simbolo con sus atributos base
     *
     * @param lexeme     Nombre del símbolo 
     * @param kind       Categoría del símbolo 
     * @param type       Tipo de dato o tipo de retorno en caso de funciones
     * @param line       Linea donde fue declarado en el archivo fuente
     * @param column     Columna donde fue declarado en el archivo fuente
     * @param scopeName  Nombre del scope donde fue declarado
     * @param scopeLevel Nivel de anidamiento del scope donde fue declarado
     */
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

    /**
     * Retorna el lexema del símbolo.
     * @return Nombre del símbolo.
     */
    public String getLexeme() {
        return lexeme;
    }

     /**
     * Retorna la categoría del símbolo.
     * @return SymbolKind del símbolo.
     */
    public SymbolKind getKind() {
        return kind;
    }

     /**
     * Retorna el tipo de dato del símbolo. En funciones representa el tipo de retorno.
     * @return DataType del símbolo.
     */
    public DataType getType() {
        return type;
    }

    /**
     * Retorna la línea de declaración del símbolo.
     * @return Número de línea en el archivo fuente.
     */
    public int getLine() {
        return line;
    }

    /**
     * Retorna la columna de declaración del simbolo.
     * @return Número de columna en el archivo fuente.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Retorna el nombre del scope donde fue declarado el simbolo
     * @return Nombre del scope.
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * Retorna el nivel de anidamiento del scope del simbolo
     * @return Nivel del scope 
     */
    public int getScopeLevel() {
        return scopeLevel;
    }

     /**
     * Establece el estado de inicialización del símbolo.
     * @param initialized true si el símbolo fue inicializado.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Indica si el símbolo fue referenciado al menos una vez después de su declaración.
     * @return true si fue usado, false en caso contrario.
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Establece si el símbolo fue usado en el código fuente.
     * @param used true si el símbolo fue referenciado.
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Retorna la primera dimensión del arreglo.
     * @return Número de filas, o null si el símbolo no es un arreglo.
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Retorna la primera dimensión del arreglo.
     * @return Número de filas, o null si el símbolo no es un arreglo.
     */
    public Integer getDim1() {
        return dim1;
    }

    /**
     * Retorna la segunda dimensión del arreglo.
     * @return Número de columnas, o null si el símbolo no es un arreglo.
     */
    public Integer getDim2() {
        return dim2;
    }

    /**
     * Establece las dimensiones del arreglo.
     * Solo aplica para símbolos de tipo ARRAY.
     *
     * @param dim1 Número de filas.
     * @param dim2 Número de columnas.
     */
    public void setDimensions(Integer dim1, Integer dim2) {
        this.dim1 = dim1;
        this.dim2 = dim2;
    }

    /**
     * Retorna la lista de nombres de los parámetros de la función.
     * @return Lista de nombres de parámetros, vacía si no es función.
     */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    /**
     * Retorna la lista de tipos de los parámetros de la función.
     * @return Lista de DataType de parámetros, vacía si no es función.
     */
    public List<DataType> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Agrega un parámetro a la firma de la función.
     *
     * @param name Nombre del parámetro.
     * @param type Tipo de dato del parámetro.
     */
    public void addParameter(String name, DataType type) {
        parameterNames.add(name);
        parameterTypes.add(type);
    }

     /**
     * Genera una representación textual de las dimensiones del arreglo.
     * @return String con formato 
     */
    public String getDimensionsAsText() {
        if (dim1 == null || dim2 == null) return "-";
        return "[" + dim1 + "," + dim2 + "]";
    }

     /**
     * Genera una representación textual de los parámetros de la función.
     * @return String con formato 
     */
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
