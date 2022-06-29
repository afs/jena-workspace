package reports;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.FmtUtils;

public class SortingBug3 {

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
        if ( false ) {
            NodeValue nv1 = NodeValue.makeNode(SSE.parseNode("'foo'@de"));
            NodeValue nv2 = NodeValue.makeNode(SSE.parseNode("'foo'"));

            int x1 = BindingComparator.compareNodesRaw( nv1, nv2 );
            int x2 = BindingComparator.compareNodesRaw( nv2, nv1 );
            System.out.println(x1);
            System.out.println(x2);

            if ( x1 != -x2 )
                System.out.println("Error");

            System.exit(0);
        }
        mainSimul();System.exit(0);
        mainTest();
    }

    public static void mainSimul() {
        String itemsStr[] = {
            "'Maß4Werk0481'",
            "'Anbringungsort der Sammlermarke'@de-DE",
            "'Werk GR178'",
            "'hat Erfahrung in'@de-DE",
            "'Consortium'@en-US",
            "'1!'@en-US",
            "'Consortium'@de",
            "'Consortium'@de-DE",
            "'1!'@en-US",
            "'89$140519319 image 00000003'",
            "'Georgetown'@es",
            "'Werk5092 [Buchdruck, Vignetten, Zierleisten]'@en-US",
            "'Georgetown'@en",
            "'Georgetown'@pt",
            "'Georgetown'@it",
            "'Georgetown'@de",
            "'Georgetown'@fr",
            "'hat weltweiten Zählwert'@de-DE",
            "'Data Maintenance Service'@en-US",
            "'Data Maintenance Service'@de-DE",
            "'Data Maintenance Service'@de",
            "'Data Maintenance Service'@en",
            "'date/time value'@en-US",
            "'mark location'@en-US",
            "'date/time value'@en",
            "'Werk0956 '@en-US",
            "'South America'@en",
            "'Lomé'@en",
            "'Lomé'@it",
            "'Lomé'@fr",
            "'Lomé'@es",
            "'Lomé'@de"};
        Node items[] = new Node[itemsStr.length];
        for ( int i = 0 ; i < itemsStr.length ; i++ ) {
            items[i] = SSE.parseNode(itemsStr[i]);
        }

        try {
            Arrays.sort(items,
                        (left,right)->BindingComparator.compareNodesRaw(NodeValue.makeNode(left), NodeValue.makeNode(right)));
            System.out.println("No exception");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            //System.err.println(ex.getMessage());
        }
    }

    // Safe copy.
//    String itemsStr[] = {
//        "'Maß4Werk0481'",
//        "'Anbringungsort der Sammlermarke'@de-DE",
//        "'Werk GR178'",
//        "'hat Erfahrung in'@de-DE",
//        "'Consortium'@en-US",
//        "'1!'@en-US",
//        "'Consortium'@de",
//        "'Consortium'@de-DE",
//        "'1!'@en-US",
//        "'89$140519319 image 00000003'",
//        "'Georgetown'@es",
//        "'Werk5092 [Buchdruck, Vignetten, Zierleisten]'@en-US",
//        "'Georgetown'@en",
//        "'Georgetown'@pt",
//        "'Georgetown'@it",
//        "'Georgetown'@de",
//        "'Georgetown'@fr",
//        "'hat weltweiten Zählwert'@de-DE",
//        "'Data Maintenance Service'@en-US",
//        "'Data Maintenance Service'@de-DE",
//        "'Data Maintenance Service'@de",
//        "'Data Maintenance Service'@en",
//        "'date/time value'@en-US",
//        "'mark location'@en-US",
//        "'date/time value'@en",
//        "'Werk0956 '@en-US",
//        "'South America'@en",
//        "'Lomé'@en",
//        "'Lomé'@it",
//        "'Lomé'@fr",
//        "'Lomé'@es",
//        "'Lomé'@de"};


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
        //String DATA = "bad-data-56.ttl";

        // "un-fix"
        //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        for (int i=0;i<1000; i++) {
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
                try ( QueryExec qe = QueryExec.graph(g).query("SELECT ?o { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o }").build() ) {
                    RowSet rs = qe.select();
                    //RowSetOps.out(rs);
                    List<Node> x = new ArrayList<>();
                    rs.forEachRemaining(b->x.add(b.get("o")));
                    System.out.println();
                    System.out.println("Sort : "+x.size()+" items");
                    System.out.println();

                    x.forEach(n->System.out.println(FmtUtils.stringForNode(n)));

                    System.out.flush();
                    System.err.flush();




                    try {
                        Arrays.sort(x.toArray(new Node[0]),
                                    (left,right)->BindingComparator.compareNodesRaw(NodeValue.makeNode(left), NodeValue.makeNode(right)));
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                        //System.err.println(ex.getMessage());
                    }
                }

                System.exit(0);
            }
        }
        System.out.println("finished " + counter);
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
