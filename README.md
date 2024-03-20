# MiniJava Compiler Project

This project is a comprehensive implementation of a compiler for the MiniJava language. It includes a parser, a translator, a MiniJava compiler, and a module for converting MiniJava code to LLVM intermediate representation.

## Project Structure

- `parser/`: This directory contains the implementation of the parser for MiniJava and a simple LL(1) calculator parser that accepts expressions with bitwise AND(&) and XOR(^) operators, as well as parentheses. The parser checks the syntax of the MiniJava code and generates a parse tree, while also remodeling the grammar to support operator precedence and eliminate left recursion, ensuring the parser doesn't fall into infinite loops. For more details, refer to the README in the `parser/` directory.

- `mini_java_compiler/`: This directory contains the implementation of the MiniJava compiler. The compiler takes the intermediate representation generated by the translator and compiles it into executable code. For more details, refer to the README in the `mini_java_compiler/` directory.

- `miniJava_to_LLVM/`: This directory contains the implementation of the module that converts MiniJava code to LLVM intermediate representation. This allows the code to be further optimized and compiled by the LLVM compiler. For more details, refer to the README in the `miniJava_to_LLVM/` directory.

## Getting Started

To get started with this project, clone the repository and navigate into each directory to understand the different components of the project. Each directory has its own README file that provides more detailed information about that part of the project.
