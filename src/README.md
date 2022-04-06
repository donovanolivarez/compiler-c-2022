# Lexical Analyzer - Part 1 - Donovan Olivarez

Program is written using C. Tested on the fox servers. Working for all .frag and .decaf files.

# Build and Run
You can build from either the root directory (src) or workdir:

    ./workdir/build.sh

    or 

    cd workdir
    ./build.sh

Lexer binary is created in src directory.
Exec can accept a filepath as a parameter.

Pass option '-all' to run the program against all .frag and .decaf files.

# Issues
The known issues are output issues, and some potential column/line number calculation issues.

# Git
https://github.com/donovanolivarez/compiler-c-2022
Currently a private repo, but for whatever reason access is desired, please let me know.

##### SECTION 2: Parser and Lexer updates

# Checkpoint as of 4/5/22

At the moment, I have the main code for parsing var declarations and functional declarations. I am going down the grammar, handling statements and then expressions. The expression code is written as well, and if we run with the input file functions.decaf, the beginning of the tree is being printed out.

The lexer was rewritten to use java. The decision to do this was for a few reasons:
1) Most resources I found in class notes and online were in Java
2) It was easier to represent the difference types of expressions with subclassing. 
3) My C implementation of the lexer was very buggy and the code was bad, so it was overall easier to rewrite using class notes and online resources.

Overall, the lexer does not seem to be throwing as many issues as before, but I'm still making improvements. The parser is in the end stages, but there are still small bugs and printing errors to iron out. 

# Build and Run
Currently I have NOT updated the build and exec scripts to handle the java code. To quickly see what I have so far, you may use the following:

# to build 
javac src/scanner/\*.java src/parser/expression/Expr.java src/parser/util/\*.java/ src/parser/\*.java


# to run
java src.scanner.Decaf <inputfile>

This functionality will be built into scripts for grading, but for the checkpoint this is where I am at.
