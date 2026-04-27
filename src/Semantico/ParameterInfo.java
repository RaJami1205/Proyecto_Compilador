package Semantico;

// Almacena la información de un parámetro durante el análisis sintáctico, antes de agregarlo a la tabla de simbolos
public class ParameterInfo {
    private final String name;
    private final DataType type;
    private final int line;
    private final int column;

    /**
     * Constructor. Inicializa el parámetro con todos sus atributos.
     *
     * @param name   Nombre del parámetro tal como aparece en el código fuente.
     * @param type   Tipo de dato del parámetro.
     * @param line   Línea donde fue declarado en el archivo fuente.
     * @param column Columna donde fue declarado en el archivo fuente.
     */
    public ParameterInfo(String name, DataType type, int line, int column) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.column = column;
    }

    /**
     * Retorna el nombre del parámetro.
     * @return Nombre del parámetro.
     */
    public String getName() {
        return name;
    }

     /**
     * Retorna el tipo de dato del parámetro.
     * @return DataType del parámetro.
     */
    public DataType getType() {
        return type;
    }

     /**
     * Retorna la línea de declaración del parámetro.
     * @return Número de línea en el archivo fuente.
     */
    public int getLine() {
        return line;
    }


    /**
     * Retorna la columna de declaración del parámetro.
     * @return Número de columna en el archivo fuente.
     */
    public int getColumn() {
        return column;
    }
}