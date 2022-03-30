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

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isDigit(int c) { return '0' <= c && c <= '9'; }
    private boolean isLeftParenthesis(int c) { return '(' == c; }
    private boolean isRightParenthesis(int c) { return ')' == c; }
    private boolean isANDoperator(int c) { return '&' == c; }
    private boolean isXORoperator(int c) { return '^' == c; }
    private boolean isEOF(int c) { return ( c == '\n' || c == -1 ); }
    private int evalDigit(int c){ return c - '0'; }

    private int Exp() throws IOException, ParseError{
        if(!isDigit(lookahead) && !isLeftParenthesis(lookahead)) throw new ParseError();
        return Rest(Term());
    }

    private int Term() throws IOException, ParseError {
        if(!isDigit(lookahead) && !isLeftParenthesis(lookahead)) throw new ParseError();
        return Rest2(Factor());
    }

    private int Rest(int num) throws IOException, ParseError{
        if(isDigit(lookahead) || isANDoperator(lookahead) || isLeftParenthesis(lookahead)) throw new ParseError();
        if(isEOF(lookahead) || isRightParenthesis(lookahead))
            return num;
        else{
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
            int cond = evalDigit(lookahead);    
            consume(lookahead);
            return cond;
        }
        else{
            consume(lookahead);
            int cond = Exp();
            consume(')');
            return cond;
        }
    }

    public int eval() throws IOException, ParseError {
        int value = Exp();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();
        return value;
    }
}
