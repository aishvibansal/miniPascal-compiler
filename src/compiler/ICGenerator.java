package compiler;

import java.util.ArrayList;
import java.util.List;

public class ICGenerator {
    private List<ICGInstruction> instructions = new ArrayList<>();
    private int tempCount = 0;
    private int labelCount = 0;

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private String newLabel() {
        return "L" + (++labelCount);
    }

    public List<ICGInstruction> generate(ProgramNode program) {
        generateNode(program.body);
        return instructions;
    }

    private void generateNode(ASTNode node) {
        if (node == null) return;

        if (node instanceof CompoundStatementNode c) {
            for (ASTNode stmt : c.statements) generateNode(stmt);

        } else if (node instanceof AssignmentNode a) {
            String exprResult = generateExpression(a.expression);
            if (a.index != null) {
                String indexResult = generateExpression(a.index);
                instructions.add(ICGInstruction.arrayStore(a.variable, indexResult, exprResult));
            } else {
                instructions.add(ICGInstruction.assign(a.variable, exprResult));
            }

        } else if (node instanceof IfNode i) {
            generateIf(i);

        } else if (node instanceof WhileNode w) {
            generateWhile(w);

        } else if (node instanceof ReadNode r) {
            for (String var : r.variables) {
                instructions.add(ICGInstruction.read(var));
            }

        } else if (node instanceof WriteNode w) {
            for (ASTNode out : w.outputs) {
                String result = generateExpression(out);
                instructions.add(ICGInstruction.write(result));
            }
        }
    }

    private void generateIf(IfNode node) {
        String condition = generateExpression(node.condition);
        String elseLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(ICGInstruction.ifGoto(condition, elseLabel.replace("GOTO ", "")));

        // Rewrite: IF condition is TRUE we want to execute then branch
        // So: IF NOT condition GOTO elseLabel
        // We handle this by flipping: store condition, jump to else if false
        // Remove last instruction and replace with proper logic
        instructions.remove(instructions.size() - 1);

        String notCond = newTemp();
        instructions.add(ICGInstruction.unary(notCond, "NOT", condition));
        instructions.add(ICGInstruction.ifGoto(notCond, elseLabel));

        // Then branch
        generateNode(node.thenBranch);

        if (node.elseBranch != null) {
            instructions.add(ICGInstruction.gotoLabel(endLabel));
            instructions.add(ICGInstruction.label(elseLabel));
            generateNode(node.elseBranch);
            instructions.add(ICGInstruction.label(endLabel));
        } else {
            instructions.add(ICGInstruction.label(elseLabel));
        }
    }

    private void generateWhile(WhileNode node) {
        String startLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(ICGInstruction.label(startLabel));
        String condition = generateExpression(node.condition);

        String notCond = newTemp();
        instructions.add(ICGInstruction.unary(notCond, "NOT", condition));
        instructions.add(ICGInstruction.ifGoto(notCond, endLabel));

        generateNode(node.body);
        instructions.add(ICGInstruction.gotoLabel(startLabel));
        instructions.add(ICGInstruction.label(endLabel));
    }

    private String generateExpression(ASTNode node) {
        if (node instanceof NumberNode n) {
            return String.valueOf(n.value);

        } else if (node instanceof StringNode s) {
            return "\"" + s.value + "\"";

        } else if (node instanceof IdentifierNode i) {
            return i.name;

        } else if (node instanceof ArrayAccessNode a) {
            String index = generateExpression(a.index);
            String temp = newTemp();
            instructions.add(ICGInstruction.arrayLoad(temp, a.name, index));
            return temp;

        } else if (node instanceof BinaryOpNode b) {
            String left = generateExpression(b.left);
            String right = generateExpression(b.right);
            String temp = newTemp();
            instructions.add(ICGInstruction.binOp(temp, left, b.operator, right));
            return temp;

        } else if (node instanceof UnaryOpNode u) {
            String operand = generateExpression(u.operand);
            String temp = newTemp();
            instructions.add(ICGInstruction.unary(temp, u.operator, operand));
            return temp;
        }

        return "?";
    }

    public List<ICGInstruction> getInstructions() {
        return instructions;
    }
}