import java.util.Vector;
import visitor.GJDepthFirst;
import syntaxtree.*;

public class VarInfo {
    private String type;
    private Integer offset;

    // Accessors
    public String getType(){ return type; }
    public Integer getOffset() { return offset; }
}