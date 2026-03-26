package compiler;
import java.util.ArrayList;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String testProgram = """
                program test;
                var x : integer;
                var i : integer;
                begin
                    x := 10 + 5;
                    if x > 10 then
                        write(x)
                    else
                        write(0);
                    while i < 5 do
                        i := i + 1;
                    read(x);
                    write('done')
                end.
                """;

        // Lexer
        Lexer lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        // Parser
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parseProgram();

        System.out.println("Program name: " + ast.name);
        System.out.println("Declarations: " + ast.declarations.size());
        System.out.println("Parsed successfully!");

        List<String> errors = new ArrayList<>(lexer.getErrors());
        errors.addAll(parser.getErrors());

        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            for (String e : errors) System.out.println(e);
        }
    }
}