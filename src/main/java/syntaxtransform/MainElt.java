/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.HashMap ;
import java.util.Map ;
import java.util.function.BiFunction ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ParameterizedSparqlString ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.AnonId ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.lang.SyntaxVarScope ;
import org.apache.jena.update.UpdateRequest ;

public class MainElt
{
    // QueryTransformOps
    //   CONSTRUCT
    //   DESCRIBE
    //   All the updates.
    //   Injecting blanknodes : <_:abc> [*]
    //   BIND, VALUE
    // Full testing.
    
    // Subquery { SELECT ?x }
    // Test with Q->A->Subst->OpAsQ->Q2
    
    // [*] NodeToLabelMapBNode.asString : bnode -> <_:label> form
    
    
    public static void main(String[] args) {
        String x[] = { x0 } ;
        mainPQ(x) ;
//        mainPQ2(x) ;
//        mainPSS(args) ;
//        mainPSS2(args);
    }
    
    static String PRE = "PREFIX : <http://example/>" ; 
    static String x0 = StrUtils.strjoinNL
        ( PRE
        , "SELECT ?x { [] :p ?x . FILTER ( bound(?x) ) }"
        ) ;
    static String x1 = StrUtils.strjoinNL
        ( PRE,  "# Comment ?x"
          , "SELECT (str(?x) as ?xs) { ?s :p ?x . OPTIONAL { ?x :r '?x' }  FILTER ( ?x > ?y ) }"
        );
    static String x2 = StrUtils.strjoinNL
        ( PRE
        , "SELECT ?x { ?s :p ?x . FILTER NOT EXISTS { ?x :r ?x }} GROUP BY ?x ORDER BY ?x"
        );
    static String x3 = StrUtils.strjoinNL
        ( PRE
        , "SELECT (123 as ?x) { }"
        );
    static String x4 = StrUtils.strjoinNL
        ( PRE
        , "SELECT * { :s :p :o\\?x}"
        );
    // ==> Double SELECT (:X AS ?x)
    static String x5 = StrUtils.strjoinNL
        ( PRE
        , "SELECT ?x { {SELECT ?x { ?s ?p ?x } } ?s ?p ?o }"
        );
    
    static String x6 = StrUtils.strjoinNL
        ( PRE
        , "DESCRIBE ?x {}"
        );
    
    static String x7 = StrUtils.strjoinNL
        ( PRE
        , "SELECT * { VALUES ?x { 123 } ?s ?p ?o }"
        );

    static String x8 = "SELECT ?x { BIND(1 AS ?x) }" ;
    
    static String x99 = PRE+"\n"+"ASK { FILTER (?x = <http://example/X>) }";
    
    public static void mainPQ(String[] x) {
        BiFunction<String, Map<Var, Node>, Query> producer = (qs, map) -> {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            return ParameterizedQuery.parameterize(q, map) ;
        } ;
            
        execute(x, producer) ;
    }
    
    public static void mainPQ2(String[] args) {
        BiFunction<String, Map<Var, Node>, Query> producer = (qs, map) -> {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            return ParameterizedQuery2.parameterize(q, map) ;
        } ;
            
        execute(args, producer) ;
    }

    
    static void execute(String x[], BiFunction<String, Map<Var, Node>, Query> producer) {
        for ( String qs : x ) {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
            Map<Var, Node> map = new HashMap<Var, Node>() ;
            //map.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ; 
            map.put(Var.alloc("x"), NodeFactory.createAnon(new AnonId())) ;
            Query q2 = producer.apply(qs, map) ;
            Op op = Algebra.compile(q2) ;
            System.out.println(op) ;
            
//            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
//            Query q2 = ParameterizedQuery.parameterize(q, map) ;
            System.out.print(q) ;
            System.out.println("-------------");
            System.out.print(q2) ;
            try {
                SyntaxVarScope.check(q2) ;
            } catch (Exception ex) {
                System.out.println(ex.getMessage()) ;
            }
            System.out.println("-------------");
        }
        
//        String z = StrUtils.strjoinNL
//            ( "PREFIX : <http://example/>"
//            , "DELETE { ?s :p ?x } WHERE {}" 
//            );
//        UpdateRequest req = UpdateFactory.create(z) ;
//        UpdateRequest req2 = UpdateTransformOps.transform(req, map) ;
//        System.out.print(req) ;
//        System.out.println("-------------");
//        System.out.print(req2) ;
//        System.out.println("-------------");
        
    }
    //Works on INSERT DATA.
    
    public static void mainPSS(String[] x) {
        BiFunction<String, Map<Var, Node>, Query> producer = (qs, map) -> {
            ParameterizedSparqlString pss = new ParameterizedSparqlString(qs) ;
            map.forEach((v,n)->pss.setParam(v.getName(), n)) ;
            return pss.asQuery() ; 
        } ;
        execute(x, producer) ;
    }
    
    public static void mainPSS2(String[] args) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString() ;
        pss.setCommandText("PREFIX : <http://example/> INSERT DATA { ?x :p :o}") ;
        pss.setIri("x", "abcdef") ;
        String qs1 = pss.toString() ;
        System.out.println(qs1) ;
        UpdateRequest req = pss.asUpdate() ;
        System.out.println(req) ;
        System.exit(0) ;
    }

} 

