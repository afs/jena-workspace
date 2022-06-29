package reports;



import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.sys.JenaSystem;

public class Jena2333_FX {

    // [ ] The mega query.
    // [ ] Fix (compareRDFTermsNEW).
    // [ ] Migration L NodeCmp.compareTerms

    // Libraryise two langs.- share with NodeValue.compare(,,true)
    // [ ] TestOrdering
    //       e./g. term
    // [x] TestNodeValue

    // Datatypes:
    // Old: 12 failures
    // New: 14 failures

    /* lang only change.
org.apache.jena.sparql.expr.TestOrdering

test_lang6(org.apache.jena.sparql.expr.TestOrdering)
java.lang.AssertionError: Lang nodes should sort by lexical form if one is plain

test_lang8(org.apache.jena.sparql.expr.TestOrdering)
java.lang.AssertionError: Lang nodes should sort by lexical form if other is XSD string

testComp_03_str3(org.apache.jena.sparql.expr.TestOrdering)
java.lang.AssertionError: 03 (typed) should be compareAlways 'less than' than "3"

ARQ - Scripts
Sorting - ARQ tests
T-3098: Sort 1 (sort-1.rq)
T-3099: Sort 2 (sort-2.rq)
T-3100: Sort 3 (sort-3.rq)

ARQTestRefEngine
ARQ - Scripts (Reference Query Engine)
Sorting - ARQ tests
T-4198: Sort 1 (sort-1.rq)
T-4199: Sort 2 (sort-2.rq)
T-4200: Sort 3 (sort-3.rq)

SPARQL [TIM]
ARQ - Scripts
Sorting - ARQ tests
T-5150: TIM-Sort 1 (sort-1.rq)
T-5151: TIM-Sort 2 (sort-2.rq)
T-5152: TIM-Sort 3 (sort-3.rq)
     */

    /* lang and datatype
Adds:

TestNodeValue.testTripleTYerm3 (string/integer compare)

org.apache.jena.sparql.expr.TestOrdering

testComp_int_float_1(org.apache.jena.sparql.expr.TestOrdering)
java.lang.AssertionError: Int 10 less than float 8 in syntatic compare

testComp_int_double_1(org.apache.jena.sparql.expr.TestOrdering)
java.lang.AssertionError: Int 10 less than double 9 in syntactic compare
     */

    // -------------------

    public static void main(String... args) {
        JenaSystem.init();

        // By term. 1 > 8e0 because of datatype. integer/double.
        compare("1", "8e0");
        //compareTerms("1", "8e0");
        System.exit(0);

        // Compare by <, then within equality by value, compare by datatype and lexical form.
        // Compare unlike:
        // undef < URIs < xsd:string < lang strings < datatypes+lexical.

        Comparator<Node> compareByTerm = (n1,n2) -> NodeCmp.compareRDFTerms(n1, n2);
        Comparator<Node> compareByValue = (n1,n2) -> NodeValue.compareAlways(NodeValue.makeNode(n1), NodeValue.makeNode(n2));

        sort(compareByTerm, "'abc'@en","123", "0124", "'abc'");
        sort(compareByValue, "'abc'@en","123", "0124","'abc'");


        sort(compareByValue, "'2'^^xsd:byte", "'0001-01-01'^^xsd:date", "'1'^^xsd:unsignedByte");
        System.setProperty("org.apache.jena.compare", "old");
        sort(compareByValue, "'2'^^xsd:byte", "'0001-01-01'^^xsd:date", "'1'^^xsd:unsignedByte");
        System.exit(0);

        // "1"^^xsd:byte", "0001-01-01Z"^^xsd:date, "1"^^xsd:unsignedByte"
        compare("'2'^^xsd:byte", "'0001-01-01Z'^^xsd:date");
        compare("'0001-01-01Z'^^xsd:date", "'1'^^xsd:unsignedByte");
        compare("'2'^^xsd:byte", "'1'^^xsd:unsignedByte");
        System.exit(0);

        // OLD lexical before lang.
        compare("'abc'@en", "'bcd'@de");

        compare("'xx'@de", "'dd'@en");
        compare("'aa'@de", "'dd'@en");
        compare("'aa'", "'dd'@en");
        compare("'dd'@EN", "'dd'@en");
        compare("'aa'@EN", "'dd'@en");
        compare("'aa'@en", "'dd'@EN");
        compare("'dd'@en-GB", "'ff'@en-gb");
        compare("'dd'@en-gb", "'ff'@en-GB");
        compare("'dd'@en", "'zz'");
        compare("'dd'@en", "1");

        //newCompare("'xx'@de", "'dd'@en");
        System.exit(0);
    }

    public static void mainTest(String... args) {
        String DIR = "/home/afs/ASF/afs-jena/jena-arq/testing/ARQ/Sort/";
        LogCtl.setLevel("org.apache.jena.riot", "error");
        LogCtl.setLevel(NodeValue.class, "error");
        executeTest("Sort 1", DIR, "sort-1.rq", "data-1.ttl", "sort-result-1.ttl");
        executeTest("Sort 2", DIR, "sort-2.rq", "data-3.ttl", "sort-result-2.ttl");
        executeTest("Sort 3", DIR, "sort-3.rq", "data-3.ttl", "sort-result-3.ttl");
        System.exit(0);

    }

