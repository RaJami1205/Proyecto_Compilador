package Semantico;

import java.util.ArrayList;

public class DeclTailInfo {
    private final boolean array;
    private final boolean initialized;
    private final Integer dim1;
    private final Integer dim2;
    private final DataType exprType;

    private final String exprPlace;
    private final ArrayList matrixInit;

    private DeclTailInfo(boolean array, boolean initialized, Integer dim1, Integer dim2,
                         DataType exprType, String exprPlace, ArrayList matrixInit) {
        this.array = array;
        this.initialized = initialized;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.exprType = exprType;
        this.exprPlace = exprPlace;
        this.matrixInit = matrixInit;
    }

    public static DeclTailInfo scalar(boolean initialized, DataType exprType, String exprPlace) {
        return new DeclTailInfo(false, initialized, null, null, exprType, exprPlace, null);
    }

    public static DeclTailInfo array(Integer dim1, Integer dim2, boolean initialized, ArrayList matrixInit) {
        return new DeclTailInfo(true, initialized, dim1, dim2, null, null, matrixInit);
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

    public DataType getExprType() {
        return exprType;
    }

    public String getExprPlace() {
        return exprPlace;
    }

    public ArrayList getMatrixInit() {
        return matrixInit;
    }
}