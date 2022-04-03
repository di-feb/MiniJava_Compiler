## <b> *Translator to Java.*
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

### <b>To do that we will use *Jflex* and *JavaCUP* files.  
- The job of Jflex aka lexer.flex file is to produce tokens from the input file.   
  These tokens will be used later by the parser to produce the translator to Java.  
- The job of parser aka parser.cup is to take a linear sequence of characters  
  and with the help of the toketns that lexer produced basically building structure from it (grammar),   
  usually in the form of a parse tree or an abstract syntax tree,  
  in order to facilitate translation into another language.  

### Ambiguity  
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

### **How to compile and execute:** 
 *Compile*: Just write **make compile** into the current directory.  
 *Execute*: Just write **make execute < Inputs/input_1.txt** into the current directory.   
 Insine the directory inputs there are the three examples from the website of class.   
