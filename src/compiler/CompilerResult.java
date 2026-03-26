package compiler;

import java.util.ArrayList;
import java.util.List;

public class CompilerResult {
    public boolean success = false;
    public String symbolTable = "";
    public String listing = "";
    public String tac = "";
    public String assembly = "";
    public List<String> errors = new ArrayList<>();
}