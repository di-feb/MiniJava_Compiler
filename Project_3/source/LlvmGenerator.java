import java.util.Map;
import java.util.LinkedHashMap;
import visitor.GJNoArguDepthFirst;
import java.util.Queue;
import java.util.LinkedList;
import syntaxtree.*;
import java.io.*;

public class LlvmGenerator extends GJNoArguDepthFirst< String >{
    private static int registerCounter;             // A counter for the register we produced
    private static int labelCounter;                // A counter for the labels
    private BufferedWriter filename;                // The filename to write the llvm code
    private String className;                       // The className of the class we are into 
    private String methodName;                      // The methodName of the class we are into 
    private Map <String, Data> symbol_table;        // The symbol table we construct later with the DeclCollector
    private LinkedHashMap <String, RegInfo> ids;    // Holds infos about identifiers (variables, arguments, fields) 
    private LinkedList<String> messageSendQueue;    // A queue that holds the classNames, or object names for every message sent.

    // Constructor
    LlvmGenerator(BufferedWriter fout, Map <String, Data> symboltable, LinkedList<String> queue){
        ids = new LinkedHashMap <String, RegInfo>();
        filename = fout;
        symbol_table = symboltable;
        messageSendQueue = queue;
        className = null;
        methodName = null;
        registerCounter = 0;
        labelCounter = 0;
    }

    private void printIntoLlFile(String str){
        try{ filename.write(str + "\n"); }

		catch(IOException ex){ System.err.println(ex.getMessage()); }
    }

    private int getRegisterCounter() {
        return registerCounter;
    }

    // Check if the given type is primitive.
    private boolean isPrimitive(String type){
        if(!type.equals("int") && !type.equals("boolean"))
            return false;
        return true;
    }

    // Get the type of the variable.
    // Precedence: method variables, method args > classe's fields.
    private String getVarType(String var, String classname){
        Data data = symbol_table.get(classname);
        
        if(methodName != null && data.getMethods().get(methodName) != null){
            if (data.getMethods().get(methodName).getVars().containsKey(var))
                return data.getMethods().get(methodName).getVars().get(var);
            if(data.getMethods().get(methodName).getArgs().containsKey(var))
                return data.getMethods().get(methodName).getArgs().get(var);
        }
        if(data.getVars().containsKey(var))
            return data.getVars().get(var).getType();
        return null;
    }

    // Create a new register and update register counter.
    String createRegister(){
        return "%_" + registerCounter++;
    }

    // Create a new label update label counter.
    String createLabel(String typeOflabel){
        return "%" + typeOflabel + String.valueOf(labelCounter++);
    }

    // Find the register of an identifier if exists.
    // If not, create a new one.
    // Returns a RegInfo
    RegInfo find_createRegister(String id, String type, boolean rvalue){
        // if the identifier exist inside the map return its info
        if(ids.containsKey(id))
            return ids.get(id);

        // if not it means that the id is a field of a class so,
        // get offset of the identifier.
        int offset = symbol_table.get(className).getVars().get(id).getOffset();

        // Create a new register.
        String new_register = createRegister();

        // set a new pointer to that field
        printIntoLlFile(new_register + " = getelementptr i8, i8* %this, i32 " + offset + "\t");

        // if the size of the field is different from i8, cast it to the right one
        if(!"i8".equals(type))
            printIntoLlFile("\t" + createRegister() + " = bitcast i8* " + new_register + " to " + type + "*\t");

        // if we are into a right value expression aka want the content of the field:
        String new_type;
        if(rvalue){
            printIntoLlFile(createRegister() + " = load " + type + ", " + type + "* %"  + (getRegisterCounter()-2));
            new_type = type + "*";
            type = new_type;
        }
        // create a new regInfo
        RegInfo reginfo = new RegInfo("%_" + (getRegisterCounter()-1), type);
        ids.put(id, reginfo);
        return reginfo;
    }

    // Make the coversion from a java type to llvm.
    String produceConversion(String type){
        if(type.equals("boolean"))
            return "i1";
        return "i32";
    }

    // Convert the return type to llvm type.
    String convertType(String type){
        if(isPrimitive(type))
            return produceConversion(type);
        else
            return "i8*";
    }

