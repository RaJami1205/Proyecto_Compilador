package Intermedio;

public class Quadruple {
    private final String operator;
    private final String arg1;
    private final String arg2;
    private final String result;

    public Quadruple(String operator, String arg1, String arg2, String result) {
        this.operator = operator == null ? "-" : operator;
        this.arg1 = arg1 == null ? "-" : arg1;
        this.arg2 = arg2 == null ? "-" : arg2;
        this.result = result == null ? "-" : result;
    }

    public String getOperator() {
        return operator;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "(" + operator + ", " + arg1 + ", " + arg2 + ", " + result + ")";
    }
}