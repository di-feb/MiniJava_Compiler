## <b> Translator to Java.
We will implement a parser and translator for a language supporting string operations.  
The language supports:
- concatenation (+) 
- "reverse" operators over strings,
- function definitions and calls,
- conditionals (if-else i.e, every "if" must be followed by an "else"),  
- The following logical expression:  
   is-prefix-of (string1 prefix string2): Whether string1 is a prefix of string2.  

All values in the language are strings.

The precedence of the operator expressions is defined as: precedence(if) < precedence(concat) < precedence(reverse).

To do that we will use lexer.flex and Java.cup files.

## Ambiguity  