    // Check if the types of an assignment are similar.
    RegInfo checkTypeMatching(String leftType, String leftReg, String rightType){
        if(!leftType.equals(rightType + "*")){
            leftType = rightType + "*";            
            leftReg = "%_" + (getRegisterCounter()-1);
        }
        return new RegInfo(leftReg, leftType); 
    }

    // Prints the code for checking if the index is oob
    void checkOOB(String index, String length, boolean booleanArray){
        if(!booleanArray){
            printIntoLlFile("\t" + createRegister() + " = icmp slt i32 " + index + ", 0");
            printIntoLlFile("\t" + createRegister() + " = icmp slt i32 " + index + ", " + length +"\t");
            printIntoLlFile("\t" + createRegister() + " = xor i1 %_" + (getRegisterCounter()-3) + ", %_" + (getRegisterCounter()-2) +"\t" );
            printIntoLlFile("\tbr i1 %_" + (getRegisterCounter()-1) + ", label %" + createLabel("oob") + ", label %" + createLabel("oob") + "\n" + createLabel("oob") + ":"); 
            printIntoLlFile("\tcall void @throw_oob()\n\tbr label %" + createLabel("oob") + "\n\n" + createLabel("oob") + ":");
        }
        else{
            printIntoLlFile("\t" + createRegister() + " = icmp slt i1 " + index + ", 0");
            printIntoLlFile("\t" + createRegister() + " = icmp slt i1 " + index + ", " + length +"\t");
            printIntoLlFile("\t" + createRegister() + " = xor i1 %_" + (getRegisterCounter()-3) + ", %_" + (getRegisterCounter()-2) +"\t" );
            printIntoLlFile("\tbr i1 %_" + (getRegisterCounter()-1) + ", label %" + createLabel("oob") + ", label %" + createLabel("oob") + "\n" + createLabel("oob") + ":"); 
            printIntoLlFile("\tcall void @throw_oob()\n\tbr label %" + createLabel("oob") + "\n\n" + createLabel("oob") + ":");
        }

    }

    // loads the element of a specific index of an array
    // returns a string "type, register"
    String loadArraysElement(String register, String index, boolean booleanArray){
        if(booleanArray){
            printIntoLlFile("\t "+ createRegister() + " = bitcast i8* %_" + (getRegisterCounter() - 1) + " to i1*");
            printIntoLlFile("\t "+ createRegister() + " = getelementptr i1, i1* %_" + (getRegisterCounter()-2) + ", i1 " + index);
            printIntoLlFile("\t "+ createRegister() + " = load i1, i1* %_" + (getRegisterCounter()-2 + "\t"));
            return "i1 %_" + (getRegisterCounter()-1);
        }
        else{
            printIntoLlFile("\t "+ createRegister() + " = bitcast i8* %_" + (getRegisterCounter() - 1) + " to i32*");
            printIntoLlFile("\t "+ createRegister() + " = getelementptr i32, i32* %_" + (getRegisterCounter()-2) + ", i32 " + index);
            printIntoLlFile("\t "+ createRegister() + " = load i32, i32* %_" + (getRegisterCounter()-2 + "\t"));
            return "i32 %_" + (getRegisterCounter()-1);
        }

        
    }

    // Check if the index of an array is in bounds.
    void checkIndex(String reg, String index, boolean rvalue, boolean booleanArray){
        String length; // length of the array

        // Check if the array is a rvalue expression.
        // If it is => it is loaded
        // else => its is not loaded and we need to load it
        if(!rvalue){
            // always load from a generic type i8* beacuse we dont know the actual type yet
            printIntoLlFile(createRegister() + " = load i8*, i8** " + reg + "\t");
            length = loadArraysElement(reg, index, booleanArray);
        }
        else
            length = loadArraysElement(reg, index, booleanArray);
        
        // Print the code for checking if the index is oob
        checkOOB(index, length, booleanArray);
    }

