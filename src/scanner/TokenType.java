package src.scanner;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    T_Or, T_LessEqual,
    T_GreaterEqual, T_Equal,
    T_NotEqual,

    // Literals.
    IDENTIFIER, STRING, NUMBER, T_DoubleConstant,
    T_IntConstant, T_Null,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    T_Void, T_BoolConstant, T_While, T_String, T_If,
    T_Else, T_Return, T_Break, T_For, Print, ReadInteger, ReadLine,
    T_Int, T_Double, T_Bool,


    // parser types
    Prog, FnDecl, VarDecl, Def,
    T_IdentLong, T_Identifier, EOF
}
