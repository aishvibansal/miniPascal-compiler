package compiler;

public class ICGInstruction {
    public String result;
    public String arg1;
    public String operator;
    public String arg2;
    public String type; // ASSIGN, BINOP, UNARY, LABEL, GOTO, IFGOTO, PARAM, CALL, READ, WRITE

    // Binary operation: result = arg1 op arg2
    public static ICGInstruction binOp(String result, String arg1, String op, String arg2) {
        ICGInstruction i = new ICGInstruction();
        i.type = "BINOP";
        i.result = result;
        i.arg1 = arg1;
        i.operator = op;
        i.arg2 = arg2;
        return i;
    }

    // Simple assignment: result = arg1
    public static ICGInstruction assign(String result, String arg1) {
        ICGInstruction i = new ICGInstruction();
        i.type = "ASSIGN";
        i.result = result;
        i.arg1 = arg1;
        return i;
    }

    // Unary: result = op arg1
    public static ICGInstruction unary(String result, String op, String arg1) {
        ICGInstruction i = new ICGInstruction();
        i.type = "UNARY";
        i.result = result;
        i.operator = op;
        i.arg1 = arg1;
        return i;
    }

    // Label: LABEL name
    public static ICGInstruction label(String name) {
        ICGInstruction i = new ICGInstruction();
        i.type = "LABEL";
        i.result = name;
        return i;
    }

    // Unconditional jump: GOTO label
    public static ICGInstruction gotoLabel(String label) {
        ICGInstruction i = new ICGInstruction();
        i.type = "GOTO";
        i.result = label;
        return i;
    }

    // Conditional jump: IF arg1 GOTO label
    public static ICGInstruction ifGoto(String condition, String label) {
        ICGInstruction i = new ICGInstruction();
        i.type = "IFGOTO";
        i.arg1 = condition;
        i.result = label;
        return i;
    }

    // Read: READ var
    public static ICGInstruction read(String var) {
        ICGInstruction i = new ICGInstruction();
        i.type = "READ";
        i.result = var;
        return i;
    }

    // Write: WRITE var
    public static ICGInstruction write(String var) {
        ICGInstruction i = new ICGInstruction();
        i.type = "WRITE";
        i.result = var;
        return i;
    }

    // Array store: result[index] = arg1
    public static ICGInstruction arrayStore(String result, String index, String arg1) {
        ICGInstruction i = new ICGInstruction();
        i.type = "ARRAY_STORE";
        i.result = result;
        i.arg1 = arg1;
        i.arg2 = index;
        return i;
    }

    // Array load: result = arg1[index]
    public static ICGInstruction arrayLoad(String result, String arg1, String index) {
        ICGInstruction i = new ICGInstruction();
        i.type = "ARRAY_LOAD";
        i.result = result;
        i.arg1 = arg1;
        i.arg2 = index;
        return i;
    }

    @Override
    public String toString() {
        return switch (type) {
            case "BINOP"       -> result + " = " + arg1 + " " + operator + " " + arg2;
            case "ASSIGN"      -> result + " = " + arg1;
            case "UNARY"       -> result + " = " + operator + " " + arg1;
            case "LABEL"       -> result + ":";
            case "GOTO"        -> "GOTO " + result;
            case "IFGOTO"      -> "IF " + arg1 + " GOTO " + result;
            case "READ"        -> "READ " + result;
            case "WRITE"       -> "WRITE " + result;
            case "ARRAY_STORE" -> result + "[" + arg2 + "] = " + arg1;
            case "ARRAY_LOAD"  -> result + " = " + arg1 + "[" + arg2 + "]";
            default            -> "UNKNOWN";
        };
    }
}