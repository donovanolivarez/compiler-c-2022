#!/bin/bash

FRAG_FILES="../samples/*.frag"
DECAF_FILES="../samples/*.decaf"

if [ -z "$1" ] 
    then
    echo "ERROR: No file supplied"
    exit 1
fi

if [ $1 == "-all" ]
then
    for f in $FRAG_FILES $DECAF_FILES; 
    do
        echo
        echo "FILENAME: $f" 
        ./lexer $f ; 
    done
else
    echo
    ./lexer $1 ;
    echo 
fi
# get list of files from ../samples directory
# loop through list, run every file with the program.
# BUT may need to ask about the requirements on the execution...