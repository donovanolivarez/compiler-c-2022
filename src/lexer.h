#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define arraySize(x) ( sizeof(x) / sizeof((x)[0]) );

// can either define or use Enum. Enum may be cleaner.
// some definitions in this file are not in use. Needs cleaning.
#define START_STATE = 0;

int isKeyword(char* token);
int isOperator(char c);
int isSpecialOperator(char c);
int bufferIsEmpty();
int checkForStringError(int, char);
void savePreviousState();
void resetState();
char advance();


enum State { 
    Rejected = -1,
    Start = 0,
    Identifer = 1,
    BoolConstant = 2,
    IntConstant = 3,
    DoubleConstant = 4,
    StringConstant = 5,
    Operator = 6,
    SpecialOperator = 7,
    HexNumber = 8,
    Complete = 9,
    ScientificNotation = 10,
    Commment = 11
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

const char validExponentialSymbols[6] = {'e', 'E'}; 
const char validExponentSigns[2] = {'+', '-'};

const char quotation = '"';

const char operators[20] = { '+', '-', '*', '/', '%',';', ',', '.', '(', ')', '{', '}' };

const char specialOperators[10] = {'<', '=', '>', '!', '&', '|'};

const char specialOperatorTokens[6][10] = {"<=", ">=", "==", "!=", "&&", "||" };

const char specialOpT_Ids[6][20] = {"T_LessEqual", "T_GreaterEqual", "T_EqualEqual", "T_NotEqual", "T_And", "T_Or"};

// anything greater than 0 is an accepted state
// first index is the current state bit
// state table - defines out finite automata
const enum State stateTable[10][10] = {
    {Start,             Identifer, Identifer, IntConstant, IntConstant, StringConstant, Operator, SpecialOperator, HexNumber },
    {Identifer,         Identifer, Identifer, Identifer, Identifer, Complete, Complete, Complete },
    {BoolConstant,      Identifer, Identifer, Identifer, Identifer, Complete, Identifer , Identifer}, // unnecessary row, maybe can delete later
    {IntConstant,       Complete, Complete, IntConstant, DoubleConstant, Complete, Complete, Complete, HexNumber},
    {DoubleConstant,    Complete, Complete, DoubleConstant, DoubleConstant, Complete, DoubleConstant, Complete},
    {StringConstant,    StringConstant, StringConstant, StringConstant, StringConstant, Complete, StringConstant, Complete},
    {Operator,          Complete, Complete, Complete, Complete, Complete, Complete, Complete},
    {SpecialOperator,   Complete, Complete, Complete, Complete, Complete, Complete, SpecialOperator},
    {HexNumber,         Complete, Complete, HexNumber, HexNumber, Complete, Complete, Complete, HexNumber}
};
