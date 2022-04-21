package src.parser.util.printers;

import src.parser.ASTNode;
import src.parser.expression.Expr;
import src.parser.util.ExprVisitor;
import src.scanner.Token;

public class ExprPrinterVisitor implements ExprVisitor {

    @Override
    public String visit(Expr.Binary binary) {
        StringBuilder builder = new StringBuilder();
        builder.append("\t").append(binary.left.accept(new ExprPrinterVisitor())).append("\n")
                .append("\t").append(binary.operator.lexeme).append("\n")
                .append("\t").append(binary.right.accept(new ExprPrinterVisitor())).append("\n");
        return builder.toString();
    }

    @Override
    public String visit(Expr.Unary unary) {
        return null;
    }

    @Override
    public String visit(Expr.Assignment assignment) {
        StringBuilder builder = new StringBuilder();
        // write some sort of block level function
        builder.append("\tAssignExpr:\n\t\tField Access:\n\t\t\t").append(assignment.left).append("\n")
                .append("\tOperator: ").append(assignment.operator.lexeme).append("\n")
                .append("\t").append(assignment.right).append("\n");
        return builder.toString();
    }

    @Override
    public String visit(Expr.Literal literal) {
        StringBuilder builder = new StringBuilder("\t");
        builder.append(literal.getValue());
        return builder.toString();
    }

    @Override
    public String visit(Expr.Grouping grouping) {
        StringBuilder builder = new StringBuilder("\t");
        builder.append(grouping);
        return builder.toString();
    }

    public String visit(Expr.Call call) {
        return null;
    }
}
