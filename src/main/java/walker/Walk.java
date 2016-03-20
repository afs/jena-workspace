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

package walker ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.algebra.* ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.sse.SSE ;

public class Walk {
    
    // Mega walker - including before/after support or
    // at least built-in graph tracking. No other before/after use.
    
    // Check for ToDo/XXX
    
    /* walker 
     * Switches for "skip service"
     */
    
    // No visitor -> no-op visitor, for both opsna d exprs
    
    // Drop before/after
    // Drop tests for "if ( opVisitor != null )" except at entry walks.
    
    // Deprecated : before/after support.
    // Remains left only in case it tunrs out we need it after all.
    // Only quadization needs it and that is better done by subclass to intercept OpGraph
    // or explict support
    // or call out just for OpGraph
    
    // Flag for skip service.
    
    // Transformer - create new AppTransformVisitor per level to keep stack checking.

    public static void main(String[] args) {
        ExprVisitor ev = new ExprVisitorBase() {
            @Override
            public void visit(ExprVar nv) {
                System.out.println("Var: " + nv) ;
            }
        } ;
        OpVisitor xv = new OpVisitorBase() {
            @Override
            public void visit(OpBGP op) {
                System.out.println("BGP: " + op.getPattern()) ;
            }
        } ;

        ExprTransform evt = new ExprTransformCopy() {
            @Override
            public Expr transform(ExprVar nv) {
                return new ExprVar(Var.alloc("#" + nv.getVarName())) ;
            }
        } ;
        Transform xvt = new TransformCopy() {
            @Override
            public Op transform(OpBGP op) {
                BasicPattern p = new BasicPattern(op.getPattern()) ;
                p.add(SSE.parseTriple("(:S :P :O)")) ;
                return new OpBGP(p) ;
            }
        } ;

        String x = StrUtils.strjoinNL // ("(filter (= ?x1 3) (table unit) )") ;
        ("(sequence ",
         //"  (filter (= ?x1 3) (bgp (:s ?p1 ?o1)) )",
         "  (filter   (notexists  (filter (= ?s :s) (bgp (triple ?s ?p ?o)) ) ) (table unit) )", ")") ;
        Op op = SSE.parseOp(x) ;

        Walker.walk(op,xv,null);
        //System.exit(0) ;

        String z = StrUtils.strjoinNL(
                                      // "(notexists (filter (= ?s :s) (bgp (triple ?s ?p
                                      // ?o))))"
                                      "(+ 1 ?s)") ;
        Expr e = SSE.parseExpr(z) ;
        Expr e2 = Walker.transform(e, xvt, null) ;

        System.out.println(e2) ;
        // System.exit(0) ;

        System.out.println(op) ;

        Op op1 = Walker.transform(op, xvt, evt) ;
        System.out.println(op1) ;

// System.out.println() ;
// System.out.println(WriterExpr.asString(e)) ;
//
// Expr e2 = ExprTransformer2.transform(evt, xvt, e) ;
// System.out.println(WriterExpr.asString(e2)) ;
    }

}
