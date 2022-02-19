//
//  main.cpp
//  CrashCourse
//
//  Created by Donovan Olivarez on 2/7/22.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

// keyword table -- 2D Array
char keywords[32][12] = {
    "void", "int", "double", "bool", "string",
    "null", "for", "while", "if", "else",
    "return", "break","Print","ReadInteger","ReadLine"
};

char *bValues[] = {"true", "false"};


// reads input from input.frag line by line
int main(int argc, const char * argv[]) {
    FILE *file;
    char buffer[50] = "";
    // take in file as argument to execution. Need to loop through all files in exec.sh
    file = fopen(argv[1], "r");
    char c;
    int lineNum = 1, colNum = 1, characterCount = 0;

    // initial read
    c = fgetc(file);
    while(c != EOF) {
        // encouter a line break
        if (c == '\n' || isspace(c)) {
            if (c == '\n') {
                lineNum++;
            }
            colNum = 0;
            fseek(file, characterCount, SEEK_SET);
            if (strlen(buffer)!= 0) {
                printf("\n**** TOKEN SHOULD BE CREATED ****\n");
                buffer[0] = '\0';
            }
        }

        // if char is identifier (special or not special does not matter at this point)
        if (isalpha(c)) {
            // update the state to be an identifier state
            printf("%c is definitely a letter.\n" , c);
            strncat(buffer, &c, 1);
            printf("The string buffer is currently: %s\n\n", buffer);
        }
        if (isnumber(c)) {
            // update to numerical state
            printf("%c is definitely a number.\n" , c);
        }
        

        colNum++;
        
        //advance
        c = fgetc(file);
        characterCount++;
    }
    return 0;
}
