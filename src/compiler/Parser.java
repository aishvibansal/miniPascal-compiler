package compiler;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;
    private List<String> errors;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.errors = new ArrayList<>();
    }

    // --- Helper methods ---

    private Token current() {
        return tokens.get(pos);
    }

    private Token peek(int offset) {
        int index = pos + offset;
        if (index < tokens.size()) return tokens.get(index);
        return tokens.get(tokens.size() - 1);
    }

    private Token consume() {
        Token t = tokens.get(pos);
        if (pos < tokens.size() - 1) pos++;
        return t;
    }

    private Token expect(TokenType type) {
        if (current().type == type) {
            return consume();
        }
        errors.add("Line " + current().line + ": Expected " + type + " but found '" + current().value + "'");
        return current(); // error recovery: don't advance
    }

    private boolean check(TokenType type) {
        return current().type == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) { consume(); return true; }
        return false;
    }

    // --- Parsing methods ---

    public ProgramNode parseProgram() {
        int line = current().line;
        expect(TokenType.PROGRAM);
        String name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.SEMICOLON);

        List<ASTNode> declarations = parseDeclarations();

        ASTNode body = parseCompoundStatement();
        expect(TokenType.DOT);

        return new ProgramNode(name, declarations, body, line);
    }

    private List<ASTNode> parseDeclarations() {
        List<ASTNode> declarations = new ArrayList<>();
        while (check(TokenType.VAR)) {
            consume(); // eat 'var'
            declarations.addAll(parseVarBlock());
        }
        return declarations;
    }

    private List<ASTNode> parseVarBlock() {
    List<ASTNode> decls = new ArrayList<>();

    while (check(TokenType.IDENTIFIER)) {
        int line = current().line;
        List<String> names = parseIdentifierList();
        expect(TokenType.COLON);

        if (check(TokenType.ARRAY)) {
            consume(); // eat 'array'
            expect(TokenType.LBRACKET);
            int size = Integer.parseInt(expect(TokenType.NUMBER).value);
            expect(TokenType.RBRACKET);
            expect(TokenType.OF);
            expect(TokenType.INTEGER);
            expect(TokenType.SEMICOLON);
            decls.add(new ArrayDeclarationNode(names, size, "integer", line));
        } else {
            expect(TokenType.INTEGER);
            expect(TokenType.SEMICOLON);
            decls.add(new VarDeclarationNode(names, "integer", line));
        }
    }
    return decls;
}

    private List<String> parseIdentifierList() {
        List<String> names = new ArrayList<>();
        names.add(expect(TokenType.IDENTIFIER).value);
        while (match(TokenType.COMMA)) {
            names.add(expect(TokenType.IDENTIFIER).value);
        }
        return names;
    }

    private ASTNode parseCompoundStatement() {
        int line = current().line;
        expect(TokenType.BEGIN);
        List<ASTNode> statements = new ArrayList<>();

        while (!check(TokenType.END) && !check(TokenType.EOF)) {
            ASTNode stmt = parseStatement();
            if (stmt != null) statements.add(stmt);
            match(TokenType.SEMICOLON); // optional semicolon between statements
        }

        expect(TokenType.END);
        return new CompoundStatementNode(statements, line);
    }

    private ASTNode parseStatement() {
        int line = current().line;

        if (check(TokenType.BEGIN)) {
            return parseCompoundStatement();

        } else if (check(TokenType.IF)) {
            return parseIf();

        } else if (check(TokenType.WHILE)) {
            return parseWhile();

        } else if (check(TokenType.READ)) {
            return parseRead();

        } else if (check(TokenType.WRITE)) {
            return parseWrite();

        } else if (check(TokenType.IDENTIFIER)) {
            return parseAssignment();

        } else if (check(TokenType.END) || check(TokenType.ELSE)) {
            return null; // end of block

        } else {
            errors.add("Line " + line + ": Unexpected token '" + current().value + "' in statement");
            consume(); // skip unknown token (error recovery)
            return null;
        }
    }

    private ASTNode parseIf() {
        int line = current().line;
        expect(TokenType.IF);
        ASTNode condition = parseExpression();
        expect(TokenType.THEN);
        ASTNode thenBranch = parseStatement();
        ASTNode elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement();
        }
        return new IfNode(condition, thenBranch, elseBranch, line);
    }

    private ASTNode parseWhile() {
        int line = current().line;
        expect(TokenType.WHILE);
        ASTNode condition = parseExpression();
        expect(TokenType.DO);
        ASTNode body = parseStatement();
        return new WhileNode(condition, body, line);
    }

    private ASTNode parseRead() {
        int line = current().line;
        expect(TokenType.READ);
        expect(TokenType.LPAREN);
        List<String> vars = parseIdentifierList();
        expect(TokenType.RPAREN);
        return new ReadNode(vars, line);
    }

    private ASTNode parseWrite() {
        int line = current().line;
        expect(TokenType.WRITE);
        expect(TokenType.LPAREN);
        List<ASTNode> outputs = new ArrayList<>();
        outputs.add(parseOutputItem());
        while (match(TokenType.COMMA)) {
            outputs.add(parseOutputItem());
        }
        expect(TokenType.RPAREN);
        return new WriteNode(outputs, line);
    }

    private ASTNode parseOutputItem() {
        if (check(TokenType.STRING)) {
            Token t = consume();
            return new StringNode(t.value, t.line);
        }
        return parseExpression();
    }

    private ASTNode parseAssignment() {
        int line = current().line;
        String name = expect(TokenType.IDENTIFIER).value;

        // Check for array access: a[i] :=
        ASTNode index = null;
        if (match(TokenType.LBRACKET)) {
            index = parseExpression();
            expect(TokenType.RBRACKET);
        }

        expect(TokenType.ASSIGN);
        ASTNode expr = parseExpression();
        return new AssignmentNode(name, index, expr, line);
    }

    // --- Expression parsing (handles precedence) ---

    private ASTNode parseExpression() {
        return parseOr();
    }

    private ASTNode parseOr() {
        ASTNode left = parseAnd();
        while (check(TokenType.OR)) {
            int line = current().line;
            String op = consume().value;
            ASTNode right = parseAnd();
            left = new BinaryOpNode(left, op, right, line);
        }
        return left;
    }

    private ASTNode parseAnd() {
        ASTNode left = parseNot();
        while (check(TokenType.AND)) {
            int line = current().line;
            String op = consume().value;
            ASTNode right = parseNot();
            left = new BinaryOpNode(left, op, right, line);
        }
        return left;
    }

    private ASTNode parseNot() {
        if (check(TokenType.NOT)) {
            int line = current().line;
            String op = consume().value;
            ASTNode operand = parseNot();
            return new UnaryOpNode(op, operand, line);
        }
        return parseComparison();
    }

    private ASTNode parseComparison() {
        ASTNode left = parseAddSub();
        while (check(TokenType.LESS) || check(TokenType.GREATER) ||
               check(TokenType.EQUALS) || check(TokenType.NOT_EQUAL) ||
               check(TokenType.LESS_EQ) || check(TokenType.GREATER_EQ)) {
            int line = current().line;
            String op = consume().value;
            ASTNode right = parseAddSub();
            left = new BinaryOpNode(left, op, right, line);
        }
        return left;
    }

    private ASTNode parseAddSub() {
        ASTNode left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            int line = current().line;
            String op = consume().value;
            ASTNode right = parseMulDiv();
            left = new BinaryOpNode(left, op, right, line);
        }
        return left;
    }

    private ASTNode parseMulDiv() {
        ASTNode left = parseUnary();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            int line = current().line;
            String op = consume().value;
            ASTNode right = parseUnary();
            left = new BinaryOpNode(left, op, right, line);
        }
        return left;
    }

    private ASTNode parseUnary() {
        if (check(TokenType.MINUS)) {
            int line = current().line;
            consume();
            return new UnaryOpNode("-", parseFactor(), line);
        }
        return parseFactor();
    }

    private ASTNode parseFactor() {
        int line = current().line;

        if (check(TokenType.NUMBER)) {
            return new NumberNode(Integer.parseInt(consume().value), line);
        }

        if (check(TokenType.STRING)) {
            return new StringNode(consume().value, line);
        }

        if (check(TokenType.IDENTIFIER)) {
            String name = consume().value;
            if (match(TokenType.LBRACKET)) {
                ASTNode index = parseExpression();
                expect(TokenType.RBRACKET);
                return new ArrayAccessNode(name, index, line);
            }
            return new IdentifierNode(name, line);
        }

        if (match(TokenType.LPAREN)) {
            ASTNode expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        errors.add("Line " + line + ": Unexpected token '" + current().value + "' in expression");
        consume();
        return new NumberNode(0, line); // dummy node for error recovery
    }

    public List<String> getErrors() {
        return errors;
    }
}