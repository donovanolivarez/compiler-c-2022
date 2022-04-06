package src.scanner;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        switch (type) {
            case T_DoubleConstant:
                return lexeme + "\tline " + line + " " + type + " (value = "+ Double.parseDouble(lexeme)+")";
            case T_IntConstant:
                return lexeme + "\tline " + line + " "+ type + " (value = "+ Integer.decode(lexeme)+")";
            case T_IdentLong:
                return lexeme + "\tline " + line + " cols " + " COL INFO HERE is T_Identifer (truncated to " + lexeme.substring(0, 31) + ")";
            case RIGHT_BRACE:
            case RIGHT_PAREN:
            case LEFT_BRACE:
            case LEFT_PAREN:
            case COMMA:
            case BANG:
            case GREATER:
            case LESS:
            case PLUS:
            case MINUS:
            case EQUAL:
            case SEMICOLON:
            case STAR:
            case SLASH:
            case DOT:
                return lexeme + "\tline " + line + " is '"+ lexeme + "'";
        }
        return lexeme + "\tline " + line + " " + type;
    }
}
