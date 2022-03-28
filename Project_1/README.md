## <b>Compilers Project 1: LL(1) Calculator Parser  

### <b>Προσωπικά στοιχεία

- __Όνομα__: Στάθης Δεμέναγας

- __Α.Μ.__: sdi1900045

### <b>Documentation
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
1)  Support priority between the two operators ^, &.  
    Για να το πετυχουμε αυτο θα πρεπει να φροντισουμε η γραμματικη   
    να τοποθετει τον operator & που εχει υψηλοτερη precedence  
    σε χαμηλοτερο επιπεδο στο parse tree, ετσι ωστε να ειναι ξεκαθαρο οτι για  
    να ασχοληθουμε με τον τελεστη & θα πρεπει να εχουμε τελειωσει με τον ^.  
    Αυτα σε περιπτωση που παρενθεσεις δεν μας αλλαζουν την precedence.      

    H γραμματικη που θα προκυψει με βαση αυτα ειναι:  

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
    will be replaced with two others. Στο πρωτο μη τερματικο θα καλει το δευτερο και η   
    κληση του δευτερου μη τερματικου θα μας δινει ξεκαθαρη πληροφορια για τον πιο κανονα  
    πρεπει να εφαρμοσουμε καθε φορα γιατι ολες οι παραγωγες θα εχουν μοναδικο πρωτο συμβολο.  
    Πιο συγκεκριμενα το πρωτο μη τερματικο θα παραγει    
    σαν κανονα το μη τερματικο που βγαινει κοινος παραγοντας απο τους κανονες του συν   
    κατι ακομα εστω rest. Αυτο το rest θα γινει ενα νεο μη τερματικο με κανονες που παραγουν  
    οτι εχει μηνει για να φτιαχτουν οι παλιοι κανονες του πρωτου τερματικου ετσι οπως ηταν   
    πριν την αλλαγη χρησιμοποιωντας δεξια αναδρομη.Δηληδη η νεα γραμματικη μας θα μοιαζει   
    καπως ετσι:   

        1) exp -> term rest
        
        2) term -> factor rest2
        
        3) rest -> ^ term rest
        4)      |  ε
        
        5) rest2 -> & factor rest2
        6)       | ε
        
        7) factor -> num
        8)         | (exp)

Tωρα θα δειξουμε οτι αυτη η νεα γραμματικη που φτιαξαμε και με βαση αυτην θα απαντησουμε στα  
ερωτηματα ειναι LL(1). Εαν δεν ηταν LL(1) θα επρεπε να την κανουμε LL(1) χρησιμοποιοντας left factoring.  

● Υπολολογιζουμε τα First ολων των παραγωγων.

    First(#1) = { term } = {'0'..'9', '('}

    First(#2) = { factor } = {'0'..'9', '('}

    First(#3) = { '^' }

    First(#4) = { ε }

    First(#5) = { '&' }

    First(#6) = { ε }

    First(#7) = { num } = {'0'..'9'}

    First(#8) = { '(' }

● Υπολογιζουμε τα Follow ολων των μη τερματικων.

    Follow(exp) = {')', '$'}

    Follow(term) = {'^', ')', '$'}

    Follow(rest) = {')', '$'}

    Follow(rest2) = {'^', ')', '$'}

    Follow(factor) = {'&', ')', '^','$'}

● Υπολογιζουμε τα First+ ολων των μη τερματικων.

    First+(exp) = {'0'..'9', '('}

    First+(term) = {'0'..'9', '('}

    First+(rest) = First(#3) + Follow(rest) = {'^', ')', '$' }

    First+(rest2) = First(#5) + Follow(rest2) = {'&', '^', ')', '$' }

    First+(factor) = {'0'..'9', '('}

Εφοσον βρικαμε τωρα τα First+ sets μπορουν να δουμε οτι
ο ορισμος για το εαν μια γραμματικη ειναι LL(1) ικανοποιειται.

Τωρα που η γραμματικη μας ειναι σε μορφη που μπορει να δεχτει 
LL(1) parsing μπορουμε να κατασκευασουμε ενα a​ lookup table
that will help us convert the above grammar into code.

### **Lookahead Table**  

|Non-Terminals | '0' .. '9'      | '^'           | '&'               | '('            | ')'     | '$'     |
|------------- |:---------------:|:-------------:|:-----------------:|:-------:       |:--------|:-------:|
| **exp**      | term rest       | error         | error             | term rest      | error   | error   |
| **term**     | factor rest2    | error         | error             | factor rest2   | error   | error   |
| **rest**     | error           | ^ term rest   | error             | error          | ε       | ε       |
| **rest2**    | error           | ε             | & factor rest2    | error          | ε       | ε       |
| **factor**   | num             | error         | error             | (exp)          | error   | error   |  

