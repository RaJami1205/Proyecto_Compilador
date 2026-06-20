package MIPS;

import Intermedio.IntermediateCode;
import Intermedio.Quadruple;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduce el código intermedio en formato de cuádruplos a código ensamblador MIPS.
 *
 * Esta clase genera un archivo .asm ejecutable en QtSpim usando:
 * - sección .data para strings y constantes flotantes,
 * - sección .text para funciones e instrucciones,
 * - stack frames por función,
 * - temporales y variables locales en memoria,
 * - etiquetas y saltos equivalentes al IR.
 */
public class MIPSCodeGenerator {

    private final IntermediateCode intermediateCode;
    private final List<Quadruple> quads;

    private final Map<String, FunctionBlock> functions = new LinkedHashMap<>();
    private final Map<String, String> stringLabels = new LinkedHashMap<>();
    private final Map<String, String> floatLabels = new LinkedHashMap<>();

    private final StringBuilder data = new StringBuilder();
    private final StringBuilder text = new StringBuilder();

    private int stringCounter = 0;
    private int floatCounter = 0;
    private int internalLabelCounter = 0;

    /**
     * Construye el generador MIPS a partir del código intermedio ya producido.
     */
    public MIPSCodeGenerator(IntermediateCode intermediateCode) {
        this.intermediateCode = intermediateCode;
        this.quads = intermediateCode.getCode();
    }

