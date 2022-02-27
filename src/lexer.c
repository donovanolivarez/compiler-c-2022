//
//  lexer.c
//  Compiler Project
//
//  Created by Donovan Olivarez on 2/7/22.
//

#include "lexer.h"

// globals
FILE *file;
char buffer[100] = "";
char c;
int lineNum = 1, colNum = 0, characterCount = 0;
int startColumn = 1;
int state = Start;
int previousState = Start;
char stateType[20] = "";
int seekFailureCount = 0;


int isKeyword(char* token) {
    int i;
    int size = arraySize(keywords);
    for (i = 0; i < 15; i++) {
        if (strcmp(token, keywords[i]) == 0) {
            return 0;
        }
    }
    return -1;
}

int isOperator(char c) {
    int size = arraySize(operators);
    int i;
    for (i = 0; i < size; i++) {
        if (operators[i] == c) {
            return 0;
        }
    }
    return 1;
} 

int isSpecialOperator(char c) {
    int size = arraySize(specialOperators);
    int i;
    for (i = 0; i < size; i++) {
        if (specialOperators[i] == c) {
            return 0;
        }
    }
    return 1;
}

int isSpecialOperatorToken(char* token) {
    int size = arraySize(specialOperatorTokens);
    int i;
    for (i = 0; i < size; i++) {
        if (strcmp(specialOperatorTokens[i], token) == 0) {
            return 0;
        }
    }
    return 1;
}

void printResultsByState(char* buffer, int lineNum, int startColumn, int colNum, char* stateType, int state) {
    switch (state)
    {
    case Identifer: 
    {
        printf("%-12s line %d cols %d-%d is %s\n", buffer, lineNum, startColumn, colNum, stateType);
        break;
    }
    case IntConstant: 
    {
        int val;
        char c;
        int isHex = 0;
        int i = 0;
        for (c = buffer[i]; c != '\0'; c = buffer[i]) {
            if (isxdigit(c) || c == '0' || c == 'x' || c == 'X') {
                isHex = 1;
            } else {
                isHex = 0;
            }
            i++;
        }
        if (isHex == 1) {
            val = strtol(buffer, NULL, 16);
            printf("%-12s line %d cols %d-%d is %s (value = %-1d)\n", buffer, lineNum, startColumn, colNum, stateType, val);
        } else {
            val = strtol(buffer, NULL, 10);
            printf("%-12s line %d cols %d-%d is %s (value = %-1d)\n", buffer, lineNum, startColumn, colNum, stateType, val);
        }
        break;
    }
    case DoubleConstant:
    {
        float val = strtof(buffer, NULL);
        printf("%-12s line %d cols %d-%d is %s (value = %-.2f)\n", buffer, lineNum, startColumn, colNum, stateType, val);
        break;
    }
    case StringConstant:
    case BoolConstant:
    {
        printf("%-12s line %d cols %d-%d is %s (value = %-1s)\n", buffer, lineNum, startColumn, colNum, stateType, buffer);
        break;
    }
    case Operator:
    {
        printf("%-12s line %d cols %d-%d is '%s'\n", buffer, lineNum, startColumn, colNum, buffer);
        break;
    }
    case SpecialOperator:
    {
        printf("%-12s line %d cols %d-%d is %s\n", buffer, lineNum, startColumn, colNum, stateType);
        break;
    }
    default:
        break;
    }
}

int bufferIsEmpty(char* buffer) {
    if (strlen(buffer) == 0) {
        return 0;
    }
    return -1;
}

void savePreviousState(int targetState) {
    // if the next state we want to traverse to does not yield complete, we want to keep track of the state of the previous token.
    if (stateTable[previousState][targetState] != Complete) {
        previousState = state;
    }
}

int checkForStringError(int state, char c) {
    if (state == StringConstant && isspace(c)) {
                strncat(buffer, &c, 1);
            }
    if (previousState == StringConstant && c == '\n') {
        state = Rejected;
        printf("ERROR 1 \n");
        state = Start;
        previousState = Start;
        colNum = 0;
        stateType[0] = '\0';
        buffer[0] = '\0';
        c = fgetc(file);
        characterCount++;
        return -1;
    }
    return 0; // no errors
}

