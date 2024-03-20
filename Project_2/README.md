# MiniJava Compiler - Semantic Analysis

This project involves building a compiler for MiniJava, a subset of Java designed to be compatible with a full Java compiler like `javac`.

## Visitors

The compiler uses two visitors, resulting in two iterations over the parse tree:

- **DeclCollector**: This visitor constructs the symbol table, which contains information for each class in the input file. It also checks for semantic errors such as class, method, or variable redeclarations.
- **TypeChecker**: This visitor uses the symbol table constructed by the DeclCollector to check for as many errors as possible in the input file.

## Running the Semantic Checker

To run the semantic checker, follow these steps:

1. Compile the program by running `make compile` in the source directory.
2. Execute the program by running `make semantic_check` in the source directory.

The `make semantic_check` command runs all tests in the `tests` directory recursively and provides messages for each part of the execution (parsing, first and second iteration, semantic error). After each test, the offsets of the fields and methods for each class in the input file are printed.
  