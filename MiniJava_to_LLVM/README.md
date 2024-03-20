# Intermediate Code Generation for MiniJava (MiniJava -> LLVM)

This project involves generating intermediate code for MiniJava, a subset of Java designed to be compatible with a full Java compiler like `javac`.

## Visitors

The compiler uses three visitors, resulting in three iterations over the parse tree:

- **DeclCollector**: This visitor constructs the symbol table, which contains information for each class in the input file. It also checks for semantic errors such as class, method, or variable redeclarations.
- **TypeChecker**: This visitor uses the symbol table constructed by the DeclCollector to check for as many errors as possible in the input file.

## Running the Intermediate Code Generator

To run the intermediate code generator, follow these steps:

1. Compile the program by running `make compile` in the source directory.
2. Execute the program by running `make llvm` in the source directory.

The `make generate_intermediate_code` command runs all tests in the `tests` directory recursively and provides messages for each part of the execution (parsing, first, second iteration, semantic error). After each test, the offsets of the fields and methods for each class in the input file are printed.
