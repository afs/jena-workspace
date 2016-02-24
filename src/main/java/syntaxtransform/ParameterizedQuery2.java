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

import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.NodeValue ;



public class ParameterizedQuery2 {
    public static Query x_parameterizeSELECT(Query query, Map<Var, Node> map) {
        // Beign developed in MainJena1111
        
        // ?x => (Xval AS ?x) -- done by ParameterizedQuery.parameterize
        // (?x+1) AS ?y => Add (Xval AS ?x)
        // (expr) AS ?x => ??????
        
        
        Query q2 = ParameterizedQuery.parameterize(query, map) ;
        // If and only if: SELECT* 
        if ( q2.isQueryResultStar() ) {
        }
        // Expressions.
        return q2 ;
    }
    
    private static void  addIfAbsent(VarExprList varsExpr, Var var, Node node) {
        if ( varsExpr.contains(var) )
            ; // Error.
        varsExpr.add(var, NodeValue.makeNode(node)) ;
    }
    
//    /** Parameterize and add a WHERE VALUES block */
//    public static Query parameterizeAddValuesWhere(Query query, Map<Var, Node> map) {
//        Query q2 = ParameterizedQuery.parameterize(query, map) ;
//        BindingMap dataMap = bindingForInput(map) ;
//        // On query level.
//        List<Var> vars = Iter.toList(dataMap.vars()) ;
//        
//        ElementData elData = new ElementData() ;
//        elData.add(dataMap); 
//        vars.forEach((v) -> elData.add(v));
//        
//        // In WHERE clause.
//        Element el = q2.getQueryPattern() ;
//        ElementGroup elg = new ElementGroup() ;
//        elg.addElement(elData); 
//        // Either copy the original ElementGroup contents or create ElementGroup
//        if ( ! ( el instanceof ElementGroup ) )
//            elg.addElement(el);
//        else
//            elg.getElements().addAll(((ElementGroup)el).getElements()) ;
//        q2.setQueryPattern(elg); 
//        return q2 ; 
//    }
//
//    /** Parameterize and add a SELECT-level VALUES block */
//    public static Query parameterizeAddValuesEnd(Query query, Map<Var, Node> map) {
//        Query q2 = ParameterizedQuery.parameterize(query, map) ;
//        BindingMap dataMap = bindingForInput(map) ;
//        // On query level.
//        List<Var> vars = Iter.toList(dataMap.vars()) ;
//        List<Binding> data = new ArrayList<>() ;
//        
//        if ( q2.hasValues() ) {
//            List<Binding> x = q2.getValuesData() ;
//            List<Var> vars2 = q2.getValuesVariables() ;
//            vars.forEach(v->addIfAbsent(vars2, v));
//            x.forEach((b)-> {
//                BindingMap b2 = BindingFactory.create(dataMap) ;
//                b2.addAll(b);
//                data.add(b2);
//            }) ;
//            q2.setValuesDataBlock(vars2, x);
//        } else {
//            data.add(dataMap) ;
//            q2.setValuesDataBlock(vars, data) ;
//        }
//        return q2 ; 
//    }
//
////        return elData ;
//
//    private static BindingMap bindingForInput(Map<Var, Node> map) {
//        BindingMap binding = BindingFactory.create() ;
//        bindingForInput(binding, map);
//        return binding ;
//        
//    }
//
//    private static void bindingForInput(BindingMap binding, Map<Var, Node> map) {
//        map.forEach((v,n)->binding.add(v, n)) ;
//    }
// 
//    // Or don't return the input.
//    public static Query parameterizeReplaceAndBIND(Query query, Map<Var, Node> map) {
//        Query q2 = QueryTransformOps.transform(query, map) ;
//        Element el = q2.getQueryPattern() ;
//        ElementGroup elg ;
//        if ( el instanceof ElementGroup )
//            elg = (ElementGroup) el ;
//        else {
//            elg = new ElementGroup() ;
//            elg.addElement(el);
//            q2.setQueryPattern(elg); 
//        }
//        // Check on scope.
//        SyntaxVarScope.check(query) ;
//        Set<Var> acc = new HashSet<>() ;
//        PatternVars.vars(acc, elg) ;
//        
//        map.forEach((v,n)->{
//            if ( acc.contains(v) )
//                System.err.println("Variable "+v+" still present") ;
//            else {
//                ElementBind elb = new ElementBind(v, NodeValue.makeNode(n)) ;
//                elg.addElement(elb);
//            }
//        }) ;
//        return q2 ; 
//    }

    
//    /** Create a new UpdateRequest with occurences of specific variables replaced by some node value. 
//     * @param request UpdateRequest
//     * @param map Mapping from {@link Var} to {@link Node}
//     * @return Query, with replacements 
//     */
//    public static UpdateRequest parameterize(UpdateRequest request, Map<Var, Node> map) {
//        return UpdateTransformOps.transform(request, map) ;
//    }
//
//    /** Create a new Update with occurences of specific variables replaced by some node value. 
//     * @param request Update
//     * @param map Mapping from {@link Var} to {@link Node}
//     * @return Query, with replacements 
//     */
//    public static Update parameterize(Update request, Map<Var, Node> map) {
//        return UpdateTransformOps.transform(request, map) ;
//    }
//    /** Create a new query with occurences of variables replaced by constants. 
//     * @param query
//     * @param map Mapping from variable name to {@link RDFNode}
//     * @return Query, with replacements 
//     */
//    public static Query setVariables(Query query, Map<String, RDFNode> map) {
//        Map<Var, Node> map2 = new HashMap<>() ;
//        map.forEach((k,v) -> map2.put( Var.alloc(k), v.asNode()) ) ;
//        return parameterize(query,map2) ;
//    }
//    
//    /** Create a new query with occurences of variables replaced by constants
//     * based on the given {@link QuerySolutionMap}.
//     * @param query
//     * @param querySolutionMap Mapping from variable name to {@link RDFNode}
//     * @return Query, with replacements 
//     */
//    public static Query setVariables(Query query, QuerySolutionMap querySolutionMap) {
//        return  setVariables(query, querySolutionMap.asMap() ) ;
//    }
}