    void storeIntoArray(String reg, String type, boolean booleanArray, String expr, String index){
        if(booleanArray){
            printIntoLlFile("\t "+ createRegister() + " = load i8*, " + type + " " + reg);
            printIntoLlFile("\t "+ createRegister() + " = bitcast i8* %_" + (getRegisterCounter() -2) + " to i1*");
            printIntoLlFile("\t "+ createRegister() + " = getelementptr i1, i1* %_" + (getRegisterCounter() -2) + " , i1 " + index);
            printIntoLlFile("\tstore " + expr + ", i1* %_" + (getRegisterCounter() -1));
        }
        else{
            printIntoLlFile("\t "+ createRegister() + " = load i8*, " + type + " " + reg);
            printIntoLlFile("\t "+ createRegister() + " = bitcast i8* %_" + (getRegisterCounter() -2) + " to i32*");
            printIntoLlFile("\t "+ createRegister() + " = getelementptr i32, i32* %_" + (getRegisterCounter() -2) + " , i32 " + index);
            printIntoLlFile("\tstore " + expr + ", i32* %_" + (getRegisterCounter() -1));
        }
    }

    // Index 0 has the length of the array, so add one to the curr index to get the 
    // actual address we need.
    String getCorrectIndex(String index){
        // Check if we get an integer or a register
        if( index.startsWith("%")){ // if we get a register, create a new one, add one to it
            printIntoLlFile(createRegister() + " = add i32 " + index + ", 1");
            return "%_" + (getRegisterCounter() - 1);
        }
        // cast the string index to int, add one to it, and cast it again to string
        return String.valueOf(Integer.parseInt(index) + 1);
    }

////////////////////////////////// --For V-TABLE--///////////////////////////////////////////////

    // Convert the args for a method declaration to llvm form
    String convertArgsForDeclaration(Map<String, String> args){
        // The first argument to every method (even if the method has no argumnets) 
        // is always the "this" pointer
        String args_as_string = "i8*" ; 
        int counter = 0;
        for(Map.Entry<String, String> entry : args.entrySet()){
            counter++;
            if(args.size() >= counter )
                args_as_string += ", ";
            args_as_string += convertType(entry.getValue());
        }
        return args_as_string;
    }

    // Convert the args for a method definition to llvm form
    String convertArgsForDefinition(Map<String, String> args){
        // The first argument to every method (even if the methodhaw no argumnets) 
        // is always the "this" pointer
        String args_as_string = "i8* %this, " ; 
        int counter = 0;
        for(Map.Entry<String, String> entry : args.entrySet()){
            String currArg = "";
            counter++;
            currArg = convertType(entry.getValue()) + " %." + entry.getKey();
            if(args.size() > counter )
                currArg += ", ";
            args_as_string += currArg;
        }
        return args_as_string;
    }

    // Take the info for each method, convert them a llvm form 
    // Returns a string with the type and name of each method (for the v-table)
    String getMethods(String className, Map<String, MethodInfo> methods){
        String methods_as_string = ""; 
        int counter = 0;
        for(Map.Entry<String, MethodInfo> entry : methods.entrySet()){
            counter++;
            String ret_type = convertType(entry.getValue().getType());
            String args = convertArgsForDeclaration(entry.getValue().getArgs());
            methods_as_string += "i8* bitcast (" + ret_type + "(" + args +")" + " @" + className + "." + entry.getKey() + " to i8*)";
            if(methods.size() > counter )
                methods_as_string += ", ";
        }
        return methods_as_string;

    }

    // Declares a global v-talbe.
    void delcareVtable(){
        for(Map.Entry<String, Data> entry : symbol_table.entrySet()){
            String className = entry.getKey();
            Map<String, MethodInfo> methods = entry.getValue().getMethods();
            printIntoLlFile("@." + className + "_vtable = global [" + methods.size() + " x i8*] [" + getMethods(className, methods) + "]\t");
        }
    }

    // Print some external methods.
    void includeFuncts(){
        printIntoLlFile("\n"
            + "declare i8* @calloc(i32, i32)\n"
            + "declare i32 @printf(i8*, ...)\n"
            + "declare void @exit(i32)\n\n"
            
            + "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"
            + "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"
            + "define void @print_int(i32 %i) {\n"
            +    "\t%_str = bitcast [4 x i8]* @_cint to i8*\n"
            +    "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"
            +    "\tret void\n}\n\n"
            
            + "define void @throw_oob() {\n"
            +    "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n"
            +    "\tcall i32 (i8*, ...) @printf(i8* %_str)\n"
            +    "\tcall void @exit(i32 1)\n"
            +    "\tret void\n}\n\n");
    }
    
