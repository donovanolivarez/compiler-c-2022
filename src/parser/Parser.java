package src.parser;


import src.parser.expression.Expr;
import src.scanner.Token;
import src.scanner.TokenType;
import src.parser.ASTNode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static src.scanner.TokenType.*;
import static src.parser.ASTNode.Statement;
import static src.parser.ASTNode.Statement.*;


public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private List<TokenType> types = new ArrayList<TokenType>(Arrays.asList(T_Int, T_String, T_Double, T_Bool));

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse(List<Token> tokens) {
        return prog(tokens);
    }

    public ASTNode prog(List<Token> tokens) {
        Token currentToken;
        Prog prog = new Prog();

        while(!isAtEnd()) {
            // current will equal 1 at the start after advance.
            // advance returns the PREVIOUS token.
            currentToken = advance();
            prog.declarations.add(decl(currentToken));
        }
        prog.toString();
            return prog;
    }

    private Decl decl(Token t) {
        // will point to a decl object, could be VarDecl or FuncDecl
        Token name = null;
        Decl decl = null;
        t = previous();

        if ((t.type == T_Void || t.type == T_Int || t.type == T_String || t.type == T_Double) && peekBy(1).type == LEFT_PAREN) {
            Decl.FuncDecl funcDecl = new Decl.FuncDecl();
             funcDecl.type = t.type;
            // consume the identifier, which is the functions name
            if (peek().type == T_Identifier) {
                name = advance();
            }
            // consume the semicolon
            if (peek().type == LEFT_PAREN) advance();

            // construct the functional decl
            funcDecl.name = name.lexeme;
            funcDecl.formals.add(formal(advance()));
            funcDecl.statementBlock = block(advance());
            return funcDecl;
        }
        if ((t.type == T_Int || t.type == T_Double || t.type == T_String || t.type == T_Bool) && peekBy(1).type == SEMICOLON){
            Decl.VarDecl varDecl;
            Token right = advance();
            if (peek().type == SEMICOLON) advance();
            varDecl = new Decl.VarDecl();
            varDecl.definition = def(t, right);
//            decl.definitions.addAll(decl(t).getDefinitions())
            return varDecl;
        }
        return decl;
    }

    private StatementBlock block(Token token) {
        StatementBlock block;
        if (token.type == LEFT_BRACE) {
            block = new StatementBlock();
            token = advance();
            while (token.type != RIGHT_BRACE) {
                while (types.contains(token.type)) {
                    block.variableDeclarations.add(decl(token));
                    token = advance();
                }
                block.statements.add(stmt(token));
//                if (token.type == SEMICOLON) token = advance();
                token = previous();
                if (token.type == SEMICOLON) token = advance();
            }
            return block;
        }
        return null;
    }

    private Statement stmt(Token token) {
        if (token.type == T_Return) {
            // consume the return keyword
            token = advance();
            ReturnStatement statement = new ReturnStatement();
            statement.expression = expression(token);
            token = previous();
            if (token.type == SEMICOLON) advance();
            return statement;
        } else if (token.type == T_For) {
            ForStatement statement = new ForStatement();
            token = advance();
            if (peek().type == LEFT_PAREN) token = advance();
            // before and after are optional
            statement.before = expression(token);
            statement.condition = expression(token);
            statement.after = expression(token);
            statement.statement = stmt(token);

            // should see right paren
            if (peek().type == RIGHT_PAREN) token = advance();
            // recurse since for statement has a statement inside of it
            statement.statement = stmt(token);
            return statement;
        } else if (token.type == T_Break) {
            BreakStatement statement = new BreakStatement();
            // todo: set this on instantiation
            statement.type = T_Break;
            if (peek().type == SEMICOLON) advance();
            return statement;
        } else if (token.type == T_While) {
            WhileStatement statement = new WhileStatement();
            token = advance();
            statement.expression = expression(token);
            if (token.type == RIGHT_PAREN) advance();
            statement.statement = stmt(previous());
            return statement;
        } else {
            Statement statement = new Statement();
            statement.expr = assignment(token);
            return statement;
        }
    }

    private Formal formal(Token token) {
        Formal formal = new Formal();
        while (types.contains(token.type)) {
            // formals have one or more variables, separated by commas potentially. Add variable definition if
            // we encounter a type.
            Token right = advance();
            if (peek().type == COMMA) advance();
            formal.variables.add(def(token, right));
            token = advance();
        }
        return formal;
    }

    private Def def(Token previous, Token right) {
        return new Def(previous.type, right.lexeme);
    }

    private ASTNode var(Token t, TokenType type) {
        if (type == VarDecl)  {
            Def def = new Def(t.type, t.literal);
            return def;
        } else {
            return null;
        }
    }

    private Expr expression(Token token) {
        return assignment(token);
    }

    private Expr assignment(Token token) {
        // logicalOR
        Expr expr = logicalOr(token);
        token = previous();
        // we have assignment
        if (token.type == EQUAL) {
            Token operator = previous();
            token = advance();
            Expr assignmentValue = assignment(token);
            // consume equals sign
            if (expr instanceof Expr.Literal) {
                Token val = (Token) ((Expr.Literal)expr).getValue();
                return new Expr.Assignment( val, operator, assignmentValue);
            }
        } else if (token.type == T_Identifier && peek().type == LEFT_PAREN) {
            // call
        } else if (token.type == T_Identifier) {
            // just LValue
        }
        return expr;
    }

    private Expr logicalOr(Token token) {
        Expr expr = logicalAnd(token);
        token = previous();
        if (token.type == T_Or) {
            advance();
            Token operator = previous();
            Expr right = logicalOr(advance());
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr logicalAnd(Token token) {
        Expr expr = equality(token);
        // now do assignment, which is a binary expression!
        token = previous();
        while (token.type == AND) {
            advance();
            Token operator = previous();
            Expr right = logicalOr(advance());
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr equality(Token token) {
        Expr expr = relational(token);
        token = previous();
        while (token.type == T_NotEqual || token.type == T_Equal) {
            advance();
            Token operator = previous();
            Expr right = relational(advance());
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr relational(Token token) {
        // term is add and subtract
        Expr expr = term(token);
        token = previous();
        while (token.type == T_GreaterEqual || token.type == T_LessEqual || token.type == LESS || token.type == GREATER) {
            advance();
            Token op = previous();
            Expr right = term(advance());
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr term(Token token) {
        // multiply, divide, modulo
        Expr expr = factor(token);
        token = previous();
        while (token.type == PLUS || token.type == MINUS) {
            Token op = previous();
            Expr right = term(advance());
            expr = new Expr.Binary(expr, op, right);
            token = previous();
        }
        return expr;
    }

    private Expr factor(Token token) {
        // unary is single operator minus and logical not
        Expr expr = unary(token);
        token = previous();
        while (token.type == STAR || token.type == SLASH) {
            Token op = previous();
            Expr right = term(advance());
            expr = new Expr.Binary(expr, op, right);
            token = previous();
        }
        return expr;
    }

    private Expr unary(Token token) {
        token = previous();
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary(token);
            return new Expr.Unary(operator, right);
        }

        // at this point, must be constant value
        return literal(token);
    }

    private Expr literal(Token token) {
        token = previous();
        if (token.type == T_Identifier) {
            advance();
            return new Expr.Literal(token);
        }
        if (token.type == T_IntConstant || token.type == T_DoubleConstant) {
            advance();
            return new Expr.Literal(token);
        }

        if(token.type == LEFT_PAREN) {
            token = advance();
            Expr expr = expression(token);
            token = previous();
            if (token.type != RIGHT_PAREN) {
                System.out.println("Error: Expected right paren for expression group");
            }
            advance();
            return new Expr.Grouping(expr);
        }
        return null;
    }

//
//    private Expr expression() {
//        return equality();
//    }
//
//    private Expr equality() {
//        Expr expr = comparison();
//
//        while(match(T_NotEqual, T_Equal)) {
//            Token operator = previous();
//            Expr right = comparison();
//            expr = new Expr.Binary(expr, operator, right);
//        }
//        return expr;
//    }
//
//    private Expr comparison() {
//        Expr expr = term();
//
//        while (match(GREATER, T_GreaterEqual, LESS, T_LessEqual)) {
//            Token operator = previous();
//            Expr right = term();
//            expr = new Expr.Binary(expr, operator, right);
//        }
//
//        return expr;
//    }
//
//    private Expr term() {
//        Expr expr = factor();
//
//        // if match is true, then we advance to the next token.
//        while (match(MINUS, PLUS)) {
//            Token operator = previous();
//            Expr right = factor();
//            expr = new Expr.Binary(expr, operator, right);
//        }
//
//        return expr;
//    }
//
//    private Expr factor() {
//        Expr expr = unary();
//
//        while (match(SLASH, STAR)) {
//            Token operator = previous();
//            Expr right = unary();
//            expr = new Expr.Binary(expr, operator, right);
//        }
//
//        return expr;
//    }
//
//    private Expr unary() {
//        if (match(BANG, MINUS)) {
//            Token operator = previous();
//            Expr right = unary();
//            return new Expr.Unary(operator, right);
//        }
//
//        return primary();
//    }
//
//    private Expr primary() {
//        if (match(T_BoolConstant)) return new Expr.Literal(false);
//        if (match(TRUE)) return new Expr.Literal(true);
//        if (match(NIL)) return new Expr.Literal(null);
//
//        if (match(T_IntConstant, STRING)) {
//            return new Expr.Literal(previous().literal);
//        }
//
//        if (match(LEFT_PAREN)) {
//            Expr expr = expression();
//            consume(RIGHT_PAREN, "Expect ')' after expression.");
//            return new Expr.Grouping(expr);
//        }
//    }


    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekBy(int lookahead) {
        return tokens.get(current + lookahead);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}