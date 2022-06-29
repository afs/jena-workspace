package reports;

import java.io.FileNotFoundException;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;

public class SortingBug2 {

    private static final String queryStr0 =
            "SELECT ?label WHERE { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label } ORDER BY ?label";

    private static final String queryStr = """
            SELECT * WHERE
            { SELECT ?label { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } }
            ORDER BY ?label
            """;

    // GraphMem.
    // Language tags?

    public static void main(String[] args) throws FileNotFoundException {
        if ( true ) {
            NodeValue nv1 = NodeValue.makeNode(SSE.parseNode("'foo'@de"));
            NodeValue nv2 = NodeValue.makeNode(SSE.parseNode("'foo'@DE"));

            System.out.println(nv1.getNode().getIndexingValue());
            System.out.println(nv2.getNode().getIndexingValue());

            int x1 = BindingComparator.compareNodesRaw( nv1, nv2 );
            int x2 = BindingComparator.compareNodesRaw( nv2, nv1 );
            System.out.println(x1);
            System.out.println(x2);

            if ( x1 != -x2 )
                System.out.println("Error");

            System.exit(0);
        }
        mainTest();
    }

    public static void mainTest() throws FileNotFoundException {
        int counter = 0;

        // lang2 = canonical langtages no subtags
        // lang3 lang tags to plain strings (various)

        // no @de - bad then no @en - good!
        // no @en - everything else - bad

        // With no @de and @en: good

        // With @de, @en, no lang on [ VIVO ]
        // 1:: no @es/bad, @fr/bad, @it/bad, no @pt (good)
        // 2:: no @pt/(bad) no @it(bad), no @fr(bad), no @es(good)
        // So remove all of the small langs? Where do they appear?

        // Only @en , @de, no other lang tags and no " image " -- bad -- bad-canon-3.ttl -- less frequent.


        // parse to NT also fails.
        //String DATA = "bad-canon-lang2.ttl";
        String DATA = "bad-small-19.ttl";

        // "un-fix"
        //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        int N = 10000;
        for (int i=0;i<N; i++) {
            // Passes as does mem2
            //Graph g = GraphFactory.createTxnGraph();
            // Fails
            Graph g = GraphFactory.createDefaultGraph();
            // Fails;
            //Graph g = GraphFactory.createPlainGraph();
            // Mem2 - passes

            RDFDataMgr.read(g, DATA);

            boolean result = isReproduced(i, queryStr, g);
            if (result) {
//                BindingComparator.DEBUG = true;
//                boolean result2 = isReproduced(i, queryStr, g);
//                if ( result != result2 )
//                    System.err.println("Not re-reproducible");
                counter++;
                System.exit(0);
            }
        }
        System.out.println("finished " + N + " : failures="+counter);
    }

    private static boolean isReproduced(int i, String queryStr, Graph g) {
        boolean result = true;
            try ( QueryExec qe = QueryExec.graph(g).query(queryStr).build() ) {
            RowSet rs = qe.select();
            rs.hasNext();
            return false;

        } catch (Exception e) {
            System.out.flush();
            System.err.println("i = "+i);
            e.printStackTrace();
            return true;
        }
    }


}
