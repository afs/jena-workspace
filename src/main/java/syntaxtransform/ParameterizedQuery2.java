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
import org.apache.jena.query.Query ;
import org.apache.jena.query.QuerySolutionMap ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.ElementData ;
import org.apache.jena.sparql.syntax.ElementGroup ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateRequest ;

/**
 * <code>ParameterizedQuery</code> - inject <code>VALUES</code> clause.
 *
 * <pre>Query template = Queryfactory.create(" ...  ") ;
 * Map<String, RDFNode> params =
 * Query instantiated = ParameterizeQuery2.setVariables(template, params) ;
 * <pre>
 */

public class ParameterizedQuery2 {
    
    /** Create a new query with occurences of specific variables replaced by some node value. 
     * @param query Query
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Query parameterize(Query query, Map<Var, Node> map) {
        //Query q2 = QueryTransformOps.transform(query, map) ;
        Query q2 = QueryTransformOps.shallowCopy(query) ;
        
        ElementData elData = new ElementData() ;
        BindingMap binding = BindingFactory.create() ;
        map.forEach((v,n)->{elData.add(v); binding.add(v, n);}) ;
        elData.add(binding); 

        Element el = q2.getQueryPattern() ;
        ElementGroup elg = new ElementGroup() ;
        elg.addElement(elData); 
        // Either copy the original ElementGroup contents or create ElementGroup
        if ( ! ( el instanceof ElementGroup ) )
            elg.addElement(el);
        else
            elg.getElements().addAll(((ElementGroup)el).getElements()) ;
        q2.setQueryPattern(elg); 
        return q2 ; 
    }
    
    /** Create a new UpdateRequest with occurences of specific variables replaced by some node value. 
     * @param request UpdateRequest
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static UpdateRequest parameterize(UpdateRequest request, Map<Var, Node> map) {
        return UpdateTransformOps.transform(request, map) ;
    }

    /** Create a new Update with occurences of specific variables replaced by some node value. 
     * @param request Update
     * @param map Mapping from {@link Var} to {@link Node}
     * @return Query, with replacements 
     */
    public static Update parameterize(Update request, Map<Var, Node> map) {
        return UpdateTransformOps.transform(request, map) ;
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

