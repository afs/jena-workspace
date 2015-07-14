/**
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

import java.util.Arrays ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.ElementGroup ;
import org.apache.jena.sparql.syntax.ElementPathBlock ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.junit.Assert ;
import org.junit.Test ;

public abstract class AbstractTestParameterized {
    
    public abstract Query process(String queryString, Map<String, Node> substitutions) ;
    public abstract UpdateRequest processUpdate(String updateString, Map<String, Node> substitutions) ;
    
    private void test(String queryString, Map<String, Node> substitutions, String expectedQuery) {
        if ( expectedQuery == null )
            expectedQuery = queryString ;
        Query got = process(queryString, substitutions) ;
        Query expected = QueryFactory.create(expectedQuery) ;
        Assert.assertEquals(expected, got) ;
    }
    
    private void test(String queryString, String[][] substitutions, String expectedQuery) {
        Map<String, Node> map = build(substitutions) ;
        test(queryString, map, expectedQuery) ;
    }
    
    private void testUpdate(String updateString, Map<String, Node> substitutions, String expectedUpdate) {
        if ( expectedUpdate == null )
            expectedUpdate = updateString ;
        UpdateRequest got = processUpdate(updateString, substitutions) ;
        UpdateRequest expected = UpdateFactory.create(expectedUpdate) ;
        Assert.assertEquals(expected, got) ;
    }

    private void testUpdate(String updateString, String[][] substitutions, String expectedUpdate) {
        Map<String, Node> map = build(substitutions) ;
        testUpdate(updateString, map, expectedUpdate) ;
    }
    


    private static Map<String, Node> build(String[][] data) {
        Map<String, Node> map = new HashMap<>() ;
        for ( String[] row : data ) {
            if ( row.length != 2 )
                System.err.println("Bad row: "+str(row)) ;
            String varName = row[0] ;
            String val = row[1] ; 
            Node n = SSE.parseNode(val) ;
            map.put(varName, n) ;
        }
        return map ;
    }
    
    private static String str(String[] x) { return Arrays.asList(x).toString() ; }
    
    // Rows of (varname, SSE Node value)
    static String[][] data0 = { } ;
    
    static String[][] data1 = { {"x", "<http://example/x>" } } ;
    
    static String[][] data2 = { {"x", "<http://example/x>" } ,
                                {"p", "<http://example/p>" } ,
                                {"y", "123" },
                                {"z", "true" },
                                } ;
    @Test public void params_basic_01() { 
        test("SELECT * {}", data0, null) ;
    }

    @Test public void params_basic_02() { 
        test("SELECT * { ?x ?p ?o }", data1, "SELECT * { <http://example/x> ?p ?o }") ; 
    }

    @Test public void params_basic_03() { 
        test("SELECT * { ?s ?x ?o }", data1, "SELECT * { ?s <http://example/x> ?o }") ; 
    }

    @Test public void params_basic_04() { 
        test("SELECT * { ?s ?p ?x }", data1, "SELECT * { ?s ?p <http://example/x> }") ; 
    }

    @Test public void params_basic_05() { 
        test("SELECT * { ?x ?x ?x }", data1,
             "SELECT * { <http://example/x> <http://example/x> <http://example/x> }") ; 
    }

    @Test public void params_basic_06() { 
        test("SELECT * { ?x ?p ?y }", data2,
             "SELECT * { <http://example/x> <http://example/p> 123 }") ;
    }
    
    @Test public void params_var_special_01() { 
        test("SELECT * { ?s ?p '?x' }", data2,
             "SELECT * { ?s <http://example/p> '?x' }") ;
    }

    @Test public void params_var_special_02() { 
        test("SELECT * { <http://example/foo?x=bar> ?p 'x' }", data2,
             "SELECT * { <http://example/foo?x=bar> <http://example/p> 'x' }") ;
    }

    @Test public void params_var_special_03() { 
        test("SELECT * { ?s ?p '? ' }", data2,
             "SELECT * { ?s  <http://example/p> '? ' }") ;
    }

    @Test public void params_var_special_04() { 
        test("SELECT ?x { ?s ?p '? ' }", data1,
             "SELECT (<http://example/x> AS ?x) { ?s  ?p '? ' }") ;
    }

    @Test public void params_bnode_01() {
        Map<String, Node> map = new HashMap<>() ;
        // Force the label.
        Node n = NodeFactory.createBlankNode("ABCDE") ;
        map.put("bnode", n) ;
        Query query2 = process("SELECT * { ?x ?p ?bnode }", map) ;
        Element elt0 = ((ElementGroup)query2.getQueryPattern()).get(0) ;
        Node n2 = ((ElementPathBlock)elt0).getPattern().get(0).asTriple().getObject() ;
        Assert.assertEquals(n,  n2) ;
    }

    @Test public void params_filter_01() { 
        test("SELECT * { ?s ?p ?y FILTER(?x = <http://example/x>) }", data1,
             "SELECT * { ?s ?p ?y FILTER(<http://example/x> = <http://example/x>) }") ;
    }

    // Structures.
    @Test public void params_struct_01() { 
        test("SELECT * { ?x ?p ?y OPTIONAL { ?x ?p ?z }}", data2,
             "SELECT * { <http://example/x> <http://example/p> 123 OPTIONAL { <http://example/x> <http://example/p> true } }") ;
    }
    
    @Test public void params_struct_02() { 
        test("SELECT * { { ?x ?p ?y } UNION { ?x ?p ?z }}", data2,
             "SELECT * { { <http://example/x> <http://example/p> 123 } UNION { <http://example/x> <http://example/p> true } }") ;
    }
    
    @Test public void params_struct_03() { 
        test("SELECT ?x { }", data1, "SELECT ( <http://example/x> AS ?x) {}") ;
    }

    
    @Test public void params_struct_04() { 
        test("SELECT * { ?x ?p ?y  { SELECT ?s { ?s ?p ?x } }}", data1,
             "SELECT * { <http://example/x> ?p ?y  { SELECT ?s { ?s ?p <http://example/x> } }}") ;
    }

    // GROUP BY
    @Test public void params_group_by_01() { 
        test("SELECT (count(*) AS ?c) { ?x ?p ?y} GROUP BY ?x", data1,
             "SELECT (count(*) AS ?c) { <http://example/x> ?p ?y } GROUP  BY (<http://example/x> AS ?x)") ;   
    }

    @Test public void params_group_by_02() { 
        test("SELECT (count(*) AS ?c) { ?x ?p ?y} GROUP BY ?x HAVING (?x > 1) ", data1,
             "SELECT (count(*) AS ?c) { <http://example/x> ?p ?y } GROUP  BY (<http://example/x> AS ?x) HAVING (<http://example/x> > 1) ") ;   
    }
    
    @Test public void params_group_by_03() { 
        test("SELECT ?x { ?x ?p ?y} GROUP BY ?x", data1,
             "SELECT (<http://example/x> AS ?x) { <http://example/x> ?p ?y } GROUP  BY (<http://example/x> AS ?x)") ;   
    }

    @Test public void params_group_by_04() { 
        test("SELECT ?x { ?x ?p ?y} GROUP BY ?x", data1,
             "SELECT (<http://example/x> AS ?x) { <http://example/x> ?p ?y } GROUP  BY (<http://example/x> AS ?x)") ;   
    }
    
    @Test public void update_01() {
        testUpdate("INSERT {?x ?p ?o } WHERE { }", data1, "INSERT {<http://example/x> ?p ?o } WHERE { }" ) ;
    }

}