    /** Goal
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    */
    public String visit(Goal n) throws Exception {
        // Declare a public v-table 
        delcareVtable();
        // Declare some functions
        includeFuncts();

        className = n.f0.accept(this);

        
        for( int i = 0; i < n.f1.size(); i++ )
            n.f1.elementAt(i).accept(this); // Go down through the parse Tree

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
        printIntoLlFile("define i32 @main() {\n\t"); // define

        // Go down through the parse Tree
        for( int i = 0; i < n.f14.size(); i++ )
            n.f14.elementAt(i).accept(this);

        for( int i = 0; i < n.f15.size(); i++ )
            n.f15.elementAt(i).accept(this);
        printIntoLlFile("\tret i32 0\n}");
        return null;
    }

    /** TypeDeclaration
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    public String visit(TypeDeclaration n) throws Exception {
        n.f0.accept(this);  // Go down through the parse Tree
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
        // Keep the name of the class we are into
        className = n.f1.accept(this);

        // We dont want to go over the fields declaration of the class
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
        // Keep the name of the class we are into
        className = n.f1.accept(this);

        // We dont want to go over the fields declaration of the class
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
        // Keep  the type of the var at an llvm form
        String var_type = convertType(n.f0.accept(this));

        // Keep the name of the var
        String name = n.f1.accept(this);

        printIntoLlFile("\t%" + name + " = alloca " + var_type); // alloca
        RegInfo info = new RegInfo("%" + name, var_type);
        ids.put(name, info); // keep the info of that variable
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
        // Get methods return type at llvm form.
        String method_RetType = convertType(n.f1.accept(this));  
        
        // Get methods name and update methodName just to 
        // know at what method we are into.
        String method_name = n.f2.accept(this); 
        methodName = method_name;

        LinkedHashMap<String, String> args = symbol_table.get(className).getMethods().get(methodName).getArgs();
        if(args != null){
            printIntoLlFile("define " + method_RetType + " @" + className + "." + methodName + "(" + convertArgsForDefinition(args) + "){");
            for(Map.Entry<String, String> entry : args.entrySet()){
                String key = entry.getKey();                        // Get the name of the argument 
                String value = convertType(entry.getValue());       // Get the type of the argument at a llvm form
                printIntoLlFile("\t%" + key + " = alloca " + value + "\t");    // alloca
                printIntoLlFile("\tstore " + value + " %." + key + ", " + value + "* %" + key); // store

                // store the variable into ids.
                RegInfo info = new RegInfo("%" + key, value);
                ids.put(key, info);
            }
        }
        if(n.f4.present())
            n.f4.accept(this);

        // Accept to go through the parse tree 
        for( int i = 0; i < n.f7.size(); i++ )
            n.f7.elementAt(i).accept(this);
        for( int i = 0; i < n.f8.size(); i++ )
            n.f8.elementAt(i).accept(this);
        
        // Get type of returned value at llvm form.
        String value_RetType = n.f10.accept(this);
        printIntoLlFile("\tret " + value_RetType + "\n}\n");
        
        methodName = null;
        return null;
    }

    /** FormalParameterList
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */

    // Go through the parse Tree we have already collect the data
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
        n.f0.accept(this);   
        n.f1.accept(this);

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

    // Go through the parse Tree
    public String visit(Statement n) throws Exception {
        n.f0.accept(this);
        return null; 
    }

    /** Block
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"

    { f1 }
    */

