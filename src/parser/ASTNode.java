package src.parser;

import src.parser.expression.Expr;
import src.parser.util.TypeToString;
import src.scanner.TokenType;

import javax.swing.plaf.nimbus.State;
import java.sql.Array;
import java.util.ArrayList;

public abstract class ASTNode {
    TokenType type;
    Object obj;
    // have subclasses for different nodes
    public static class Prog extends ASTNode {
        ArrayList<Decl> declarations;
        public Prog() {
            this.declarations = new ArrayList<Decl>();
        }

        public ArrayList<Decl> getDeclarations() {
            return declarations;
        }

        @Override
        public String toString() {
            String result = "";
            for(Decl d: this.declarations) {
                if (d != null)
                    result += d + "\n";
            }
            return result;
        }
    }

    public static class Decl extends ASTNode {
        public Def definition;

        //        public ArrayList<Def> definitions;
        // Can be a variable declaration or a function declaration
        public Decl() {
//            this.definitions = new ArrayList<Def>();
        }

        public static class VarDecl extends Decl {

            public VarDecl() {
                this.definition = new Def();
            }

            @Override
            public String toString() {
                String result;
                result = "VarDecl:\n";
                return result + definition.toString();
            }
        }


        // at some point, need the line in our result strings.
        // TODO: need the type in this block!
        public static class FuncDecl extends Decl {
            String name;
            public TokenType type;
            public ArrayList<Formal> formals;
            public Statement.StatementBlock statementBlock;
            public FuncDecl() {
                this.formals = new ArrayList<Formal>();
            }

            @Override
            public String toString() {
                return "FnDcl: \n" +
                        "\t(return type) " +TypeToString.getTypeAsString(this.type) + "\n"  +
                        "\t(formals) " + formals + "\n" +
                        "statementBlock=" + statementBlock + "\t\t" +
                        '}';
            }
        }
    }

    public static class Formal extends ASTNode {
        public ArrayList<Def> variables;
        public Formal() {
            this.variables = new ArrayList<Def>();
        }

        @Override
        public String toString() {
            return "Formal{" +
                    "variables=" + variables +
                    '}';
        }
    }


    public static class Statement extends ASTNode {
        // since statement can derive an expression
        Expr expr;

        public Statement() {

        }

        public static class StatementBlock extends Statement {
            ArrayList<Decl> variableDeclarations;
            ArrayList<Statement> statements;

            public StatementBlock() {
                this.variableDeclarations = new ArrayList<>();
                this.statements = new ArrayList<>();
            }
        }

        public static class IfStatement extends Statement {
            Expr ifExpression;
            Statement statement;
            Expr elseExpression;

            public IfStatement() {
            }
        }

        public static class WhileStatement extends Statement {
            Expr expression;
            Statement statement;

            public WhileStatement() {
            }
        }

        public static class ForStatement extends Statement {
            Expr before;
            Expr condition;
            Expr after;
            Statement statement;

            public ForStatement() {
            }
        }

        public static class ReturnStatement extends Statement {
            Expr expression;

            public ReturnStatement() {
            }
        }

        public static class BreakStatement extends Statement {
            public BreakStatement() {
            }
        }

        public static class PrintStatement extends Statement {
            public ArrayList<Expr> expressions;

            public PrintStatement() {
                this.expressions = new ArrayList<>();
            }
        }
    }

    public static class Def extends ASTNode {
        Object ident;

        public Def() {
        }

        public Def(TokenType type, Object ident) {
            this.type = type;
            this.ident = ident;
        }

        @Override
        public String toString() {
            return  "\tType: " + TypeToString.getTypeAsString(this.type) + "\n" +
                    "\tIdentifier: " + this.ident + "\n";
        }
    }
}
