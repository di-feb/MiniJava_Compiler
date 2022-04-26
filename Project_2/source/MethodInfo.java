import java.util.Map;
import java.util.HashMap; 
import java.util.List;
import java.util.ArrayList;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class MethodInfo {
    private String returnType;                  // Return type of the method
    private Integer offset;                     // Offset of the method
    private List < String > parameters;         // Args of the method
    private HashMap < String, VarInfo > vars;   // Key:Var_Name, Value:Class that contains info about that var
    // Constructor
    MethodInfo(){
        parameters = new ArrayList<String>();
        vars = new HashMap < String, VarInfo >();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public List < String > getArgs(){ return parameters; }
    public HashMap < String, VarInfo > getVars(){ return vars; }

    // Mutators
    public void setType(String _type_){ this.returnType = _type_; }
    public void setOffset(Integer _offset_){ this.offset = _offset_; }
    public void setParameters(List < String > param){ parameters.addAll(param); }
    public void setVars(HashMap < String, VarInfo > var){ vars.putAll(var); }
} 