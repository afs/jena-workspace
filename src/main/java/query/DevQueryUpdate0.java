/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package query;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.NodeValue;
import query.parameterized.ParameterizedQuery;

public class DevQueryUpdate0 {

    static String prefixes = StrUtils.strjoinNL
        ("PREFIX : <http://example/>"
        ,"PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>"
        ,"PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
        ) ;
    static Node bn = NodeFactory.createBlankNode() ;
    // centralize."_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()) ;
    static String bnUri = "<_:"+NodeFmtLib.encodeBNodeLabel(bn.getBlankNodeLabel())+">" ;
    static Map<Var, Node> map2 = new LinkedHashMap<Var, Node>() ;
    static {
        map2.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ;
        map2.put(Var.alloc("b"), bn) ;
    }


    static public void test_22() {
        test("SELECT * { }", "SELECT ?o (:X as ?x) ("+bnUri+" AS ?b) { }", map2, true) ;
    }
    /*
    expected:
        SELECT  ?o (:X AS ?x) (_:b0 AS ?b)
        WHERE
          { _:b0  :p  ?o }
    was: (is this a print effect?)
        SELECT  ?o (:X AS ?x) (<_:B28bdcb4bX2D4639X2D4e7aX2Db8bfX2D52ab58c432f5> AS ?b)
        WHERE
          { _:b0  :p  ?o }
    */

    protected static void test(String input, String expected, Map<Var, Node> map, boolean includeInput) {
        Query qInput = QueryFactory.create(prefixes+input, Syntax.syntaxARQ) ;
        Query qExpected = QueryFactory.create(prefixes+expected, Syntax.syntaxARQ) ;
        Query output = includeInput
            ? ParameterizedQuery.parameterizeIncludeMapped(qInput, map)
            : ParameterizedQuery.parameterize(qInput, map) ;
        assertEquals("ParameterizedQuery", qExpected, output) ;
    }

