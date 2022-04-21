package src.parser.util;

import src.parser.ASTNode;
import src.parser.expression.Expr;

public interface Visitor {
    String visit(ASTNode.Decl.FuncDecl funcDecl);
    String visit(ASTNode.Decl.VarDecl varDecl);
}
