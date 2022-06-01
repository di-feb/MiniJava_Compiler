import syntaxtree.*;
import java.io.*;
import java.util.Map;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws Exception {
        final String ANSI_RED = "\033[1;91m";
        final String ANSI_GREEN = "\u001B[32m";
        final String GREEN_BRIGHT = "\033[0;92m";
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RESET = "\u001B[0m";

        if(args.length == 0){
            System.err.println(ANSI_RED + "Usage: java Main <inputFile>" + ANSI_RESET);
            System.exit(1);
        }
        FileInputStream fis = null;
        for(int i = 0; i < args.length; i++){
            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                System.out.println("\nStart parsing the file: " + args[i]);
                // Parsing of the program
                Goal root = parser.Goal();
                System.out.println(ANSI_YELLOW + "Program parsed successfully." + ANSI_RESET);

                // First iteration of the parse tree in order to build the symbol_table
                DeclCollector collector = new DeclCollector();
                root.accept(collector, null);
                System.out.println(ANSI_GREEN + "First iteration completed successfully." + ANSI_RESET);

                // Second iteration of the parse tree in order to do type checking
                // with the help of the symbol table we built at the first iteration.
                LlvmGenerator generator = new LlvmGenerator(collector.getSymbolTable(), collector.getQueue());
                root.accept(generator);
                System.err.println(GREEN_BRIGHT + "Llvm code generated successfully." + ANSI_RESET);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