    public static void main(String[] args) {
        // SUMMARY
        /*
         * ParameterizedQuery.nodeEnc uses NodeFmtLib.encodeBNodeLabel
         * Parsing does not reverse.
         *
         *   RiotLib.createIRIorBNode(iri) ;
         *
         *   does to allow NodeFmtLib.decodeBNodeLabel
         *
         *   Target:
         *   RiotLib.bNodeToURI(blank node) ;
         *   RiotLib.itiToBlankNode(iri node ) ;
         *
         *   Use in ParameterizedQuery and elsewhere.
         *     Special encoding?
         */
        if ( true ) {
            Node bnode = NodeFactory.createBlankNode() ;
            // Different bnode.
            String bnodeUriEnc = "<_:"+NodeFmtLib.encodeBNodeLabel(bnode.getBlankNodeLabel())+">" ;
            String bnodeUriPlain = "<_:"+bnode.getBlankNodeLabel()+">" ;

            Map<Var, Node> map3 = new LinkedHashMap<Var, Node>() ;
            map3.put(Var.alloc("b"), bnode) ;
            //test("SELECT * { }", "SELECT ("+bnUri+" AS ?b) { }", map3, true) ;

            String input = "SELECT * { }";
            String expected = "SELECT ("+bnodeUriPlain+" AS ?b) { }";
            Query qInput = QueryFactory.create(input, Syntax.syntaxARQ) ;
            Query qExpected = QueryFactory.create(expected, Syntax.syntaxARQ) ;
            Query output = ParameterizedQuery.parameterizeIncludeMapped(qInput, map3);
            System.out.println("** Expected");
            System.out.println("String::\n"+expected);
            System.out.print("Query::\n"+qExpected);
            VarExprList ve1 = qExpected.getProject();
            NodeValue n1 = (NodeValue)ve1.getExprs().get(Var.alloc("b"));
            System.out.println("Blank node: " +n1.asNode().getBlankNodeLabel());
            System.out.println("Encoded");

            System.out.println();

            System.out.println("** Output");
            System.out.println(output);
            VarExprList ve2 = output.getProject();
            // Because of ParameterizedQuery.nodeEnc
            NodeValue n2 = (NodeValue)ve2.getExprs().get(Var.alloc("b"));
            if ( n2.asNode().isBlank() )
                //ParameterizedQuery.nodeEnc - no encoding.
                System.out.println("Blank node: " +n2.asNode().getBlankNodeLabel());
            else
                System.out.println("URI: <"+n2.asNode().getURI()+">");

            // IF bnodeUriPlain and ParameterizedQuery.nodeEnc - no encoding, then equals.
            System.out.println(qExpected.equals(output));

//            VarExprList ve1 = qExpected.getProject();
//            NodeValue n1 = (NodeValue)ve1.getExprs().get(Var.alloc("b"));
//            VarExprList ve2 = output.getProject();
//            NodeValue n2 = (NodeValue)ve2.getExprs().get(Var.alloc("b"));

            System.exit(0);
        }

        if ( false ) {
            Node bn = NodeFactory.createBlankNode() ;
            // *** encode **

            String bnUri = "<_:"+NodeFmtLib.encodeBNodeLabel(bn.getBlankNodeLabel())+">" ;
            // Parsing does not reverse encodeBNodeLabel
            bnUri = "<_:"+bn.getBlankNodeLabel()+">" ;

            Map<Var, Node> map1 = new LinkedHashMap<Var, Node>() ;
            map1.put(Var.alloc("b"), bn) ;

            String input = "SELECT * { }";
            String expected = "SELECT ("+bnUri+" AS ?b)  { }";

            Query qInput = QueryFactory.create(input, Syntax.syntaxARQ) ;
            Query qExpected = QueryFactory.create(expected, Syntax.syntaxARQ) ;
//        Query output = includeInput
//            ? ParameterizedQuery.parameterizeIncludeInput(qInput, map)
//            : ParameterizedQuery.parameterize(qInput, map) ;
            Query output = ParameterizedQuery.parameterizeIncludeMapped(qInput, map1);

            //qExpected.equals(output);

            VarExprList ve1 = qExpected.getProject();
            VarExprList ve2 = output.getProject();

            boolean b1 = ve1.equals(ve2);
            NodeValue n1 = (NodeValue)ve1.getExprs().get(Var.alloc("b"));
            NodeValue n2 = (NodeValue)ve2.getExprs().get(Var.alloc("b"));
//        B0f76921eX2D42deX2D4b9bX2D9e39X2D4757bb23b168 -- syntax
//        0f76921e-42de-4b9b-9e39-4757bb23b168 -- raw label.

            System.out.println("Expected"); // Blank node.
            System.out.println(qExpected);
            System.out.println(n1.asNode().getBlankNodeLabel());

            //ParameterizedQuery.nodeEnc
            System.out.println("Actual");
            System.out.println(output);
            //NodeFactory.createURI("_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()));
            //System.out.println(n2.asNode().getBlankNodeLabel());
            System.out.println("<"+n2.asNode().getURI()+">");

            //NodeFmtLib.encodeBNodeLabel(n2.asNode().getBlankNodeLabel()));

            System.exit(0);
        }
        //BNodes have different labels.

        //test_22() ; System.exit(0);
        /*
         * test_20 - subst <_:...> in {} -- ATM Needs ARQ.constantBNodeLabels, true
         * test_21 - subst <_:...> in (AS) -- ATM Needs ARQ.constantBNodeLabels, false
         *     Generates warning because <_:...> is still checked as an IRI.
         */

        if ( false ) {

            String queryString = "SELECT (<_:Babc> AS ?X){ }";
            //String queryString = "SELECT (<_:Babc> AS ?X) { <_:Babc> ?p ?o . FILTER(<_:Babc> != 1) }";

            System.out.println("ARQ.constantBNodeLabels = true (dft)");
            //RDFParser.create().fromString("<_:xyz> <x:p> 123 . ").lang(Lang.TTL).parse(StreamRDFLib.writer(System.out));
            dwim(queryString);

            System.out.println("ARQ.constantBNodeLabels = false");
            //RDFParser.create().fromString("<_:xyz> <x:p> 123 . ").lang(Lang.TTL).parse(StreamRDFLib.writer(System.out));
            ARQ.getContext().set(ARQ.constantBNodeLabels, false) ;
            // Parsing this creates a real blank node i.e. variable. IRI warning for <_:123>
            dwim(queryString);
            // And check Turtle.
            System.exit(0);
        }

        if ( false ) {
            // if ARQ.constantBNodeLabels == true, the converted SELECT clause has the bnodeURI
            String queryString = "SELECT ?b (<_:abc> AS ?X){?b ?p ?o }";
            Map<Var, Node> map = new HashMap<>();
            map.put(Var.alloc("b"), NodeFactory.createBlankNode("abc"));
            Query query = QueryFactory.create(queryString);
            Query query2 = ParameterizedQuery.parameterizeIncludeMapped(query, map);
            System.out.println(query2);
            System.exit(0);
        }

        /*
         * ARQ.constantBNodeLabels, true
         *   ==> SPARQL: resolveQuotedIRI, resolveIRI, isBNodeIRI  --> true --> createNode --> real bnode.
         *   ==> TurtleParserBase: createNode --> real bnode.
         *     TurtleParserBase :: javacc version!!!!
         */
        /*
         * CURRENT
         * ARQ.isTrue(ARQ.constantBNodeLabels) = "labels?"

         * isBNodeIRI - "labels?" = false ==> resolveIRI: drops through to resolver in ParserBase.resolveURI. ==> warning; is URI.
         * isBNodeIRI - "labels?" = true ==> ParserBase.createNode -> real bnode.
         *
         *      Only SPARQL uses resolveQuotedIRI, resolveIRI
         *
         */

        // SPARQL Query vs SPARQL Update.
        // FIX:
        // [1]
        //   Ideal: default to data bnodes, and change to startPattern, finishPattern.
        //    TriplesBlock is the pattern form.

        //       Already calls startTriplesBlock(),  finishTriplesBlock() <-- should in SPARQLParserBase?
        //         Add startExpression, finishExpression

        // WhereClause --> go into bnode, variable mode.  NB recursive via sub-SELECT
        //   startWherePattern() , finishWherePattern!!! -- alerady do pushes and level counting. 2 syntax-subselect failures.

        //   expressions - setBNodesAreVariables == false; ??
        //           ** startExpression, finishExpression. Recursive because of EXISTS.

        // stack state.

        //       (data uses: QuadData->TriplesTemplate)

        // In pattern: createNodePattern : bnodes are variables.
        // In data:    createNodeData : bnodes are variables.<_:...> are left alone?

        // [2??]
        //   React based on "bnodes are variables" or "in pattern flag."
        //  resolveIRI - always leave as <_:...> and no warning. Remove "skolomizedBNodes" test in:
        //      protected boolean isBNodeIRI(String iri) {
        //         return skolomizedBNodes && RiotLib.isBNodeIRI(iri) ;
        //      }



        //ARQ.getContext().set(ARQ.constantBNodeLabels, false) ;
        Query query = QueryFactory.create("SELECT * { <_:Babc> ?p ?o . FILTER(<_:abc> != 1) }");
        //Query query = QueryFactory.create("SELECT * { [] ?p ?o . FILTER(<_:abc> != 1) }");
        Op op = Algebra.compile(query);
        System.out.println(op);
        //query.serialize(System.out);

        System.out.println("DONE");
    }