    public String visit(Block n) throws Exception {
        // Go through the parse Tree
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
        // Get the id of the identifier
        String lid = n.f0.accept(this);

        // Find its type and register
        String type = getVarType(lid, className);
        RegInfo info = find_createRegister(lid, type, false); // if id is a field,we dont have info for that
        String lreg = info.getRegister();
        String lType = info.getType();

        // Get leftExpressions type and register
        String rexp = n.f2.accept(this);
        String rType = rexp.split(" ")[0];
        String rvalue = rexp.split(" ")[1];
        
        // Check if the types of the left id, right expression are matching
        // if not, cast the left ids type and return a regInfo with 
        // the info for that id.
        RegInfo reginfo = checkTypeMatching(lType, lreg, rType);
        lType = reginfo.getType();
        lreg = reginfo.getRegister();
        printIntoLlFile("\tstore " + rType + " " + rvalue +  ", " + lType + " %" + lid);
 
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
        // Get the id of the identifier and find its type, register
        String lid = n.f0.accept(this); 

        RegInfo info = find_createRegister(lid, convertType(getVarType(lid, className)), false);
        String lreg = info.getRegister();
        String lType = info.getType();
        
        // Get the index of the array ( returns an register with the index of the array)
        // or the index itself
        String index = getCorrectIndex(n.f2.accept(this).split(" ")[1]); 

        // Check if the index is in bounds and act accordingly.
        checkIndex(lreg, index, false, lType.equals("i1"));

        String expr = n.f5.accept(this); 
        // Store the value of the expr f5 inside the array.
        storeIntoArray(lreg, lType, lType.equals("i1"), expr, index );
        
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
        // Get the condition
        String cond = n.f2.accept(this);

        // Make the labels
        String if_0 = createLabel("if");
        String if_1 = createLabel("if");
        String if_2 = createLabel("if");
        
        //if(condition)
        printIntoLlFile("\tbr " + cond + ", label " + if_0 + ", label " + if_1);
        printIntoLlFile(if_0 + ":");

        // Do that
        n.f4.accept(this);
        printIntoLlFile("\tbr label " + if_2);
        printIntoLlFile(if_1 + ":");

        // Else do this
        n.f6.accept(this);
        printIntoLlFile("\tbr label " + if_2);
        printIntoLlFile(if_2 + ":");

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
        // Make the labels 
        String while_0 = createLabel("loop");
        String while_1 = createLabel("loop");

        // Go to while loop to check the condition
        printIntoLlFile("\tbr label " + while_0);
        printIntoLlFile(while_0 + ":");

        // Get the condition
        String cond = n.f2.accept(this);

        // Go to the loop to execute the statement
        printIntoLlFile("\tbr " + cond + ", label " + while_0 + ", label " + while_1);
        printIntoLlFile(while_1 + ":");

        // Get the statement
        n.f4.accept(this);

        return null;
    }


    /** PrintStatement 
    * f0 -> "printIntoLlFile"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n) throws Exception {
        // Get type and register of the expr as string.
        String expr = n.f2.accept(this);

        // Get the type of the expr.
        String type = expr.split(" ")[0];

        // Find about the type. Is either boolean or integer.
        if(type.equals("i1"))
            printIntoLlFile("\tcall void (" + type + ") @print_bool(" + expr +")");
        else
            printIntoLlFile("\tcall void (" + type + ") @print_int(" + expr +")");

        return null;
}


    /** Expression
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | Arraylength()
    *       | MessageSend()
    *       | Clause()
    */

    // Go through the parse Tree
    public String visit(Expression n) throws Exception {
        return n.f0.accept(this);
    }

    /** AndExpression   
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */

    public String visit(AndExpression n) throws Exception {
        // Make the labels 
        String and_0 = createLabel("andclause");
        String and_1 = createLabel("andclause");

        // If the first clause if false dont even check the second one.
        // Just jump into the next lebal.
        String clause_1 = n.f0.accept(this);
        String clause_1_reg = clause_1.split(" ")[1];   // get the register
        printIntoLlFile("\tbr " + clause_1 + ", label " + and_0 + ", label " + and_1);
        printIntoLlFile(and_0 +":");
        printIntoLlFile("\tbr label " + and_1);
        printIntoLlFile(and_1 +":");

        // Get the second clause
        String clause_2 = n.f2.accept(this);
        String clause_2_reg = clause_2.split(" ")[1];   // get the register

        // Use phi to go to the correct label according to the values of the clauses.
        printIntoLlFile("\t" + createRegister() + " = phi i1 [" + clause_2_reg + ", %" + and_0 + "]," + "[" + clause_1_reg + ", %" + and_1 + "]");

        return "i1 %_" + (getRegisterCounter()-1);
    }