    private static void sort(Comparator<Node> comparator, String ...strings) {
        int N = strings.length;
        Node nodes[] = new Node[N];
        for ( int i = 0 ; i < N ; i++ ) {
            nodes[i] = SSE.parseNode(strings[i]);
        }

        Arrays.sort(nodes, comparator);
        System.out.println(List.of(nodes));
        System.out.println();
    }

    private static void sort(String ... strings) {
        System.out.println("==== Sort");
        System.out.println(List.of(strings));
        int N = strings.length;
        Node nodes[] = new Node[N];
        for ( int i = 0 ; i < N ; i++ ) {
            nodes[i] = SSE.parseNode(strings[i]);
        }
        sort((n1,n2)->NodeCmp.compareRDFTerms(n1, n2), strings);
    }

    private static Node parseNode(String str) {
        try {
            return SSE.parseNode(str);
        } catch (RuntimeException ex) {
            System.err.println("Node: "+str);
            System.err.println(ex.getMessage());
            System.exit(0);
            return null;
        }
    }

    private static void executeTest(String label, String DIR, String queryFile, String dataFile, String resultsFile) {
        System.out.println("==== "+label);
        Query query = QueryFactory.read(DIR+queryFile);
        DatasetGraph data = RDFParser.source(DIR+dataFile).toDatasetGraph();
        RowSet rs = QueryExec.dataset(data).query(query).select();
        RowSetOps.out(rs);
        ResultSet rs2 = ResultSetFactory.load(DIR+resultsFile);
        ResultSetFormatter.out(rs2);
        System.out.println();
    }

    public static void compare(String ns1, String ns2) {
        Node node1 = SSE.parseNode(ns1);
        Node node2 = SSE.parseNode(ns2);

        compareNV(node1, node2);
        compareTerms(node1, node2);

//        compareTerms(node1, node2);
//        compareTerms(node2, node1);
        System.out.println();
    }

    public static void compareTerms(String ns1, String ns2) {
        Node node1 = SSE.parseNode(ns1);
        Node node2 = SSE.parseNode(ns2);
        compareTerms(node1, node2);
    }

    public static void compareTerms(Node node1, Node node2) {
        int x1 = NodeUtilsOLD.compareRDFTerms(node1, node2);
        int x2 = NodeCmp.compareRDFTerms(node1, node2);
        if ( x1 == x2 )
            System.out.printf("Compare: %s %s %s\n",
                              NodeFmtLib.strNT(node1), compStr(x1), NodeFmtLib.strNT(node2));
        else
            System.out.printf("**** %s %s : old '%s' : new '%s' ****\n",
                              NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2),
                              compStr(x1), compStr(x2));
    }

//    public static void compareNV(Node n1, Node n2, int expected) {
//        NodeValue nv1 = NodeValue.makeNode(n1);
//        NodeValue nv2 = NodeValue.makeNode(n2);
//
//        // Lexical then language - wrong.
//        //int xn = compareLiteralsBySyntaxNEW(n1, n2);
//        int xn = NodeCmp.compareRDFTerms(n1, n2);
//        //int xn1 = compareRDFTermsNEW(n1, n2);
//        // Language then lexical
//        int xvn = NodeValue.compareAlways(nv1, nv2);
//
//        if ( expected != xn )
//            System.out.printf("**** Node : Expected %s ; Got %s\n", compStr(expected), compStr(xn));
//        System.out.printf("Node %s\n", compStr(xn));
//        System.out.printf("  %s   %s\n", NodeFmtLib.strNT(n1), NodeFmtLib.strNT(n2));
//        if ( expected != xvn )
//            System.out.printf("**** NodeValue : Expected %s -- Got %s\n", compStr(expected), compStr(xn));
//        System.out.printf("NodeValue %s\n", compStr(xvn));
//        System.out.printf("  %s   %s\n", nv1, nv2);
//
//    }

    public static void compareNV(Node node1, Node node2) {
        NodeValue nv1 = NodeValue.makeNode(node1);
        NodeValue nv2 = NodeValue.makeNode(node2);

        // Both (should) be language then lexical
        int x1 = NodeCmp.compareRDFTerms(node1, node2);
        int x2 = NodeValue.compareAlways(nv1, nv2);

        if ( x1 == x2 )
            System.out.printf("Compare: %s %s %s\n",
                              NodeFmtLib.strNT(node1), compStr(x1), NodeFmtLib.strNT(node2));
        else
            System.out.printf("**** %s %s : syntax '%s' : value '%s' ****\n",
                              NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2),
                              compStr(x1), compStr(x2));
    }

    public static String compStr(int x) {
        String s = "("+String.format("%+d",x)+") ";

        if ( x == Expr.CMP_GREATER ) return s+">";
        if ( x == Expr.CMP_EQUAL) return s+"=";
        if ( x == Expr.CMP_LESS ) return s+"<";
        if ( x == Expr.CMP_INDETERMINATE ) return s+"?";
        return "*";
    }
}
