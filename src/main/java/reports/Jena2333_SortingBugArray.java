package reports;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeCmp;

public class Jena2333_SortingBugArray {
    public static void main(String[] args) {
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

//        // Calculate all transitive
//
//        for ( int i = 0 ; i < items.length ; i++ ) {
//            Node node = items[i];
//            for ( int j = 0 ; j < items.length ; j++ ) {
//                Node left = items[j];
//                for ( int k = 0 ; k < items.length ; k++ ) {
//                    Node right = items[k];
//                    testCompare(left, node, right, (n1,n2)->NodeValue.compareAlways(NodeValue.makeNode(n1), NodeValue.makeNode(n2)));
//                }
//            }
//        }
    }

    static void testCompare(String node1, String node2, String node3) {
        System.out.println("Node");
        testCompare(node1, node2, node3, NodeCmp::compareRDFTerms);
        System.out.println("NodeValue");
        testCompare(node1, node2, node3, (n1,n2)->BindingComparator.compareNodesRaw(NodeValue.makeNode(n1), NodeValue.makeNode(n2)));
    }

    static void testCompare(String node1, String node2, String node3, BiFunction<Node, Node, Integer> compare) {
        testCompare(SSE.parseNode(node1), SSE.parseNode(node2), SSE.parseNode(node3), compare);
    }

    static void testCompare(Node node1, Node node2, Node node3, BiFunction<Node, Node, Integer> compare) {
        // node1 < node2, node2 < node3 => node1 < node3.
        // node1 < node2, node2 = node3 => node1 < node3.
        // node1 = node2, node2 < node3 => node1 < node3.
        // Similarly for >

        int x12 = compare.apply(node1,node2);
        int x23 = compare.apply(node2,node3);
        int x13 = compare.apply(node1,node3);

        boolean duff = false;
        if ( x12 < 0 && x23 < 0 ) {
            if ( x13 >= 0 )
                System.out.printf("< <: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( x12 < 0 && x23 == 0 ) {
            if ( x13 >= 0 )
                System.out.printf("< =: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( x12 == 0 && x23 < 0 ) {
            if ( x13 >= 0 )
                System.out.printf("= <: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( x12 > 0 && x23 > 0 ) {
            if ( x13 <= 0 )
                System.out.printf("> >: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( x12 > 0 && x23 == 0 ) {
            if ( x13 <= 0 )
                System.out.printf("> =: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( x12 == 0 && x23 > 0 ) {
            if ( x13 <= 0 )
                System.out.printf("= >: %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
            duff = true;
        }

        if ( ! duff )
            System.out.printf("   : %2d %2d %2d ::  %s   %s   %s\n", x12, x23, x13, NodeFmtLib.strNT(node1), NodeFmtLib.strNT(node2), NodeFmtLib.strNT(node3));
    }
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
}
