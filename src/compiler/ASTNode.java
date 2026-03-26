package compiler;

import java.util.List;

// Base class for all AST nodes
public abstract class ASTNode {
    public int line;
}

// program -> program id ;
class ProgramNode extends ASTNode {
    public String name;
    public List<ASTNode> declarations;
    public ASTNode body;

    public ProgramNode(String name, List<ASTNode> declarations, ASTNode body, int line) {
        this.name = name;
        this.declarations = declarations;
        this.body = body;
        this.line = line;
    }
}

// var x, y : integer;
class VarDeclarationNode extends ASTNode {
    public List<String> names;
    public String type;

    public VarDeclarationNode(List<String> names, String type, int line) {
        this.names = names;
        this.type = type;
        this.line = line;
    }
}

// array[num] of integer
class ArrayDeclarationNode extends ASTNode {
    public List<String> names;
    public int size;
    public String type;

    public ArrayDeclarationNode(List<String> names, int size, String type, int line) {
        this.names = names;
        this.size = size;
        this.type = type;
        this.line = line;
    }
}

// begin ... end
class CompoundStatementNode extends ASTNode {
    public List<ASTNode> statements;

    public CompoundStatementNode(List<ASTNode> statements, int line) {
        this.statements = statements;
        this.line = line;
    }
}

// x := expression
class AssignmentNode extends ASTNode {
    public String variable;
    public ASTNode index;   // for array assignment a[i] := ...
    public ASTNode expression;

    public AssignmentNode(String variable, ASTNode index, ASTNode expression, int line) {
        this.variable = variable;
        this.index = index;
        this.expression = expression;
        this.line = line;
    }
}

// if expression then statement else statement
class IfNode extends ASTNode {
    public ASTNode condition;
    public ASTNode thenBranch;
    public ASTNode elseBranch; // can be null

    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch, int line) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        this.line = line;
    }
}

// while expression do statement
class WhileNode extends ASTNode {
    public ASTNode condition;
    public ASTNode body;

    public WhileNode(ASTNode condition, ASTNode body, int line) {
        this.condition = condition;
        this.body = body;
        this.line = line;
    }
}

// read(x, y)
class ReadNode extends ASTNode {
    public List<String> variables;

    public ReadNode(List<String> variables, int line) {
        this.variables = variables;
        this.line = line;
    }
}

// write(x, "hello")
class WriteNode extends ASTNode {
    public List<ASTNode> outputs;

    public WriteNode(List<ASTNode> outputs, int line) {
        this.outputs = outputs;
        this.line = line;
    }
}

// Binary operation: a + b, a < b, etc.
class BinaryOpNode extends ASTNode {
    public ASTNode left;
    public String operator;
    public ASTNode right;

    public BinaryOpNode(ASTNode left, String operator, ASTNode right, int line) {
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.line = line;
    }
}

// Unary operation: not x, -x
class UnaryOpNode extends ASTNode {
    public String operator;
    public ASTNode operand;

    public UnaryOpNode(String operator, ASTNode operand, int line) {
        this.operator = operator;
        this.operand = operand;
        this.line = line;
    }
}

// A plain identifier: x
class IdentifierNode extends ASTNode {
    public String name;

    public IdentifierNode(String name, int line) {
        this.name = name;
        this.line = line;
    }
}

// An array access: a[i]
class ArrayAccessNode extends ASTNode {
    public String name;
    public ASTNode index;

    public ArrayAccessNode(String name, ASTNode index, int line) {
        this.name = name;
        this.index = index;
        this.line = line;
    }
}

// A number literal: 42
class NumberNode extends ASTNode {
    public int value;

    public NumberNode(int value, int line) {
        this.value = value;
        this.line = line;
    }
}

// A string literal: 'hello'
class StringNode extends ASTNode {
    public String value;

    public StringNode(String value, int line) {
        this.value = value;
        this.line = line;
    }
}