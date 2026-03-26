package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {

    // Stores info about each declared variable
    static class Symbol {
        String name;
        String type;      // "integer"
        boolean isArray;
        int arraySize;

        Symbol(String name, String type, boolean isArray, int arraySize) {
            this.name = name;
            this.type = type;
            this.isArray = isArray;
            this.arraySize = arraySize;
        }
    }

    private Map<String, Symbol> symbolTable = new HashMap<>();
    private List<String> errors = new ArrayList<>();

    public void analyze(ProgramNode program) {
        // Register all declared variables into the symbol table
        for (ASTNode decl : program.declarations) {
            if (decl instanceof VarDeclarationNode v) {
                for (String name : v.names) {
                    if (symbolTable.containsKey(name)) {
                        errors.add("Line " + v.line + ": Variable '" + name + "' is already declared");
                    } else {
                        symbolTable.put(name, new Symbol(name, v.type, false, 0));
                    }
                }
            } else if (decl instanceof ArrayDeclarationNode a) {
                for (String name : a.names) {
                    if (symbolTable.containsKey(name)) {
                        errors.add("Line " + a.line + ": Variable '" + name + "' is already declared");
                    } else {
                        symbolTable.put(name, new Symbol(name, a.type, true, a.size));
                    }
                }
            }
        }

        // Analyze the body
        analyzeNode(program.body);
    }

    private void analyzeNode(ASTNode node) {
        if (node == null) return;

        if (node instanceof CompoundStatementNode c) {
            for (ASTNode stmt : c.statements) analyzeNode(stmt);

        } else if (node instanceof AssignmentNode a) {
            checkVariableDeclared(a.variable, a.line);
            if (a.index != null) {
                checkIsArray(a.variable, a.line);
                analyzeNode(a.index);
            } else {
                checkIsNotArray(a.variable, a.line);
            }
            analyzeNode(a.expression);

        } else if (node instanceof IfNode i) {
            analyzeNode(i.condition);
            analyzeNode(i.thenBranch);
            analyzeNode(i.elseBranch);

        } else if (node instanceof WhileNode w) {
            analyzeNode(w.condition);
            analyzeNode(w.body);

        } else if (node instanceof ReadNode r) {
            for (String var : r.variables) checkVariableDeclared(var, r.line);

        } else if (node instanceof WriteNode w) {
            for (ASTNode out : w.outputs) analyzeNode(out);

        } else if (node instanceof BinaryOpNode b) {
            analyzeNode(b.left);
            analyzeNode(b.right);

        } else if (node instanceof UnaryOpNode u) {
            analyzeNode(u.operand);

        } else if (node instanceof IdentifierNode i) {
            checkVariableDeclared(i.name, i.line);

        } else if (node instanceof ArrayAccessNode a) {
            checkVariableDeclared(a.name, a.line);
            checkIsArray(a.name, a.line);
            analyzeNode(a.index);

        } else if (node instanceof NumberNode || node instanceof StringNode) {
            // literals are always fine
        }
    }

    private void checkVariableDeclared(String name, int line) {
        if (!symbolTable.containsKey(name)) {
            errors.add("Line " + line + ": Undeclared variable '" + name + "'");
        }
    }

    private void checkIsArray(String name, int line) {
        Symbol s = symbolTable.get(name);
        if (s != null && !s.isArray) {
            errors.add("Line " + line + ": Variable '" + name + "' is not an array");
        }
    }

    private void checkIsNotArray(String name, int line) {
        Symbol s = symbolTable.get(name);
        if (s != null && s.isArray) {
            errors.add("Line " + line + ": Array '" + name + "' must be accessed with an index");
        }
    }

    public Map<String, Symbol> getSymbolTable() {
        return symbolTable;
    }

    public List<String> getErrors() {
        return errors;
    }
}