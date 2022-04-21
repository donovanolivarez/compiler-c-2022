package src.parser.util.printers;

import src.parser.ASTNode;
import src.parser.util.TypeToString;
import src.parser.util.Visitor;

public class DeclPrinterVisitor implements Visitor {

    public DeclPrinterVisitor() {
    }

    @Override
    public String visit(ASTNode.Decl.FuncDecl funcDecl) {
        return "\tFnDcl: \n" +
                "\t\t(return type) " + TypeToString.getTypeAsString(funcDecl.type) + "\n"  +
                "\t\tIdentifier: " + funcDecl.name + "\n" +
                "\t\t(formals) " + funcDecl.formals + "\n" +
                "\t\t(body) StmtBlock:\n" + funcDecl.statementBlock.accept(new StatementPrinterVisitor());
    }

    @Override
    public String visit(ASTNode.Decl.VarDecl varDecl) {
        return "\t" + "VarDecl:\n" + varDecl.definition.toString();
    }
}
