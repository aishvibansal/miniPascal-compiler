package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private String source;
    private int pos;
    private int line;
    private List<Token> tokens;
    private List<String> errors;

    private static final Map<String, TokenType> RESERVED_WORDS = new HashMap<>();

    static {
        RESERVED_WORDS.put("and",       TokenType.AND);
        RESERVED_WORDS.put("array",     TokenType.ARRAY);
        RESERVED_WORDS.put("begin",     TokenType.BEGIN);
        RESERVED_WORDS.put("integer",   TokenType.INTEGER);
        RESERVED_WORDS.put("do",        TokenType.DO);
        RESERVED_WORDS.put("else",      TokenType.ELSE);
        RESERVED_WORDS.put("end",       TokenType.END);
        RESERVED_WORDS.put("function",  TokenType.FUNCTION);
        RESERVED_WORDS.put("if",        TokenType.IF);
        RESERVED_WORDS.put("of",        TokenType.OF);
        RESERVED_WORDS.put("or",        TokenType.OR);
        RESERVED_WORDS.put("not",       TokenType.NOT);
        RESERVED_WORDS.put("procedure", TokenType.PROCEDURE);
        RESERVED_WORDS.put("program",   TokenType.PROGRAM);
        RESERVED_WORDS.put("read",      TokenType.READ);
        RESERVED_WORDS.put("then",      TokenType.THEN);
        RESERVED_WORDS.put("var",       TokenType.VAR);
        RESERVED_WORDS.put("while",     TokenType.WHILE);
        RESERVED_WORDS.put("write",     TokenType.WRITE);
    }

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.tokens = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) break;

            char c = source.charAt(pos);

            if (c == '!') {
                skipComment();
            } else if (Character.isLetter(c)) {
                readIdentifierOrKeyword();
            } else if (Character.isDigit(c)) {
                readNumber();
            } else if (c == '\'') {
                readString();
            } else {
                readSymbol();
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line));
        return tokens;
    }

    private void skipWhitespace() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == '\n') {
                line++;
                pos++;
            } else if (Character.isWhitespace(c)) {
                pos++;
            } else {
                break;
            }
        }
    }

    private void skipComment() {
        // Skip everything from ! to end of line
        while (pos < source.length() && source.charAt(pos) != '\n') {
            pos++;
        }
    }

    private void readIdentifierOrKeyword() {
        int start = pos;
        while (pos < source.length() &&
               (Character.isLetterOrDigit(source.charAt(pos)))) {
            pos++;
        }
        String word = source.substring(start, pos).toLowerCase();

        // Truncate to 32 chars for comparison (as per spec)
        String key = word.length() > 32 ? word.substring(0, 32) : word;

        TokenType type = RESERVED_WORDS.getOrDefault(key, TokenType.IDENTIFIER);
        tokens.add(new Token(type, word, line));
    }

    private void readNumber() {
        int start = pos;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            pos++;
        }
        tokens.add(new Token(TokenType.NUMBER, source.substring(start, pos), line));
    }

    private void readString() {
        pos++; // skip opening apostrophe
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == '\n') {
                errors.add("Line " + line + ": Unterminated string literal");
                line++;
                break;
            } else if (c == '\\') {
                pos++;
                if (pos < source.length()) {
                    char next = source.charAt(pos);
                    if (next == 'n') sb.append('\n');
                    else if (next == 't') sb.append('\t');
                    else sb.append(next);
                    pos++;
                }
            } else if (c == '\'') {
                pos++; // skip closing apostrophe
                break;
            } else {
                sb.append(c);
                pos++;
            }
        }
        tokens.add(new Token(TokenType.STRING, sb.toString(), line));
    }

    private void readSymbol() {
        char c = source.charAt(pos);
        int currentLine = line;

        switch (c) {
            case '(' -> { tokens.add(new Token(TokenType.LPAREN,    "(", currentLine)); pos++; }
            case ')' -> { tokens.add(new Token(TokenType.RPAREN,    ")", currentLine)); pos++; }
            case '[' -> { tokens.add(new Token(TokenType.LBRACKET,  "[", currentLine)); pos++; }
            case ']' -> { tokens.add(new Token(TokenType.RBRACKET,  "]", currentLine)); pos++; }
            case ';' -> { tokens.add(new Token(TokenType.SEMICOLON, ";", currentLine)); pos++; }
            case '.' -> { tokens.add(new Token(TokenType.DOT,       ".", currentLine)); pos++; }
            case ',' -> { tokens.add(new Token(TokenType.COMMA,     ",", currentLine)); pos++; }
            case '*' -> { tokens.add(new Token(TokenType.STAR,      "*", currentLine)); pos++; }
            case '-' -> { tokens.add(new Token(TokenType.MINUS,     "-", currentLine)); pos++; }
            case '+' -> { tokens.add(new Token(TokenType.PLUS,      "+", currentLine)); pos++; }
            case '/' -> { tokens.add(new Token(TokenType.SLASH,     "/", currentLine)); pos++; }
            case '=' -> { tokens.add(new Token(TokenType.EQUALS,    "=", currentLine)); pos++; }
            case '<' -> {
                if (peek() == '>') { tokens.add(new Token(TokenType.NOT_EQUAL, "<>", currentLine)); pos += 2; }
                else if (peek() == '=') { tokens.add(new Token(TokenType.LESS_EQ, "<=", currentLine)); pos += 2; }
                else { tokens.add(new Token(TokenType.LESS, "<", currentLine)); pos++; }
            }
            case '>' -> {
                if (peek() == '=') { tokens.add(new Token(TokenType.GREATER_EQ, ">=", currentLine)); pos += 2; }
                else { tokens.add(new Token(TokenType.GREATER, ">", currentLine)); pos++; }
            }
            case ':' -> {
                if (peek() == '=') { tokens.add(new Token(TokenType.ASSIGN, ":=", currentLine)); pos += 2; }
                else { tokens.add(new Token(TokenType.COLON, ":", currentLine)); pos++; }
            }
            default -> {
                errors.add("Line " + line + ": Unknown character '" + c + "'");
                pos++;
            }
        }
    }

    private char peek() {
        if (pos + 1 < source.length()) return source.charAt(pos + 1);
        return '\0';
    }

    public List<String> getErrors() {
        return errors;
    }
}