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

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ParameterizedSparqlString ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.lang.SyntaxVarScope ;
import org.apache.jena.update.UpdateRequest ;

public class MainElt
{
    // QueryTransformOps
    //   CONSTRUCT
    //   DESCRIBE
    //   All the updates.
    //   Injecting blanknodes : <_:abc>
    
    // Missed: HAVING, VALUE
    
    // Subquery { SELECT ?x }
    // Q->A->Subst->OpAsQ->Q2
    
    public static void main(String[] args) {
        mainPSS2(args) ; 
    }
    
    public static void mainPQ(String[] args) {
        
        String PRE = "PREFIX : <http://example/>" ; 
        String x1 = StrUtils.strjoinNL
            ( PRE,  "# Comment ?x"
              , "SELECT (str(?x) as ?xs) { ?s :p ?x . OPTIONAL { ?x :r '?x' }  FILTER ( ?x > ?y ) }"
            );
        String x2 = StrUtils.strjoinNL
            ( PRE
            , "SELECT ?x { ?s :p ?x . FILTER NOT EXISTS { ?x :r ?x }} GROUP BY ?x ORDER BY ?x"
            );
        String x3 = StrUtils.strjoinNL
            ( PRE
            , "SELECT (123 as ?x) { }"
            );
        String x4 = StrUtils.strjoinNL
            ( PRE
            , "SELECT * { :s :p :o\\?x}"
            );
        // ==> Double SELECT (:X AS ?x)
        String x5 = StrUtils.strjoinNL
            ( PRE
            , "SELECT ?x { {SELECT ?x { ?s ?p ?x } } ?s ?p ?o }"
            );
        
        String x6 = StrUtils.strjoinNL
            ( PRE
            , "DESCRIBE ?x {}"
            );
        
        String x7 = StrUtils.strjoinNL
            ( PRE
            , "SELECT * { VALUES ?x { 123 } ?s ?p ?o }"
            );

        String x9 = PRE+"\n"+"ASK { FILTER (?x = <http://example/X>) }";
        
        String x[] = { x7 } ;
        
        for ( String qs : x ) {
            Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ;
    
            Map<Var, Node> map = new HashMap<Var, Node>() ;
            map.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ; 
            Query q2 = ParameterizedQuery.parameterize(q, map) ;
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
    
    public static void mainPSS(String[] args) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString() ;
        pss.setCommandText("{ : :p ?x}") ;
        pss.setIri("x", "abcdef") ;
        String qs1 = pss.toString() ;
        System.out.println(qs1) ;
        System.exit(0) ;
        
        //pss.setCommandText("PREFIX : <http://example/> # Comment ?x\nSELECT (str(?x) as ?xs) { ?s :p ?x . OPTIONAL { ?x :r '?x' }  FILTER ( ?x > ?y ) }") ;
        pss.setCommandText("PREFIX : <http://example/> SELECT * { : :p :o\\?x}") ;
        pss.setIri("x", "abcdef") ;
        
        String qs = pss.toString() ;
        System.out.println(qs) ;
        Query query = QueryFactory.create(qs) ;
        System.out.print(query) ;
        
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

