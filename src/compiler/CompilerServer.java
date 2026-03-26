package compiler;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CompilerServer {

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", CompilerServer::handleRoot);
        server.createContext("/compile", CompilerServer::handleCompile);
        server.start();
        System.out.println("miniPascal IDE running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }

    // Serves the HTML IDE page
    private static void handleRoot(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String html;
        // Try to load from file first, fall back to embedded
        java.io.File htmlFile = new java.io.File("web/index.html");
        if (htmlFile.exists()) {
            html = new String(Files.readAllBytes(htmlFile.toPath()));
        } else {
            html = getEmbeddedHTML();
        }

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    // Handles compile requests from the browser
    private static void handleCompile(HttpExchange exchange) throws IOException {
        // Allow CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Read source code from request body
        String sourceCode = new String(
            exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8
        );

        // Run the compiler pipeline
        CompilerResult result = runCompiler(sourceCode);

        // Build JSON response
        String json = toJSON(result);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    // Runs the full compiler pipeline and returns results
    static CompilerResult runCompiler(String sourceCode) {
        CompilerResult result = new CompilerResult();

        try {
            // Lexer
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();

            // Parser
            Parser parser = new Parser(tokens);
            ProgramNode ast = parser.parseProgram();

            // Semantic Analysis
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(ast);

            // Collect errors
            List<String> errors = new ArrayList<>(lexer.getErrors());
            errors.addAll(parser.getErrors());
            errors.addAll(analyzer.getErrors());
            result.errors = errors;

            // Symbol table
            StringBuilder sb = new StringBuilder();
            analyzer.getSymbolTable().forEach((name, symbol) ->
                sb.append(name).append(" : ").append(symbol.type)
                  .append(symbol.isArray ? "[" + symbol.arraySize + "]" : "")
                  .append("\n")
            );
            result.symbolTable = sb.toString();

            // Listing file
            StringBuilder listing = new StringBuilder();
            String[] lines = sourceCode.split("\n", -1);
            java.util.Map<Integer, List<String>> errorMap = new java.util.TreeMap<>();
            for (String error : errors) {
                int line = extractLineNumber(error);
                errorMap.computeIfAbsent(line, k -> new ArrayList<>()).add(error);
            }
            for (int i = 0; i < lines.length; i++) {
                int lineNum = i + 1;
                listing.append(String.format("%4d | %s\n", lineNum, lines[i]));
                if (errorMap.containsKey(lineNum)) {
                    for (String err : errorMap.get(lineNum)) {
                        listing.append("     *** ERROR: ").append(err).append("\n");
                    }
                }
            }
            result.listing = listing.toString();

            // Only generate code if no errors
            if (errors.isEmpty()) {
                ICGenerator icg = new ICGenerator();
                List<ICGInstruction> instructions = icg.generate(ast);

                StringBuilder tac = new StringBuilder();
                for (ICGInstruction instr : instructions) {
                    tac.append(instr.toString()).append("\n");
                }
                result.tac = tac.toString();

                CodeGenerator codeGen = new CodeGenerator(instructions);
                List<String> asmLines = codeGen.generate();
                StringBuilder asm = new StringBuilder();
                for (String line : asmLines) {
                    asm.append(line).append("\n");
                }
                result.assembly = asm.toString();
                result.success = true;
            }

        } catch (Exception e) {
            result.errors = List.of("Internal compiler error: " + e.getMessage());
        }

        return result;
    }

    private static int extractLineNumber(String error) {
        try {
            String[] parts = error.split(" ");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1].replace(":", "").trim());
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    // Builds JSON response string
    private static String toJSON(CompilerResult result) {
        return "{"
            + "\"success\":" + result.success + ","
            + "\"symbolTable\":" + jsonStr(result.symbolTable) + ","
            + "\"listing\":" + jsonStr(result.listing) + ","
            + "\"tac\":" + jsonStr(result.tac) + ","
            + "\"assembly\":" + jsonStr(result.assembly) + ","
            + "\"errors\":" + jsonArray(result.errors)
            + "}";
    }

    private static String jsonStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            + "\"";
    }

    private static String jsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(jsonStr(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Embedded HTML so the IDE works even without the web/ folder
    private static String getEmbeddedHTML() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>miniPascal IDE</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: 'Segoe UI', sans-serif; background: #1e1e2e; color: #cdd6f4; height: 100vh; display: flex; flex-direction: column; }
            header { background: #313244; padding: 12px 20px; display: flex; align-items: center; gap: 16px; border-bottom: 1px solid #45475a; }
            header h1 { font-size: 18px; color: #cba6f7; font-weight: 600; }
            header span { font-size: 12px; color: #6c7086; }
            .main { display: flex; flex: 1; overflow: hidden; }
            .editor-pane { display: flex; flex-direction: column; width: 45%; border-right: 1px solid #45475a; }
            .editor-pane label { padding: 8px 12px; font-size: 11px; color: #6c7086; background: #313244; border-bottom: 1px solid #45475a; text-transform: uppercase; letter-spacing: 1px; }
            textarea { flex: 1; background: #1e1e2e; color: #cdd6f4; border: none; outline: none; padding: 16px; font-family: 'Courier New', monospace; font-size: 14px; line-height: 1.6; resize: none; }
            .controls { padding: 10px 12px; background: #313244; border-top: 1px solid #45475a; display: flex; gap: 10px; align-items: center; }
            button { padding: 8px 20px; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600; transition: opacity 0.2s; }
            #compileBtn { background: #a6e3a1; color: #1e1e2e; }
            #clearBtn { background: #45475a; color: #cdd6f4; }
            button:hover { opacity: 0.85; }
            #status { font-size: 12px; margin-left: auto; }
            .status-ok  { color: #a6e3a1; }
            .status-err { color: #f38ba8; }
            .output-pane { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
            .tabs { display: flex; background: #313244; border-bottom: 1px solid #45475a; }
            .tab { padding: 8px 16px; font-size: 12px; cursor: pointer; border-bottom: 2px solid transparent; color: #6c7086; transition: all 0.2s; user-select: none; }
            .tab.active { color: #cba6f7; border-bottom-color: #cba6f7; }
            .tab-content { display: none; flex: 1; overflow-y: auto; padding: 16px; }
            .tab-content.active { display: block; }
            pre { font-family: 'Courier New', monospace; font-size: 13px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
            .error-line { color: #f38ba8; }
            .symbol-row { display: flex; gap: 12px; padding: 4px 8px; border-radius: 4px; font-family: monospace; font-size: 13px; }
            .symbol-row:nth-child(odd) { background: #313244; }
          </style>
        </head>
        <body>
          <header>
            <h1>miniPascal IDE</h1>
            <span>Lexer → Parser → Semantic → TAC → Assembly</span>
          </header>
          <div class="main">
            <div class="editor-pane">
              <label>Source Code</label>
              <textarea id="code" spellcheck="false">program example;
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
        end.</textarea>
              <div class="controls">
                <button id="compileBtn" onclick="compile()">▶ Compile</button>
                <button id="clearBtn" onclick="clearOutput()">Clear</button>
                <span id="status"></span>
              </div>
            </div>
            <div class="output-pane">
              <div class="tabs">
                <div class="tab active" onclick="showTab('listing')">Listing</div>
                <div class="tab" onclick="showTab('symbols')">Symbols</div>
                <div class="tab" onclick="showTab('tac')">TAC</div>
                <div class="tab" onclick="showTab('asm')">Assembly</div>
                <div class="tab" onclick="showTab('errors')">Errors</div>
              </div>
              <div id="listing" class="tab-content active"><pre id="listingOut">Click Compile to see output...</pre></div>
              <div id="symbols" class="tab-content"><pre id="symbolsOut"></pre></div>
              <div id="tac" class="tab-content"><pre id="tacOut"></pre></div>
              <div id="asm" class="tab-content"><pre id="asmOut"></pre></div>
              <div id="errors" class="tab-content"><pre id="errorsOut"></pre></div>
            </div>
          </div>
          <script>
            function showTab(name) {
              document.querySelectorAll('.tab').forEach((t,i) => {
                const names = ['listing','symbols','tac','asm','errors'];
                t.classList.toggle('active', names[i] === name);
              });
              document.querySelectorAll('.tab-content').forEach(c => {
                c.classList.toggle('active', c.id === name);
              });
            }

            async function compile() {
              const code = document.getElementById('code').value;
              const btn = document.getElementById('compileBtn');
              const status = document.getElementById('status');
              btn.textContent = '⏳ Compiling...';
              btn.disabled = true;
              status.textContent = '';

              try {
                const res = await fetch('/compile', {
                  method: 'POST',
                  body: code
                });
                const data = await res.json();

                document.getElementById('listingOut').innerHTML = formatListing(data.listing);
                document.getElementById('symbolsOut').textContent = data.symbolTable || 'No symbols.';
                document.getElementById('tacOut').textContent = data.tac || 'Not generated.';
                document.getElementById('asmOut').textContent = data.assembly || 'Not generated.';

                if (data.errors && data.errors.length > 0) {
                  document.getElementById('errorsOut').innerHTML =
                    data.errors.map(e => '<span class="error-line">' + e + '</span>').join('\\n');
                  status.innerHTML = '<span class="status-err">✗ ' + data.errors.length + ' error(s)</span>';
                  showTab('errors');
                } else {
                  document.getElementById('errorsOut').textContent = 'No errors.';
                  status.innerHTML = '<span class="status-ok">✓ Compiled successfully</span>';
                  showTab('listing');
                }
              } catch (e) {
                status.innerHTML = '<span class="status-err">✗ Server error</span>';
              }

              btn.textContent = '▶ Compile';
              btn.disabled = false;
            }

            function formatListing(text) {
              if (!text) return '';
              return text.split('\\n').map(line => {
                if (line.includes('*** ERROR')) {
                  return '<span class="error-line">' + line + '</span>';
                }
                return line;
              }).join('\\n');
            }

            function clearOutput() {
              ['listingOut','symbolsOut','tacOut','asmOut','errorsOut']
                .forEach(id => document.getElementById(id).textContent = '');
              document.getElementById('status').textContent = '';
            }
          </script>
        </body>
        </html>
        """;
    }
}