package Semantico;

import java.util.LinkedHashMap;
import java.util.Map;


// Representa una tabla de símbolos para un scope específico del programa.
public class ScopeTable {
    private final int id;
    private final String name;
    private final int level;
    private final ScopeTable parent;
    private final Map<String, SymbolInfo> symbols = new LinkedHashMap<>();

     /**
     * Constructor. Inicializa el scope con sus atributos de identificación.
     *
     * @param id     Identificador numérico único del scope.
     * @param name   Nombre del scope 
     * @param level  Nivel de anidamiento 
     * @param parent Referencia al scope padre, null si es el scope global.
     */
    public ScopeTable(int id, String name, int level, ScopeTable parent) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.parent = parent;
    }

    /**
     * Retorna el identificador único del scope.
     * @return ID numérico del scope.
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna el nombre del scope.
     * @return Nombre del scope.
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna el nivel de anidamiento del scope.
     * @return Nivel del scope (0 = global).
     */
    public int getLevel() {
        return level;
    }

    /**
     * Retorna el scope padre de este scope.
     * @return ScopeTable padre, o null si es el scope global.
     */
    public ScopeTable getParent() {
        return parent;
    }

    /**
     * Verifica si un símbolo existe localmente en este scope.
     *
     * @param lexeme Nombre del símbolo a verificar.
     * @return true si el símbolo existe en este scope, false en caso contrario.
     */
    public boolean containsLocal(String lexeme) {
        return symbols.containsKey(lexeme);
    }

    /**
     * Inserta un símbolo en este scope si no existe previamente.
     *
     * @param info Información del símbolo a insertar.
     * @return true si se insertó correctamente, false si ya existía un símbolo con el mismo nombre.
     */
    public boolean insert(SymbolInfo info) {
        if (containsLocal(info.getLexeme())) {
            return false;
        }
        symbols.put(info.getLexeme(), info);
        return true;
    }

    /**
     * Busca un símbolo únicamente dentro de este scope, sin subir al scope padre.
     *
     * @param lexeme Nombre del símbolo a buscar.
     * @return SymbolInfo si se encontró, null si no existe en este scope.
     */
    public SymbolInfo lookupLocal(String lexeme) {
        return symbols.get(lexeme);
    }
      /**
     * Retorna el mapa completo de símbolos de este scope.
     * El mapa mantiene el orden de inserción.
     *
     * @return Map con los símbolos del scope, donde la clave es el lexema.
     */
    public Map<String, SymbolInfo> getSymbols() {
        return symbols;
    }
}
