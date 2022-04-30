import java.util.Map;

import javax.swing.text.html.HTMLDocument.BlockElement;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import visitor.GJNoArguDepthFirst;
import syntaxtree.*;

public class TypeChecker extends GJNoArguDepthFirst< String >{
    private String className;                   // The className of the class we are into 
    private String methodName;                  // The methodName of the class we are into 
    // private List <String> varsInScope;          // A list that holds every variable that we can use
    private Map <String, Data> symbol_table;    // The symbol table we construct later with the DeclCollector

    // Constructor
    TypeChecker(Map <String, Data> symbol_table){
        this.symbol_table = symbol_table;
        this.className = null;
    }

    boolean isPrimitive(String type){
        if(type != "int[]" && type != "boolean[]" && type != "boolean" && type != "int")
            return false;
        return true;
    }

    private void CheckForDeclaration(String value, int mode) throws Exception {
        Boolean varFlag = false;
        Boolean methodFlag = false;
        Boolean classFlag = false;
        if(mode == 0){
            varFlag = CheckForVarDeclaration(value, false);
            if(!varFlag)
                throw new SemanticError();
        }
        else if(mode == 1){
            methodFlag = CheckForMethodDeclaration(value);
            if(!methodFlag)
                throw new SemanticError();
        }
        else if(mode == 2){
            classFlag = CheckForClassDeclaration(value);
            if(!classFlag)
                throw new SemanticError();
        }
        else if (mode == 3){
            classFlag = CheckForVarDeclaration(value, false);
            methodFlag = CheckForMethodDeclaration(value);
            if(!varFlag && !methodFlag)
                throw new SemanticError();
        }
        else{
            varFlag = CheckForVarDeclaration(value, false);
            methodFlag = CheckForMethodDeclaration(value);
            classFlag = CheckForClassDeclaration(value);
            if(!varFlag && !methodFlag && !classFlag)
                throw new SemanticError();
        }
    }

    private boolean CheckForVarDeclaration(String var, boolean parentFlag){
        String curClassName = className;
        boolean flag = false;
        Data data = symbol_table.get(className);
        String parentClassName = symbol_table.get(className).getName();
        // Check for decleration recursevly to the parent classes.
        if(parentFlag == false){
            if(data.getVars().containsKey(var) || (methodName != null && data.getMethods().get(methodName).getVars().containsKey(var)) || (methodName != null && data.getMethods().get(methodName).getArgs().getcontains(var)))
                return true;
        }
        if( parentClassName == null)
            return false;
        do{

        }while(parentClassName != null);
        // else if(parentClassName != null) {
        //     if(symbol_table.get(parentClassName).getVars().containsKey(var))
        //         return true;
        //     className = parentClassName;
        //     flag = CheckForVarDeclaration(var, true);
        // }
        className = curClassName;
        return flag;
    }
    
    private boolean CheckForMethodDeclaration(String method){
        String curClassName = className;
        boolean flag = false;
        Data data = symbol_table.get(className);
        String parentClassName = symbol_table.get(className).getName();
        if( parentClassName == null){
            if (data.getMethods().containsKey(method))
                return true;
            else return false;
        }
        else if( parentClassName != null){
            if (data.getMethods().containsKey(method))
                return true;
            else{ 
                className = parentClassName;
                flag = CheckForMethodDeclaration(method);
            }
        }
        className = curClassName;
        return flag;
    }

    private boolean CheckForClassDeclaration(String class_){
        if(symbol_table.containsKey(class_))
            return true;
        return false;
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
        className = n.f1.accept(this);

        // Go down through the parse Tree
        for( int i = 0; i < n.f14.size(); i++ )
            n.f14.elementAt(i).accept(this);

        for( int i = 0; i < n.f15.size(); i++ )
            n.f15.elementAt(i).accept(this);

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

        // Go down through the parse Tree
        for( int i = 0; i < n.f3.size(); i++ )
            n.f3.elementAt(i).accept(this);

        for( int i = 0; i < n.f4.size(); i++ )
            n.f4.elementAt(i).accept(this);
        
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

        return null;
    }

