import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.lang.ProcessBuilder.Redirect.Type;
import java.util.Map;
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("Program parsed successfully.\n");

            // First iteration of the parse tree in order to build the symbol_table
            DeclCollector collector = new DeclCollector();
            root.accept(collector, null);
            System.err.println("First iteration completed successfully.\n");

            // Second iteration of the parse tree in order to do type checking
            // with the help of the symbol table we built at the first iteration.
            TypeChecker checker = new TypeChecker(collector.getSymbolTable());
            root.accept(checker);
            System.err.println("Second iteration completed successfully.\n");
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
