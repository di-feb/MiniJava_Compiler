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

class Evaluator {

    private final InputStream in;

    private int lookahead;

    public Evaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void Exp() {
        return;
    }

    private void Term(){
        return;
    }
    private void Rest(){
        return;
    }
    private void Rest2(){
        return;
    }
    private void Factor(){
        return;
    }
}