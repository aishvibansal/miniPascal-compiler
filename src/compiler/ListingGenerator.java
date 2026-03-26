package compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ListingGenerator {

    private String sourceCode;
    private List<String> errors;

    public ListingGenerator(String sourceCode, List<String> errors) {
        this.sourceCode = sourceCode;
        this.errors = errors;
    }

    // Groups errors by line number
    private Map<Integer, List<String>> groupErrorsByLine() {
        Map<Integer, List<String>> map = new TreeMap<>();
        for (String error : errors) {
            int line = extractLineNumber(error);
            map.computeIfAbsent(line, k -> new ArrayList<>()).add(error);
        }
        return map;
    }

    // Extracts line number from error string like "Line 4: ..."
    private int extractLineNumber(String error) {
        try {
            String[] parts = error.split(" ");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase("Line")) {
                String num = parts[1].replace(":", "").trim();
                return Integer.parseInt(num);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return -1;
    }

    // Prints listing to console
    public void printListing() {
        String[] lines = sourceCode.split("\n", -1);
        Map<Integer, List<String>> errorMap = groupErrorsByLine();

        System.out.println("=== Listing File ===\n");
        for (int i = 0; i < lines.length; i++) {
            int lineNum = i + 1;
            System.out.printf("%4d | %s%n", lineNum, lines[i]);
            if (errorMap.containsKey(lineNum)) {
                for (String err : errorMap.get(lineNum)) {
                    System.out.println("     *** ERROR: " + err);
                }
            }
        }

        System.out.println("\n=== Summary ===");
        if (errors.isEmpty()) {
            System.out.println("Compilation successful. No errors found.");
        } else {
            System.out.println("Total errors found: " + errors.size());
        }
    }

    // Saves listing to a file in the output/ folder
    public void saveListing(String outputPath) {
        String[] lines = sourceCode.split("\n", -1);
        Map<Integer, List<String>> errorMap = groupErrorsByLine();

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("=== Listing File ===\n");
            for (int i = 0; i < lines.length; i++) {
                int lineNum = i + 1;
                writer.printf("%4d | %s%n", lineNum, lines[i]);
                if (errorMap.containsKey(lineNum)) {
                    for (String err : errorMap.get(lineNum)) {
                        writer.println("     *** ERROR: " + err);
                    }
                }
            }

            writer.println("\n=== Summary ===");
            if (errors.isEmpty()) {
                writer.println("Compilation successful. No errors found.");
            } else {
                writer.println("Total errors found: " + errors.size());
            }

            System.out.println("Listing file saved to: " + outputPath);
        } catch (IOException e) {
            System.out.println("Could not save listing file: " + e.getMessage());
        }
    }
}