void resetState() {
    state = Start;
    previousState = Start;
    colNum = 1;
    stateType[0] = '\0';
    buffer[0] = '\0';
}

int isScientificNotation(char c, int state) {
    if ((c == 'E' || c == 'e') && state == DoubleConstant) {
        return 0; 
    }
    return -1;
}

int isHexadecimalForm(char c,char* buffer) {
    if (buffer[0] == '0' && (c == 'x' || c == 'X')) {
        return 0;
    }
    return -1;
}

int isDoubleForm(char c, int previousState) {
    if (previousState == IntConstant && c == '.') {
        return 0;
    } 
    return -1;
}

char advance(FILE *file) {
    char c = fgetc(file);
    return c;
}

int newlineOrSpaceEncountered(char c) {
    if (c == '\n' || isspace(c)) {
        return 0;
    }
    return -1;
}

int isBooleanConstant(char* buffer) {
    int i;
    int size = arraySize(bValues);
    for (i = 0; i < size; i++ ) {
        if (strcmp(buffer, bValues[i]) == 0) {
            previousState = BoolConstant;
            state = Complete;
            return 0;
            break;
        }
    }
    return -1;
}

int getSpecialOpIndex(char* buffer) {
    int i;
    int arraySize = arraySize(specialOperatorTokens);
    for (i = 0; i < arraySize; i++) {
        if (strcmp(buffer, specialOperatorTokens[i]) == 0)
            return i;
    }
    return -1;
}

int isValidExponentialSymbol(char c) {
    int i;
    int size = arraySize(validExponentialSymbols);
    for (i = 0; i <size; i++) {
        if (c == validExponentialSymbols[i]) {
            return 0;
        }
    }
    return -1;
}

int notIn(char c, char* str) {
    int size = strlen(str);
    int i = 0;
    for (; i < size; i++) {
        if (c == str[i]) return -1;
    }
    return 0;
}

void endToken() {
    printResultsByState(buffer, lineNum, startColumn, colNum, stateType, state);
}

