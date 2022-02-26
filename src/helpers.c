#include "lexer.h"

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
        long val = strtol(buffer, NULL, 10);
        printf("%-12s line %d cols %d-%d is %s (value = %-1ld)\n", buffer, lineNum, startColumn, colNum, stateType, val);
        break;
    }
    case DoubleConstant:
    case StringConstant:
    case BoolConstant:
        printf("%-12s line %d cols %d-%d is %s (value = %-1s)\n", buffer, lineNum, startColumn, colNum, stateType, buffer);
        break;
    case Operator:
    {
        printf("%-12s line %d cols %d-%d is '%s'\n", buffer, lineNum, startColumn, colNum, buffer);
        break;
    }
    case SpecialOperator:
    {
        printf("%-12s line %d cols %d-%d is '%s'\n", buffer, lineNum, startColumn, colNum, stateType);
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
    
}