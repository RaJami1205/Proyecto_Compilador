package Intermedio;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class IntermediateCode {
    private final List<Quadruple> code = new ArrayList<>();

    public void add(String op, String arg1, String arg2, String res) {
        code.add(new Quadruple(op, arg1, arg2, res));
    }

    public String newTemp() {
        return GenerateTemp.newTemp();
    }

    public String newLabel(String base) {
        return GenerateLabel.newLabel(base);
    }

    public List<Quadruple> getCode() {
        return code;
    }

    public void reset() {
        code.clear();
        GenerateTemp.reset();
        GenerateLabel.reset();
    }

    public void exportToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Quadruple q : code) {
                writer.println(q.toString());
            }
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo exportar el código intermedio: " + e.getMessage());
        }
    }

    public void printCode() {
        for (Quadruple q : code) {
            System.out.println(q);
        }
    }
}