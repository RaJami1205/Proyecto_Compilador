package Semantico;

// Almacena la información del sufijo de una declaración durante el análisis sintáctico.
public class DeclTailInfo {
    private final boolean array;
    private final boolean initialized;
    private final Integer dim1;
    private final Integer dim2;

     /**
     * Constructor privado. Use los métodos de fábrica scalar() o array().
     *
     * @param array       true si la declaración es un arreglo.
     * @param initialized true si la declaración incluye un valor inicial.
     * @param dim1        Primera dimensión del arreglo, null si es escalar.
     * @param dim2        Segunda dimensión del arreglo, null si es escalar.
     */
    private DeclTailInfo(boolean array, boolean initialized, Integer dim1, Integer dim2) {
        this.array = array;
        this.initialized = initialized;
        this.dim1 = dim1;
        this.dim2 = dim2;
    }
    
    /**
     * Crea un DeclTailInfo para una variable escalar.
     *
     * @param initialized true si la variable fue inicializada en su declaración.
     * @return DeclTailInfo configurado como variable escalar.
     */
    public static DeclTailInfo scalar(boolean initialized) {
        return new DeclTailInfo(false, initialized, null, null);
    }

     /**
     * Crea un DeclTailInfo para un arreglo bidimensional.
     *
     * @param dim1        Número de filas del arreglo.
     * @param dim2        Número de columnas del arreglo.
     * @param initialized true si el arreglo fue inicializado con una matriz literal.
     * @return DeclTailInfo configurado como arreglo.
     */
    public static DeclTailInfo array(Integer dim1, Integer dim2, boolean initialized) {
        return new DeclTailInfo(true, initialized, dim1, dim2);
    }

     /**
     * Indica si la declaración corresponde a un arreglo.
     * @return true si es arreglo, false si es escalar.
     */
    public boolean isArray() {
        return array;
    }

     /**
     * Indica si la declaración incluye un valor inicial.
     * @return true si fue inicializada, false en caso contrario.
     */
    public boolean isInitialized() {
        return initialized;
    }   

     /**
     * Retorna la primera dimensión del arreglo.
     * @return Número de filas, o null si es escalar.
     */
    public Integer getDim1() {
        return dim1;
    }

      /**
     * Retorna la segunda dimensión del arreglo.
     * @return Número de columnas, o null si es escalar.
     */
    public Integer getDim2() {
        return dim2;
    }
}