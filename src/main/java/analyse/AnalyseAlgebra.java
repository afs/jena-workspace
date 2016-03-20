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

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitorBase ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.expr.E_Function ;
import org.apache.jena.sparql.expr.ExprFunctionN ;
import org.apache.jena.sparql.expr.ExprVisitorBase ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import walker1.OpWalker2 ;

public class AnalyseAlgebra {
    
    public static void main(String[] args) {
        List<String> functionURIs = new ArrayList<>() ;
        List<Node> propFuncURIs = new ArrayList<>() ;
        
        Query query = QueryFactory.read("Q.rq") ;
        System.out.println(query) ;
        Op op = Algebra.compile(query) ;
        //op = Algebra.optimize(op) ;
        //System.out.println(op) ;
        
        
        System.out.println("==== Property functions");
        propFuncURIs.forEach(System.out::println) ;
        System.out.println("==== Filter functions");
        functionURIs.forEach(System.out::println) ;
    }

    
    public static void process(Query query,  List<String> functionURIs, List<Node> propFuncURIs ) {
        Op op = Algebra.compile(query) ;
        //op = Algebra.optimize(op) ;
//        FindRegistered v = new FindRegistered(propFuncURIs, functionURIs) ;
//        OpWalker.walk(op, v);
        OpWalker2.walk(op, new SearcherPF(propFuncURIs), new SearcherF(functionURIs)) ;
    }


    static class SearcherPF extends OpVisitorBase {
        private final List<Node> pf ;
        
        public SearcherPF(List<Node> pf) {
            this.pf = pf ;
        }
        
        // Before optimization.
        @Override public void visit(OpBGP op) {
            op.getPattern().forEach((t)-> {
                Node p = t.getPredicate() ;
                if ( p.isURI() ) {
                    if ( PropertyFunctionRegistry.get().isRegistered(p.getURI()) )
                        pf.add(p) ;
                }
            }) ;
        }
        
        // After optimization.
        @Override public void visit(OpPropFunc opPropFunc) {
            Node p = opPropFunc.getProperty() ;
            if ( p.isURI() )
                pf.add(p) ;
        }
    }
    
    static class SearcherF extends ExprVisitorBase {
        final List<String> cf ;
        public SearcherF(List<String> cf) {
            this.cf = cf ;
        }
        
        @Override
        public void visit(ExprFunctionN e) {
            if ( e instanceof E_Function ) {
                E_Function ef = (E_Function)e ;
                String x = ef.getFunctionIRI() ;
                cf.add(x) ;
            }
        } 
    }

    // -----------------------------------

    
    static class FindRegistered extends OpVisitorBase {
        private final A.FindFunction eVisitor ;
        private final List<String> functionURIs ;
        private final List<Node> pf ;
        
        public FindRegistered(List<Node> pf, List<String> functionURIs) {
            this.functionURIs = functionURIs ;
            this.eVisitor = new A.FindFunction(functionURIs) ;
            this.pf = pf ;
        }

        // Before optimization.
        @Override public void visit(OpBGP op) {
            op.getPattern().forEach((t)-> {
                Node p = t.getPredicate() ;
                if ( p.isURI() ) {
                    if ( PropertyFunctionRegistry.get().isRegistered(p.getURI()) )
                        pf.add(p) ;
                }
            }) ;
        }
        
        // After optimization.
        @Override public void visit(OpPropFunc opPropFunc) {
            Node p = opPropFunc.getProperty() ;
            if ( p.isURI() )
                pf.add(p) ;
        }
        
        // Places to hide expressions.
        
        @Override public void visit(OpFilter opFilter) {
            A.accExpr(functionURIs, opFilter.getExprs()) ;
        }

        @Override public void visit(OpExtend op) {
            A.accExpr(functionURIs, op.getVarExprList()) ;
        }
        
        @Override public void visit(OpAssign op) {
            A.accExpr(functionURIs, op.getVarExprList()) ;
        }

        @Override public void visit(OpGroup op) {
            A.accExpr(functionURIs,op.getGroupVars()) ;
        }
    }
}