char fpeek(FILE * const fp)
{
  const int c = getc(fp);
  return c == EOF ? EOF : ungetc(c, fp);
}
int main(int argc, const char * argv[]) {
    file = fopen(argv[1], "r");
    // initial read
    c = fgetc(file);

    while(c != EOF) {

        if (isOperator(c) != 0 && isSpecialOperator(c) != 0 && !isalpha(c) && !isdigit(c) && c != '\n' && !isspace(c) && c != quotation) {
            printf("\n\n***Error line %d\n***Unrecognized char '%c'\n", lineNum, c);
            c =advance(file);
            continue;
        }

        while (c != '\n' && !isspace(c) && c != EOF) {
            // handle strings
            if (c == '"') {
                strcpy(stateType, "T_StringConstant");
                strncat(buffer, &c, 1);
                c = advance(file);
                colNum++;
                state = StringConstant;
                // go through string elements
                while(c != '\n' && c != '"') {
                    strncat(buffer, &c, 1);
                    c = advance(file);
                    characterCount++;
                }
                // error
                if (c == '\n') {
                    printf("Error line %d\n*** Unterminated string constant: %s\n", lineNum, buffer);
                    buffer[0] = '\0';
                    state = Complete;
                    break;
                }
                strncat(buffer, &c, 1);
                endToken();
                buffer[0] = '\0';
                state = Complete;
                break;
            }

            // handle IDs
            if (( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') && state != IntConstant) {
                characterCount++;
                state = Identifer;
                strcpy(stateType, "T_Identifier");
                while ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' || c == '_') || (c >= '0' && c <= '9') && isOperator(c) != 0) {
                    strncat(buffer, &c, 1);
                    c = advance(file);
                    colNum++;
                }

                if (strlen(buffer) > 31) {
                        printf("***Error line %d\n*** Identifier too long: \"%s\"\n", lineNum, buffer);
                        c = advance(file);
                }
                if (isKeyword(buffer) == 0) {
                    char output[20] = "";
                    strcpy(output, buffer);
                    output[0] = toupper(output[0]);

                    strcpy(stateType, "T_");
                    strncat(stateType, output, 15);
                }

                if (isBooleanConstant(buffer) == 0) {
                    state = BoolConstant;
                    strcpy(stateType, "T_BoolConstant");
                }
                if (strlen(buffer) != 0) {
                    endToken(); 
                    state = Start;
                    buffer[0] = '\0';
                }
            }

            // handle numbers
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                state = IntConstant;
                strcpy(stateType, "T_IntConstant");

                int value = c - '0';
                if (value > 0) {
                    while(c >= '0' && c <= '9') {
                        strncat(buffer, &c, 1);
                        c = advance(file);
                        colNum++;
                    }
                    if (strlen(buffer) > 0 && c == '\n' && isspace(c) && (c != '.' || c != 'e' || c!='E')) {
                        endToken();
                        buffer[0] = '\0';
                        state = Start;
                    } else {
                        // handle doubles
                        if (c == '.') {
                            strcpy(stateType, "T_DoubleConstant");
                            state = DoubleConstant;
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            colNum++;

                            while ((c >= '0' && c <= '9') || (c == 'e' || c == 'E') || (c == '+' || c == '-')) {

                            if (c == 'e' || c == 'E') {
                                int failCount = 1;
                                char temp[5] = "";
                                strncat(temp, &c, 1);
                                if (fpeek(file) == '+' || fpeek(file) == '-' || (fpeek(file) >= '0' && fpeek(file) <= '9')) {
                                    c = advance(file);
                                    colNum++;
                                    strncat(temp, &c, 1);
                                    failCount++;
                                    // valid exponent
                                    if (fpeek(file) >= '0' && fpeek(file) <= '9') {
                                        strncat(buffer, temp, 5);
                                        // strncat(buffer, &c, 1);
                                        c = advance(file);
                                        colNum++;
                                        while (c >= '0' && c <= '9') {
                                            strncat(buffer, &c, 1);
                                            c = advance(file);
                                            colNum++;
                                        }
                                        break;
                                    } else {
                                        //invalid exponent
                                        fseek(file, -failCount, SEEK_CUR);
                                        c = advance(file);
                                        colNum = colNum - failCount;
                                        startColumn = colNum;
                                        break;
                                    }
                                }
                            }
                                strncat(buffer, &c, 1);
                                c = advance(file);
                                colNum++;
                            }
                        } 
                        endToken();
                        buffer[0] = '\0';
                        state = Start;
                    }
                } else {
                    // current char is 0.
                    char temp[3] = "";
                    int failCount = 1;
                    // handle hex values
                    if (fpeek(file) == 'x' || fpeek(file) == 'X') {
                        strncat(temp, &c, 1);
                        c = advance(file);
                        colNum++;
                        strncat(temp, &c, 1);
                        if ( !isxdigit(fpeek(file))) {
                            fseek(file, -failCount, SEEK_CUR);
                            endToken();
                            buffer[0] = '\0';
                            state = Start;
                        } else {
                            strcpy(buffer, temp);
                            c = advance(file);
                            colNum++;
                            while ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                                printf("buffer: %s\n", buffer);
                                strncat(buffer, &c, 1);
                                c = advance(file);
                                colNum++;
                            }
                        }
                        endToken();
                        buffer[0] = '\0';
                        state = Start;
                        // non-hex, but begin with zero
                    } else {
                        while((c >= '0' && c <= '9')) {
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            colNum++;
                        }

                        if (strlen(buffer) > 0 && c == '\n' && isspace(c) && (c != '.' || c != 'e' || c!='E')) {
                            endToken();
                            buffer[0] = '\0';
                            state = Start;
                        } else {
                        // encountered a period , signifying a double
                        if (c == '.') {
                            strcpy(stateType, "T_DoubleConstant");
                            state = DoubleConstant;
                            strncat(buffer, &c, 1);
                            c = advance(file);

                            while ((c >= '0' && c <= '9') || (c == 'e' || c == 'E') || (c == '+' || c == '-')) {

                            if (c == 'e' || c == 'E') {
                                int failCount = 1;
                                char temp[5] = "";
                                strncat(temp, &c, 1);
                                if (fpeek(file) == '+' || fpeek(file) == '-' || (fpeek(file) >= '0' && fpeek(file) <= '9')) {
                                    c = advance(file);
                                    strncat(temp, &c, 1);
                                    failCount++;
                                    // valid exponent
                                    if (fpeek(file) >= '0' && fpeek(file) <= '9') {
                                        strncat(buffer, temp, 5);
                                        // strncat(buffer, &c, 1);
                                        c = advance(file);
                                        while (c >= '0' && c <= '9') {
                                            strncat(buffer, &c, 1);
                                            c = advance(file);
                                        }
                                        break;
                                    } else {
                                        //in valid exponent
                                        fseek(file, -failCount, SEEK_CUR);
                                        c = advance(file);
                                        colNum = colNum - failCount;
                                        startColumn = colNum;
                                        break;
                                    }
                                }
                            }
                                strncat(buffer, &c, 1);
                                c = advance(file);
                                colNum++;
                            }
                        } 
                            endToken();
                            buffer[0] = '\0';
                            state = Start;
                        }
                    }
                }
            }
            // handle operators
            if (isOperator(c) == 0 || isSpecialOperator(c) == 0) {
                state = Operator;
                if (isOperator(c) == 0) {
                    while(isOperator(c) == 0) {
                        state = Operator;
                        // comments
                        if (c == '/') {
                            char peek = fpeek(file);
                            if (peek == '/') {
                                // single line
                                char line[100] = "";
                                size_t len = 0;
                                fgets (line, sizeof(line), file);
                                lineNum++;
                                c = advance(file);
                                colNum =1;
                                startColumn = colNum;
                                continue;
                            } else if (peek == '*') {
                                /*
                                multi-line
                                */
                                char line[100] = "";
                                while(fgets(line, sizeof(line), file)) {
                                    char temp;
                                    int size = strlen(line);
                                    lineNum++;
                                    if (fpeek(file) == '*' || (line[size-3] == '*' && line[size-2] == '/')) {
                                        break;
                                    }
                                }
                                c = advance(file);
                                colNum = 1;
                                startColumn = colNum;
                                continue;
                            }
                        } else if (c == '*') {
                            char peek = fpeek(file);
                            if (peek == '/') {
                                c = advance(file);
                                colNum++;
                                break;
                                // multiline comments stop
                            }
                        } else {
                            strncat(buffer, &c, 1);
                            characterCount++;  
                        }
                        if (strlen(buffer) > 0) {
                            endToken();
                            state = Start;
                            buffer[0] = '\0';
                        }
                        c = advance(file);
                    }
                } else if (isSpecialOperator(c) == 0) {
                    while(isSpecialOperator(c) == 0) {
                        if (c == '=' || c == '!' || c == '<' || c == '>')  {
                            state = Operator;
                            char peek;
                            char temp[3] = "";
                            peek = fpeek(file);
                            if (peek == '=') {
                                strncat(buffer, &c, 1);
                                c = advance(file);
                                strncat(buffer, &c, 1);
                                state = SpecialOperator;
                                if (strcmp(buffer, "==") == 0) {
                                    strcpy(stateType, "T_Equal");
                                } else if (strcmp(buffer, "!=") == 0) {
                                    strcpy(stateType, "T_NotEqual");
                                } else if (strcmp(buffer, "<=") == 0) {
                                    strcpy(stateType, "T_LessEqual");
                                }  else if (strcmp(buffer, ">=") == 0) {
                                    strcpy(stateType, "T_GreaterEqual");
                                }

                                endToken();
                                buffer[0] = '\0';
                            } else {
                                state = Operator;
                                strncat(buffer, &c, 1);
                                endToken();
                                buffer[0] = '\0';
                            }
                        } else if (c == '|' || c == '&') {

                        }
                        c = advance(file);
                        characterCount++;   
                    }
                }
                if (strlen(buffer) > 0) {
                    endToken();
                    state = Start;
                    buffer[0] = '\0';
                }
            }
            startColumn = colNum;
        }

        if (c == '\n') {
            lineNum++;
        }
        state = Start;
        buffer[0] = '\0';
        characterCount++;
        c = advance(file);
        colNum = 1;
    }
    return 0;
}

