import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;

public class Data {
    String parentClassName;
    Map < String, MethodInfo > method_info;
    Map < String, VarInfo > var_info;
}
