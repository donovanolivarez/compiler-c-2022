#!/bin/bash

name=${PWD##*/}

if [ $name == workdir ]
then
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
            ../lexer $f ; 
        done
    else
        echo
        ../lexer $1 ;
        echo 
    fi
fi

if [ $name == src ]
then
    FRAG_FILES="./samples/*.frag"
    DECAF_FILES="./samples/*.decaf"
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
fi