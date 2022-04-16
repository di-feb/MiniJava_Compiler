import java.lang.reflect.Method;
import java.util.Vector;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class MethodInfo {
    private String returnType;            // Return type of the method
    private Integer offset;               // Offset of the method
    private Vector < String > arguments;  // Args of the method
    private Vector < String > vars;       // Any other var that has been declared inside the scope of the method

    // Constructor
    MethodInfo(){
        this.arguments = new Vector<String>();
        this.vars = new Vector<String>();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public Vector < String > getArgs(){ return arguments; }
    public Vector < String > getVars(){ return vars; }
}