package compiler;

public enum TokenType {
    // Literals
    IDENTIFIER,
    NUMBER,
    STRING,

    // Reserved words
    AND, ARRAY, BEGIN, INTEGER, DO, ELSE, END,
    FUNCTION, IF, OF, OR, NOT, PROCEDURE, PROGRAM,
    READ, THEN, VAR, WHILE, WRITE,

    // Delimiters
    LPAREN,       // (
    RPAREN,       // )
    LBRACKET,     // [
    RBRACKET,     // ]
    SEMICOLON,    // ;
    COLON,        // :
    DOT,          // .
    COMMA,        // ,
    STAR,         // *
    MINUS,        // -
    PLUS,         // +
    SLASH,        // /
    LESS,         // 
    EQUALS,       // =
    GREATER,      // >

    // Compound symbols
    NOT_EQUAL,    // <>
    ASSIGN,       // :=
    LESS_EQ,      // <=
    GREATER_EQ,   // >=

    // Special
    EOF,
    UNKNOWN
}