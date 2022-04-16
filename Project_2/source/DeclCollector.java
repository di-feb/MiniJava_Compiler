import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap; 
import visitor.GJDepthFirst;
import syntaxtree.*;

public class DeclCollector extends GJDepthFirst< String, String >{
    private Map <String, Data> symbol_table;

    public DeclCollector(){
        this.symbol_table = new Map < String, Data >();
    }
    /** Goal
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    */
    @Override
    public String visit(Goal n, Integer argu) throws Exception {
        // Accept at main class
        n.f0.accept(this, null);
        // When a production has a * it means that this production can appear
        // zero or more times. So for the  ( TypeDeclaration() )* f.e. we need to
        // iterate all the declarations. 
        for( int i = 0; i < n.f1.size(); i++ )
            n.f1.elementAt(i).accept(this, null);

        return null; 
    }

    /** MainClass
    * f1 -> Identifier() { void main ( String[]
    * f11 -> Identifier()
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*

    * class f1 { 
        void main ( String[] f11 ){
            f14 
            f15 
        }
    }
    */
    public String visit(MainClass n, A argu) throws Exception {


        return null;
    }
}