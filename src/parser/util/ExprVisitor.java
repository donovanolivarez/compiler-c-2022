package src.parser.util;

import src.parser.expression.Expr;

public interface ExprVisitor {
    String visit(Expr.Binary binary);
    String visit(Expr.Unary unary);
    String visit(Expr.Assignment assignment);
    String visit(Expr.Literal literal);
    String visit(Expr.Grouping grouping);
}
