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

package transforms;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.algebra.* ;
import org.apache.jena.sparql.algebra.op.OpAssign ;
import org.apache.jena.sparql.algebra.op.OpExtend ;
import org.apache.jena.sparql.algebra.op.OpExtendAssign ;
import org.apache.jena.sparql.algebra.op.OpTable ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.Expr ;

/** Transform <tt>{ BIND(const AS ?Var) ..}</tt>, i.e. 
 * <tt>(bind ((?var const) (table unit))</tt>
 * to a <tt>(table ...)</tt> 
 *   
 */
public class TransformBindData extends TransformCopy {
    public TransformBindData() {}
    
    @Override
    public Op transform(OpAssign opAssign, Op subOp) { 
        Op op2 = process(opAssign, subOp) ;
        if ( op2 == null )
            return super.transform(opAssign, subOp) ;
        return op2 ;
    }
    
    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        Op op2 = process(opExtend, subOp) ;
        if ( op2 == null )
            return super.transform(opExtend, subOp) ;
        return op2 ;
    }

    private Op process(OpExtendAssign op, Op subOp) {
        if ( ! isUnitTable(subOp) )
            return null ;
        // if OpAssign, check no ?var in subOp.
        VarExprList varExprList = op.getVarExprList() ;
        if ( varExprList.size() == 1 ) {
            Var v = varExprList.getVars().get(0) ;
            Expr e = varExprList.getExpr(v) ;
            if ( e.isConstant() ) {
                Table t = TableFactory.create(v, e.getConstant().asNode()) ;
                return OpTable.create(t) ;
            }
        }
        return null ;
//        boolean onlyConstants = true ;
//        for ( Var v : varExprList.getVars() ) {
//            Expr e = varExprList.getExpr(v) ;
//            if ( ! e.isConstant() ) {
//                onlyConstants = false;
//                break ;
//            }
//        }
    }

    // DRY : TransformFilterEquality, TransformFilterInequality
    private static boolean isUnitTable(Op op) {
        if (op instanceof OpTable) {
            if (((OpTable) op).isJoinIdentity())
                return true;
        }
        return false;
    }
    
    public static void main(String ...args) {
        //Query query = QueryFactory.create("SELECT * { BIND(1 as ?X) }") ;
        Query query = QueryFactory.create("SELECT * { VALUES ?x { 1 } }") ;
        //Op x = SSE.parseOp("(join (triple ?X ?Y ?Z) (union (triple ?a ?b ?c) (triple ?d ?e ?f) ) )") ;
        Op x = Algebra.compile(query) ;
        Op z = Transformer.transform(new TransformBindData(), x) ;
        System.out.println(x);
        System.out.println(z);
    }
}
