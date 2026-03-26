package compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        // If --server flag is passed, start the web IDE
        if (args.length > 0 && args[0].equals("--server")) {
            int port = 8080;
            if (args.length > 1) port = Integer.parseInt(args[1]);
            CompilerServer.start(port);
            return;
        }

        String sourceCode;
        String baseName = "output/output";

        if (args.length > 0) {
            sourceCode = new String(Files.readAllBytes(Paths.get(args[0])));
            baseName = "output/" + Paths.get(args[0]).getFileName()
                       .toString().replace(".txt", "");
        } else {
            sourceCode = """
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
        }

        // Lexer
        Lexer lexer = new Lexer(sourceCode);
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
        System.out.println();

        // Generate listing file
        ListingGenerator listing = new ListingGenerator(sourceCode, errors);
        listing.printListing();
        listing.saveListing(baseName + "_listing.txt");

        if (errors.isEmpty()) {
            System.out.println("\n=== Intermediate Code (TAC) ===");
            ICGenerator icg = new ICGenerator();
            List<ICGInstruction> instructions = icg.generate(ast);
            for (ICGInstruction instr : instructions) {
                System.out.println("  " + instr);
            }

            System.out.println();
            CodeGenerator codeGen = new CodeGenerator(instructions);
            codeGen.generate();
            codeGen.printCode();
            codeGen.saveCode(baseName + "_assembly.txt");
        } else {
            System.out.println("\nSkipping code generation due to errors.");
        }
    }
}