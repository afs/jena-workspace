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

package walker;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.OpVisitorBase ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.ExprVisitor ;
import org.apache.jena.sparql.expr.ExprVisitorBase ;
import org.apache.jena.sparql.sse.SSE ;

public class Walk {

    public static void main(String[] args) {
        String x = StrUtils.strjoinNL
            (
             "(sequence "
            , "  (filter (= ?x1 3) (bgp (:s ?p1 ?o1)) )"
            , "  (filter   (notexists  (filter (= ?s :s) (bgp (triple ?s ?p ?o)) ) ) (table unit) )"
            ,")"
            ) ;
        Op op = SSE.parseOp(x) ;
        ExprVisitor ev = new ExprVisitorBase() {
           @Override public void visit(ExprVar nv) {
               System.out.println("Var: "+nv) ;
           }
        } ;
        OpVisitor xv = new OpVisitorBase() {
            @Override public void visit(OpBGP op) {
                System.out.println("BGP: "+op.getPattern()) ;
            }
        } ;
        
        OpWalker2.walk(op, xv, ev); 
        
    }

}

