import java.lang.reflect.Method;
import java.util.Vector;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class MethodInfo {
    private String returnType;            // Return type of the method
    private Integer offset;               // Offset of the method
    private Vector < String > parameters;  // Args of the method

    // Constructor
    MethodInfo(){
        parameters = new Vector<String>();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public Vector < String > getArgs(){ return parameters; }

    // Mutators
    public void setType(String _type_){ this.returnType = _type_; }
    public void setOffset(Integer _offset_){ this.offset = _offset_; }
    public void setParameters(String param){ this.parameters.putAll(param.split(",")); }
} 