    /** VarDeclaration
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n) throws Exception {
        // Keep  the type off the var and go down into the parse tree.
        String var_type = n.f0.accept(this);
        n.f1.accept(this);

        // The type of the variable(class) has not been declared.
        if(!isPrimitive(var_type) && !symbol_table.containsKey(var_type))
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
        String ret_type = n.f1.accept(this);  

        String method_name = n.f2.accept(this); 
        methodName = method_name;

        // We need to check if type is different from (int, boolean, int[], boolean[]) or the other non-primitive types.
        // if it is => Throw Semantic Error!
        if(!isPrimitive(ret_type) && !symbol_table.containsKey(ret_type))
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
        if(!(n.f10.accept(this)).equals(ret_type)) // μπορει να ειναι και υποτυπος.
            throw new SemanticError();
        
        methodName = null;
        return null;
    }

    /** FormalParameterList
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    // It will go and fill up recursivly the args field of the method_info field of the Data class
    public String visit(FormalParameterList n) throws Exception {
        n.f0.accept(this);   
        n.f1.accept(this);   
        return null;
    }

    /** FormalParameter
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n) throws Exception {
        String type = n.f0.accept(this);   
        String name = n.f1.accept(this);

        // We need to check if type is different from (int, boolean, int[], boolean[]) or the other non-primitive types.
        // if it is => Throw Semantic Error!
        if(!isPrimitive(type) && !symbol_table.containsKey(type))
            throw new SemanticError();
        return null;
    }

    /** FormalParameterTail
    * f0 -> ( FormalParameterTerm() )*
    */
    public String visit(FormalParameterTail n) throws Exception {
        for( int i = 0; i < n.f0.size(); i++ )
            n.f0.elementAt(i).accept(this);
        return null;
    }

    /** FormalParameterTerm
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    public String visit(FormalParameterTerm n) throws Exception {   
        n.f1.accept(this);
        return null;
    }

    /** Type
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n) throws Exception {
        return n.f0.accept(this);
    }

    /** ArrayType
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    public String visit(ArrayType n) throws Exception {
        return n.f0.accept(this);
    }

    /** BooleanArrayType 
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(BooleanArrayType n) throws Exception {
        return "boolean[]";
    }

    /** IntegerArrayType
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(IntegerArrayType n) throws Exception {
        return "int[]";
    }

    /** BooleanType
    * f0 -> "boolean"
    */
    public String visit(BooleanType n) throws Exception {
        return "boolean";
    }

