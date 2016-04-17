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

package analyse;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropertyFunction ;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;

public class RunCustom {

    public static void main(String[] args) {
        PropertyFunctionFactory pff = (uri) -> new PropertyFunction() {

            @Override
            public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                              ExecutionContext execCxt) {}

            @Override
            public QueryIterator exec(QueryIterator input, PropFuncArg argSubject, Node predicate,
                                      PropFuncArg argObject, ExecutionContext execCxt) {
                return null ;
            }} ;
        PropertyFunctionRegistry.get().put("http://ex/PF1", pff) ;
        PropertyFunctionRegistry.get().put("http://ex/PF2", pff) ;
        
        String x = StrUtils.strjoinNL
        ( 
         "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
         ,"   PREFIX :        <http://example/>"
         ,"   PREFIX math:    <http://www.w3.org/2005/xpath-functions/math#>"
         ,""
         ,"SELECT (math:log(?x) AS ?X) {"
         , "    { ?x rdfs:member ?y . ?x <http://ex/PF1> ?y }"
         , "       UNION {"
         , "          ?x :normal ?y ."
         , "          FILTER ( 1 + math:sqrt(?x) )"
         , "          }"
         , "   }") ;
        
        //Query query = QueryFactory.read("Q.rq") ;
        Query query = QueryFactory.create(x) ;
        
        System.out.println(query) ;
        analyseSyntax(query) ;
        System.out.println() ;
        analyseAlgebra(query) ;
    }

    private static void analyseSyntax(Query query) {
        List<String> functionURIs = new ArrayList<>() ;
        List<Node> propFuncURIs = new ArrayList<>() ;
        System.out.println("**** Syntax") ;
        AnalyseSyntax.process(query, functionURIs, propFuncURIs);
        System.out.println("==== Property functions");
        propFuncURIs.forEach(System.out::println) ;
        System.out.println("==== Filter functions");
        functionURIs.forEach(System.out::println) ;
    }
    
    private static void analyseAlgebra(Query query) {
        List<String> functionURIs = new ArrayList<>() ;
        List<Node> propFuncURIs = new ArrayList<>() ;
        System.out.println("**** Algebra") ;
        AnalyseAlgebra.process(query, functionURIs, propFuncURIs);
        System.out.println("==== Property functions");
        propFuncURIs.forEach(System.out::println) ;
        System.out.println("==== Filter functions");
        functionURIs.forEach(System.out::println) ;
    }
}
