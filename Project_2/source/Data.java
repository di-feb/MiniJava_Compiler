import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;


// Contains all the usefull information about a class.
public class Data {
    // Name of the ParentClass of this current class
    // if parent class does not exist string is Null
    String parentClassName;                 
    Map < String, MethodInfo > method_info; // Key:Method_Name, Value:Class that contains info about that method
    Map < String, VarInfo > var_info;       // Key:Var_Name, Value:Class that contains info about that var
}
