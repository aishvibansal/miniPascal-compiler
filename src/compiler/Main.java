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

        // Semantic Analysis
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(ast);

        // Collect all errors
        List<String> errors = new ArrayList<>(lexer.getErrors());
        errors.addAll(parser.getErrors());
        errors.addAll(analyzer.getErrors());

        // Print symbol table
        System.out.println("=== Symbol Table ===");
        analyzer.getSymbolTable().forEach((name, symbol) ->
            System.out.println("  " + name + " : " + symbol.type +
                (symbol.isArray ? "[" + symbol.arraySize + "]" : ""))
        );

        // Print errors or success
        System.out.println("\n=== Errors ===");
        if (errors.isEmpty()) {
            System.out.println("  No errors found. Semantic analysis passed!");
        } else {
            for (String e : errors) System.out.println("  " + e);
        }
    }
}