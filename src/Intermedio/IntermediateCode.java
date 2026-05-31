package Intermedio;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra el código intermedio generado por el compilador.
 * 
 * Almacena las instrucciones en forma de cuádruplos y ofrece
 * operaciones para agregarlas, imprimirlas y exportarlas.
 */
public class IntermediateCode {
    /** Lista ordenada de cuádruplos generados */
    private final List<Quadruple> code = new ArrayList<>();

    /**
     * Agrega una nueva instrucción al código intermedio.
     * 
     * @param op   Operador de la instrucción
     * @param arg1 Primer argumento
     * @param arg2 Segundo argumento
     * @param res  Resultado o destino
     */
    public void add(String op, String arg1, String arg2, String res) {
        code.add(new Quadruple(op, arg1, arg2, res));
    }

    /**
     * Genera un nuevo temporal único.
     * 
     * @return Nombre del temporal generado
     */
    public String newTemp() {
        return GenerateTemp.newTemp();
    }

    /**
     * Genera una nueva etiqueta a partir de un nombre base.
     * 
     * @param base Prefijo de la etiqueta
     * @return Etiqueta única generada
     */
    public String newLabel(String base) {
        return GenerateLabel.newLabel(base);
    }

    /**
     * Retorna la lista completa de cuádruplos generados.
     * 
     * @return Lista de instrucciones intermedias
     */
    public List<Quadruple> getCode() {
        return code;
    }

    /**
     * Reinicia el código intermedio y los generadores auxiliares.
     * 
     * Limpia la lista de instrucciones y reinicia temporales y etiquetas.
     */
    public void reset() {
        code.clear();
        GenerateTemp.reset();
        GenerateLabel.reset();
    }

    /**
     * Exporta el código intermedio a un archivo de texto.
     * 
     * @param filePath Ruta de salida del archivo
     */
    public void exportToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Quadruple q : code) {
                writer.println(q.toString());
            }
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo exportar el código intermedio: " + e.getMessage());
        }
    }

    /**
     * Imprime el código intermedio en consola.
     */
    public void printCode() {
        for (Quadruple q : code) {
            System.out.println(q);
        }
    }
}