import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;


// Contains all the usefull information about a class.
public class Data {
    // Name of the ParentClass of this current class
    // if parent class does not exist string is Null
    final private String parentClassName;                 
    private Map < String, MethodInfo > methods; // Key:Method_Name, Value:Class that contains info about that method
    private Map < String, VarInfo > vars;       // Key:Var_Name, Value:Class that contains info about that var

    // Constructor
    Data(String parentName){
        parentClassName = parentName;
        methods = new LinkedHashMap<String, MethodInfo>();
        vars = new LinkedHashMap<String, VarInfo>();
    }

    // Accessors
    public String getName(){ return parentClassName; }
    public Map getMethods() { return methods; }
    public Map getVars() { return vars; }
}
