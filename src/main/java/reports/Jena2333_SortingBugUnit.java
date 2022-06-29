package reports;


import static org.apache.jena.sparql.util.NodeUtils.isLangString;
import static org.apache.jena.sparql.util.NodeUtils.isSimpleString;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeCmp;

public class Jena2333_SortingBugUnit {

    // [ ] The mega query.
    // [ ] Fix (compareRDFTermsNEW).


    public static void main(String[] args) {

//        test3("'xx'@de", "'dd'@en","'ee'@en");
//        //test3("'xx'", "'dd'@en","'ee'@en");
//        test3("'dd'@en","'ee'@en", "'xx'");

        test2("'xx'@de", "'dd'@en");
        test2("'dd'@en","'ee'@en");
        test2("'aa'","'ee'@en");

        System.exit(0);
    }

//    NodeUtils.compareRDFTerms(Node, Node)

//       NodeValue.compare(NodeValue, NodeValue, boolean) -- VSPACE_NODE
//       NodeValue.compareAlways(NodeValue, NodeValue) -- NodeValue.compare(nv1,nv2) then compareRDFTerms

//       BindingComparator.compareBindingsSyntactic(Binding, Binding)
//       NodeUtils.compareRDFTerms(Node, Node) <- Quoted triple recursion

    // BlazeGraph, Virtuoso plain before lang ; lang before lex
    // GraphDB  lang (side effect of xsd:string? lang before lex)

    public static void test3(String ns1, String ns2, String ns3) {
        Node n1 = SSE.parseNode(ns1);
        Node n2 = SSE.parseNode(ns2);
        Node n3 = SSE.parseNode(ns3);
        // n1 < n2 < n3 by sorting

        inspect(n1,n2, Expr.CMP_LESS);
        System.out.println();
        inspect(n2,n3, Expr.CMP_LESS);
        System.out.println();
        inspect(n1,n3, Expr.CMP_LESS);
        System.out.println("------------------");
        System.out.println();
    }

    public static void test2(String ns1, String ns2) {

        System.out.printf("Compare: "+ns1+ " : "+ns2);

        Node n1 = SSE.parseNode(ns1);
        Node n2 = SSE.parseNode(ns2);
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);

