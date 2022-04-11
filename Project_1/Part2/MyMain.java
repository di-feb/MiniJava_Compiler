import java_cup.runtime.*;
import java.io.*;

class MyMain {
    public static void main(String[] argv) throws Exception{
        Parser p = new Parser(new Lexer(new InputStreamReader(System.in)));
        p.parse();
    }
}