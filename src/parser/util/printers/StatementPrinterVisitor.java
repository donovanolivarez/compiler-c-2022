package src.parser.util.printers;

import src.parser.ASTNode;
import src.parser.expression.Expr;
import src.parser.util.StatementVisitor;

public class StatementPrinterVisitor implements StatementVisitor {

    public StatementPrinterVisitor() {
    }

    @Override
    public String visit(ASTNode.Statement.StatementBlock block) {
        StringBuilder result = new StringBuilder("\t");
        DeclPrinterVisitor visitor = new DeclPrinterVisitor();
        for (ASTNode.Decl decl : block.variableDeclarations) {
            result.append("\t").append(decl.accept(visitor));
        }
        StatementPrinterVisitor statementPrinterVisitor = new StatementPrinterVisitor();
        for (ASTNode.Statement stmt: block.statements) {
            result.append("\t").append(stmt.accept(statementPrinterVisitor));
        }

        return result.toString();
    }

    @Override
    public String visit(ASTNode.Statement.IfStatement ifStatement) {
        String builder = "\t" + "\t" +
                "IfStmt:\n" +
                "\t" + ifStatement.statement.accept(new StatementPrinterVisitor()) +
                ifStatement.ifExpression.accept(new ExprPrinterVisitor());
//                .append(ifStatement.elseExpression.accept(new ExprPrinterVisitor()));
        return builder;
    }

    @Override
    public String visit(ASTNode.Statement.WhileStatement whileStatement) {
        String builder = "\t" +
                "WhileStmt:\n" +
                "\t" + whileStatement.expression.accept(new ExprPrinterVisitor()) +
                whileStatement.statement.accept(new StatementPrinterVisitor());
        return builder;
    }

    @Override
    public String visit(ASTNode.Statement.ForStatement forStatement) {
        StringBuilder builder = new StringBuilder("\t");
        if (forStatement.before != null) {
            builder.append(forStatement.before.accept(new ExprPrinterVisitor()))
                    .append(forStatement.statement.accept(new StatementPrinterVisitor()))
                    .append(forStatement.after.accept(new ExprPrinterVisitor()));
            return builder.toString();
        }
        builder.append(forStatement.statement.accept(new StatementPrinterVisitor()))
                .append(forStatement.after.accept(new ExprPrinterVisitor()));
        return builder.toString();
    }

    @Override
    public String visit(ASTNode.Statement.ReturnStatement returnStatement) {
        StringBuilder builder = new StringBuilder("\t");
        if (returnStatement.expression != null) {
            builder.append(returnStatement.expression.accept(new ExprPrinterVisitor()));
        } else {
            builder.append(returnStatement);
        }
        return builder.toString();
    }

    @Override
    public String visit(ASTNode.Statement.BreakStatement breakStatement) {
        StringBuilder builder = new StringBuilder("\t");
        builder.append(breakStatement);
        return builder.toString();
    }

    @Override
    public String visit(ASTNode.Statement.PrintStatement printStatement) {
        StringBuilder builder = new StringBuilder("\t");
        for (Expr expr : printStatement.expressions) {
            builder.append(expr.accept(new ExprPrinterVisitor())).append("\n");
        }
        return builder.toString();
    }

    @Override
    public String visit(ASTNode.Statement.ExpressionStatement expressionStatement) {
        return expressionStatement.expr.accept(new ExprPrinterVisitor());
    }
}
