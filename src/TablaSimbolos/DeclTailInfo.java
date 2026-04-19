package TablaSimbolos;

public class DeclTailInfo {
    private final boolean array;
    private final boolean initialized;
    private final Integer dim1;
    private final Integer dim2;

    private DeclTailInfo(boolean array, boolean initialized, Integer dim1, Integer dim2) {
        this.array = array;
        this.initialized = initialized;
        this.dim1 = dim1;
        this.dim2 = dim2;
    }

    public static DeclTailInfo scalar(boolean initialized) {
        return new DeclTailInfo(false, initialized, null, null);
    }

    public static DeclTailInfo array(Integer dim1, Integer dim2, boolean initialized) {
        return new DeclTailInfo(true, initialized, dim1, dim2);
    }

    public boolean isArray() {
        return array;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Integer getDim1() {
        return dim1;
    }

    public Integer getDim2() {
        return dim2;
    }
}