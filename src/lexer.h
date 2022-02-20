#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define arraySize(x) ( sizeof(x) / sizeof((x)[0]) );

// can either define or use Enum. Enum may be cleaner.
#define START_STATE = 0;

enum State { 
    Rejected = -1,
    Start = 0,
    Identifer = 1,
    BoolConstant = 2,
    IntConstant = 3,
    DoubleConstant = 4,
    StringConstant = 5,
    Operator = 6,
    Complete = 7
};
// constants for checking reserved words and boolean constants
const char keywords[32][12] = {
    "void", "int", "double", "bool", "string",
    "null", "for", "while", "if", "else",
    "return", "break","Print","ReadInteger","ReadLine"
};

const char *bValues[] = {
    "true", "false"
};

// anything greater than 0 is an accepted state
// first index is the current state bit
// state table - defines out finite automata
const enum State stateTable[7][7] = {
    {Start, Identifer, Identifer, IntConstant, IntConstant, StringConstant, Operator },
    {Identifer, Identifer, Identifer, Identifer, Identifer, Complete, Complete },
    {BoolConstant, Identifer, Identifer, Identifer, Identifer, Identifer, Identifer },
    {IntConstant, Complete, Complete, IntConstant, DoubleConstant, Complete, Complete},
    {DoubleConstant, Complete, Complete, Complete, DoubleConstant, Complete, Complete},
    {StringConstant, StringConstant, StringConstant, StringConstant},
    // {Operator, }
};