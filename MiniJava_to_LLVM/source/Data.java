import java.util.LinkedHashMap;

// Contains all the usefull information about a class.
public class Data {
    final private String parentClassName;                 // Name of the ParentClass of this current class, if parent class does not exist string is Null
    private LinkedHashMap < String, MethodInfo > methods; // Key:Method_Name, Value:Class that contains info about that method
    private LinkedHashMap < String, VarInfo > vars;       // Key:Var_Name, Value:Class that contains info about that var


    // Constructor
    Data(String parentName){
        parentClassName = parentName;
        methods = new LinkedHashMap<String, MethodInfo>();
        vars = new LinkedHashMap<String, VarInfo>();
    }

    // Accessors
    public String getName(){ return parentClassName; }
    public LinkedHashMap< String, MethodInfo > getMethods() { return methods; }
    public LinkedHashMap< String, VarInfo > getVars() { return vars; }
}
