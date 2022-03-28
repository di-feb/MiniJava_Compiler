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