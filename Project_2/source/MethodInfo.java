import java.lang.reflect.Method;
import java.util.Vector;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class MethodInfo {
    final private String returnType;
    final private Integer offset;
    final private Vector < String > arguments;

    // Constructor
    MethodInfo(String name){
        this.name = name;
        this.vars = new Vector< String >();
    }

    // Accessors
    public String getType(){ return returnType; }
    public Integer getOffset() { return offset; }
    public Vector < String > getArgs(){ return arguments; }

}