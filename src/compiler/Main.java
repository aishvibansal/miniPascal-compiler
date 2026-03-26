package compiler;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String testProgram = """
                program test;
                var x : integer;
                begin
                    x := 10 + 5;
                    write(x);
                end.
                """;

        Lexer lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }

        List<String> errors = lexer.getErrors();
        if (!errors.isEmpty()) {
            System.out.println("\nLexer Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }
}