import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;

public class TypeChecker extends GJDepthFirst< String, Data >{
    String className;                           // The className of the class we are into at a specific moment
    ArrayList <String> typesInScope;            // A list that holds every type that has been declared primitive and non primitive
    private Map <String, Data> symbol_table;    // The symbol table we construct later with the DeclCollector

    public TypeChecker(){
        this.symbol_table = new LinkedHashMap < String, Data >();
        this.types = new ArrayList<String>();
        this.varAtScope = new ArrayList<String>();
        this.methodsAtScope = new ArrayList<String>();
    }

    boolean isPrimitive(String type){
        if(type != "int[]" && type != "boolean[]" && type != "boolean" && type != "int")
            return false;
        return true;
    }

    // Find all non primitive types and store them into an referenceTypes ArrayList.
    private void UpdateTypesInScope(){
        typesInScope.clear();
        Data data = symbol_table.get(className);
        for (String varname: data.getVars().keySet()){
            VarInfo varinfo = data.get(varname);
            String type = varinfo.getType();
            if( !typesInScope.contains(type))
                typesInScope.add(type);
        }
    }

    private void CheckForDeclaration(String value, int mode){
        Boolean varFlag = false;
        Boolean methodFlag = false;
        if(mode == 0){
            varFlag = CheckForVarDeclaration(value);
            if(!varFlag)
                throw new SemanticError();
        }
        else if(mode == 1){
            methodFlag = CheckForMethodDeclaration(value);
            if(!methodFlag)
                throw new SemanticError();
        }
        else{
            varFlag = CheckForVarDeclaration(value);
            methodFlag = CheckForMethodDeclaration(value);
            if(!varFlag && !methodFlag)
                throw new SemanticError();
        }

        
    }

    private boolean CheckForVarDeclaration(String var){
        Data data = symbol_table.get(className);
        boolean flag = false;
        for (String varname: data.getVars().keySet())
            if (varname.equals(var)){
                flag = true; 
                break;
            }
        return flag;
    }
    
    private boolean CheckForMethodDeclaration(String method){
        Data data = symbol_table.get(className);
        boolean flag = false;
        for (String methodname: data.getMethods().keySet())
            if (methodname.equals(method)){
                flag = true; 
                break;
            }
        return flag;
    }

