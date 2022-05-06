## MiniJava Static Checking (Semantic Analysis)

For this proj we are going to build a compiler for MiniJava, a subset of Java.  
MiniJava is designed so that its programs can be compiled by a full Java compiler    
like javac.

## Visitors
We will use two visitors, so we are going to iterate the parse tree two times.  
- The first visitor (aka DeclCollector) is responsible for the construction of the symbol table  
that will contain information for each class of the input file that we are going to analyze.  
Also it tries to catch some semantic errors such us redeclaration of a class , method or variable.    
- The second visitor (aka TypeChecker) with the help of the symbol table we construct at the first    
iteration is responsible for inspection as much errors as possible that exist into the input file. 

## To Run it
In order to run the semantic checker we need to:  
●Compile the program writing: 

    -make compile (inside source directory)

●Execute the program writing:  

    -make semantic_check (inside source directory)

This command will run recursivly all the tests inside tests directory and  
it will give messages for each part of the execution (parsing, first - second iteration, semantic error).  
Also after the execution of each test the offsets of the fields, methods for each class of the input file    
will be printed.

  