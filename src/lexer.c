//
//  lexer.c
//  Compiler Project
//
//  Created by Donovan Olivarez on 2/7/22.
//

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
    default:
        break;
    }
}


int main(int argc, const char * argv[]) {
    FILE *file;
    file = fopen(argv[1], "rb");

    char buffer[50] = "";
    char c;
    int lineNum = 0, colNum = 0, characterCount = 0;
    int startColumn = 1;

    int state = Start;
    int previousState = Start;
    char stateType[15] = "";

    // initial read
    c = fgetc(file);

    while(c != EOF) {
        // encouter a line break or a space, meaning the end of a char stream
        if (c == '\n' || isspace(c)) {

            // if newline, increase line count, reset column count.
            if (c == '\n') {
                lineNum++;
            }

            // seek to previous token to ensure none are skipped.
            // fseek(file, characterCount-1, SEEK_SET);

            // if the buffer is populated and we've reached a seperator, need to identify token.
            if (strlen(buffer) != 0) {

                if (isKeyword(buffer) == 0) {
                    strncpy(stateType, "T_", 3);
                    buffer[0] = toupper(buffer[0]);
                    strncat(stateType, buffer, 15);
                    state = Complete;
                }

                if (state == SpecialOperator) {
                    int bufferSize = strlen(buffer);
                    if (bufferSize == 1) {
                        state = Rejected;
                    } else {
                        state = Complete;
                    }
                }

                // check if boolean constant. Maybe place in it's own function for organization.
                int i;
                int size = arraySize(bValues);
                for (i = 0; i < size; i++ ) {
                    if (strcmp(buffer, bValues[i]) == 0) {
                        previousState = BoolConstant;
                        state = Complete;
                        break;
                    }
                }
                state = Complete;
            }
        }

        // if char is identifier (special or not special does not matter at this point)
        if (isalpha(c) || (c == '_')) {

            if (stateTable[previousState][Operator] != Complete) {
                previousState = state;
            }
            // update the state to be an identifier state
            state = stateTable[state][Identifer];
            if (strlen(buffer) == 0 ) {
                strncpy(stateType, "T_Identifier", 13);
            }
            // printf("state is: %d\n", state);
            if (state == Complete) {
                fseek(file, characterCount--, SEEK_SET);
            } else {
                previousState = state;
                strncat(buffer, &c, 1);
            }
        }

        // handle numbers 
        if (isnumber(c)) {
            if (stateTable[previousState][Operator] != Complete) {
                previousState = state;
            }
            // update to numerical state
            state = stateTable[state][IntConstant];
            if (strlen(buffer) == 0 ) {
                strncpy(stateType, "T_IntConstant", 13);
            }
            if (state == Complete){
                fseek(file, characterCount--, SEEK_SET);
            } else {
                previousState = state;
                strncat(buffer, &c, 1);
            }
        }

        // handle operators and punctuation
        if (ispunct(c) && (c != '_')) {
            if (isOperator(c) == 0) {
                if (isSpecialOperator(c) == 0) {
                    if (isSpecialOperatorToken(buffer) != 0) {
                        fseek(file, characterCount--, SEEK_SET);
                        state = stateTable[state][Operator];
                    }
                } else {
                    if (stateTable[previousState][Operator] != Complete) {
                        previousState = state;
                    }
                    state = stateTable[state][Operator];

                    if (state == Complete) {
                        // printf("at completion, buffer is %s and previous state is %d\n", buffer, previousState);
                        fseek(file, characterCount--, SEEK_SET);
                    } else {
                        previousState = state;
                        strncat(buffer, &c, 1);
                    }
                }
            } else {
                state = Rejected;
            }
        }

        if (state == Complete || state == Rejected) {
            printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);

            // printf("%-12s line %d cols %d-%d is %s\n", buffer, lineNum, startColumn, colNum, stateType);
            state = Start;
            previousState = Start;
            colNum = 0;
            stateType[0] = '\0';
            buffer[0] = '\0';
        } else if (c != '\n' || !isspace(c)) {
            colNum++;
        }
        
        // printf("buffer is %s and previous state is %d\n", buffer, previousState);
        //advance. if EOF, output whatever is in the buffer.
        c = fgetc(file);
        if (c == EOF) {
            printf("%-12sline %d cols %d-%d is '%s'\n", buffer, lineNum, startColumn, colNum, buffer);
            buffer[0] = '\0';
        }
        characterCount++;
    }
    return 0;
}
