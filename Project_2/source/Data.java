import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;


// Contains all the usefull information about a class.
public class Data {
    // Name of the ParentClass of this current class
    // if parent class does not exist string is Null
    final private String parentClassName;                 
    private HashMap < String, MethodInfo > methods; // Key:Method_Name, Value:Class that contains info about that method
    private HashMap < String, VarInfo > vars;       // Key:Var_Name, Value:Class that contains info about that var

    // Constructor
    Data(String parentName){
        parentClassName = parentName;
        methods = new HashMap<String, MethodInfo>();
        vars = new HashMap<String, VarInfo>();
    }

    // Accessors
    public String getName(){ return parentClassName; }
    public HashMap< String, MethodInfo > getMethods() { return methods; }
    public HashMap< String, VarInfo > getVars() { return vars; }
}
