package src.parser.util;

import src.parser.ASTNode;

public interface StatementVisitor {
    String visit(ASTNode.Statement.StatementBlock block);
    String visit(ASTNode.Statement.IfStatement ifStatement);
    String visit(ASTNode.Statement.WhileStatement whileStatement);
    String visit(ASTNode.Statement.ForStatement forStatement);
    String visit(ASTNode.Statement.ReturnStatement returnStatement);
    String visit(ASTNode.Statement.BreakStatement breakStatement);
    String visit(ASTNode.Statement.PrintStatement printStatement);
    String visit(ASTNode.Statement.ExpressionStatement expressionStatement);
}
