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

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ParameterizedSparqlString ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QuerySolutionMap ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateRequest ;

/**
 * <code>ParameterizedQuery</code> - use a {@link Query} as a template; replace variables by values.   
 *
 * <pre>Query template = QueryFactory.create(" ...  ") ;
 * Map<String, RDFNode> params =
 * Query instantiated = ParameterizeQuery.setVariables(template, params) ;
 * <pre>
 * <p><b>Examples</b></p>
 * 
 *<p>
 * {@link ParameterizedSparqlString} is another approach based on building a string. See
 * <a href="http://jena.apache.org/documentation/query/parameterized-query.html">Parameterized SPARQL Queries</a>
 * </p>
 */

public class ParameterizedQuery {
    
    /** Create a new query with occurences of specific variables replaced by the given node value from the Map. 
     * @param query Query
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Query parameterize(Query query, Map<Var, Node> map) {
        return QueryTransformOps.transform(query, map) ;
    }
    
    /** Create a new query with occurences of specific variables replaced by the given node value from the Map.
     *  Additional, the query query returns the input mapping as bindings in the results.   
     * @param query Query
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Query parameterizeIncludeInput(Query query, Map<Var, Node> map) {
        Query q2 = parameterize(query, map) ;
        if ( ! q2.isSelectType() )
            return q2 ;

        // There are 3 cases for a variabe ?x being mapped to a value:  
        // 1 - The query had "SELECT ?x" - this has already become (Xval AS ?x)
        // 2 - The query has "SELECT *" - and ?x will have disappeared. 
        //     We write to SELECT ... vars ... (Xval AS ?x)
        //     Adding a trailing BIND is also possible.
        // 3 - The query has "SELECT vars" that did not include ?x.
        //     We add (Xval AS ?x)
        

        // (1) is done so that leaves two cases: 
        if ( q2.isQueryResultStar() ) {
            // SELECT *
            // Calculate correct
            // This should not include substtuted variables. 
            
            List<Var> vars = new ArrayList<>(q2.getProjectVars()) ;
            q2.setQueryResultStar(false) ;
            q2.getProject().clear() ;
            VarExprList exprvars = q2.getProject() ;
            
            vars.forEach((var)-> {
                if ( ! q2.getProject().contains(var) )
                    q2.addResultVar(var);
            }) ;
            map.forEach((var, node)-> {
                // Safety check.
                if ( ! q2.getProject().contains(var) )
                    q2.addResultVar(var, nodeEnc(node)) ;
            }) ;
        } else {
            // Unmentioned substituted variable.
            VarExprList exprvars = q2.getProject() ;
            map.forEach((var, node)-> {
                if ( ! q2.getProject().contains(var) )
                    q2.addResultVar(var, nodeEnc(node)) ;
            }) ;
        }
        return q2 ;
    }

    static NodeValue nodeEnc(Node node) {
        if ( node.isBlank() )
            node = NodeFactory.createURI("_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()));
        return NodeValue.makeNode(node) ;
    }
    
    /** Create a new  {@link UpdateRequest} with occurences of specific variables replaced by the associated node value. 
     * @param request UpdateRequest
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static UpdateRequest parameterize(UpdateRequest request, Map<Var, Node> map) {
        return UpdateTransformOps.transform(request, map) ;
    }

    /** Create a new {@link Update} with occurences of specific variables replaced by the associated node value. 
     * @param request Update
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Update parameterize(Update request, Map<Var, Node> map) {
        return UpdateTransformOps.transform(request, map) ;
    }
    
    // Avoid name clash : Map<> after erasure. 
    
    /** Create a new query with occurences of variables replaced by constants. 
     * @param query
     * @param map Mapping from variable name to {@link RDFNode}
     * @return Query, with replacements.
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

