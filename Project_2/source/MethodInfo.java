import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class MethodInfo {
    private String returnType;           // Return type of the method
    private Integer offset;              // Offset of the method
    private List < String > parameters;  // Args of the method

    // Constructor
    MethodInfo(){
        parameters = new ArrayList<String>();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public List < String > getArgs(){ return parameters; }

    // Mutators
    public void setType(String _type_){ this.returnType = _type_; }
    public void setOffset(Integer _offset_){ this.offset = _offset_; }
    public void setParameters(List < String > param){ parameters.addAll(param); }
} 