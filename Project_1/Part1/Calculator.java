/* 
Implements a simple calculator. 
The calculator should accept expressions with 
the bitwise AND(&) and XOR(^) operators, as well as parentheses.
The grammar (for single-digit numbers) is summarized in:

1) exp -> term rest
        
2) term -> factor rest2

3) rest -> ^ term rest
4)      |  ε

5) rest2 -> & factor rest2
6)       | ε

7) factor -> num
8)         | (exp) 
*/

import java.io.InputStream;
import java.awt.SystemTray;
import java.io.IOException;

class Calculator {
    public static void main(String[] args) {
        try {
            System.out.println((new Evaluator(System.in)).eval());
        } catch (IOException | ParseError e) {
            System.err.println(e.getMessage());
        }
    }
}
class Evaluator {

    private final InputStream in;
    private int lookahead;

    public Evaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }
    // Represents the arrow that shows us which character we are reading at the moment(lookahead).
    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    // Functions for check what the lookahead is.
    private boolean isDigit(int c) { return '0' <= c && c <= '9'; }
    private boolean isLeftParenthesis(int c) { return '(' == c; }
    private boolean isRightParenthesis(int c) { return ')' == c; }
    private boolean isANDoperator(int c) { return '&' == c; }
    private boolean isXORoperator(int c) { return '^' == c; }
    private boolean isEOF(int c) { return ( c == '\n' || c == -1 ); }
    private int evalDigit(int c){ return c - '0'; }

    // Produces the code of the non terminals.
    // We will do that with the help of the board we built at README.

    // If the lookahead is valid produce the only rule
    // else throw exception
    private int Exp() throws IOException, ParseError{
        if(!isDigit(lookahead) && !isLeftParenthesis(lookahead)) throw new ParseError();
        return Rest(Term());
    }

    // If the lookahead is valid produce the only rule
    // else throw exception
    private int Term() throws IOException, ParseError {
        if(!isDigit(lookahead) && !isLeftParenthesis(lookahead)) throw new ParseError();
        return Rest2(Factor());
    }

    // If the lookahead is not valid throw exception.
    // Else check what the lookahead is and produce the similar rule.
    private int Rest(int num) throws IOException, ParseError{
        if(isDigit(lookahead) || isANDoperator(lookahead) || isLeftParenthesis(lookahead)) throw new ParseError();
        // If the rule is the ε, just pass up to the tree (return) the number you keep. 
        if(isEOF(lookahead) || isRightParenthesis(lookahead))
            return num;
        else{
        // If you read a valid char that its rule is not ε, 
        // consume the lookahead
        // calculate next number by producing the rule of the lookahead
        // and keep it to a value
        // Pass the result up to the tree.
        // Same logic in REST2
            consume(lookahead);
            int num2 = Rest(Term());
            return (num ^ num2);  
        }
    }
    private int Rest2(int num) throws IOException, ParseError{
        if(isDigit(lookahead) || isLeftParenthesis(lookahead)) throw new ParseError();
        if(isXORoperator(lookahead) || isEOF(lookahead) || isRightParenthesis(lookahead))
            return num;
        else {
            consume(lookahead);
            int num2 = Rest2(Factor());
            return (num & num2);
        }
    }
    private int Factor() throws IOException, ParseError{
        if(!isDigit(lookahead) && !isLeftParenthesis(lookahead)) throw new ParseError();
        if(isDigit(lookahead)){
            // If the lookahead is digit take its value, 
            // consume it and pass its value up.
            int cond = evalDigit(lookahead);    
            consume(lookahead);
            return cond;
        }
        else{
            // If the lookahead is ( that means that the rule is 
            // (exp) we consume ( 
            // produce the exp rule
            // consume ) 
            // pass its result up
            consume(lookahead);
            int cond = Exp();
            consume(')');
            return cond;
        }
    }

    // Calls the first production and if it all goes well
    // return the value.
    public int eval() throws IOException, ParseError {
        int value = Exp();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();
        return value;
    }
}
