package Intermedio;

/**
 * Representa una instrucción del código intermedio en forma de cuádruplo.
 * 
 * Cada cuádruplo sigue la estructura:
 * (operador, argumento1, argumento2, resultado)
 */
public class Quadruple {
    /** Operación principal de la instrucción */
    private final String operator;

    /** Primer operando de la instrucción */
    private final String arg1;

    /** Segundo operando de la instrucción */
    private final String arg2;

    /** Resultado o destino de la instrucción */
    private final String result;

    /**
     * Construye un cuádruplo y reemplaza valores nulos por "-".
     * Esto evita problemas al imprimir instrucciones incompletas.
     */
    public Quadruple(String operator, String arg1, String arg2, String result) {
        this.operator = operator == null ? "-" : operator;
        this.arg1 = arg1 == null ? "-" : arg1;
        this.arg2 = arg2 == null ? "-" : arg2;
        this.result = result == null ? "-" : result;
    }

    /**
     * Retorna el operador de la instrucción.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Retorna el primer argumento.
     */
    public String getArg1() {
        return arg1;
    }

    /**
     * Retorna el segundo argumento.
     */
    public String getArg2() {
        return arg2;
    }

    /**
     * Retorna el resultado o destino.
     */
    public String getResult() {
        return result;
    }

    /**
     * Devuelve el cuádruplo en formato legible:
     * (op, arg1, arg2, result)
     */
    @Override
    public String toString() {
        return "(" + operator + ", " + arg1 + ", " + arg2 + ", " + result + ")";
    }
}