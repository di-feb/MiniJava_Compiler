import java.util.LinkedHashMap;

public class MethodInfo {
    private String returnType;                      // Return type of the method
    private int offset;                             // Offset of the method
    private LinkedHashMap < String, String > args;  // Args of the method
    private LinkedHashMap < String, String > vars;  // Key:Var_Name, Value:Class that contains info about that var
    
    // Constructor
    MethodInfo(){
        args = new LinkedHashMap < String, String >();
        vars = new LinkedHashMap < String, String >();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public LinkedHashMap < String, String > getArgs(){ return args; }
    public LinkedHashMap < String, String > getVars(){ return vars; }

    // Mutators
    public void setType(String _type_){ this.returnType = _type_; }
    public void setOffset(Integer _offset_){ this.offset = _offset_; }
} 