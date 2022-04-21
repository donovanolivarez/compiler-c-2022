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
//            Decl funcDecl = new Decl.FuncDecl();
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
            // consume the semi colon
            if (peek().type == SEMICOLON) advance();
            varDecl = new Decl.VarDecl();
            varDecl.definition = def(t, right);

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
                token = previous();
                if (token.type == SEMICOLON) token = advance();
            }
            return block;
        }
        System.out.println("Syntax Error: Statement Block");
        return null;
    }

    private Statement stmt(Token token) {
        if (token.type == T_Return) {
            return handleReturn(token);
            // consume the return keyword
//            token = advance();
//            ReturnStatement statement = new ReturnStatement();
//            statement.expression = expression(token);
//            token = previous();
//            if (token.type == SEMICOLON) advance();
//            return statement;
        } else if (token.type == T_For) {
            ForStatement statement = new ForStatement();
            statement.type = FOR;
            // consume the for keyword, we know we have it.
            token = advance();
            // if checking for paren, if we don't have this we raise an error.
            if (token.type == LEFT_PAREN) {
                token = advance();
            } else {
                // potential error solution, extract to different function!
                // only diff here is the token, which we can pass easily.
                String errorLine = "Syntax Error: " + token.lexeme;
                // get starting index of our error token
                int startIndex = errorLine.indexOf(token.lexeme);
                System.out.println("Syntax Error: \t\t" + token.lexeme);
                StringBuilder errorTicks = new StringBuilder();
                for (int i = 0; i < errorLine.chars().count(); i++) {
                    System.out.println(i);
                    if (i != startIndex) {
                        errorTicks.append(" ");
                    } else {
                        errorTicks.append("^");
                    }
                }
                System.out.println(errorLine);
                System.out.println(errorTicks);
            }
            // before and after are optional
            // this is gross
            if (token.type == SEMICOLON) {
                statement.before = null;
                token = advance();
            } else {
                statement.before = expression(token);
                token = previous();
            }
            // not optional
            statement.condition = expression(token);
            token = previous();

            if (token.type == SEMICOLON) token = advance();

            if (token.type == SEMICOLON) {
                statement.after = null;
                token = advance();
            } else {
                statement.after = expression(token);
                token = previous();
            }

            statement.statement = stmt(token);
            token = previous();
            // should see right paren
            if (token.type == RIGHT_PAREN) token = advance();
            // recurse since for statement has a statement inside of it
            statement.statement = stmt(token);
            return statement;
        } else if (token.type == T_Break) {
            BreakStatement statement = new BreakStatement();
            statement.type = T_Break;
            if (peek().type == SEMICOLON) advance();
            return statement;
        } else if (token.type == T_While) {
            WhileStatement statement = new WhileStatement();
            token = advance();
            if (token.type == LEFT_PAREN) token = advance();
            statement.expression = expression(token);
            token = previous();
            if (token.type == RIGHT_PAREN) advance();
            statement.statement = stmt(previous());
            return statement;
        } else if (token.type == T_If) {
            IfStatement statement = new IfStatement();
            token = advance();

            if (token.type == LEFT_PAREN) token = advance();
            statement.ifExpression = expression(token);
            token = previous();

            if (token.type == RIGHT_PAREN) token = advance();
            statement.statement = stmt(token);

            token = previous();
            // check if this is a statement block, which will guide how we parse
            if (!((Statement) statement instanceof StatementBlock)) {
                if (token.type == SEMICOLON) token = advance();
                if (token.type == RIGHT_BRACE) token = advance();
            }

            if (token.type == T_Else) {
                if (peek().type == LEFT_BRACE) {
                    token = advance();
                    statement.elseExpression = expression(token);
                    token = previous();
                    if (token.type == SEMICOLON) token = advance();
                    if (token.type == RIGHT_BRACE) token = advance();
                } else {
                    token = advance();
                    statement.elseExpression = expression(token);
                    token = previous();
                }

                statement.elseExpression = expression(token);
                token = previous();
            }
            if (token.type == RIGHT_PAREN) advance();
            return statement;
            // this condition represents a statement block
        } else if (token.type == LEFT_BRACE) {
            // we have a statement block
            StatementBlock statementBlock = new StatementBlock();
            statementBlock = block(token);
            return statementBlock;
        } else if (token.type == PRINT) {
            token = advance();
            PrintStatement statement = new PrintStatement();
            while (token.type != RIGHT_PAREN) {
                if (token.type == LEFT_PAREN)
                    token = advance();
                else
                    token = advance();
                statement.expressions.add(expression(token));
                token = previous();
            }
            advance();
            return statement;
        } else {
            ExpressionStatement expressionStatement = new ExpressionStatement();
            expressionStatement.expr = assignment(token);
            return expressionStatement;
        }
    }

    private ReturnStatement handleReturn(Token token) {
        token = advance();
        ReturnStatement statement = new ReturnStatement();
        statement.expression = expression(token);
        token = previous();
        if (token.type == SEMICOLON) advance();
        return statement;
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
        } else if (token.type == LEFT_PAREN) {
            System.out.println("Do call");
            Token val = (Token) ((Expr.Literal)expr).getValue();
            Expr.Call call = new Expr.Call(val);
            token = advance();
            while(token.type != RIGHT_PAREN) {
                call.addActual(assignment(token));
                if (previous().type == COMMA) {
                    token = advance();
                } else {
                    token = previous();
                }
            }
            advance();
            expr = call;
            // call
        } else if (token.type == ReadInteger) {
            // just LValue
            // NOTE: I think this is already handled in the recursive call...
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
            token = previous();
        }
        return expr;
    }

    private Expr equality(Token token) {
        Expr expr = relational(token);
        token = previous();
        while (token.type == T_NotEqual || token.type == T_Equal) {
//            advance();
            Token operator = previous();
            Expr right = relational(advance());
            expr = new Expr.Binary(expr, operator, right);
            token = previous();
        }
        return expr;
    }

    private Expr relational(Token token) {
        // term is add and subtract
        Expr expr = term(token);
        token = previous();
        while (token.type == T_GreaterEqual || token.type == T_LessEqual || token.type == LESS || token.type == GREATER) {
//            advance();
            Token op = previous();
            Expr right = term(advance());
            expr = new Expr.Binary(expr, op, right);
            token = previous();
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
        while (token.type == STAR || token.type == SLASH || token.type == MOD) {
            Token op = previous();
            Expr right = term(advance());
            expr = new Expr.Binary(expr, op, right);
            token = previous();
        }
        return expr;
    }

    private Expr unary(Token token) {
        token = previous();
        if (token.type == BANG || token.type == MINUS) {
            Token operator = previous();
            Expr right = unary(advance());
            Expr expr = new Expr.Unary(operator, right);
            return expr;
        }

        // at this point, must be constant value
        return literal(token);
    }

    private Expr literal(Token token) {
        token = previous();
        if (token.type == T_Identifier || token.type == ReadInteger) {
            advance();
            return new Expr.Literal(token);
        }
        if (token.type == T_IntConstant || token.type == T_DoubleConstant) {
            advance();
            return new Expr.Literal(token);
        }

        if (token.type == T_BoolConstant || token.type == STRING) {
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