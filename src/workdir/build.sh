#!/bin/bash
name=${PWD##*/}

if [ $name == workdir ]
then
    gcc ../lexer.c -o ../lexer
fi

if [ $name == src ]
then
    gcc ./lexer.c -o lexer
fi