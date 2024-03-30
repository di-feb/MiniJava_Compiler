# String Operations Parser and Translator to Java

This project implements a parser and translator for a language that supports string operations. The language includes:

- String concatenation (+)
- String reversal
- Function definitions and calls
- Conditionals (every "if" must be followed by an "else")
- Logical expression: is-prefix-of (string1 prefix string2) - checks if string1 is a prefix of string2

All values in the language are strings. The operator precedence is defined as: precedence(if) < precedence(concat) < precedence(reverse).

## Implementation using JFlex and JavaCUP

- **JFlex (lexer.flex)**: This component is responsible for tokenizing the input file. These tokens are used by the parser to generate the Java translator.
- **JavaCUP (parser.cup)**: This component takes a sequence of characters and, with the help of the tokens produced by the lexer,  
builds a structure from it (usually in the form of a parse tree or an abstract syntax tree).  
This structure is used to facilitate translation into another language.

## Ambiguity

The JFLEX deals with ambiguity with the two ways below:

- The first one is to use the rule that matches more characters in the source code.  
This solution is called *maximal munch*.  
- The second one is to use the help of *priority* specifying that a certain rule  
should be applied if two or more regular expressions match the same number  
of characters from a string.  

But in the JavaCUP to deal with ambiguity we need to write the grammar to be unambiguous.  
We will do that by:  

- using the preference (as above in the lexer).
- associativity: when two non-terminals produce the same rule up to a point  
and we have a conflict we produce a non-terminal for both of them.  

## Compile and Execute

To compile and execute the project, use the following commands in the project's root directory:

- `Compile`: Enter `make compile` into the src directory.
- `Execute`: Enter `make execute` into the src directory.

The `Inputs` directory contains three example input files from the class website. You can use these files as input for the parser by modifying the Makefile.

The parser generates code that is redirected to a `Main.java` file. This file is created when you run the above commands.

After running these commands, the `Main.java` file is compiled and executed, and the output is displayed in the terminal.

The `Inputs` directory contains example input files. You can use these files as input for the parser by running the following command:

```bash
make execute < ../Inputs/your_input_file.txt
