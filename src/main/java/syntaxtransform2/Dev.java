package syntaxtransform2;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class Dev {
    public static void main(String...a) {
//        exec1("TIM", DatasetGraphFactory.createTxnMem().getDefaultGraph());
//        exec1("Mem", GraphFactory.createGraphMem()); 
//        exec1("TDB", TDBFactory.createDatasetGraph().getDefaultGraph());
//        System.exit(1);
        
        String x = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,"SELECT ?this {"
            ,"   $this a $that"
            ,"   FILTER(?that < ?value)"
            ,"}"
            );
        
        Query query = QueryFactory.create(x);
        
        Map<Var, Node> substitutions = new HashMap<>();
        set(substitutions, "this", ":foo");
        set(substitutions, "value", "111");
        ElementTransform eltrans = new ElementTransformSubst(substitutions);
        NodeTransform nodeTransform = new NodeTransformSubst(substitutions);
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans);
        
        Query query2 = QueryTransformOps.transform(query, substitutions);
        System.out.println(query2);
        
        
    }

    public static void exec1(String label, Graph g) {
        System.out.print(label);
        System.out.println(" -- "+g.getCapabilities().handlesLiteralTyping());
        g.clear();
        g.add(SSE.parseTriple("(:s :p 123 )"));
        g.add(SSE.parseTriple("(:s :p 00123 )"));
        g.find(null, null, SSE.parseNode("00123")).forEachRemaining(t->{
            System.out.println("  "+SSE.str(t));
        });
        RDFDataMgr.write(System.out, g, Lang.TTL);
    }
    
    private static void set(Map<Var, Node> substitutions, String varStr, String valStr) {
        substitutions.put(Var.alloc(varStr), SSE.parseNode(valStr));
        
    }
    
}