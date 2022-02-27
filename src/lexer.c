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
int lineNum = 1, colNum = 1, characterCount = 0;
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
        long val = strtol(buffer, NULL, 10);
        printf("%-12s line %d cols %d-%d is %s (value = %-1ld)\n", buffer, lineNum, startColumn, colNum, stateType, val);
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
    colNum = 0;
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
int main(int argc, const char * argv[]) {
    file = fopen(argv[1], "r");
    // initial read
    c = fgetc(file);

    while(c != EOF) {

        while (c != '\n' && !isspace(c)) {
            characterCount++;
            // handle strings
            if (c == '"') {
                strcpy(stateType, "T_StringConstant");
                strncat(buffer, &c, 1);
                c = advance(file);
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
                while (!isspace(c) && c != '\n' && c != '"' && isOperator(c) != 0) {
                    strncat(buffer, &c, 1);
                    c = advance(file);
                    characterCount++;
                }
                if (isKeyword(buffer) == 0) {
                    toupper(buffer[0]);
                    strcpy(stateType, "T_");
                    strncat(stateType, buffer, 15);
                    tolower(buffer[0]);
                }
                endToken();
                buffer[0] = '\0';
            }

            // handle numbers
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '.') {
                characterCount++;
                state = IntConstant;
                strcpy(stateType, "T_IntConstant");

                 if (c == '.') {
                     if (strlen(buffer) == 0) {
                        strncat(buffer, &c, 1);
                        c = advance(file);
                        characterCount++;
                        state = Operator;
                     } else {
                     c = advance(file);
                     state = DoubleConstant;
                     while ((c >= '0' && c <= '9')) {
                        strncat(buffer, &c, 1);
                        c = advance(file);
                        characterCount++;
                     }
                     }
                     endToken();
                     state = IntConstant;
                     buffer[0] = '\0';
                     continue;
                 }

                int value = c - '0';
                if (value > 0) {
                    while(c >= '0' && c <= '9') {
                        strncat(buffer, &c, 1);
                        c = advance(file);
                        characterCount++;
                    }
                    // encountered a period , signifying a double
                    if ((c == '.' || c == 'e' || c == 'E') ) {
                        strncat(buffer, &c, 1);
                        c = advance(file);
                        state = DoubleConstant;
                        strcpy(stateType, "T_DoubleConstant");
                        while ((c >= '0' && c <= '9') || (c == 'e' || c == 'E') || (c == '+' || c == '-')) {
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            characterCount++;
                        }
                    }
                    endToken();
                    state = Start;
                    buffer[0] = '\0';
                    
                } else {
                    // handle hex values
                    if (c == 'x' || c == 'X') {
                        while ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            characterCount++;
                        }
                        endToken();
                        // non-hex, but begin with zero
                    } else {
                        while((c >= '0' && c <= '9')) {
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            characterCount++;
                        }
                        // encountered a period , signifying a double
                        if (c == '.' || c == 'e' || c == 'E') {
                            strncat(buffer, &c, 1);
                            c = advance(file);
                            state = DoubleConstant;
                            strcpy(stateType, "T_DoubleConstant");
                            while ((c >= '0' && c <= '9')) {
                                strncat(buffer, &c, 1);
                                c = advance(file);
                                characterCount++;
                            }
                        }
                        endToken();
                        buffer[0] = '\0';
                    }
                }
            }
            // handle operators
            if (isOperator(c) == 0 || isSpecialOperator(c) == 0) {

                if (isOperator(c) == 0) {

                } else if (isSpecialOperator(c) == 0) {

                }

                strncat(buffer, &c, 1);
                c = advance(file);
                state = Operator;

                while ((c >= '0' && c <= '9') || (c == 'e' || c == 'E') || (c == '+' || c == '-')) {
                    strncat(buffer, &c, 1);
                    c = advance(file);
                    characterCount++;
                }
                endToken();
                buffer[0] = '\0';
            }
        }
        if (c == '\n') {
            lineNum++;
        }

        buffer[0] = '\0';
        characterCount++;
        c = advance(file);


        // printf("\n******CURRENT CHAR*****: %c\n", c);
        // printf("Line Number: %d\n", lineNum);
        // if (c == '\n' && strlen(buffer) == 0) {
        //     characterCount++;
        //     lineNum++;
        //     c = fgetc(file);
        //     continue;
        // }

        // if (newlineOrSpaceEncountered(c) == 0) {
        //     colNum = 1;
        // }

        // if (c == '\n' || isspace(c)) {

        //     // if (state == StringConstant && c == '\n') {
        //     //     printf("***Error line %d\n***Unterminated String Constant: %s\n", lineNum, buffer);
        //     //     c = fgetc(file);
        //     //     characterCount++;
        //     //     resetState();
        //     //     continue;
        //     // } else if (state == StringConstant && isspace(c)) {
        //     //     strncat(buffer, &c, 1);
        //     //     c = fgetc(file);
        //     //     characterCount++;
        //     //     continue;
        //     // }

        //     if (strlen(buffer) != 0 && state != StringConstant) {

        //         if (isKeyword(buffer) == 0) {
        //             strncpy(stateType, "T_", 3);
        //             buffer[0] = toupper(buffer[0]);
        //             strncat(stateType, buffer, 15);
        //             state = Complete;
        //         }

        //         if (state == SpecialOperator && (strlen(buffer) == 1)) {
        //             // print as single operator.
        //             previousState = Operator;
        //             state = stateTable[state][Operator];
        //         } else if (state == SpecialOperator) {
        //             // print ambiguous operator with type instead of value.
        //             int index = getSpecialOpIndex(buffer);
        //             stateType[0] = '\0';
        //             strncpy(stateType, specialOpT_Ids[index], 15);
        //             state = stateTable[state][Operator];
        //         }

        //         // check if boolean constant. Maybe place in it's own function for organization.
        //         if (isBooleanConstant(buffer) == 0) {

        //         }

        //         state = Complete;

        //         if (state == Complete) {
        //             printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);
        //             resetState();
        //             c = fgetc(file);
        //             characterCount++;
        //             continue;
        //         }
        //     } else {
        //         colNum++;
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }
        // }

        // // if ((isxdigit(c) != 0) && (state == HexNumber)) {
        // //     if (stateTable[previousState][Identifer] != Complete) {
        // //         previousState = state;
        // //     }
        // //     state = stateTable[state][HexNumber];
        // //     strncat(buffer, &c, 1);
        // //     characterCount++;
        // //     c = fgetc(file);
        // //     continue;
        // // }
        // // if char is identifier (special or not special does not matter at this point)
        // if (isalpha(c) || (c == '_')) {

        //     if (state == HexNumber) {

        //     }
        //     // handle exponential
        //     if (state == DoubleConstant && isValidExponentialSymbol(c) == 0) {
        //         while(c != '\n' && !isspace(c) && notIn(c, buffer) == 0 && (c == validExponentSigns[0] || c == validExponentSigns[1]) && (c >= '0' && c <= '9')) {
        //             strncat(buffer, &c, 1);
        //             c = advance(file);
        //             characterCount++;
        //         }
        //     }

        //     if (isScientificNotation(c, state) == 0) {
        //         state = stateTable[state][DoubleConstant];
        //         strncat(buffer, &c, 1);
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }

        //     if (isHexadecimalForm(c,buffer) == 0) {
        //         state = stateTable[state][HexNumber];
        //         strncat(buffer, &c, 1);
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }
        //     savePreviousState(Identifer);
        //     state = stateTable[state][Identifer];

        //     if (state == Identifer) {

        //         savePreviousState(Identifer);
        //         state = stateTable[state][Identifer];
        //         strncpy(stateType, "T_Identifier", 13);
        //         strncat(buffer, &c, 1);
        //         colNum++;
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }
        //     if (state == StringConstant) {
        //         state = stateTable[state][Identifer];
        //         strncat(buffer, &c, 1);
        //         colNum++;
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }

        //     if (state == Complete) {
        //         if (previousState != StringConstant) {
        //             fseek(file, characterCount--, SEEK_SET);
        //         }
        //         printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);
        //         resetState();
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     } 
        // }

        // // handle numbers 
        // if (isnumber(c)) {
        //     savePreviousState(IntConstant);
        //     state = stateTable[state][IntConstant];
        //     if (state == IntConstant) {
        //         savePreviousState(IntConstant);
        //         strncat(buffer, &c, 1);
        //     } else if (state == DoubleConstant) {
        //         while(c != '\n' && !isspace(c) && (c >= '0' && c <= '9') && (c != 'E' || c != 'e')) {
        //             printf("Hello!");
        //             printf("\nCurrent char: %c\n", c);
        //             strncat(buffer, &c, 1);
        //             c = advance(file);
        //             characterCount++;
        //             printf("buffer: %s\n", buffer);
        //             continue;
        //         }
        //         savePreviousState(DoubleConstant);
        //     } else if (state == StringConstant) {
        //         savePreviousState(StringConstant);
        //         strncat(buffer, &c, 1);
        //     } else if (state == Identifer) {
        //         savePreviousState(Identifer);
        //         strncat(buffer, &c, 1);
        //     }
            // colNum++;
            // characterCount++;
            // c = advance(file);
            // continue;
            // savePreviousState(IntConstant);
            // if (state == DoubleConstant) {
            //     state = stateTable[state][DoubleConstant];
            //     characterCount++;
            // } else {
            //     state = stateTable[state][IntConstant];
            //     characterCount++;
            // }

            
            
            // if (strlen(buffer) == 0 ) {
            //     strncpy(stateType, "T_IntConstant", 13);
            // }
        //     if (state == Complete) {
        //         fseek(file, characterCount--, SEEK_SET);
        //         // output previous token, backspace operation occurs before this.
        //         printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);
        //         resetState();
        //         c = fgetc(file);
        //         characterCount++;
        //         continue;
        //     } else {
        //         previousState = state;
        //         colNum++;
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }
        // }

        // handle operators and punctuation
        // if (ispunct(c) && (c != '_')) {
        //     if (state == DoubleConstant) {
        //     }
        //     if (state == SpecialOperator) {
        //         char tempVal[8] = "";
        //         strcpy(tempVal, buffer);
        //         strncat(tempVal, &c, 1);
        //         if (isSpecialOperatorToken(tempVal) == 0) {
        //             savePreviousState(SpecialOperator);
        //             state = stateTable[state][SpecialOperator];
        //         } else {
        //             savePreviousState(SpecialOperator);
        //             state = stateTable[state][Operator];
        //         }
        //     } else {
        //         if (isSpecialOperator(c) == 0) {
        //             savePreviousState(SpecialOperator);
        //             state = stateTable[state][SpecialOperator];
        //         } else if (isDoubleForm(c, previousState) == 0) {
        //             state = stateTable[state][DoubleConstant];
        //             stateType[0] = '\0';
        //             strncpy(stateType, "T_DoubleConstant", 13);
        //             printf("DOUBLE HERE\n");
        //         } else if (isOperator(c) == 0) {
        //             savePreviousState(Operator);
        //             state = stateTable[state][Operator];
        //         } else if (c == quotation) {
        //             // this is the start of a string, which will get recognized as punctuation at first.
        //             if (state == Start || state == StringConstant) {
        //                 savePreviousState(StringConstant);
        //                 state = stateTable[state][StringConstant];
        //             } else if (state != StringConstant && state != Start) {
        //                 // output what is currently in buffer, then report the error.
        //                 printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);
        //                 printf("***Error line %d\n***Unterminated String Constant: %c\n", lineNum, c);
        //                 c = fgetc(file);
        //                 characterCount++;
        //                 resetState();
        //                 continue;
        //             }
        //             if (state == Complete) {
        //                 strncat(buffer, &c, 1);
        //             }
        //         } else if(state != StringConstant){
        //             printf("Error line %d\n*** Unrecognized char: '%c'\n",lineNum, c );
        //             c = fgetc(file);
        //             characterCount++;
        //             resetState();
        //             continue;
        //         }


        //     // if (isOperator(c) == 0) {
        //     //     if (isSpecialOperator(c) == 0) {
        //     //         if (isSpecialOperatorToken(buffer) != 0) {
        //     //             strncpy(stateType, "T_", 3);
        //     //             buffer[0] = toupper(buffer[0]);
        //     //             strncat(stateType, buffer, 15);
        //     //             state = stateTable[state][SpecialOperator];
        //     //         } else {
        //     //             savePreviousState(SpecialOperator);
        //     //             state = stateTable[state][SpecialOperator];
        //     //         }
        //     //     }
        //     // }

        //     // if (c == quotation) {
        //     //     // we have the beginning or the end of a string if we enter this block.
        //     //     if (stateTable[previousState][StringConstant] != Complete) {
        //     //         previousState = state;
        //     //     }

        //     //     if (strlen(buffer) == 0 ) {
        //     //         strncpy(stateType, "T_StringConstant", 15);
        //     //     }
        //     //     state = stateTable[state][StringConstant];

        //     //     if (strlen(buffer) > 0 && previousState == StringConstant) {
        //     //         colNum++;
        //     //         strncat(buffer, &c, 1);
        //     //     }
        //     // }
        //     }

        //     if (state == Complete) {
        //         if (previousState != StringConstant) {
        //             fseek(file, characterCount--, SEEK_SET);
        //         }
        //         printResultsByState(buffer, lineNum, startColumn, colNum, stateType, previousState);
        //         resetState();
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     } else {
        //         previousState = state;
        //         colNum++;
        //         strncat(buffer, &c, 1);
        //         characterCount++;
        //         c = fgetc(file);
        //         continue;
        //     }
        // }
    }
    return 0;
}

