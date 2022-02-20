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

// reads input from input.frag line by line
int main(int argc, const char * argv[]) {
    FILE *file;
    char buffer[200] = "";
    // take in file as argument to execution. Need to loop through all files in exec.sh
    file = fopen(argv[1], "r");
    char c;
    int lineNum = 1, colNum = 1, characterCount = 0;

    int state = Start;

    // initial read
    c = fgetc(file);

    while(c != EOF) {
        // encouter a line break or a space, meaning the end of a char stream
        if (c == '\n' || isspace(c)) {

            if (c == '\n') {
                lineNum++;
            }
            colNum = 0;
            fseek(file, characterCount, SEEK_SET);
            if (strlen(buffer) != 0) {
                // check for a reserved word
                // checkForBoolConstant()
                // checkForReserve()

                if (isKeyword(buffer) == 0) {
                    state = Complete;
                    printf("State is: %d\n", state);
                }
                int i;
                int size = arraySize(bValues);
                for (i = 0; i < size; i++ ) {
                    if (strcmp(buffer, bValues[i]) == 0) {
                        state = BoolConstant;
                        break;
                    }
                }
                state = Complete;
                // buffer[0] = '\0';
            }
        }

        // if char is identifier (special or not special does not matter at this point)
        if (isalpha(c)) {
            // update the state to be an identifier state
            state = stateTable[state][Identifer];
            strncat(buffer, &c, 1);
        }
        if (isnumber(c)) {
            // update to numerical state
            state = stateTable[state][IntConstant];
            strncat(buffer, &c, 1);
        }

        if (state == Complete) {
            printf("token %s is complete\n", buffer);
            printf("State is: %d\n", state);
            state = Start;
            colNum = 1;
            buffer[0] = '\0';
        } else {
            colNum++;
        }
        
        //advance if possible
        c = fgetc(file);
        characterCount++;
    }
    return 0;
}