    /**
     * Genera el archivo .asm final.
     *
     * @param outputPath Ruta donde se guardará el código MIPS.
     */
    public void generate(String outputPath) throws IOException {
        splitFunctions();
        analyzeFunctions();
        collectDataLiterals();
        emitProgram();

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.print(data);
            writer.print(text);
        }
    }

    /**
     * Divide los cuádruplos en bloques de función usando FUNC_BEGIN y FUNC_END.
     */
    private void splitFunctions() {
        FunctionBlock current = null;

        for (Quadruple q : quads) {
            String op = q.getOperator();

            if ("FUNC_BEGIN".equals(op)) {
                current = new FunctionBlock(q.getArg1(), q.getArg2());
                functions.put(current.name, current);
                current.quads.add(q);
            } else if (current != null) {
                current.quads.add(q);

                if ("FUNC_END".equals(op)) {
                    current = null;
                }
            }
        }
    }

    /**
     * Analiza cada función para determinar variables, temporales, arreglos,
     * parámetros y tamaño del stack frame.
     */
    private void analyzeFunctions() {
        for (FunctionBlock f : functions.values()) {
            inferSymbols(f);
            assignFrameOffsets(f);
        }
    }

    /**
     * Recolecta literales de string y float para declararlos en .data.
     */
    private void collectDataLiterals() {
        for (Quadruple q : quads) {
            registerLiteralIfNeeded(q.getArg1());
            registerLiteralIfNeeded(q.getArg2());
            registerLiteralIfNeeded(q.getResult());
        }

        registerStringLiteral("\"true\"");
        registerStringLiteral("\"false\"");
    }

    /**
     * Emite la estructura completa del programa MIPS.
     */
    private void emitProgram() {
        data.append(".data\n");

        for (Map.Entry<String, String> e : stringLabels.entrySet()) {
            data.append(e.getValue()).append(": .asciiz ").append(e.getKey()).append("\n");
        }

        for (Map.Entry<String, String> e : floatLabels.entrySet()) {
            data.append(e.getValue()).append(": .float ").append(normalizeFloatLiteral(e.getKey())).append("\n");
        }

        text.append("\n.text\n");
        text.append(".globl main\n\n");

        for (FunctionBlock f : functions.values()) {
            emitFunction(f);
        }
    }

    /**
     * Emite una función completa, con prólogo, cuerpo y epílogo.
     */
    private void emitFunction(FunctionBlock f) {
        String asmName = f.isMain() ? "main" : f.name;

        text.append(asmName).append(":\n");

        emitPrologue(f);
        emitParameterStores(f);

        List<String> pendingParams = new ArrayList<>();

        for (int i = 0; i < f.quads.size(); i++) {
            Quadruple q = f.quads.get(i);
            String op = q.getOperator();

            switch (op) {
                case "FUNC_BEGIN":
                case "FUNC_END":
                case "LOCAL_DEF":
                case "PARAM_DEF":
                case "ARRAY_DEF":
                    break;

                case "ASSIGN":
                    emitAssign(f, q.getArg1(), q.getResult());
                    break;

                case "+":
                case "-":
                case "*":
                case "/":
                case "%":
                case "^":
                    emitArithmetic(f, op, q.getArg1(), q.getArg2(), q.getResult());
                    break;

                case "NEG":
                case "INC":
                case "DEC":
                case "NOT":
                    emitUnary(f, op, q.getArg1(), q.getResult());
                    break;

                case "EQ":
                case "NE":
                case "LT":
                case "LE":
                case "GT":
                case "GE":
                    emitComparison(f, op, q.getArg1(), q.getArg2(), q.getResult());
                    break;

                case "AND":
                case "OR":
                    emitBooleanBinary(f, op, q.getArg1(), q.getArg2(), q.getResult());
                    break;

                case "LABEL":
                    text.append(q.getResult()).append(":\n");
                    break;

                case "GOTO":
                    text.append("    j    ").append(q.getResult()).append("\n");
                    break;

                case "IF_FALSE":
                    emitIfFalse(f, q.getArg1(), q.getResult());
                    break;

                case "IF_TRUE":
                    emitIfTrue(f, q.getArg1(), q.getResult());
                    break;

                case "WRITE":
                    emitWrite(f, q.getArg1());

                    if (hayOtroWriteSeguido(f.quads, i)) {
                        emitSpace();
                    }

                    break;
                
                case "NEWLINE":
                    emitNewline();
                    break;

                case "READ":
                    emitRead(f, q.getResult());
                    break;

                case "PARAM":
                    pendingParams.add(q.getArg1());
                    break;

                case "CALL":
                    emitCall(f, q.getArg1(), q.getResult(), pendingParams);
                    pendingParams.clear();
                    break;

                case "RETURN":
                    emitReturn(f, q.getArg1());
                    break;

                case "ARRAY_LOAD":
                    emitArrayLoad(f, q.getArg1(), q.getResult());
                    break;

                case "ARRAY_STORE":
                    emitArrayStore(f, q.getArg1(), q.getResult());
                    break;

                default:
                    text.append("    # Operador no implementado: ").append(q).append("\n");
                    break;
            }
        }

        emitEpilogue(f);
        text.append("\n");
    }

    /**
     * Emite el prólogo de función y reserva el stack frame.
     */
    private void emitPrologue(FunctionBlock f) {
        text.append("    addiu $sp, $sp, -").append(f.frameSize).append("\n");
        text.append("    sw    $ra, ").append(f.frameSize - 4).append("($sp)\n");
        text.append("    sw    $fp, ").append(f.frameSize - 8).append("($sp)\n");
        text.append("    move  $fp, $sp\n");
    }

    /**
     * Copia los parámetros recibidos en $a0-$a3 hacia sus espacios locales.
     */
    private void emitParameterStores(FunctionBlock f) {
        for (int i = 0; i < f.parameters.size() && i < 4; i++) {
            String param = f.parameters.get(i);
            VarInfo v = f.vars.get(param);

            if (v == null) continue;

            if (v.isFloat()) {
                text.append("    mtc1  $a").append(i).append(", $f0\n");
                text.append("    s.s   $f0, ").append(v.offset).append("($fp)\n");
            } else {
                text.append("    sw    $a").append(i).append(", ").append(v.offset).append("($fp)\n");
            }
        }
    }

    /**
     * Emite el epílogo de función o salida del programa principal.
     */
    private void emitEpilogue(FunctionBlock f) {
        String endLabel = functionEndLabel(f);

        text.append(endLabel).append(":\n");

        if (f.isMain()) {
            text.append("    li    $v0, 10\n");
            text.append("    syscall\n");
        } else {
            text.append("    lw    $ra, ").append(f.frameSize - 4).append("($fp)\n");
            text.append("    lw    $fp, ").append(f.frameSize - 8).append("($fp)\n");
            text.append("    addiu $sp, $sp, ").append(f.frameSize).append("\n");
            text.append("    jr    $ra\n");
        }
    }

    /**
     * Genera una asignación simple.
     */
    private void emitAssign(FunctionBlock f, String source, String target) {
        if (isArrayAccess(target)) {
            emitArrayStore(f, source, target);
            return;
        }

        VarInfo targetInfo = f.vars.get(target);

        if (targetInfo != null && targetInfo.isFloat()) {
            loadFloatOperand(f, source, "$f0");
            text.append("    s.s   $f0, ").append(targetInfo.offset).append("($fp)\n");
        } else {
            loadIntOperand(f, source, "$t0");
            storeIntOperand(f, target, "$t0");
        }
    }

    /**
     * Emite operaciones aritméticas.
     */
    private void emitArithmetic(FunctionBlock f, String op, String a, String b, String result) {
        VarInfo resultInfo = f.vars.get(result);

        if (resultInfo != null && resultInfo.isFloat()) {
            loadFloatOperand(f, a, "$f0");
            loadFloatOperand(f, b, "$f2");

            switch (op) {
                case "+":
                    text.append("    add.s $f4, $f0, $f2\n");
                    break;
                case "-":
                    text.append("    sub.s $f4, $f0, $f2\n");
                    break;
                case "*":
                    text.append("    mul.s $f4, $f0, $f2\n");
                    break;
                case "/":
                    text.append("    div.s $f4, $f0, $f2\n");
                    break;
                default:
                    text.append("    mov.s $f4, $f0\n");
                    break;
            }

            text.append("    s.s   $f4, ").append(resultInfo.offset).append("($fp)\n");
            return;
        }

        loadIntOperand(f, a, "$t0");
        loadIntOperand(f, b, "$t1");

        switch (op) {
            case "+":
                text.append("    add   $t2, $t0, $t1\n");
                break;
            case "-":
                text.append("    sub   $t2, $t0, $t1\n");
                break;
            case "*":
                text.append("    mul   $t2, $t0, $t1\n");
                break;
            case "/":
                text.append("    div   $t0, $t1\n");
                text.append("    mflo  $t2\n");
                break;
            case "%":
                text.append("    div   $t0, $t1\n");
                text.append("    mfhi  $t2\n");
                break;
            case "^": {
                String loopLabel = internalLabel("pow_loop");
                String endLabel = internalLabel("pow_end");

                text.append("    li    $t2, 1\n");
                text.append(loopLabel).append(":\n");
                text.append("    blez  $t1, ").append(endLabel).append("\n");
                text.append("    mul   $t2, $t2, $t0\n");
                text.append("    addi  $t1, $t1, -1\n");
                text.append("    j     ").append(loopLabel).append("\n");
                text.append(endLabel).append(":\n");
                break;
            }
            default:
                text.append("    move  $t2, $zero\n");
                break;
        }

        storeIntOperand(f, result, "$t2");
    }

   /**
     * Emite operaciones unarias como NEG, INC, DEC y NOT.
     */
    private void emitUnary(FunctionBlock f, String op, String a, String result) {
        VarInfo resultInfo = f.vars.get(result);

        if (resultInfo != null && resultInfo.isFloat()) {
            loadFloatOperand(f, a, "$f0");

            switch (op) {
                case "NEG":
                    text.append("    neg.s $f4, $f0\n");
                    break;

                case "INC":
                    text.append("    li    $t9, 1\n");
                    text.append("    mtc1  $t9, $f2\n");
                    text.append("    cvt.s.w $f2, $f2\n");
                    text.append("    add.s $f4, $f0, $f2\n");
                    break;

                case "DEC":
                    text.append("    li    $t9, 1\n");
                    text.append("    mtc1  $t9, $f2\n");
                    text.append("    cvt.s.w $f2, $f2\n");
                    text.append("    sub.s $f4, $f0, $f2\n");
                    break;

                default:
                    text.append("    mov.s $f4, $f0\n");
                    break;
            }

            text.append("    s.s   $f4, ").append(resultInfo.offset).append("($fp)\n");
            return;
        }

        loadIntOperand(f, a, "$t0");

        switch (op) {
            case "NEG":
                text.append("    sub   $t1, $zero, $t0\n");
                break;

            case "INC":
                text.append("    addi  $t1, $t0, 1\n");
                break;

            case "DEC":
                text.append("    addi  $t1, $t0, -1\n");
                break;

            case "NOT":
                text.append("    seq   $t1, $t0, $zero\n");
                break;

            default:
                text.append("    move  $t1, $t0\n");
                break;
        }

        storeIntOperand(f, result, "$t1");
    }

    /**
     * Emite comparaciones relacionales y de igualdad.
     */
    private void emitComparison(FunctionBlock f, String op, String a, String b, String result) {
        String trueLabel = internalLabel("cmp_true");
        String endLabel = internalLabel("cmp_end");

        loadIntOperand(f, a, "$t0");
        loadIntOperand(f, b, "$t1");

        switch (op) {
            case "EQ":
                text.append("    beq   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            case "NE":
                text.append("    bne   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            case "LT":
                text.append("    blt   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            case "LE":
                text.append("    ble   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            case "GT":
                text.append("    bgt   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            case "GE":
                text.append("    bge   $t0, $t1, ").append(trueLabel).append("\n");
                break;
            default:
                break;
        }

        text.append("    li    $t2, 0\n");
        text.append("    j     ").append(endLabel).append("\n");
        text.append(trueLabel).append(":\n");
        text.append("    li    $t2, 1\n");
        text.append(endLabel).append(":\n");

        storeIntOperand(f, result, "$t2");
    }

    /**
     * Emite AND y OR booleanos.
     */
    private void emitBooleanBinary(FunctionBlock f, String op, String a, String b, String result) {
        loadIntOperand(f, a, "$t0");
        loadIntOperand(f, b, "$t1");

        if ("AND".equals(op)) {
            text.append("    and   $t2, $t0, $t1\n");
        } else {
            text.append("    or    $t2, $t0, $t1\n");
        }

        storeIntOperand(f, result, "$t2");
    }

    /**
     * Emite salto condicional cuando la condición es falsa.
     */
    private void emitIfFalse(FunctionBlock f, String condition, String label) {
        loadIntOperand(f, condition, "$t0");
        text.append("    beq   $t0, $zero, ").append(label).append("\n");
    }

    /**
     * Emite salto condicional cuando la condición es verdadera.
     */
    private void emitIfTrue(FunctionBlock f, String condition, String label) {
        loadIntOperand(f, condition, "$t0");
        text.append("    bne   $t0, $zero, ").append(label).append("\n");
    }

    /**
     * Emite impresión por consola.
     */
    private void emitWrite(FunctionBlock f, String value) {
        if (isStringLiteral(value)) {
            String label = stringLabels.get(value);
            text.append("    li    $v0, 4\n");
            text.append("    la    $a0, ").append(label).append("\n");
            text.append("    syscall\n");
            return;
        }

        if ("true".equals(value)) {
            emitBooleanLiteral(true);
            return;
        }

        if ("false".equals(value)) {
            emitBooleanLiteral(false);
            return;
        }

        if (isBooleanVariable(f, value)) {
            emitBooleanVariable(f, value);
            return;
        }

        if (isCharLiteral(value)) {
            text.append("    li    $a0, ").append((int) value.charAt(1)).append("\n");
            text.append("    li    $v0, 11\n");
            text.append("    syscall\n");
            return;
        }

        if (isFloatLike(value) || isFloatVariable(f, value)) {
            loadFloatOperand(f, value, "$f12");
            text.append("    li    $v0, 2\n");
            text.append("    syscall\n");
            return;
        }

        loadIntOperand(f, value, "$a0");
        text.append("    li    $v0, 1\n");
        text.append("    syscall\n");
    }

    /**
     * Imprime un espacio en consola.
     */
    private void emitSpace() {
        text.append("    li    $a0, 32\n");
        text.append("    li    $v0, 11\n");
        text.append("    syscall\n");
    }

    /**
     * Emite un salto de línea.
     */
    private void emitNewline() {
        text.append("    li    $a0, 10\n");
        text.append("    li    $v0, 11\n");
        text.append("    syscall\n");
    }

    /**
     * Indica si el siguiente cuádruplo también es WRITE.
     * Sirve para imprimir un espacio entre valores de un mismo cout.
     */
    private boolean hayOtroWriteSeguido(List<Quadruple> code, int index) {
        if (index + 1 >= code.size()) {
            return false;
        }

        return "WRITE".equals(code.get(index + 1).getOperator());
    }

    /**
     * Imprime un booleano literal como texto.
     */
    private void emitBooleanLiteral(boolean value) {
        String label = registerStringLiteral(value ? "\"true\"" : "\"false\"");

        text.append("    li    $v0, 4\n");
        text.append("    la    $a0, ").append(label).append("\n");
        text.append("    syscall\n");
    }

    /**
     * Imprime una variable booleana como true o false.
     */
    private void emitBooleanVariable(FunctionBlock f, String value) {
        String trueLabel = internalLabel("print_bool_true");
        String endLabel = internalLabel("print_bool_end");

        String trueString = registerStringLiteral("\"true\"");
        String falseString = registerStringLiteral("\"false\"");

        loadIntOperand(f, value, "$t0");

        text.append("    bne   $t0, $zero, ").append(trueLabel).append("\n");
        text.append("    li    $v0, 4\n");
        text.append("    la    $a0, ").append(falseString).append("\n");
        text.append("    syscall\n");
        text.append("    j     ").append(endLabel).append("\n");

        text.append(trueLabel).append(":\n");
        text.append("    li    $v0, 4\n");
        text.append("    la    $a0, ").append(trueString).append("\n");
        text.append("    syscall\n");

        text.append(endLabel).append(":\n");
    }

     /**
     * Verifica si un valor corresponde a una variable booleana.
     */
    private boolean isBooleanVariable(FunctionBlock f, String value) {
        VarInfo v = f.vars.get(value);
        return v != null && isBooleanType(v.type);
    }

    /**
     * Emite lectura por consola.
     */
    private void emitRead(FunctionBlock f, String target) {
        VarInfo v = f.vars.get(target);

        if (v != null && v.isFloat()) {
            text.append("    li    $v0, 6\n");
            text.append("    syscall\n");
            text.append("    s.s   $f0, ").append(v.offset).append("($fp)\n");
        } else {
            text.append("    li    $v0, 5\n");
            text.append("    syscall\n");
            storeIntOperand(f, target, "$v0");
        }
    }

    /**
     * Emite llamada a función.
     */
    private void emitCall(FunctionBlock f, String functionName, String result, List<String> params) {
        FunctionBlock callee = functions.get(functionName);

        for (int i = 0; i < params.size() && i < 4; i++) {
            String paramValue = params.get(i);
            String expectedType = getParameterType(callee, i);

            if (isFloatType(expectedType)) {
                loadFloatOperand(f, paramValue, "$f0");
                text.append("    mfc1  $a").append(i).append(", $f0\n");
            } else {
                loadIntOperand(f, paramValue, "$a" + i);
            }
        }

        text.append("    jal   ").append(functionName).append("\n");

        if (!"-".equals(result)) {
            VarInfo resultInfo = f.vars.get(result);

            if (resultInfo != null && resultInfo.isFloat()) {
                text.append("    s.s   $f0, ").append(resultInfo.offset).append("($fp)\n");
            } else {
                storeIntOperand(f, result, "$v0");
            }
        }
    }

    /**
     * Emite retorno de función.
     */
    private void emitReturn(FunctionBlock f, String value) {
        if (f.isFloatReturn()) {
            loadFloatOperand(f, value, "$f0");
        } else {
            loadIntOperand(f, value, "$v0");
        }

        text.append("    j     ").append(functionEndLabel(f)).append("\n");
    }

    /**
     * Obtiene el tipo de un parámetro de una función.
     */
    private String getParameterType(FunctionBlock function, int index) {
        if (function == null) {
            return "UNKNOWN";
        }

        if (index < 0 || index >= function.parameters.size()) {
            return "UNKNOWN";
        }

        String paramName = function.parameters.get(index);
        VarInfo paramInfo = function.vars.get(paramName);

        return paramInfo != null ? paramInfo.type : "UNKNOWN";
    }

    /**
     * Indica si un tipo es numérico flotante
     */
    private boolean isFloatType(String type) {
        return "FLOAT".equals(type) ||
            "SCIENTIFIC".equals(type) ||
            "FRAC".equals(type);
    }

    /**
     * Indica si un tipo es booleano
     */
    private boolean isBooleanType(String type) {
        return "BOOL".equals(type);
    }

    /**
     * Carga una celda de arreglo en una variable temporal.
     */
    private void emitArrayLoad(FunctionBlock f, String access, String result) {
        ArrayAccess parsed = parseArrayAccess(access);
        if (parsed == null) return;

        VarInfo arr = f.vars.get(parsed.name);
        VarInfo resultInfo = f.vars.get(result);

        emitArrayAddress(f, arr, parsed.row, parsed.col, "$t9");

        if (resultInfo != null && resultInfo.isFloat()) {
            text.append("    l.s   $f0, 0($t9)\n");
            text.append("    s.s   $f0, ").append(resultInfo.offset).append("($fp)\n");
        } else {
            text.append("    lw    $t0, 0($t9)\n");
            storeIntOperand(f, result, "$t0");
        }
    }

    /**
     * Guarda un valor en una celda de arreglo.
     */
    private void emitArrayStore(FunctionBlock f, String source, String access) {
        ArrayAccess parsed = parseArrayAccess(access);
        if (parsed == null) return;

        VarInfo arr = f.vars.get(parsed.name);
        emitArrayAddress(f, arr, parsed.row, parsed.col, "$t9");

        if (arr != null && arr.isFloat()) {
            loadFloatOperand(f, source, "$f0");
            text.append("    s.s   $f0, 0($t9)\n");
        } else {
            loadIntOperand(f, source, "$t0");
            text.append("    sw    $t0, 0($t9)\n");
        }
    }

    /**
     * Calcula la dirección efectiva de una celda de arreglo 2D.
     */
    private void emitArrayAddress(FunctionBlock f, VarInfo arr, String row, String col, String targetReg) {
        if (arr == null) {
            text.append("    move  ").append(targetReg).append(", $zero\n");
            return;
        }

        loadIntOperand(f, row, "$t0");
        loadIntOperand(f, col, "$t1");

        text.append("    li    $t2, ").append(arr.cols).append("\n");
        text.append("    mul   $t0, $t0, $t2\n");
        text.append("    add   $t0, $t0, $t1\n");
        text.append("    sll   $t0, $t0, 2\n");
        text.append("    addiu ").append(targetReg).append(", $fp, ").append(arr.offset).append("\n");
        text.append("    addu  ").append(targetReg).append(", ").append(targetReg).append(", $t0\n");
    }

    /**
     * Carga un operando entero, booleano o char en un registro.
     */
    private void loadIntOperand(FunctionBlock f, String value, String reg) {
        if (value == null || "-".equals(value)) {
            text.append("    move  ").append(reg).append(", $zero\n");
            return;
        }

        if ("true".equals(value)) {
            text.append("    li    ").append(reg).append(", 1\n");
            return;
        }

        if ("false".equals(value)) {
            text.append("    li    ").append(reg).append(", 0\n");
            return;
        }

        if (isCharLiteral(value)) {
            text.append("    li    ").append(reg).append(", ").append((int) value.charAt(1)).append("\n");
            return;
        }

        if (isIntegerLiteral(value)) {
            text.append("    li    ").append(reg).append(", ").append(value).append("\n");
            return;
        }

        if (isFractionLiteral(value)) {
            int intValue = fractionToInt(value);
            text.append("    li    ").append(reg).append(", ").append(intValue).append("\n");
            return;
        }

        if (isArrayAccess(value)) {
            emitArrayLoadToRegister(f, value, reg);
            return;
        }

        VarInfo v = f.vars.get(value);
        if (v != null) {
            text.append("    lw    ").append(reg).append(", ").append(v.offset).append("($fp)\n");
        } else {
            text.append("    move  ").append(reg).append(", $zero\n");
        }
    }

    /**
     * Guarda un registro entero en una variable.
     */
    private void storeIntOperand(FunctionBlock f, String target, String reg) {
        VarInfo v = f.vars.get(target);

        if (v != null) {
            text.append("    sw    ").append(reg).append(", ").append(v.offset).append("($fp)\n");
        } 
    }

    /**
     * Carga un operando flotante en un registro de punto flotante.
     */
    private void loadFloatOperand(FunctionBlock f, String value, String freg) {
        if (isFloatLike(value)) {
            String label = floatLabels.get(value);
            text.append("    l.s   ").append(freg).append(", ").append(label).append("\n");
            return;
        }

        VarInfo v = f.vars.get(value);
        if (v != null && v.isFloat()) {
            text.append("    l.s   ").append(freg).append(", ").append(v.offset).append("($fp)\n");
            return;
        }

        if (isIntegerLiteral(value)) {
            text.append("    li    $t9, ").append(value).append("\n");
            text.append("    mtc1  $t9, ").append(freg).append("\n");
            text.append("    cvt.s.w ").append(freg).append(", ").append(freg).append("\n");
            return;
        }

        text.append("    l.s   ").append(freg).append(", ").append(registerFloatLiteral("0.0")).append("\n");
    }

    /**
     * Carga un acceso de arreglo entero directamente a un registro.
     */
    private void emitArrayLoadToRegister(FunctionBlock f, String access, String reg) {
        ArrayAccess parsed = parseArrayAccess(access);
        if (parsed == null) return;

        VarInfo arr = f.vars.get(parsed.name);
        emitArrayAddress(f, arr, parsed.row, parsed.col, "$t9");
        text.append("    lw    ").append(reg).append(", 0($t9)\n");
    }

    /**
     * Registra variables, parámetros, arreglos y temporales de una función.
     */
    private void inferSymbols(FunctionBlock f) {
        Map<String, String> functionReturnTypes = getFunctionReturnTypes();

        for (Quadruple q : f.quads) {
            switch (q.getOperator()) {
                case "PARAM_DEF":
                    f.parameters.add(q.getArg1());
                    f.addVar(q.getArg1(), q.getArg2(), false, 1, 1);
                    break;

                case "LOCAL_DEF":
                    f.addVar(q.getArg1(), q.getArg2(), false, 1, 1);
                    break;

                case "ARRAY_DEF":
                    String[] dims = q.getResult().split(",");
                    int rows = Integer.parseInt(dims[0].trim());
                    int cols = Integer.parseInt(dims[1].trim());
                    f.addVar(q.getArg1(), q.getArg2(), true, rows, cols);
                    break;

                case "ASSIGN":
                    String target = q.getResult();

                    if (target != null &&
                        !"-".equals(target) &&
                        !isArrayAccess(target) &&
                        !f.vars.containsKey(target)) {

                        String inferredType = typeOf(f, q.getArg1());

                        if ("UNKNOWN".equals(inferredType) && target.startsWith("switch_match")) {
                            inferredType = "BOOL";
                        }

                        f.addVar(target, inferredType, false, 1, 1);
                    }
                    break;

                case "+":
                case "-":
                case "*":
                case "/":
                case "%":
                case "^":
                    f.addVar(q.getResult(), inferNumericType(f, q.getArg1(), q.getArg2()), false, 1, 1);
                    break;

                case "NEG":
                case "INC":
                case "DEC":
                    f.addVar(q.getResult(), typeOf(f, q.getArg1()), false, 1, 1);
                    break;

                case "NOT":
                case "EQ":
                case "NE":
                case "LT":
                case "LE":
                case "GT":
                case "GE":
                case "AND":
                case "OR":
                    f.addVar(q.getResult(), "BOOL", false, 1, 1);
                    break;

                case "CALL":
                    if (!"-".equals(q.getResult())) {
                        String returnType = functionReturnTypes.getOrDefault(q.getArg1(), "INT");
                        f.addVar(q.getResult(), returnType, false, 1, 1);
                    }
                    break;

                case "ARRAY_LOAD":
                    ArrayAccess access = parseArrayAccess(q.getArg1());
                    if (access != null) {
                        VarInfo arr = f.vars.get(access.name);
                        String t = arr != null ? arr.type : "INT";
                        f.addVar(q.getResult(), t, false, 1, 1);
                    }
                    break;
            }
        }
    }

    /**
     * Asigna offsets positivos desde $fp para cada símbolo del frame.
     */
    private void assignFrameOffsets(FunctionBlock f) {
        int offset = 0;

        for (VarInfo v : f.vars.values()) {
            v.offset = offset;
            offset += v.sizeBytes();
        }

        f.frameSize = align(offset + 8, 8);
    }

    /**
     * Obtiene los tipos de retorno de todas las funciones.
     */
    private Map<String, String> getFunctionReturnTypes() {
        Map<String, String> map = new HashMap<>();
        for (FunctionBlock f : functions.values()) {
            map.put(f.name, f.returnType);
        }
        return map;
    }

    /**
     * Determina el tipo numérico resultante entre dos operandos.
     */
    private String inferNumericType(FunctionBlock f, String a, String b) {
        String ta = typeOf(f, a);
        String tb = typeOf(f, b);

        if ("FLOAT".equals(ta) || "FLOAT".equals(tb)) return "FLOAT";
        if ("SCIENTIFIC".equals(ta) || "SCIENTIFIC".equals(tb)) return "FLOAT";
        if ("FRAC".equals(ta) || "FRAC".equals(tb)) return "FLOAT";

        return "INT";
    }

    /**
     * Obtiene el tipo aproximado de un operando.
     */
    private String typeOf(FunctionBlock f, String value) {
        if (value == null || "-".equals(value)) return "UNKNOWN";
        if ("true".equals(value) || "false".equals(value)) return "BOOL";
        if (isStringLiteral(value)) return "STRING";
        if (isCharLiteral(value)) return "CHAR";
        if (isFloatLike(value)) return "FLOAT";
        if (isFractionLiteral(value)) return "FRAC";
        if (isIntegerLiteral(value)) return "INT";

        VarInfo v = f.vars.get(value);
        return v != null ? v.type : "UNKNOWN";
    }

    /**
     * Registra literales en la sección .data si corresponde.
     */
    private void registerLiteralIfNeeded(String value) {
        if (isStringLiteral(value)) {
            stringLabels.computeIfAbsent(value, v -> "__str_" + (++stringCounter));
        } else if (isFloatLike(value)) {
            registerFloatLiteral(value);
        }
    }

    /**
     * Registra un literal string en .data y retorna su etiqueta.
     */
    private String registerStringLiteral(String value) {
        return stringLabels.computeIfAbsent(value, v -> "__str_" + (++stringCounter));
    }

    /**
     * Registra un literal flotante y devuelve su etiqueta.
     */
    private String registerFloatLiteral(String value) {
        return floatLabels.computeIfAbsent(value, v -> "__float_" + (++floatCounter));
    }

     /**
     * Indica si una cadena representa un literal entero (opcionalmente negativo).
     */
    private boolean isIntegerLiteral(String s) {
        return s != null && s.matches("-?\\d+");
    }

     /**
     * Indica si una cadena representa un literal flotante.
     */
   private boolean isFloatLike(String s) {
        return s != null &&
            (
                s.matches("-?\\d+\\.\\d+") ||
                s.matches("-?\\d+(\\.\\d+)?[eE][+-]?\\d+")
            );
    }

     /**
     * Indica si una cadena representa un literal fraccionario.
     */
    private boolean isFractionLiteral(String s) {
        return s != null && s.matches("-?\\d+//\\d+");
    }

     /**
     * Indica si una cadena representa un literal de cadena.
     */
    private boolean isStringLiteral(String s) {
        return s != null && s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"");
    }

     /**
     * Indica si una cadena representa un literal de carácter.
     */
    private boolean isCharLiteral(String s) {
        return s != null && s.length() >= 3 && s.startsWith("'") && s.endsWith("'");
    }

     /**
     * Indica si una cadena representa un acceso a un arreglo.
     */
    private boolean isArrayAccess(String s) {
        return s != null && s.contains("[") && s.endsWith("]");
    }

     /**
     * Indica si una cadena representa una variable flotante.
     */
    private boolean isFloatVariable(FunctionBlock f, String value) {
        VarInfo v = f.vars.get(value);
        return v != null && v.isFloat();
    }

     /**
     * Convierte un literal fraccionario a entero.
     */
    private int fractionToInt(String value) {
        String[] parts = value.split("//");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        return a / b;
    }

     /**
     * Normaliza un literal flotante.
     */
    private String normalizeFloatLiteral(String value) {
        if (value == null) {
            return "0.0";
        }

        if (isFractionLiteral(value)) {
            String[] parts = value.split("//");
            double a = Double.parseDouble(parts[0]);
            double b = Double.parseDouble(parts[1]);
            return String.valueOf(a / b);
        }

        if (value.matches("-?\\d+")) {
            return value + ".0";
        }

        if (value.matches("-?\\d+(\\.\\d+)?[eE][+-]?\\d+")) {
            return String.valueOf(Double.parseDouble(value));
        }

        if (value.matches("-?\\d+\\.\\d+")) {
            return value;
        }

        return "0.0";
    }

     /**
     * Alinea un valor en un múltiplo de 8.
     */
    private int align(int value, int multiple) {
        return ((value + multiple - 1) / multiple) * multiple;
    }

     /**
     * Genera una etiqueta interna.
     */
    private String internalLabel(String base) {
        return "__" + base + "_" + (++internalLabelCounter);
    }

     /**
     * Genera una etiqueta de final de función.
     */
    private String functionEndLabel(FunctionBlock f) {
        return (f.isMain() ? "main" : f.name) + "_end";
    }

     /**
     * Analiza un acceso a un arreglo.
     */
    private ArrayAccess parseArrayAccess(String access) {
        Pattern p = Pattern.compile("(.+)\\[(.+),(.+)\\]");
        Matcher m = p.matcher(access);

        if (!m.matches()) return null;

        return new ArrayAccess(
                m.group(1).trim(),
                m.group(2).trim(),
                m.group(3).trim()
        );
    }

    /**
     * Representa una función dentro del código intermedio.
     */
    private static class FunctionBlock {
        String name;
        String returnType;
        List<Quadruple> quads = new ArrayList<>();
        Map<String, VarInfo> vars = new LinkedHashMap<>();
        List<String> parameters = new ArrayList<>();
        int frameSize;

        FunctionBlock(String name, String returnType) {
            this.name = name;
            this.returnType = returnType;
        }

        boolean isMain() {
            return "__main__".equals(name);
        }

        boolean isFloatReturn() {
            return "FLOAT".equals(returnType);
        }

        void addVar(String name, String type, boolean array, int rows, int cols) {
            if (name == null || "-".equals(name)) return;
            if (vars.containsKey(name)) return;

            vars.put(name, new VarInfo(name, type, array, rows, cols));
        }
    }

    /**
     * Información de una variable, temporal o arreglo dentro del stack frame.
     */
    private static class VarInfo {
        String name;
        String type;
        boolean array;
        int rows;
        int cols;
        int offset;

        VarInfo(String name, String type, boolean array, int rows, int cols) {
            this.name = name;
            this.type = type;
            this.array = array;
            this.rows = rows;
            this.cols = cols;
        }

        boolean isFloat() {
            return "FLOAT".equals(type) || "SCIENTIFIC".equals(type) || "FRAC".equals(type);
        }

        int sizeBytes() {
            if (array) return rows * cols * 4;
            return 4;
        }
    }

    /**
     * Representa un acceso de arreglo en forma nombre[fila,columna].
     */
    private static class ArrayAccess {
        String name;
        String row;
        String col;

        ArrayAccess(String name, String row, String col) {
            this.name = name;
            this.row = row;
            this.col = col;
        }
    }
}
