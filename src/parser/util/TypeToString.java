package src.parser.util;

import src.scanner.TokenType;
import static src.scanner.TokenType.*;

//import static src.scanner.TokenType.T_Int;

public class TypeToString {
    public static String getTypeAsString(TokenType type) {
        switch(type) {
            case T_Int: return "int";
            case T_Double: return "double";
            case T_String: return "String";
            case T_Bool: return "boolean";
            case T_Void: return "void";
        }
        return null;
    }
}
