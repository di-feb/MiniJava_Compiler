## Generating intermediate code (MiniJava -> LLVM)

In this part of the project we will write visitors that convert MiniJava code  
into the intermediate representation used by the LLVM compiler project.

## Visitors
We will use two visitors, so we are going to iterate the parse tree two times.  
- The first visitor (aka DeclCollector) is responsible for the construction of the symbol table  
that will contain information for each class of the input file that we are going to analyze.     
- The second visitor (aka LlvmGenerator) with the help of the symbol table we constructed at the first    
iteration is responsible for producing the LLVM code. 

## To Run it
In order to run the semantic checker we need to:  
●Compile the program writing: 

    -make compile (inside source directory)

●Execute the program writing:  

    -make llvm (inside source directory)

This command will run recursivly all the tests inside tests directory and  
it will give messages for each part of the execution (parsing, first - second iteration of the parse tree).  

  