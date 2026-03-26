# miniPascal Compiler

A full compiler pipeline for a Pascal-like language, built in Java from scratch.
Includes a web-based IDE as a novelty feature.

## Features

- **Lexer** — Tokenizes identifiers, keywords, numbers, strings, operators, and comments
- **Parser** — Recursive descent parser that builds an Abstract Syntax Tree (AST)
- **Semantic Analyzer** — Symbol table, type checking, and scope validation
- **Listing File Generator** — Line-numbered output with inline error messages
- **Intermediate Code Generator** — Produces Three Address Code (TAC)
- **Code Generator** — Produces pseudo-assembly code
- **Web-based IDE** — Browser editor where you can type and compile miniPascal code live

## Project Structure
```
miniPascal-compiler/
├── src/compiler/
│   ├── Main.java               # Entry point
│   ├── Lexer.java              # Lexical analyzer
│   ├── Token.java              # Token model
│   ├── TokenType.java          # Token type enum
│   ├── Parser.java             # Recursive descent parser
│   ├── ASTNode.java            # AST node classes
│   ├── SemanticAnalyzer.java   # Semantic analysis + symbol table
│   ├── ListingGenerator.java   # Listing file with error reporting
│   ├── ICGenerator.java        # Intermediate code generator (TAC)
│   ├── ICGInstruction.java     # TAC instruction model
│   ├── CodeGenerator.java      # Pseudo-assembly code generator
│   ├── CompilerServer.java     # Built-in HTTP server for web IDE
│   └── CompilerResult.java     # Result model for server responses
├── testcases/
│   └── test_errors.txt         # Sample program with intentional errors
├── output/                     # Generated listing and assembly files
└── README.md
```

## Requirements

- Java 21 or higher

## How to Run

### Command line mode

Compile all source files:
```bash
javac src/compiler/*.java
```

Run with built-in test program:
```bash
java -cp src compiler.Main
```

Run with your own source file:
```bash
java -cp src compiler.Main testcases/test_errors.txt
```

### Web IDE mode
```bash
java -cp src compiler.Main --server
```

Then open your browser at `http://localhost:8080`

## Example miniPascal Program
```pascal
program counter;
var i : integer;
var total : integer;
begin
    i := 1;
    total := 0;
    while i < 6 do
        begin
            total := total + i;
            i := i + 1
        end;
    write(total)
end.
```

## Compiler Output

For a valid program the compiler produces:
- A **symbol table** listing all declared variables
- A **listing file** with line numbers
- **Three Address Code (TAC)** as intermediate representation
- **Pseudo-assembly code** as the final output

For a program with errors the compiler produces:
- A **listing file** with error messages interleaved after the offending lines
- A **summary** with the total error count
- Code generation is skipped until all errors are resolved

## Language Features

- Integer variables and arithmetic (`+`, `-`, `*`, `/`)
- Boolean expressions (`and`, `or`, `not`)
- Comparison operators (`<`, `>`, `<=`, `>=`, `=`, `<>`)
- `if / then / else` statements
- `while / do` loops
- `read` and `write` statements
- Single-line comments starting with `!`
- String literals with escape sequences (`\'`, `\n`, `\t`)
- Arrays of integers

## Novelty Feature

The project includes a **web-based IDE** built using Java's built-in HTTP server.
No external frameworks or libraries are required. The IDE provides:
- A dark-themed code editor
- Live compilation in the browser
- Tabbed output for Listing, Symbol Table, TAC, Assembly, and Errors

## Author

Aishvi Bansal — [github.com/aishvibansal](https://github.com/aishvibansal)