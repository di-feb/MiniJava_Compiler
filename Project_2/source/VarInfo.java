import java.util.Vector;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class VarInfo {
    private String type;        // Type of var
    private Integer offset;     // Offset of the var

    // Accessors
    public String getType(){ return type; }
    public Integer getOffset() { return offset; }
}