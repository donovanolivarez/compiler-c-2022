package src.parser.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary: Expr left, Token operator, Expr right",
                "Unary: Token operator, Expr right",
                "Literal: Object value",
                "Grouping: Expr expression"
        ));
    }
    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package src.parser.expression;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("import src.scanner.Token;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        // TODO: this method below, if it's needed
//        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println("}");
        writer.close();
    }
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println(" public static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("  public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
