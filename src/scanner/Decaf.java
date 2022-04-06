package src.scanner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import src.parser.ASTNode;
import src.parser.Parser;



public class Decaf {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: decaf [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        ASTNode.Prog tree = (ASTNode.Prog) parser.parse(tokens);

        System.out.println( "Program: \n" + tree.toString());

    }


    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("*** Error line " + line + ".\n"+ "*** " + message);
        hadError = true;
    }
}
