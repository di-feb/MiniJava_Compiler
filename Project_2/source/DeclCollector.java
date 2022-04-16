import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;

public class DeclCollector extends GJDepthFirst< String, Data >{
    private Map <String, Data> symbol_table;

    public DeclCollector(){
        this.symbol_table = new LinkedHashMap < String, Data >();
    }

    /** Goal
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    */
    public String visit(Goal n, Data data) throws Exception {
        // Accept at main class
        n.f0.accept(this, null);
        // When a production has a * it means that this production can appear
        // zero or more times. So for the  ( TypeDeclaration() )* f.e. we need to
        // iterate all the classes declarations. 
        for( int i = 0; i < n.f1.size(); i++ )
            n.f1.elementAt(i).accept(this, null);

        return null; 
    }

    /** MainClass
    * f1 -> Identifier() { void main ( String[]
    * f11 -> Identifier()
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*

    * class f1 { 
        void main ( String[] f11 ){
            f14 
            f15 
        }
    }
    */
    public String visit(MainClass n, Data data) throws Exception {
        // Keep the name of the "main" class
        String name = n.f1.accept(this, null);

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Statements.
        Data mains_data = new Data();

        // Pass mains_data down to parse tree to collect the info
        for( int i = 0; i < n.f14.size(); i++ )
            n.f14.elementAt(i).accept(this, mains_data);

        for( int i = 0; i < n.f15.size(); i++ )
            n.f15.elementAt(i).accept(this, mains_data);

        // After the infos for the "main" classes gathered push the data into the Map
        symbol_table.put(name, mains_data);
        return null;
    }

    /** TypeDeclaration
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    public R visit(TypeDeclaration n, Data data) throws Exception {
        n.f0.accept(this, null);
        return null;
    }  

    /** ClassDeclaration
    * f1 -> Identifier()
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*

    class f1 {
        f3
        f4
    }
    */
    public String visit(ClassDeclaration n, Data data) throws Exception {
        // Keep the name of the "main" class
        String name = n.f1.accept(this, null);
        // Check if the name of the class already existed inside the symbol table.
        // If it does that means we have redeclare a class.
        // We do not want that => Throw Semantic Error! 
        if(sumbol_table.containsKey(name))
            throw new SemError();

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Methods.
        Data class_data = new Data();

        // Pass mains_data down to parse tree to collect the info
        for( int i = 0; i < n.f3.size(); i++ )
            n.f3.elementAt(i).accept(this, class_data);

        for( int i = 0; i < n.f4.size(); i++ )
            n.f4.elementAt(i).accept(this, class_data);

        // After the infos for the "main" classes gathered push the data into the Map
        symbol_table.put(name, class_data);
        return null;
    }


    /**
    * f1 -> Identifier()
    * f3 -> Identifier()
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*

    class f1 extends f3{
        f5
        f5
    }
    */
    public R visit(ClassExtendsDeclaration n, A argu) throws Exception {
        // Keep the name of the "main" class
        String name = n.f1.accept(this, null);

        // Check if the name of the class already existed inside the symbol table.
        // If it does that means we have redeclare a class.
        // We do not want that => Throw Semantic Error! 
        if(sumbol_table.containsKey(name))
            throw new SemError();

        // Check if the name of the parent class not existed inside the symbol table.
        // If it does not that means we have declare a class whose parent class has not been declared yet.
        // We do not want that => Throw Semantic Error! 
        String parent_name = n.f3.accept(this, null);
        if(!sumbol_table.containsKey(parent_name))
            throw new SemError();

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Methods.
        // Every child class has all the methods and vars of the parent class too.
        Data class_data = new Data();

        // Pass mains_data down to parse tree to collect the info
        for( int i = 0; i < n.f3.size(); i++ )
            n.f3.elementAt(i).accept(this, class_data);

        for( int i = 0; i < n.f4.size(); i++ )
            n.f4.elementAt(i).accept(this, class_data);

        // After the infos for the "main" classes gathered push the data into the Map
        symbol_table.put(name, class_data);
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, Data data) throws Exception {

    }
    
}