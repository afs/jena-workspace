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

import java.util.Iterator ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;

/** Walk the algebra */
public class Walker implements OpVisitorByTypeAndExpr, ExprVisitorFunction {
 // Mega walker - including before/after support or
    // at least built-in graph tracking. No other before/after use.
    /* One extends OpVisitor, ExprVisitor
     * Or an intimate combination 
     * Need switches for "skip service"
     * A whole package for walk/transforms 
     * Two internal walk deeper methods walk(expr), walk(op) +exprList+varExprlist for
     * convenience */
    
    // Delete ExprVisitorFunction or convert to a mixin
    // OpVisitorByTypeAndExpr as mixin
    // Drop before/after
    // Drop tests for "if ( opVisitor != null )" except at entry walks.
    
    private final OpVisitor   beforeVisitor ;
    private final OpVisitor   afterVisitor ;
    protected final ExprVisitor exprVisitor ;
    protected final OpVisitor opVisitor ;

    // ---- Expr
    // Used at all?
    boolean topDown = false ;

    public Walker(OpVisitor visitor, ExprVisitor exprVisitor) {
        this.opVisitor = visitor ;
        this.exprVisitor = exprVisitor ;
        this.beforeVisitor = null ; // beforeVisitor ;
        this.afterVisitor = null; // afterVisitor ;
    }
    
    public void walk(Op op) {
        op.visit(this);
    }
    
    public void walk(Expr expr) {
        expr.visit(this);
    }
    
    public void walk(ExprList exprList) {
        exprList.forEach(e->walk(e));
    }

    public void walk(VarExprList varExprList) {
        varExprList.forEach((v,e) -> walk(e));
    }

    public static Walker createWalker(OpVisitor visitorOp, ExprVisitor visitorExpr) {
        return new Walker(visitorOp, visitorExpr)  ;
    }

    protected final void before(Op op) {
        if ( beforeVisitor != null )
            op.visit(beforeVisitor) ;
    }

    protected final void after(Op op) {
        if ( afterVisitor != null )
            op.visit(afterVisitor) ;
    }

    // ---- Mode swapping between op and expr. visit=>?walk
    @Override
    public void visitExpr(ExprList exprList) {
        if ( exprVisitor != null )
            exprList.forEach(e->e.visit(this));
    }

    @Override
    public void visitExpr(VarExprList varExprList) {
        if ( exprVisitor != null )
            varExprList.forEach((v,e) -> e.visit(this));
    }
    
    public void visitOp(Op op) {
        if ( opVisitor != null )
            op.visit(this);
    }
    // ----

    @Override
    public void visit0(Op0 op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visit1(Op1 op) {
        before(op) ;
        if ( op.getSubOp() != null )
            op.getSubOp().visit(this) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visit2(Op2 op) {
        before(op) ;
        if ( op.getLeft() != null )
            op.getLeft().visit(this) ;
        if ( op.getRight() != null )
            op.getRight().visit(this) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visitN(OpN op) {
        before(op) ;
        for (Iterator<Op> iter = op.iterator(); iter.hasNext();) {
            Op sub = iter.next() ;
            sub.visit(this) ;
        }
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visitExt(OpExt op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }
    
    @Override
    public void visit(ExprFunction0 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction1 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction2 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction3 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunctionN func) { visitExprFunction(func) ; }
    
    public void visitExprFunction(ExprFunction func) {
        if ( topDown )
            func.visit(exprVisitor) ;    
        for ( int i = 1 ; i <= func.numArgs() ; i++ )
        {
            Expr expr = func.getArg(i) ;
            if ( expr == null )
                // Put a dummy in, e.g. to keep the transform stack aligned.
                NodeValue.nvNothing.visit(this) ;
            else
                expr.visit(this) ;
        }
        if ( !topDown )
            func.visit(exprVisitor) ;
    }
    
    @Override
    public void visit(ExprFunctionOp funcOp) {
        // Walk the op
        funcOp.getGraphPattern().visit(this); 
        funcOp.visit(exprVisitor) ;
    }
    
    @Override
    public void visit(NodeValue nv)         { nv.visit(exprVisitor) ; }
    @Override
    public void visit(ExprVar v)            { v.visit(exprVisitor) ; }
    @Override
    public void visit(ExprAggregator eAgg)  {
        // This is the assignment variable of the aggregation
        // not a normal variable of an expression.
        // (It might be better if ExprAggregator did not use the 
        //eAgg.getAggVar().visit(visitorExpr);
        
        // XXX Hack for varsMentioned

//        if ( topDown )
//            ExprWalker2.walk(visitorExpr, visitorOp, eAgg.getAggregator().getExprList());
        eAgg.visit(exprVisitor) ; 
//        if ( ! topDown )
//            ExprWalker2.walk(visitorExpr, visitorOp, eAgg.getAggregator().getExprList());
    }
}