    /** IntegerType
    * f0 -> "int"
    */
    public String visit(IntegerType n) throws Exception {
        return "int";
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
        for( int i = 0; i < n.f1.size(); i++ )
            n.f1.elementAt(i).accept(this);
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
        String var = n.f0.accept(this);
        CheckForDeclaration(var, 0);
        
        String idType = symbol_table.get(className).getVars().get(var).getType();
        
        // Check if the type of the expression match the type of the identifier.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).equals(idType))
            throw new SemanticError();  //ypotupos
        
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
        String id = n.f0.accept(this);
        CheckForDeclaration(id, 3);
        
        String idType = symbol_table.get(className).getVars().get(id).getType();

        // Check if the type of the expression f2 is interger.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).equals("int"))
            throw new SemanticError();

        // Check if the type of the expression is either int or boolean 
        // cause only these two types of arrays we could have.
        // If not => Throw Semantic Error!
        String expType = n.f5.accept(this);
        if(!expType.equals("int") && !expType.equals("boolean"))
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
        // Check if the type of the expression is boolean.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).equals("boolean"))
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
        // Check if the type of the expression is boolean.
        // If not => Throw Semantic Error!
        if(!n.f2.accept(this).equals("boolean"))
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
        if(!expType.equals("int") && !expType.equals("boolean"))
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
    public String visit(AndExpression n) throws Exception {
        if(!n.f0.accept(this).equals("boolean") || !n.f2.accept(this).equals("boolean"))
            throw new SemanticError();
        return "boolean";
    }

    /** CompareExpression
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n) throws Exception {
        if(!n.f0.accept(this).equals("boolean") || !n.f2.accept(this).equals("boolean"))
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
        if(!n.f0.accept(this).equals("int") || !n.f2.accept(this).equals("int"))
            throw new SemanticError();
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()

    f0 - f2
    */
    public String visit(MinusExpression n) throws Exception {
        if(!n.f0.accept(this).equals("int") || !n.f2.accept(this).equals("int"))
            throw new SemanticError();
        return "int";   
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()

    f0 * f2
    */
    public String visit(TimesExpression n) throws Exception {
        if(!n.f0.accept(this).equals("int") || !n.f2.accept(this).equals("int"))
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
        String type = n.f0.accept(this);

        // Check if the type of var is arrayType.
        if(!type.equals("int[]") && !type.equals("boolean[]"))
            throw new SemanticError();

        // The exp2 must be an integer.
        String exp2 = n.f2.accept(this);
        if(!exp2.equals("int"))
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
        CheckForDeclaration(var, 3);

        // Check if the type of var is arrayType.
        if(!var.equals("int[]") && !var.equals("boolean[]"))
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
        // Check if the var or method have been declared.
        String var = n.f0.accept(this);
        if(!var.equals("this"))
            CheckForDeclaration(var, 4);

        // Check if the method has been declared.
        String method = n.f2.accept(this);
        if(CheckForClassDeclaration(var)){
            String curClassName = className;
            className = var;
            if(!CheckForMethodDeclaration(method)){
                className = curClassName;
                CheckForDeclaration(method, 1);
            }
        }

        // Check if the argument types are correct.
        Data data = symbol_table.get(className);

        List<String> info = new ArrayList<String>();
        info = data.getMethods().get(method).getArgs();

        if(n.f4.present() == false && info.size() != 0)
            throw new SemanticError(); 
        if(n.f4.present() == true && info.size() == 0)
            throw new SemanticError(); 
        // if(n.f4.present() && info.size() != 0){
        //     List<String> list = new ArrayList<String>();
        //     // Take the args on a list
        //     list = Arrays.asList(n.f4.accept(this).split(","));

        //     // Hold the types of the method's argument for matching checking.
        //     List<String> types = new ArrayList<String>();   
        //     for( int i = 0; i < info.size(); i++)
        //         types.add(Arrays.asList(info.get(i).split(" ")).get(0));

        //     // Check if the argument type are the same as they declared later.
        //     for( int i = 0; i < list.size(); i++)
        //         if(!list.get(i).equals(types.get(i))){
        //             throw new SemanticError();
        //         } 


        // }
        return data.getMethods().get(method).getType(); 
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
        // Check if the PrimaryExpression is an identifier.
        String var = n.f0.accept(this);
        if(n.f0.which == 3){
            CheckForDeclaration(var, 3); // Check for declaration
            // If it has been declared => find and return its type.
            String type = symbol_table.get(className).getVars().get(var).getType();
            return type;
        }
        else
            return var;
    }

    /** IntegerLiteral
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n) throws Exception {
        return "integer";
    }

    /** TrueLiteral
    * f0 -> "true"
    */
    public String visit(TrueLiteral n) throws Exception {
        return "boolean";
    }

    /** FalseLiteral
    * f0 -> "false"
    */
    public String visit(FalseLiteral n) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n) throws Exception {
        return n.f0.toString();
    }

    /** ThisExpression
    * f0 -> "this"
    */
    public String  visit(ThisExpression n) throws Exception {
        return "this";
    }

    /** ArrayAllocationExpression
    * f0 -> BooleanArrayAllocationExpression()
    *       | IntegerArrayAllocationExpression()
    */
    public String visit(ArrayAllocationExpression n) throws Exception {
        return n.f0.accept(this);
    }

    /** BooleanArrayAllocationExpression
    * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"

    new boolean[f3]
    */
    public String visit(BooleanArrayAllocationExpression n) throws Exception {
        // Check if the type of expression f3 is integer.
        // If not => Throw Semantic Error!
        if(!n.f3.accept(this).equals("int"))
            throw new SemanticError();
        return "boolean[]";
    }

    /** IntegerArrayAllocationExpressions
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"

    new int[f3]
    */
    public String visit(IntegerArrayAllocationExpression n) throws Exception {
        // Check if the type of expression f3 is integer.
        // If not => Throw Semantic Error!
        if(!n.f3.accept(this).equals("int"))
            throw new SemanticError();
        return "int[]";
    }

    /** AllocationExpression
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"

    new f1()
    */
    public String visit(AllocationExpression n) throws Exception {
        // Check if the identifier f1 has been declared as a class
        String id = n.f1.accept(this);
        boolean flag = false;
        for(String classname : symbol_table.keySet())
            if(id.equals(classname))
                flag = true;
        if(!flag)
            throw new SemanticError();
        return id.toString();
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()

    !f1
    */
    public String visit(NotExpression n) throws Exception {
        if(!n.f0.accept(this).equals("boolean"))
            throw new SemanticError();
        return "boolean";
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"

    (f1)
    */
    public String visit(BracketExpression n) throws Exception {
        return n.f1.accept(this);
    }
}