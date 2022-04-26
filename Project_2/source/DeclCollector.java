import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;

public class DeclCollector extends GJDepthFirst<String, Data>{
    private Map <String, Data> symbol_table;
    private String currMethod;

    public DeclCollector(){
        currMethod = null;
        this.symbol_table = new HashMap <String, Data>();
    }

    public Map <String, Data> getSymbolTable(){
        return this.symbol_table;
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
        currMethod = null;
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
        // Keep the name of the class
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

        // After the infos for the class gathered push the data into the Map
        symbol_table.put(name, class_data);
        currMethod = null;
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
        // Keep the name of the class
        String name = n.f1.accept(this, null);
        // Keep the name of the parent class
        String parent_name = n.f3.accept(this, null);

        // Check if the name of the class already existed inside the symbol table.
        // If it does that means we have redeclare a class.
        // We do not want that => Throw Semantic Error! 
        if(symbol_table.containsKey(name))
            throw new SemanticError();

        // Allocate memory for a Data Object to fill it up with the info
        // provided by VarDecls and Methods.
        // Every child class has all the methods and vars of the parent class too.
        Data class_data = new Data(parent_name);
        Data parentData = symbol_table.get(parent_name);
        
        // Copy all info for the parent methods and vars to the child
        class_data.getMethods().putAll(parentData.getMethods());
        class_data.getVars().putAll(parentData.getVars());

        // Pass class_data down to parse tree to collect the info
        for( int i = 0; i < n.f5.size(); i++ )
            n.f5.elementAt(i).accept(this, class_data);

        for( int i = 0; i < n.f6.size(); i++ )
            n.f6.elementAt(i).accept(this, class_data);

        // After the infos for the class gathered push the data into the Map
        symbol_table.put(name, class_data);
        currMethod = null;
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

        if(currMethod != null) {// That means that the varDeclaration is inside a method
            // If var_name already existed inside data.getMethods().get( currMethod).getVars() map and that 
            // class has not been declared as a child of another class it means,
            // we had a redeclaration of that variable inside the same method.
            // We do not want that => Throw Semantic Error!
            if(data.getMethods().get(currMethod).getVars().containsKey(var_name) && data.getName() == null)
                throw new SemanticError();
            data.getMethods().get(currMethod).getVars().put(var_name, vars_value);
        }
        else {   // This means that the varDeclaration is for a field of the class
            if(currMethod == null) {// That means that the varDeclaration is inside a method
                // If var_name already existed inside data.getVars() map and that 
                // class has not been declared as a child of another class it means,0
                // we had a redeclaration of that variable field inside the same class.
                // We do not want that => Throw Semantic Error!
                if(data.getVars().containsKey(var_name) && data.getName() == null)
                    throw new SemanticError();
                data.getVars().put(var_name, vars_value);
            }
        }
        

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
        currMethod = method_name; 
        List<String> parameters = new ArrayList<String>();

        // If var_name already existed inside data.getMethods map and that 
        // class has not been declared as a child of another class it means,
        // we had a redeclaration of that method inside the same class.
        // We do not want that => Throw Semantic Error!
        if(data.getMethods().containsKey(method_name) && data.getName() == null)
            throw new SemanticError();

        // If the method has arguments accept will return a string with the arguments in a way like this:
        // (type id, type id, ...)
        if(n.f4.present()) {
            List<String> list = new ArrayList<String>();
            parameters = Arrays.asList(n.f4.accept(this, null).split(","));
            // Take only the name of the variable and store it to a list to check
            // for redeclaration.
            // If we have declare a parameter into a method more than one time 
            // (same name) =>throw parse error!!! 
            for( int i = 0; i < parameters.size(); i++)
                list.add(Arrays.asList(parameters.get(i).split(" ")).get(1));

            for(int i = 0; i < list.size() - 1; i++)
                for(int j = i + 1; j < list.size(); j++)
                    if(list.get(i).equals(list.get(j)))
                        throw new SemanticError();
            
        }

        // Make a VarInfo class to store the info you get,
        // and then pass the varInfo into the Data. 
        MethodInfo method_value = new MethodInfo();
        method_value.setType(ret_type);
        method_value.setParameters(parameters);
        method_value.setOffset(0); // We will deal with offset later!!!

        data.getMethods().put(method_name, method_value);

        // Pass data down to parse tree to collect the info 
        for( int i = 0; i < n.f7.size(); i++ )
            n.f7.elementAt(i).accept(this, data);

        return null;
    }


    /** FormalParameterList
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    // It will go and fill up recursivly the args field of the method_info field of the Data class
    public String visit(FormalParameterList n, Data data) throws Exception {
        String parameter = n.f0.accept(this, null);   
        String parameter_tail = n.f1.accept(this, null);   
        return parameter + parameter_tail;
    }

    /** FormalParameter
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, Data data) throws Exception {
        String type = n.f0.accept(this, null);   
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    /** FormalParameterTail
    * f0 -> ( FormalParameterTerm() )*
    */
    public String visit(FormalParameterTail n, Data data) throws Exception {
        String parameter_tail = "";
        for( int i = 0; i < n.f0.size(); i++ )
            parameter_tail += n.f0.elementAt(i).accept(this, null);
        return parameter_tail;
    }

    /** FormalParameterTerm
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    public String visit(FormalParameterTerm n, Data data) throws Exception {   
        String parameter = n.f1.accept(this, null);
        return ", " + parameter;
    }

    /** Type
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, Data data) throws Exception {
        return n.f0.accept(this, null);
    }

    /** ArrayType
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    public String visit(ArrayType n, Data data) throws Exception {
        return n.f0.accept(this, null);
    }

    /** BooleanArrayType 
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(BooleanArrayType n, Data data) throws Exception {
        return "boolean[]";
    }

    /** IntegerArrayType
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(IntegerArrayType n, Data data) throws Exception {
        return "int[]";
    }

    /** BooleanType
    * f0 -> "boolean"
    */
    public String visit(BooleanType n, Data data) throws Exception {
        return "boolean";
    }

    /** IntegerType
    * f0 -> "int"
    */
    public String visit(IntegerType n, Data data) throws Exception {
        return "int";
    }

    public String visit(Identifier n, Data data) throws Exception {
        return n.f0.toString();
    }

}