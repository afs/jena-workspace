/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package syntaxtransform;

import java.util.LinkedHashMap ;
import java.util.Map ;
import java.util.function.BiFunction ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.ResultBinding ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.lang.SyntaxVarScope ;

public class MainJena1111
{
    // 1 - replace
    // 2 - replace, include values
    // 3 - replace, eval to add values.
    // Finish setVariables.
    
    // Skolemization
    // Join on values.
    
    static String sel0 = StrUtils.strjoinNL
        ( CONST.PRE
        , "SELECT ?Z ?x { ?Z :p ?x . FILTER ( bound(?x) ) }"
        ) ;
    static String sel1 = StrUtils.strjoinNL
        ( CONST.PRE
        , "SELECT * { ?Z :p ?x . FILTER ( bound(?x) ) }"
        ) ;
    static String sel2 = StrUtils.strjoinNL
        ( CONST.PRE
        , "SELECT ((?x+1) AS ?Y) ?Z { ?Z :p ?x . FILTER ( bound(?x) ) }"
        ) ;
    
    public static void main(String... args) {
        {
            Query qx = QueryFactory.create("SELECT ?s { ?s ?p ?o } GROUP BY ?s HAVING(count(?o)>1)")  ;
            Map<Var, Node> map = new LinkedHashMap<Var, Node>() ;
            map.put(Var.alloc("s"), NodeFactory.createURI("http:/example/S")) ; 
            map.put(Var.alloc("o"), NodeFactory.createLiteral("foo")) ;
            Query q2 = ParameterizedQuery.parameterizeIncludeInput(qx, map) ;


            System.out.println(q2) ;
            System.exit(0) ;
        }
        
        
        
        
        
        
        
        
        
        
        // Next : test cases.
        // BNodes in returned values.
        // BNodes -> <_:label> first?
        
        String x[] = { sel0 , sel1 , sel2 } ;
        System.out.println("==== v1") ;

        BiFunction<String, Map<Var, Node>, Query> producer = (qs, map) -> {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            //Query q2 = ParameterizedQuery.parameterize(q, map) ;
            Query q2 = ParameterizedQuery.parameterizeIncludeInput(q, map) ;
            return q2 ;
        } ;

        for ( String qs : x ) {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            Map<Var, Node> map = new LinkedHashMap<Var, Node>() ;
            map.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ; 
            map.put(Var.alloc("b"), NodeFactory.createBlankNode()) ;
            System.out.println(map);
            Query q2 = producer.apply(qs, map) ;
            printQuery(q) ;
            System.out.println("-------------");
            printQuery(q2) ;

            try {
                SyntaxVarScope.check(q2) ;
            } catch (Exception ex) {
                System.out.println(ex.getMessage()) ;
            }
            System.out.println("-=-=-=-=-=-=-=-=-=-");
        }
    }
    
    private static void printQuery(Query q) {
        IndentedWriter out = new IndentedWriter(System.out) ;
        out.setFlatMode(true);
        q.serialize(out);
        out.flush();
        System.out.println();
    }
    
    private static ResultSet wrapExecution(ResultSet rs, Map<Var, Node> values) {
        ResultSet rs2 = new ResultSetWrapper(rs) {
            @Override
            public QuerySolution nextSolution() {
                return new ResultBinding(super.getResourceModel(), nextBinding()) ;
            }

            @Override
            public Binding nextBinding() {
                Binding b = get().nextBinding() ;
                BindingMap b2 = BindingFactory.create(b) ;
                values.forEach((v,n)->{
                    if ( b.contains(v) ) {
                        if ( ! b.get(v).equals(n) ) 
                            System.err.println("Mismatch") ;
                    } else {
                        b2.add(v, n); 
                    }
                }) ;
                return b2 ;
            }
        } ;
        return rs2 ;
    }

} 

