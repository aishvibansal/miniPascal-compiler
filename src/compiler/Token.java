package compiler;

public class Token {
    public TokenType type;
    public String value;
    public int line;

    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("Token(%-15s | %-20s | line %d)", type, value, line);
    }
}