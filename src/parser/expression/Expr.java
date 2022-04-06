package src.parser.expression;

import java.util.List;
import src.scanner.Token;

public abstract class Expr {
 public static class Binary extends Expr {
  public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    final Expr left;
    final Token operator;
    final Expr right;
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

        final Token left;
        final Token operator;
        final Expr right;
    }


 public static class Unary extends Expr {
  public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    final Token operator;
    final Expr right;
  }
 public static class Literal extends Expr {
  public Literal(Object value) {
      this.value = value;
    }

     public Object getValue() {
         return value;
     }

     final Object value;
  }
 public static class Grouping extends Expr {
  public Grouping(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;
  }
}
