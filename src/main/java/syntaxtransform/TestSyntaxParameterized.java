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

package syntaxtransform;

import static org.junit.Assert.assertEquals ;

import java.util.Collections ;
import java.util.LinkedHashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.core.Var ;
import org.junit.Test ;

/** Tests for syntax substitution.
 *  These build on top of TestSyntaxTransform. */
public class TestSyntaxParameterized {
    
    static String prefixes = StrUtils.strjoinNL
        ("PREFIX : <http://example/>"
        ,"PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>" 
        ,"PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
        ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
        ) ;
    
    static Map<Var, Node> mapEmpty = Collections.emptyMap() ;
    static Map<Var, Node> map1 = new LinkedHashMap<Var, Node>() ;
    static {
        map1.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ; 
    }
    
    static Node bn = NodeFactory.createBlankNode() ;
    // cedtralize."_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()) ;
    static String bnUri = "<_:"+NodeFmtLib.encodeBNodeLabel(bn.getBlankNodeLabel())+">" ;
    static Map<Var, Node> map2 = new LinkedHashMap<Var, Node>() ;
    static {
        map2.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ;
        map2.put(Var.alloc("b"), bn) ;
    }

    @Test public void test_01() {
        test("ASK{}", "ASK{}", mapEmpty, true) ;
    }
    
    @Test public void test_02() {
        test("ASK{}", "ASK{}", map1, false) ;
    }
    
    @Test public void test_03() {
        test("ASK{}", "ASK{}", map1, true) ;
    }
    
    @Test public void test_10() {
        test("SELECT * {}", "SELECT * {}", mapEmpty, false) ;
    }

    @Test public void test_11() {
        test("SELECT * {}", "SELECT * {}", map1, false) ;
    }

    @Test public void test_12() {
        test("SELECT * {}", "SELECT (:X as ?x) {}", map1, true) ;
    }

    @Test public void test_13() {
        test("SELECT * { ?x :p ?o }", "SELECT * { :X :p ?o}", map1, false) ;
    }
    
    @Test public void test_14() {
        test("SELECT * { ?x :p ?o }", "SELECT ?o (:X as ?x) { :X :p ?o }", map1, true) ;
    }

    // BNodes
    @Test public void test_20() {
        test("SELECT * { }", "SELECT (:X as ?x) ("+bnUri+" AS ?b)  { }", map2, true) ;
    }
    
    @Test public void test_21() {
        test("SELECT * { ?b :p ?o }", "SELECT ?o (:X as ?x) ("+bnUri+" AS ?b)  { "+bnUri+" :p ?o }", map2, true) ;
    }

    @Test public void test_22() {
        test("SELECT * { ?x :p ?b }", "SELECT (:X AS ?x) ("+bnUri+" AS ?b) { :X :p ?o }", map2, true) ;
    }

    protected void test(String input, String expected, Map<Var, Node> map, boolean includeInput) {
        Object v = ARQ.getContext().get(ARQ.constantBNodeLabels) ;
        ARQ.getContext().set(ARQ.constantBNodeLabels, false) ;
        try {
            Query qInput = QueryFactory.create(prefixes+input, Syntax.syntaxARQ) ;
            Query qExpected = QueryFactory.create(prefixes+expected, Syntax.syntaxARQ) ;
            Query output = includeInput
                ? ParameterizedQuery.parameterizeIncludeInput(qInput, map)
                : ParameterizedQuery.parameterize(qInput, map) ;

            assertEquals("ParameterizedQuery", qExpected, output) ;
        } finally {
            ARQ.getContext().set(ARQ.constantBNodeLabels, v) ;
        }
    }
}
