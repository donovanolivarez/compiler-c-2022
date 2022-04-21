package src.parser.expression;

import java.util.ArrayList;

import src.parser.util.StatementVisitor;
import src.parser.util.printers.ExprPrinterVisitor;
import src.scanner.Token;
import src.scanner.TokenType;

public abstract class Expr {
    abstract public String accept(ExprPrinterVisitor v);
 public static class Binary extends Expr {
  public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

     @Override
     public String toString() {
         return "\tBinary{" +
                 "\t\tleft=" + left + "\n" +
                 "\t\toperator=" + operator + "\n" +
                 "\t\tright=" + right + "\n" +
                 '}';
     }

     public final Expr left;
  public final Token operator;
    public final Expr right;

     @Override
     public String accept(ExprPrinterVisitor v) {
         return v.visit(this);
     }
 }

    public static class Assignment extends Expr {
        public Assignment(Token left, Token operator, Expr right) {
            // left should be identifier
            this.left = left;
            // should be equals sign
            this.operator = operator;
            // Will be expressions
            this.right = right;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("\tAssignExpr:")
                    .append(this.left.lexeme).append("")
                    .append(this.operator.toString())
                    .append(this.right.toString());
            return result.toString();
        }


        public final Token left;
        public final Token operator;
        public final Expr right;

        @Override
        public String accept(ExprPrinterVisitor v) {
            return v.visit(this);
        }
    }


 public static class Unary extends Expr {
  public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

     @Override
     public String toString() {
         return "Unary{\n" +
                 "operator=" + operator + "\n" +
                 ", right=" + right + "\n" +
                 '}';
     }

     public final Token operator;
    public final Expr right;

     @Override
     public String accept(ExprPrinterVisitor v) {
         return v.visit(this);
     }
 }
 public static class Literal extends Expr {
  public Literal(Object value) {
      this.value = value;
    }

     public Object getValue() {
         return value;
     }

     @Override
     public String toString() {
         return "Literal{" +
                 "value=" + value +
                 '}';
     }

     public final Object value;

     @Override
     public String accept(ExprPrinterVisitor v) {
         return v.visit(this);
     }
 }
 public static class Grouping extends Expr {
  public Grouping(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;

     @Override
     public String accept(ExprPrinterVisitor v) {
         return v.visit(this);
     }
 }

    public static class Call extends Expr {
        public Call(Token identifier) {
            this.identifier = identifier;
            this.actuals = new ArrayList<>();
        }

        public ArrayList<Expr> getActuals() {
            return actuals;
        }

        public void addActual(Expr expr) {
            this.actuals.add(expr);
        }

        @Override
        public String toString() {
            String result = null;
            result += "\tCall\n\t" + this.identifier;
            for (Expr expr : this.actuals) {
                result += "\t" + expr.toString() + "\n";
            }
            return result;
        }

        public final ArrayList<Expr> actuals;
        public final Token identifier;

        @Override
        public String accept(ExprPrinterVisitor v) {
            return v.visit(this);
        }
    }
}


