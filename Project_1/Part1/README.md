# Compilers Project 1: LL(1) Calculator Parser  

## Documentation

Implements a simple calculator.  
The calculator should accept expressions with  
the bitwise AND(&) and XOR(^) operators, as well as parentheses.  
The grammar (for single-digit numbers) is summarized in:  

    exp -> num 
        | exp op exp
        | (exp)

    op -> ^ 
        | &

    num -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

First off, we need to remodel our grammar in order to meet the following requirements.

1) Support priority between the two operators ^, &.  
    To achieve this we must take care of grammar  
     to place the operator & which has a higher precedence  
     at a lower level in the parse tree, so that it is clear what for  
     to deal with the operator & we should be done with ^.  
     These in case parentheses do not change our precedence.      

    The grammar that will emerge based on these is: 

        exp -> exp ^ term
            |  term

        term -> term & factor
            |   factor
        
        factor -> num
                | (exp)

2) Eliminate Left-Recursion.  
    Left recursive grammars are possible to trap a LL(1) parser into infinity loops.  
    That happens because at a specific time we do not have enough   
    information on which rule to apply to proceed into the unfolding of the expression.  

        Formally a grammar is left recursive if exist at least one non-terminal  
        A such as that: A->* A a. (for some set of symbols a).  
        * Means A-> Bx 
                B-> Ay 

    Now to eliminate the left recursion we need to remodel our grammar one more time.  
    We will rewrite our grammar so that each non-terminal having a left-recursive rule   
    will be replaced with two others.In the first non-terminal will call the second and  
     calling the second non-terminal will give us clear information about the most common  
     we have to apply every time because all producers will have a unique first symbol.  

        1) exp -> term rest
        
        2) term -> factor rest2
        
        3) rest -> ^ term rest
        4)      |  ε
        
        5) rest2 -> & factor rest2
        6)       | ε
        
        7) factor -> num
        8)         | (exp)

Now we will show that this new grammar we made and based on it we will answer the queries  
are LL (1). If it was not LL (1) we would have to make it LL (1) using left factoring.    

● We calculate the First of every produce.

    First(#1) = { term } = {'0'..'9', '('}

    First(#2) = { factor } = {'0'..'9', '('}

    First(#3) = { '^' }

    First(#4) = { ε }

    First(#5) = { '&' }

    First(#6) = { ε }

    First(#7) = { num } = {'0'..'9'}

    First(#8) = { '(' }

● We calculate the Follow of every non terminal.

    Follow(exp) = {')', '$'}

    Follow(term) = {'^', ')', '$'}

    Follow(rest) = {')', '$'}

    Follow(rest2) = {'^', ')', '$'}

    Follow(factor) = {'&', ')', '^','$'}

● We calculate the First+ of every non terminal.

    First+(exp) = {'0'..'9', '('}

    First+(term) = {'0'..'9', '('}

    First+(rest) = First(#3) + Follow(rest) = {'^', ')', '$' }

    First+(rest2) = First(#5) + Follow(rest2) = {'&', '^', ')', '$' }

    First+(factor) = {'0'..'9', '('}

Since we have now found the First + sets we can see that  
the definition for if a grammar is LL (1) is satisfied.  

Now that our grammar is in a form that it can accept  
LL (1) parsing we can build a lookup table  
that will help us convert the above grammar into code.  

## Lookahead Table  

|Non-Terminals | '0' .. '9'      | '^'           | '&'               | '('            | ')'     | '$'     |
|------------- |:---------------:|:-------------:|:-----------------:|:-------:       |:--------|:-------:|
| **exp**      | term rest       | error         | error             | term rest      | error   | error   |
| **term**     | factor rest2    | error         | error             | factor rest2   | error   | error   |
| **rest**     | error           | ^ term rest   | error             | error          | ε       | ε       |
| **rest2**    | error           | ε             | & factor rest2    | error          | ε       | ε       |
| **factor**   | num             | error         | error             | (exp)          | error   | error   |  

## Compile and run

- Write *make run* into the current directory.
