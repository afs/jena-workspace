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

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ParameterizedSparqlString ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QuerySolutionMap ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;

/**
 *
 *
 * <pre>Query template = Queryfactory.create(" ...  ") ;
 * Map<String, RDFNode> params =
 * Query instantiated = ParameterizeQuery.setVariables(template, params) ;
 * <pre>
 * <p><b>Examples</b></p>
 * 
 *
 * {@link ParameterizedSparqlString} is another approach based on building 
 * 
 * 
 * @see ParameterizedSparqlString
 */

public class ParameterizedQuery {
    
    /** Create a new query with occurences of specific variables replaced by some node value. 
     * @param query Query
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Query parameterize(Query query, Map<Var, Node> map) {
        Query q2 = QueryTransformOps.transform(query, map) ;
        return q2 ; 
    }
    
    /** Create a new query with occurences of variables replaced by constants. 
     * @param query
     * @param map Mapping from variable name to {@link RDFNode}
     * @return Query, with replacements 
     */
    public static Query setVariables(Query query, Map<String, RDFNode> map) {
        Map<Var, Node> map2 = new HashMap<>() ;
        map.forEach((k,v) -> map2.put( Var.alloc(k), v.asNode()) ) ;
        return parameterize(query,map2) ;
    }
    
    /** Create a new query with occurences of variables replaced by constants
     * based on the given {@link QuerySolutionMap}.
     * @param query
     * @param querySolutionMap Mapping from variable name to {@link RDFNode}
     * @return Query, with replacements 
     */
    public static Query setVariables(Query query, QuerySolutionMap querySolutionMap) {
        return  setVariables(query, querySolutionMap.asMap() ) ;
    }
}