    // Turtle wants <_:..> to become blank nodes based on ARQ.constantBNodeLabels, default true.

    // SPARQL wants <_:..> to be left as URIs.?????

    private static void dwim(String queryString) {
        Query query = QueryFactory.create(queryString);
        query.serialize(System.out);
        Op op = Algebra.compile(query);
        System.out.println(op);
    }




    protected static Node createNode(String iri, boolean skolomizedBNodes) {
        if ( skolomizedBNodes )
            return RiotLib.createIRIorBNode(iri) ;
        else
            return NodeFactory.createURI(iri) ;
    }

    /*
    protected String resolveIRI(String iriStr, int line, int column) {
        if ( isBNodeIRI(iriStr) )
            return iriStr ;

        if ( getPrologue() != null ) {
            if ( getPrologue().getResolver() != null )
                try {
                    // Used to be errors (pre Jena 2.12.0)
                    // .resolve(iriStr)
                    IRI iri = getPrologue().getResolver().resolveSilent(iriStr) ;
                    if ( true )
                        CheckerIRI.iriViolations(iri, errorHandler, line, column) ;
                    iriStr = iri.toString() ;
                }
                catch (JenaURIException ex) {
                    throwParseException(ex.getMessage(), line, column) ;
                }
        }
        return iriStr ;
    }
    */

}

