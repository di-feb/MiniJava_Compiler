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
    private Map < String, MethodInfo > method_info; // Key:Method_Name, Value:Class that contains info about that method
    private Map < String, VarInfo > var_info;       // Key:Var_Name, Value:Class that contains info about that var

    // Constructor
    Data(String parentName){
        parentClassName = parentName;
        method_info = new LinkedHashMap<String, MethodInfo>();
        var_info = new LinkedHashMap<String, VarInfo>();
    }

    // Accessors
    public String getName(){ return parentClassName; }
    public Map getMethod_Info() { return method_info; }
    public Map getVar_Info() { return var_info; }

}
