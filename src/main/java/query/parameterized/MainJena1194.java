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

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.sparql.core.ResultBinding ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;

public class MainJena1194
{
    /*


QueryTransformOps.transform(query, substitutions) does not handle variables in HAVING clauses. For example in

SELECT * { } HAVING (?count > $minCount)

the variable $minCount would not be substituted.
 
 See also jena-workspace::syntaxtransform
 
     */
    
    public static void main(String ... argv) {
        Query query = QueryFactory.create("SELECT (count(?Y) AS ?C) { } HAVING (?count > $X)") ;
        Map<Var, Node> x = new HashMap<>() ;
        x.put(Var.alloc("X"), SSE.parseNode("123")) ;
        x.put(Var.alloc("Y"), SSE.parseNode("'abc'")) ;
        //x.put(Var.alloc("C"), SSE.parseNode("<abc>")) ;
        Query query2 = QueryTransformOps.transform(query, x) ;
        }
    
    private static void printQuery(Query q) {
        IndentedWriter out = new IndentedWriter(System.out) ;
        out.setFlatMode(true);
        q.serialize(out);
        out.flush();
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

