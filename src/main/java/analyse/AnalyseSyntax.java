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

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.syntax.* ;

public class AnalyseSyntax {
    // Also : does the walker walk inside EXISTS?
    public static void process(Query query,  List<String> functionURIs, List<Node> propFuncURIs ) {
        WalkSyntax ws = new WalkSyntax(functionURIs, propFuncURIs) ;
        ElementWalker.walk(query.getQueryPattern(), ws);
        processExtra(query, functionURIs, propFuncURIs);
    }
    
    static class WalkSyntax extends ElementVisitorBase {
        final A.FindFunction functionFinder ;
        private List<Node> pf;
        public WalkSyntax(List<String> functionURIs, List<Node> pf) {
            this.functionFinder = new A.FindFunction(functionURIs) ;
            this.pf = pf ;
        }
        @Override
        public void visit(ElementTriplesBlock el)   {
            el.getPattern().forEach((t)-> {
                propertyCheck(pf, t.getPredicate()) ;
            }) ;
            super.visit(el);
        }
        
        private static void propertyCheck(List<Node> pf, Node predicate) {
            if ( PropertyFunctionRegistry.get().isRegistered(predicate.getURI()) ) {
                pf.add(predicate) ;
            }
        }
        
        @Override
        public void visit(ElementPathBlock el)      { 
            el.getPattern().forEach((tp)-> {
                if ( tp.isTriple() )
                    propertyCheck(pf, tp.getPredicate()) ;
            }) ;
        }
        
        // The syntax walker should do this.
//        @Override
//        public void visit(ElementSubQuery el)         {
//            el.getQuery().visit(this);
//        }   
    }
    
    static void processExtra(Query query, List<String> functionURIs, List<Node> propFuncURIs) {
        // And other places expressions hide.
        A.accExpr(functionURIs, query.getHavingExprs()) ;
        A.accExpr(functionURIs, query.getGroupBy()) ;
        A.accExpr(functionURIs, query.getProject()) ;
    }
    
}