    /** Goal
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    */
    public String visit(Goal n) throws Exception {
        // Accept at main class
        n.f0.accept(this);
        // When a production has a * it means that this production can appear
        // zero or more times. So for the  ( TypeDeclaration() )* f.e. we need to
        // iterate all the classes declarations. 
        for( int i = 0; i < n.f1.size(); i++ )
            n.f1.elementAt(i).accept(this);

        typesInScope.clear();
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
    public String visit(MainClass n) throws Exception {
        // Keep the name of the "main" class
        className = n.f1.accept(this, null);
        UpdateTypesInScope();

        // Go down through the parse Tree
        for( int i = 0; i < n.f14.size(); i++ )
            n.f14.elementAt(i).accept(this);

        for( int i = 0; i < n.f15.size(); i++ )
            n.f15.elementAt(i).accept(this);

        typesInScope.clear();
        return null;
    }

    /** TypeDeclaration
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    public String visit(TypeDeclaration n) throws Exception {
        n.f0.accept(this);
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
    public String visit(ClassDeclaration n) throws Exception {
        // Keep the name of the "main" class
        className = n.f1.accept(this);
        UpdateTypesInScope();

        // Go down through the parse Tree
        for( int i = 0; i < n.f3.size(); i++ )
            n.f3.elementAt(i).accept(this);

        for( int i = 0; i < n.f4.size(); i++ )
            n.f4.elementAt(i).accept(this);
        
        typesInScope.clear();
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
    public String visit(ClassExtendsDeclaration n) throws Exception {
        // Keep the name of the class
        className = n.f1.accept(this);
        UpdateTypesInScope();

        // Check if the name of the parent class not existed inside the symbol table.
        // If it does not that means we have declare a class whose parent class has not been declared yet.
        // We do not want that => Throw Semantic Error! 
        String parent_name = n.f3.accept(this);
        if(!symbol_table.containsKey(parent_name))
            throw new SemanticError();

        // Go down through the parse Tree
        for( int i = 0; i < n.f5.size(); i++ )
            n.f5.elementAt(i).accept(this);

        for( int i = 0; i < n.f6.size(); i++ )
            n.f6.elementAt(i).accept(this);

        typesInScope.clear();
        return null;
    }

    /** VarDeclaration
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n) throws Exception {
        // Keep  the type off the var and go down into the parse tree.
        String var_type = n.f0.accept(this, null);
        n.f1.accept(this, null);

        // We need to check if type is different from (int, boolean, int[], boolean[]) or the other non-primitive types.
        // if it is => Throw Semantic Error!
        if(!typesInScope.contains(var_type) && !isPrimitive(var_type))
            throw new SemanticError();

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
    public String visit(MethodDeclaration n) throws Exception {
        String ret_type = n.f1.accept(this, null);   
        String method_name = n.f2.accept(this, null); 

        // We need to check if type is different from (int, boolean, int[], boolean[]) or the other non-primitive types.
        // if it is => Throw Semantic Error!
        if(!types.contains(ret_type)  && !isPrimitive(ret_type))
            throw new SemanticError();

            
        if(n.f4.present())
            n.f4.accept(this);

        // Accept to go through the parse tree 
        for( int i = 0; i < n.f7.size(); i++ )
            n.f7.elementAt(i).accept(this);
        for( int i = 0; i < n.f8.size(); i++ )
            n.f8.elementAt(i).accept(this);
        
        // The return type of the return statement need to match with the return type of this method.
        // If it does not => Throw Semantic Error!
        if(!n.f10.accept(this).getClass().getName().eguals(ret_type))
            throw new SemanticError();

        return null;
    }

    /** Statement
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    public String visit(Statement n) throws Exception {
        return n.f0.accept(this);
    }

    /** Block
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"

    { f1 }
    */
    public String visit(Block n) throws Exception {
        for( int i = 0; i < n.f0.size(); i++ )
            n.f0.elementAt(i).accept(this);
        return null;
    }

    /** AssignmentStatement
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    
    f0 = f2;
    
    */
    public String visit(AssignmentStatement n) throws Exception {
        // Check if the var(f0) has been declared.
        // If not => Throw Semantic Error!
        string var = n.f0.accept(this);
        CheckForDeclaration(var, 0);
        
        String idType = data.get(var).getType();
        
        // Check if the time of the expression match the type of the identifier.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).getClass().getName().eguals(idType))
            throw new SemanticError();
        
        return null;
    }

    /** ArrayAssignmentStatement
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"

    f0[f2]=f5;

    */
    public String visit(ArrayAssignmentStatement n) throws Exception {
        string id = n.f0.accept(this);
        CheckForDeclaration(id, 2);
        
        String idType = data.get(var).getType();

        // Check if the time of the expression f2 is interger.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).getClass().getName().eguals("int"))
            throw new SemanticError();

        // Check if the time of the expression match the type of the identifier.
        // If not => Throw Semantic Error!
        if(!n.f5.accept(this).getClass().getName().eguals(idType))
            throw new SemanticError();
        
        return null;

    }

    /** IfStatement
     * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()

    if (f2){
        f4
    }
    else{
        f6
    }
    */
    public String visit(IfStatement n) throws Exception {
        // Check if the time of the expression is boolean.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).getClass().getName().eguals("boolean"))
            throw new SemanticError();
        n.f4.accept(this);
        n.f6.accept(this);
        
        return null;
    }

    /** WhileStatement
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()

    while(f2){
        f4
    }
    */
    public String visit(WhileStatement n) throws Exception {
        // Check if the time of the expression is boolean.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).getClass().getName().eguals("boolean"))
            throw new SemanticError();
        n.f4.accept(this);
        return null;
    }


    /** PrintStatement 
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n) throws Exception {
        // We need to check if type of the expression f2 is different from (int, boolean).
        // if it is => Throw Semantic Error!
        String expType = n.f2.accept(this);
        if(!expType.getClass().getName().eguals("int") && !expType.getClass().getName().eguals("boolean"))
            throw new SemanticError();
        return null;
 }

    /** Expression
     * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
    public String visit(Expression n) throws Exception {
        return n.f0.accept(this);
    }

    /** AndExpression   
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public R visit(AndExpression n) throws Exception {
        String clause1 = n.f0.accept(this);
        if(!clause1.getClass().getName().eguals("int") && !expType.getClass().getName().eguals("boolean"))
            throw new SemanticError();
        String clause2 = n.f2.accept(this);
        if(!clause2.getClass().getName().eguals("int") && !expType.getClass().getName().eguals("boolean"))
            throw new SemanticError();

        return "boolean";
    }

    /** CompareExpression
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n) throws Exception {
        String exp1 = n.f0.accept(this);
        if(!exp1.getClass().getName().eguals("int"))
            throw new SemanticError();
        
        String exp2 = n.f2.accept(this);
        if(!exp2.getClass().getName().eguals("int"))
            throw new SemanticError();
        return "boolean";
    }

    /** PlusExpression
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()

    f0 + f2
    */
    public String visit(PlusExpression n) throws Exception {
        String exp1 = n.f0.accept(this);
        if(!exp1.getClass().getName().eguals("int"))
            throw new SemanticError();
        
        String exp2 = n.f2.accept(this);
        if(!exp2.getClass().getName().eguals("int"))
            throw new SemanticError();
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()

    f0 - f2
    */
    public String visit(MinusExpression n, A argu) throws Exception {
        String exp1 = n.f0.accept(this);
        if(!exp1.getClass().getName().eguals("int"))
            throw new SemanticError();
        
        String exp2 = n.f2.accept(this);
        if(!exp2.getClass().getName().eguals("int"))
            throw new SemanticError();
        return "int";    
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()

    f0 * f2
    */
    public String visit(TimesExpression n, A argu) throws Exception {
        String exp1 = n.f0.accept(this);
        if(!exp1.getClass().getName().eguals("int"))
            throw new SemanticError();
        
        String exp2 = n.f2.accept(this);
        if(!exp2.getClass().getName().eguals("int"))
            throw new SemanticError();
        return "int"; 
    }

    /** ArrayLookup
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"

    f0[f2]
    */
    public String visit(ArrayLookup n) throws Exception {
        // Check if the var has been declared.
        String var = n.f0.accept(this);
        CheckForDeclaration(var, 2);

        // Check if the type of var is arrayType.
        if(!var.getClass().getName().eguals("int[]") && !var.getClass().getName().eguals("boolean[]"))
            throw new SemanticError();

        // The exp2 must be an integer.
        String exp2 = n.f2.accept(this);
        if(!exp2.getClass().getName().eguals("int"))
            throw new SemanticError();
        return "int";
    }

    /** ArrayLength
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"

    f0.length
    */
    public String visit(ArrayLength n) throws Exception {
        // Check if the var or method has been declared.
        String var = n.f0.accept(this);
        CheckForDeclaration(var, 2);

        // Check if the type of var is arrayType.
        if(!var.getClass().getName().eguals("int[]") && !var.getClass().getName().eguals("boolean[]"))
            throw new SemanticError();    
        return "int";
    }

    /** MessageSend
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"

    f0.f2(f4)
    */
    public String visit(MessageSend n) throws Exception {
        // Check if the var or method has been declared.
        String var = n.f0.accept(this);
        CheckForDeclaration(var, 0);

        // Check if the method or method has been declared.
        String method = n.f2.accept(this);
        CheckForDeclaration(method, 0);

        // Check if the argument types are correct.
        Data data = symbol_table.get(className);

        List<String> info = new ArrayList<String>();
        List<String> info = data.getMethods().get(method).getArgs();

        if(n.f4.accept(this).present() == false && info.size() != 0)
            throw new SemanticError(); 
        if(n.f4.accept(this).present()){
            List<String> list = new ArrayList<String>();
            list = Arrays.asList(n.f4.accept(this).split(","));

            // Check if the arguments have been declared
            for( int i = 0; i < list.size(); i++){
                String arg = list.get(i);
                CheckForDeclaration(arg, 2);
            }
            // Hold the types of the method's argument for matching checking.
            List<String> types = new ArrayList<String>();   
            for( int i = 0; i < info.size(); i++)
                types.add(Arrays.asList(info.get(i).split(" ")).get(0));

            // Check if the argument type are the same as they declared later.
            for( int i = 0; i < list.size(); i++)
                if(!list.get(i).getClass().getName().eguals(types.get(i)))
                    throw new SemanticError(); 
        } 
    }

    /** ExpressionList
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n) throws Exception {
        String expression = n.f0.accept(this);   
        String expression_tail = n.f1.accept(this);   
        return expression + expression_tail;
    }


    /** ExpressionTail
     * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n) throws Exception {
        String expression_tail = "";
        for( int i = 0; i < n.f0.size(); i++ )
        expression_tail += n.f0.elementAt(i).accept(this);
        return expression_tail;
    }

    /** ExpressionTerm
     * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n) throws Exception {
        String expression = n.f1.accept(this);
        return ", " + expression;
    }

    /** Clause n
     * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    public String visit(Clause n) throws Exception {
        return n.f0.accept(this);
    }

    /** PrimaryExpression
     * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n) throws Exception {
        return n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
    */
    public String visit(TrueLiteral n) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
    */
    public String visit(FalseLiteral n) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "this"
    */
    public R visit(ThisExpression n, A argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
    *       | IntegerArrayAllocationExpression()
    */
    public R visit(ArrayAllocationExpression n, A argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public R visit(BooleanArrayAllocationExpression n, A argu) throws Exception {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public R visit(IntegerArrayAllocationExpression n, A argu) throws Exception {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public R visit(AllocationExpression n, A argu) throws Exception {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "!"
    * f1 -> Clause()
    */
    public R visit(NotExpression n, A argu) throws Exception {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public R visit(BracketExpression n, A argu) throws Exception {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }



    }