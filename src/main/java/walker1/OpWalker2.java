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

package walker1;

import java.util.Iterator ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprVisitor ;

/** Apply a visitor to the whole structure of Ops, recursively, includingTExpr expressions.
 *  Visit sub Op before the current level
 */
public class OpWalker2
{
    public static void walk(Op op, OpVisitor visitor) {
        walk(new WalkerVisitor(visitor, null, null, null), op);
    }

    public static void walk(Op op, OpVisitor visitor, ExprVisitor exprVisitor) {
        walk(new WalkerVisitor(visitor, exprVisitor, null, null), op);
    }

//    // Deprecate
//    public static void walk(Op op, OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
//        walk(new WalkerVisitor(visitor, null, beforeVisitor, afterVisitor), op);
//    }

    public static void walk(WalkerVisitor walkerVisitor, Op op) {
        op.visit(walkerVisitor);
    }
    
    // DEV
    public static void walk(Op op, OpVisitor visitor, ExprVisitor exprVisitor, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        walk(new WalkerVisitor(visitor, exprVisitor, beforeVisitor, afterVisitor), op);
    }
    // Add ExprVisitor to WalkerVisitor?
    // DEV


    // Include expressions.
    public static class WalkerVisitor extends OpVisitorByTypeAndExpr {
        private final OpVisitor   beforeVisitor ;
        private final OpVisitor   afterVisitor ;
        protected final ExprVisitor exprVisitor ;
        protected final OpVisitor visitor ;

        public WalkerVisitor(OpVisitor visitor, ExprVisitor exprVisitor, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
            this.visitor = visitor ;
            this.exprVisitor = exprVisitor ;
            this.beforeVisitor = beforeVisitor ;
            this.afterVisitor = afterVisitor ;
        }

        @Deprecated //??
        protected WalkerVisitor(OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
            this(visitor, null, beforeVisitor, afterVisitor) ;
        }

        public WalkerVisitor(OpVisitor visitor, ExprVisitor exprVisitor) {
            this(visitor, exprVisitor, null, null) ;
        }

        protected WalkerVisitor(OpVisitor visitor) {
            this(visitor, null, null, null) ;
        }

        protected final void before(Op op) {
            if ( beforeVisitor != null )
                op.visit(beforeVisitor) ;
        }

        protected final void after(Op op) {
            if ( afterVisitor != null )
                op.visit(afterVisitor) ;
        }

        @Override
        protected void visitExpr(ExprList exprs) {
            if ( exprVisitor != null )
                ExprWalker2.walk(exprVisitor, visitor, exprs);
        }

        @Override
        protected void visitExpr(VarExprList varExprs) {
            if ( exprVisitor != null )
                ExprWalker2.walk(exprVisitor, visitor, varExprs);
        }

        @Override
        protected void visit0(Op0 op) {
            before(op) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visit1(Op1 op) {
            before(op) ;
            if ( op.getSubOp() != null )
                op.getSubOp().visit(this) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visit2(Op2 op) {
            before(op) ;
            if ( op.getLeft() != null )
                op.getLeft().visit(this) ;
            if ( op.getRight() != null )
                op.getRight().visit(this) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visitN(OpN op) {
            before(op) ;
            for (Iterator<Op> iter = op.iterator(); iter.hasNext();) {
                Op sub = iter.next() ;
                sub.visit(this) ;
            }
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visitExt(OpExt op) {
            before(op) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }
    }
}