        int x1 = NodeCmp.compareRDFTerms(n1, n2);
        int x2 = NodeValue.compareAlways(nv1, nv2);
        if ( x1 == x2 )
            System.out.printf(" : %s\n", compStr(x1));
        else
            System.out.printf(" : %s %s ****\n", compStr(x1), compStr(x2));
    }

    public static void inspect(Node n1, Node n2, int expected) {

        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);

        // Lexical then language - wrong.
        int xn = compareLiteralsBySyntaxNEW(n1, n2);
        //int xn1 = compareRDFTermsNEW(n1, n2);
        // Language then lexical
        int xvn = NodeValue.compareAlways(nv1, nv2);

        if ( expected != xn )
            System.out.printf("**** Node : Expected %s ; Got %s\n", compStr(expected), compStr(xn));
        System.out.printf("Node %s\n", compStr(xn));
        System.out.printf("  %s   %s\n", NodeFmtLib.strNT(n1), NodeFmtLib.strNT(n2));
        if ( expected != xvn )
            System.out.printf("**** NodeValue : Expected %s -- Got %s\n", compStr(expected), compStr(xn));
        System.out.printf("NodeValue %s\n", compStr(xvn));
        System.out.printf("  %s   %s\n", nv1, nv2);

    }

    public static String compStr(int x) {
        if ( x == Expr.CMP_GREATER ) return ">";
        if ( x == Expr.CMP_EQUAL) return "=";
        if ( x == Expr.CMP_LESS ) return "<";
        if ( x == Expr.CMP_INDETERMINATE ) return "?";
        return "*";
    }

    /** Compare two Nodes, based on their RDF terms forms, not value */
    public static int compareRDFTermsNEW(Node node1, Node node2) {
        if ( node1 == null ) {
            if ( node2 == null )
                return Expr.CMP_EQUAL ;
            return Expr.CMP_LESS ;
        }

        if ( node2 == null )
            return Expr.CMP_GREATER ;

        // No nulls.

        // Two literals
        if ( node1.isLiteral() && node2.isLiteral() )
            return compareLiteralsBySyntaxNEW(node1, node2) ;

        // One or both not literals
        // Variables < Blank nodes < URIs < Literals < Triple Terms

        //-- Variables
        if ( node1.isVariable() ) {
            if ( node2.isVariable() ) {
                return StrUtils.strCompare(node1.getName(), node2.getName()) ;
            }
            // Variables before anything else
            return Expr.CMP_LESS ;
        }

        if ( node2.isVariable() ) {
            // node1 is a not variable
            return Expr.CMP_GREATER ;
        }

        // No variables.

        //-- Blank nodes
        if ( node1.isBlank() ) {
            if ( node2.isBlank() ) {
                String s1 = node1.getBlankNodeId().getLabelString() ;
                String s2 = node2.getBlankNodeId().getLabelString() ;
                return StrUtils.strCompare(s1, s2) ;
            }
            // bNodes before anything but variables
            return Expr.CMP_LESS ;
        }

        if ( node2.isBlank() )
            // node1 not blank.
            return Expr.CMP_GREATER ;

        // Not blanks. 2 URI or one URI and one literal

        //-- URIs
        if ( node1.isURI() ) {
            if ( node2.isURI() ) {
                String s1 = node1.getURI() ;
                String s2 = node2.getURI() ;
                return StrUtils.strCompare(s1, s2) ;
            }
            return Expr.CMP_LESS ;
        }

        if ( node2.isURI() )
            return Expr.CMP_GREATER ;

        // -- Two literals already done just leaving one or other of the node is a literal
        if ( node2.isLiteral() )
            return Expr.CMP_GREATER;

        // Because triple terms are after literals ...
        if ( node1.isLiteral() )
            return Expr.CMP_LESS;

        // -- Triple nodes.
        if ( node1.isNodeTriple() ) {
            if ( node2.isNodeTriple() ) {
                Triple t1 = node1.getTriple();
                Triple t2 = node2.getTriple();
                int x1 = compareRDFTermsNEW(t1.getSubject(), t2.getSubject());
                if ( x1 != Expr.CMP_EQUAL )
                    return x1;
                int x2 = compareRDFTermsNEW(t1.getPredicate(), t2.getPredicate());
                if ( x2 != Expr.CMP_EQUAL )
                    return x2;
                int x3 = compareRDFTermsNEW(t1.getObject(), t2.getObject());
                if ( x3 != Expr.CMP_EQUAL )
                    return x3;
                return Expr.CMP_EQUAL;
            }
        }

        if ( node2.isNodeTriple() )
            return Expr.CMP_GREATER;

        // No URIs, no blanks, no literals, no triple terms nodes by this point

        // Should not happen.
        throw new ARQInternalErrorException("Compare: " + node1 + "  " + node2) ;
    }

    /** Compare literals by kind - not by value.
     *  Gives a deterministic, stable, arbitrary ordering between unrelated literals.
     *
     * Ordering:
     *  <ol>
     *  <li>By lexical form</li>
     *  <li> For same lexical form:
     *       <ul>
     *       <li>  RDF 1.0 : simple literal < literal by lang < literal with type
     *       <li>  RDF 1.1 : xsd:string < rdf:langString < other dataypes.<br/>
     *             This is the closest to SPARQL 1.1: treat xsd:string as a simple literal</ul></li>
     *  <li> Lang by sorting on language tag (first case insensitive then case sensitive)
     *  <li> Datatypes by URI
     *  </ol>
     */

    private static int compareLiteralsBySyntaxNEW(Node node1, Node node2) {
        if ( node1 == null || !node1.isLiteral() || node2 == null || !node2.isLiteral() )
            throw new ARQInternalErrorException("compareLiteralsBySyntax called with non-literal: (" + node1 + "," + node2 + ")") ;

        if ( node1.equals(node2) )
            return Expr.CMP_EQUAL ;

        // Literal ordering.



        // ----------------

        String lex1 = node1.getLiteralLexicalForm() ;
        String lex2 = node2.getLiteralLexicalForm() ;

        int x = StrUtils.strCompare(lex1, lex2) ;
        if ( x != Expr.CMP_EQUAL )
            return x ;

        // Same lexical form. Not .equals()
        if ( isSimpleString(node1) ) // node2 not a simple string because they
                                     // would be .equals
            return Expr.CMP_LESS ;
        if ( isSimpleString(node2) )
            return Expr.CMP_GREATER ;
        // Neither simple string / xsd:string(RDF 1.1)

        // Both language strings?
        if ( isLangString(node1) && isLangString(node2) ) {
            String lang1 = node1.getLiteralLanguage() ;
            String lang2 = node2.getLiteralLanguage() ;
            x = StrUtils.strCompareIgnoreCase(lang1, lang2) ;
            if ( x != Expr.CMP_EQUAL )
                return x ;
            x = StrUtils.strCompare(lang1, lang2) ;
            if ( x != Expr.CMP_EQUAL )
                return x ;
            throw new ARQInternalErrorException("compareLiteralsBySyntax: lexical form and languages tags identical on non.equals literals") ;
        }

        // One a language string?
        if ( isLangString(node1) )
            return Expr.CMP_LESS ;
        if ( isLangString(node2) )
            return Expr.CMP_GREATER ;

        // Both have other datatypes. Neither simple nor language tagged.
        String dt1 = node1.getLiteralDatatypeURI() ;
        String dt2 = node2.getLiteralDatatypeURI() ;
        // Two datatypes.
        return StrUtils.strCompare(dt1, dt2) ;
    }

}
