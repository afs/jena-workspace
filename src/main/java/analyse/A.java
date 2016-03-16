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

import java.util.Collection ;
import java.util.List ;

import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.function.FunctionRegistry ;

// What about PFs inside EXISTS? 
public class A {

    static void accExpr(List<String> acc, VarExprList varExprList) {
        varExprList.forEach((v,e)->accExpr(acc, e));
    }

    static void accExpr(List<String> acc, ExprList exprList) {
        exprList.forEach((e)->accExpr(acc, e));
    }

    static void accExpr(List<String> acc, Collection<Expr> exprs) {
        exprs.forEach((e)->accExpr(acc, e));
    }

    static void accExpr(List<String> acc, Expr expr) {
        ExprVisitor ev = new FindFunction(acc) ;
        ExprWalker.walk(ev, expr); 
    }
    
    static void accExpr1(List<String> acc, Expr expr) {
        if ( expr instanceof E_Function ) {
            E_Function ef = (E_Function)expr ;
            String x = ef.getFunctionIRI() ;
            if ( FunctionRegistry.get().isRegistered(x) )
                acc.add(x) ;
        }
    }

    static class FindFunction extends ExprVisitorBase {
        final List<String> cf ;
        public FindFunction(List<String> cf) {
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

}
