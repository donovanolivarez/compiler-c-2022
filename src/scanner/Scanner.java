package src.scanner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static src.scanner.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   T_Else);
        keywords.put("false",  T_BoolConstant);
        keywords.put("for",    T_For);
        keywords.put("fun",    FUN);
        keywords.put("if",     T_If);
        keywords.put("void",    T_Void);
        keywords.put("or",     T_Or);
        keywords.put("print",  PRINT);
        keywords.put("return", T_Return);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   T_BoolConstant);
        keywords.put("var",    VAR);
        keywords.put("while",  T_While);
        keywords.put("string", T_String);
        keywords.put("int", T_Int);
        keywords.put("double", T_Double);
        keywords.put("break", T_Break);
        keywords.put("bool", T_Bool);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? T_NotEqual : BANG);
                break;
            case '=':
                addToken(match('=') ? T_Equal : EQUAL);
                break;
            case '<':
                addToken(match('=') ? T_LessEqual : LESS);
                break;
            case '>':
                addToken(match('=') ? T_GreaterEqual : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Decaf.error(line, "Unexpected character." + c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (text.length() > 31) {
            Decaf.error(line, "Identifier too long: \"" + text + "\"");
            if (type == null) type = T_IdentLong;
            addToken(type, text);
            return;
        }

        if (type == null) type = T_Identifier;
        addToken(type);
    }

    private void number() {
        boolean hasSign = false;
        boolean isHexString = false;
        // handle int constants
        while (isDigit(peek()) || (isHexDigit(peek()) && isHexString)) {
            if (peek() == '0' && (peekNext() == 'x' || peekNext() == 'X') && (isDigit(peekAheadByTwo())) || isHexDigit(peekAheadByTwo())) isHexString = true;
            advance();
        }

        // handles double constant
        if (peek() == '.' && (isDigit(peekNext()) || peekNext() == 'e' || peekNext() == 'E')) {
            // Consume the "."
            advance();

            while (isDigit(peek()) || (peek() == 'e' || peek() == 'E' || peek() == '+' || peek() == '-')) {
                // if we find two signs, we can't consume the E
                if ((peek() == 'e' || peek() == 'E') && (peekNext() == '+' || peekNext() == '-') && (peekAheadByTwo() == '+') || peekAheadByTwo() == '-') {
                    addToken(T_DoubleConstant, Double.parseDouble(source.substring(start, current)));
                    return;
                }
                advance();
            }
            addToken(T_DoubleConstant, Double.parseDouble(source.substring(start, current)));
        } else {
            if (isHexString) addToken(T_IntConstant, Integer.decode(source.substring(start, current)));
            else addToken(T_IntConstant, Integer.parseInt(source.substring(start,current)));
        }
    }

    private boolean isHexDigit(char c) {
        return (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
    }

    private void string() {
        // string value continues..
        // isAtEnd refers to the end of the entire stream
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                Decaf.error(line, "Unterminated string");
                line++;
                return;
            }
            advance();
        }


        if (isAtEnd()) {
            Decaf.error(line, "Unterminated string");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peekAheadByTwo() {
        if (current + 2 >= source.length()) return '\0';
        return source.charAt(current + 2);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        Token token = new Token(type, text, literal, line);
        tokens.add(token);
        System.out.println(token);
    }
}
