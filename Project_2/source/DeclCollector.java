import java.util.Map;
import java.util.Vector;
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
        Data mains_data = new Data(null);

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
    public String visit(TypeDeclaration n, Data data) throws Exception {
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
        if(symbol_table.containsKey(name))
            throw new SemanticError();

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Methods.
        Data class_data = new Data(null);

        // Pass mains_data down to parse tree to collect the info
        for( int i = 0; i < n.f3.size(); i++ )
            n.f3.elementAt(i).accept(this, class_data);

        for( int i = 0; i < n.f4.size(); i++ )
            n.f4.elementAt(i).accept(this, class_data);

        // After the infos for the "main" classes gathered push the data into the Map
        symbol_table.put(name, class_data);
        return null;
    }


    /** ClassExtendsDeclaration
    * f1 -> Identifier()
    * f3 -> Identifier()
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*

    class f1 extends f3{
        f5
        f6
    }
    */
    public String visit(ClassExtendsDeclaration n, Data data) throws Exception {
        // Keep the name of the "main" class
        String name = n.f1.accept(this, null);

        // Check if the name of the class already existed inside the symbol table.
        // If it does that means we have redeclare a class.
        // We do not want that => Throw Semantic Error! 
        if(symbol_table.containsKey(name))
            throw new SemanticError();

        // Check if the name of the parent class not existed inside the symbol table.
        // If it does not that means we have declare a class whose parent class has not been declared yet.
        // We do not want that => Throw Semantic Error! 
        String parent_name = n.f3.accept(this, null);
        if(!symbol_table.containsKey(parent_name))
            throw new SemanticError();

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Methods.
        // Every child class has all the methods and vars of the parent class too.
        Data class_data = new Data(parent_name);
        Data parentData = symbol_table.get(parent_name);
        
        // Copy all info for the parent methods and vars to the child
        class_data.getMethod_Info().putAll(parentData.getMethod_Info());
        class_data.getVar_Info().putAll(parentData.getVar_Info());

        // Pass mains_data down to parse tree to collect the info
        for( int i = 0; i < n.f5.size(); i++ )
            n.f5.elementAt(i).accept(this, class_data);

        for( int i = 0; i < n.f6.size(); i++ )
            n.f6.elementAt(i).accept(this, class_data);

        // After the infos for the "main" classes gathered push the data into the Map
        symbol_table.put(name, class_data);
        return null;
    }

    /** VarDeclaration
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, Data data) throws Exception {
        // Keep the name and the type off the var.
        String var_type = n.f0.accept(this, null);
        String var_name = n.f1.accept(this, null);

        // Make a VarInfo class to store the info you get,
        // and then pass the varInfo into the Data. 
        VarInfo vars_value = new VarInfo();
        vars_value.setType(var_type);
        vars_value.setOffset(0); // We will deal with offset later!!!

        data.getVar_Info().put(var_name, vars_value);

        return null;
    }  

    /** MethodDeclaration
    * f1 -> Type()
    * f2 -> Identifier()
    * f4 -> ( FormalParameterList() )?
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f10 -> Expression()

    *   public f1 f2( f4 ){
            f7
            f8
            return f10;
        }
    */
    public String visit(MethodDeclaration n, Data data) throws Exception {
        String ret_type = n.f1.accept(this, null);   
        String method_name = n.f2.accept(this, null); 

        // If the method has arguments store them inside MethodInfo class of Data
        if(n.f4.present())
            n.f4.accept(this, null);
         
        // Make a VarInfo class to store the info you get,
        // and then pass the varInfo into the Data. 
        MethodInfo method_value = new MethodInfo();
        method_value.setType(ret_type);
        vars_value.setOffset(0); // We will deal with offset later!!!

        // Pass data down to parse tree to collect the info
        for( int i = 0; i < n.f7.size(); i++ )
            n.f7.elementAt(i).accept(this, data);

        for( int i = 0; i < n.f8.size(); i++ )
            n.f8.elementAt(i).accept(this, data);

        data.getMethod_Info().put(method_name, method_value);

        return null;
    }


    /** FormalParameterList
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    // It will go and fill up recursivly the args field of the method_info field of the Data class
    public String visit(FormalParameterList n, Data data) throws Exception {
        data.getMethod_Info().getArgs().add(n.f0.accept(this, null));   
        n.f1.accept(this, null);   
        return null;
    }

    /** FormalParameter
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, Data data) throws Exception {
        String type = n.f0.accept(this, null);   
        String name = n.f1.accept(this, null);
        return type + "" + name;
    }

    /** FormalParameterTail
    * f0 -> ( FormalParameterTerm() )*
    */
    public String visit(FormalParameterTail n, Data data) throws Exception {
        for( int i = 0; i < n.f0.size(); i++ )
            data.getMethod_Info().getArgs().add(n.f0.elementAt(i).accept(this, null));
        return null;
    }

    /** FormalParameterTerm
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    public String visit(FormalParameterTerm n, Data data) throws Exception {   
        String parameter = n.f1.accept(this, null);
        return parameter;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, Data data) throws Exception {


        return null;
    }
}