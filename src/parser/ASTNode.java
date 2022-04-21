package src.parser;

import src.parser.expression.Expr;
import src.parser.util.StatementVisitor;
import src.parser.util.TypeToString;
import src.parser.util.Visitor;
import src.parser.util.printers.DeclPrinterVisitor;
import src.scanner.TokenType;

import java.util.ArrayList;

public abstract class ASTNode {
//    abstract public void accept(Visitor v);
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
            StringBuilder result = new StringBuilder();
            DeclPrinterVisitor visitor = new DeclPrinterVisitor();
            for(Decl d: this.declarations) {
                if (d != null)
                    result.append(d.accept(visitor)).append("\n");
            }
            return result.toString();
        }
    }

    public static abstract class Decl extends ASTNode {
        public Def definition;
        abstract public String accept(Visitor v);
        //        public ArrayList<Def> definitions;
        // Can be a variable declaration or a function declaration
        public Decl() {
//            this.definitions = new ArrayList<Def>();
        }

        public static class VarDecl extends Decl {

            @Override
            public String accept(Visitor v) {
                return v.visit(this);
            }

            public VarDecl() {
                this.definition = new Def();
            }

//            @Override
//            public String toString() {
//                String result;
//                result = "VarDecl:\n";
//                return result + definition.toString();
//            }
        }


        // at some point, need the line in our result strings.
        // TODO: need the type in this block!
        public static class FuncDecl extends Decl {
            public String name;
            public TokenType type;
            public ArrayList<Formal> formals;
            public Statement.StatementBlock statementBlock;

            @Override
            public String accept(Visitor v) {
                return v.visit(this);
            }

            public FuncDecl() {
                this.formals = new ArrayList<Formal>();
            }

//            @Override
//            public String toString() {
//                return "FnDcl: \n" +
//                        "\t(return type) " +TypeToString.getTypeAsString(this.type) + "\n"  +
//                        "\tIdentifier: " + name + "\n" +
//                        "\t(formals) " + formals + "\n" +
//                        "statementBlock=" + statementBlock + "\t\t" +
//                        '}';
//            }
        }
    }

    public static class Formal extends ASTNode {
        public ArrayList<Def> variables;
        public Formal() {
            this.variables = new ArrayList<Def>();
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Def def : variables) {
                result.append(def.toString());
            }
            return result.toString();
        }
    }


    public static abstract class Statement extends ASTNode {
        // since statement can derive an expression. Though this is optional
        abstract public String accept(StatementVisitor v);

        public static class StatementBlock extends Statement {
            public ArrayList<Decl> variableDeclarations;
            public ArrayList<Statement> statements;

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            public StatementBlock() {
                this.variableDeclarations = new ArrayList<>();
                this.statements = new ArrayList<>();
            }

            @Override
            public String toString() {
                StringBuilder result = new StringBuilder();
                for (Decl decl : this.variableDeclarations) {
                    result.append("\t").append(decl.toString()).append("\n");
                }

                for (Statement stmt: this.statements) {
                    result.append("\t").append(stmt.toString()).append("\n");
                }

                return result.toString();
            }
        }

        public static class IfStatement extends Statement {
            public Expr ifExpression;
            public Statement statement;
            // optional
            public Expr elseExpression;

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            public IfStatement() {
            }

            @Override
            public String toString() {
                StringBuilder result = new StringBuilder();
                result.append("\t");
                result.append("(test) ")
                        .append(ifExpression.toString())
                        .append("\n")
                        .append(statement.toString());
                return result.toString();
            }
        }

        public static class WhileStatement extends Statement {
            public Expr expression;
            public Statement statement;
            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }
            public WhileStatement() {
            }

            @Override
            public String toString() {
                return "WhileStatement{" +
                        "expression=" + expression +
                        ", statement=" + statement +
                        '}';
            }
        }

        public static class ForStatement extends Statement {
            public Expr before;
            public Expr condition;
            public Expr after;
            public Statement statement;

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            public ForStatement() {
            }

            @Override
            public String toString() {
                return "ForStatement{" +
                        "before=" + before +
                        ", condition=" + condition +
                        ", after=" + after +
                        ", statement=" + statement +
                        '}';
            }
        }

        public static class ReturnStatement extends Statement {
            public Expr expression;

            public ReturnStatement() {
            }

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            @Override
            public String toString() {
                return "ReturnStatement{" +
                        "expression=" + expression +
                        '}';
            }
        }

        public static class BreakStatement extends Statement {
            public BreakStatement() {
            }
            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }
        }



//        @Override
//        public String toString() {
//            return "Statement{" +
//                    "expr=" + expr +
//                    '}';
//        }

        public static class PrintStatement extends Statement {
            public ArrayList<Expr> expressions;

            public PrintStatement() {
                this.expressions = new ArrayList<>();
            }

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            @Override
            public String toString() {
                String result = null;
                for (Expr expr : this.expressions) {
                    result += "\t" + expr.toString() + "\n";
                }
                return result;
            }
        }
        public static class ExpressionStatement extends Statement {
            public Expr expr;

            @Override
            public String accept(StatementVisitor v) {
                return v.visit(this);
            }

            public ExpressionStatement() {
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
            return  "\t\t\tType: " + TypeToString.getTypeAsString(this.type) + "\n" +
                    "\t\t\tIdentifier: " + this.ident + "\n";
        }
    }
}
