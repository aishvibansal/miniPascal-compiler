package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    private List<String> code = new ArrayList<>();
    private List<ICGInstruction> instructions;

    public CodeGenerator(List<ICGInstruction> instructions) {
        this.instructions = instructions;
    }

    public List<String> generate() {
        code.add("section .data");
        code.add("section .text");
        code.add("START:");

        for (ICGInstruction instr : instructions) {
            switch (instr.type) {

                case "ASSIGN" -> {
                    code.add("  LOAD  " + instr.arg1);
                    code.add("  STORE " + instr.result);
                }

                case "BINOP" -> {
                    code.add("  LOAD  " + instr.arg1);
                    code.add("  " + mapOperator(instr.operator) + "   " + instr.arg2);
                    code.add("  STORE " + instr.result);
                }

                case "UNARY" -> {
                    if (instr.operator.equals("-")) {
                        code.add("  LOAD  " + instr.arg1);
                        code.add("  NEG");
                        code.add("  STORE " + instr.result);
                    } else if (instr.operator.equalsIgnoreCase("NOT")) {
                        code.add("  LOAD  " + instr.arg1);
                        code.add("  NOT");
                        code.add("  STORE " + instr.result);
                    }
                }

                case "LABEL" -> {
                    code.add(instr.result + ":");
                }

                case "GOTO" -> {
                    code.add("  JUMP  " + instr.result);
                }

                case "IFGOTO" -> {
                    code.add("  LOAD  " + instr.arg1);
                    code.add("  JMPT  " + instr.result);
                }

                case "READ" -> {
                    code.add("  READ  " + instr.result);
                }

                case "WRITE" -> {
                    code.add("  LOAD  " + instr.result);
                    code.add("  PRINT");
                }

                case "ARRAY_STORE" -> {
                    code.add("  LOAD  " + instr.arg1);
                    code.add("  SIDX  " + instr.result + " " + instr.arg2);
                }

                case "ARRAY_LOAD" -> {
                    code.add("  LIDX  " + instr.arg1 + " " + instr.arg2);
                    code.add("  STORE " + instr.result);
                }
            }
        }

        code.add("  HALT");
        return code;
    }

    private String mapOperator(String op) {
        return switch (op) {
            case "+"  -> "ADD  ";
            case "-"  -> "SUB  ";
            case "*"  -> "MUL  ";
            case "/"  -> "DIV  ";
            case ">"  -> "CMPGT";
            case "<"  -> "CMPLT";
            case ">=" -> "CMPGE";
            case "<=" -> "CMPLE";
            case "="  -> "CMPEQ";
            case "<>" -> "CMPNE";
            case "and" -> "AND  ";
            case "or"  -> "OR   ";
            default   -> "UNK  ";
        };
    }

    public void printCode() {
        System.out.println("=== Generated Assembly Code ===");
        for (String line : code) {
            System.out.println(line);
        }
    }

    public void saveCode(String outputPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            for (String line : code) {
                writer.println(line);
            }
            System.out.println("Assembly code saved to: " + outputPath);
        } catch (IOException e) {
            System.out.println("Could not save code: " + e.getMessage());
        }
    }
}