    /** CompareExpression
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n) throws Exception {

        String left_expr = n.f0.accept(this);

        String right_expr = n.f2.accept(this);
        printIntoLlFile("\t" + createRegister() + " = icmp slt " + left_expr + ", " + right_expr.split(" ")[1]); 
        return "i1 %_" + (getRegisterCounter() - 1);
    }

    /** PlusExpression
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()

    f0 + f2
    */
    public String visit(PlusExpression n) throws Exception {
        String left_expr = n.f0.accept(this);

        String right_expr = n.f2.accept(this);
        printIntoLlFile("\t" +createRegister() + " = add " + left_expr + ", " + right_expr.split(" ")[1]); 
        return "i32 %_" + (getRegisterCounter() - 1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()

    f0 - f2
    */
    public String visit(MinusExpression n) throws Exception {
        // Exp should be type int
        String left_expr = n.f0.accept(this);

        String right_expr = n.f2.accept(this);
        printIntoLlFile("\t" + createRegister() + " = sub " + left_expr + ", " + right_expr.split(" ")[1]); 
        return "i32 %_" + (getRegisterCounter() - 1); 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()

    f0 * f2
    */
    public String visit(TimesExpression n) throws Exception {
        String left_expr = n.f0.accept(this);

        String right_expr = n.f2.accept(this);
        printIntoLlFile("\t" + createRegister() + " = mul " + left_expr + ", " + right_expr.split(" ")[1]); 
        return "i32 %_" + (getRegisterCounter() - 1);
    }

    /** ArrayLookup
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"

    f0[f2]
    */
    public String visit(ArrayLookup n) throws Exception {
        // Get array type and register
        String exp = n.f0.accept(this);
        String expType = exp.split(" ")[0];
        String expReg = exp.split(" ")[1];

        // Get the index
        String index = getCorrectIndex(n.f2.accept(this).split(" ")[1]); 

        // Return the register that holds the address of the index element
        checkIndex(expReg, index, true, expType.equals("i1"));
        return loadArraysElement(expReg, index, expType.equals("i1"));

                
    }

    /** ArrayLength
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"

    f0.length
    */
    public String visit(ArrayLength n) throws Exception {
        String exp = n.f0.accept(this);
        String expType = exp.split(" ")[0];
        String expReg = exp.split(" ")[1];
        
        return loadArraysElement(expReg, "0", expType.equals("i1"));
        
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
        // Get the register and type of expression
        String exp = n.f0.accept(this);
        String methodType;
        int methodOffset;

        // Get method name, retType and offset
        String methodName = n.f2.accept(this);
        String obj = messageSendQueue.removeFirst();
        methodType = convertType(symbol_table.get(obj).getMethods().get(methodName).getType());
        methodOffset = symbol_table.get(obj).getMethods().get(methodName).getOffset();

        printIntoLlFile("\t" + createRegister() + " = bitcast " + exp + " to i8***"); 
        printIntoLlFile("\t" + createRegister() + " = load i8**, i8*** %_" + (getRegisterCounter()-2));
        printIntoLlFile("\t" + createRegister() + " = getelementptr i8*, i8** %_" + (getRegisterCounter()-2) + ", i32 " + methodOffset);
        printIntoLlFile("\t" + createRegister() + " = load i8*, i8** %_" + (getRegisterCounter()-2));

        String args = "";  // For method argument
        String onlyTypes = ""; // Holds only the type of the arguments
        if(n.f4.present()){
            args = exp + ",";
            args += n.f4.accept(this);
            String[] argsInfos = args.split(",");
            
        // always one argument (this)
            if(argsInfos.length == 1)
                onlyTypes = "i8*";
            else{
                for(int i = 0; i < argsInfos.length; i++){
                    onlyTypes += argsInfos[i].split(" ")[0];
                    if(i + 1 < argsInfos.length)
                        onlyTypes += ", ";
                }
            }
        }
        else{
            args = exp;
            onlyTypes = "i8*";
        }
        
        printIntoLlFile("\t" + createRegister() + " = bitcast i8* %_" + (getRegisterCounter()-4) + " to " + methodType + " (" + onlyTypes + ")*");
        printIntoLlFile("\t" + createRegister() + " = call " + methodType + " %_" + (getRegisterCounter()-2) + "(" + args + ")");
        return methodType + " %_" + (getRegisterCounter()-1);	   
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
        return "," + expression;
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
        String exp = n.f0.accept(this);
        RegInfo info = null;
        if(n.f0.which == 3){
            // If an id has been declared => find its type and return a "name type register" string.
            info = find_createRegister(exp, convertType(getVarType(exp, className)), true);
            printIntoLlFile("\t" + createRegister() + " = load " + info.getType() + ", " + info.getType() + "* " + info.getRegister());
            return info.getType() + " %_" + (getRegisterCounter() - 1);
        }
        return exp;
    }
    

    /** IntegerLiteral
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n) throws Exception {
        return "i32 " + n.f0;
    }

    /** TrueLiteral
    * f0 -> "true"
    */
    public String visit(TrueLiteral n) throws Exception {
        return "i1 1";
    }

    /** FalseLiteral
    * f0 -> "false"
    */
    public String visit(FalseLiteral n) throws Exception {
        return "i1 0";
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
    // Return the name of the class we are into
    public String  visit(ThisExpression n) throws Exception {
        return "i8* %this";
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
        //Get the size of the array
        String size = n.f3.accept(this).split(" ")[1];

        printIntoLlFile("\t" + createRegister() + " = add i1 " + size + ", 1");
        printIntoLlFile("\t" + createRegister() + " = call i8* @calloc(i1 4, i1 %_" + (getRegisterCounter()-2) + ")" );
        printIntoLlFile("\t" + createRegister() + " = bitcast i8* %_" + (getRegisterCounter()-2) + " to i1*");
        printIntoLlFile("\tstore i1 " + size + ", i1* %_" + (getRegisterCounter()-1));

        return  "i1* %_" + (getRegisterCounter()-1);
            
    }

    /** IntegerArrayAllocationExpression
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"

    new int[f3]
    */
    public String visit(IntegerArrayAllocationExpression n) throws Exception {
        //Get the size of the array
        String size = n.f3.accept(this).split(" ")[1];

        printIntoLlFile("\t" + createRegister() + " = add i32 " + size + ", 1");
        printIntoLlFile("\t" + createRegister() + " = call i8* @calloc(i32 4, i32 %_" + (getRegisterCounter()-2) + ")" );
        printIntoLlFile("\t" + createRegister() + " = bitcast i8* %_" + (getRegisterCounter()-2) + " to i32*");
        printIntoLlFile("\tstore i32 " + size + ", i32* %_" + (getRegisterCounter()-1));
        
        return  "i32* %_" + (getRegisterCounter()-1);
    }

    /** AllocationExpression
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"

    new f1()
    */
    public String visit(AllocationExpression n) throws Exception {
        // Get className
        String id = n.f1.accept(this);
        int size = 8;
        int numOfmethods = symbol_table.get(id).getMethods().size();

        Map<String, VarInfo> vars = symbol_table.get(className).getVars();
        if(vars.size() > 0){
            for(Map.Entry<String, VarInfo> entry : vars.entrySet()){
                String type = entry.getValue().getType();
                if(type.equals("i1")) 
                    size += 1;
                else if(type.equals("i32"))
                    size += 4;
                else
                    size += 8;
            }
        }

        printIntoLlFile("\t" + createRegister() + " = call i8* @calloc(i32 1, i32 " + String.valueOf(size) + ")");
        printIntoLlFile("\t" + createRegister() + " = bitcast i8* %_" + (getRegisterCounter()-2) + " to i8***");
        printIntoLlFile("\t" + createRegister() + " = getelementptr [" + numOfmethods + "x i8*], [" + numOfmethods + "x i8*]* @." + className + "_vtable, i32 0, i32 0");
        printIntoLlFile("\tstore i8** %_" + (getRegisterCounter()-1) + ", i8*** %_" + (getRegisterCounter()-2));

        return "i8* %_" + (getRegisterCounter()-3);
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()

    !f1
    */
    public String visit(NotExpression n) throws Exception {
        printIntoLlFile("\t" + createRegister() + " = xor " + n.f1.accept(this) + " 1,");
        return "i1 %_" + (getRegisterCounter()-1);
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