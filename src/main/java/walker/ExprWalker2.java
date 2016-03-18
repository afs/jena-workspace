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

import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.expr.* ;

/** Walk the expression tree */
public class ExprWalker2 
{
    private final Walker visitor ;

    private ExprWalker2(ExprVisitor visitorExpr , OpVisitor visitorOp) {
        this.visitor = new WalkerBottomUp(visitorExpr, visitorOp) ;
    }

    /** Walk the expression, carrying over the per-object visitors */  
    public void walk(Expr expr) {
        expr.visit(visitor) ;
    }

    public static void walk(ExprVisitor visitor, Expr expr) {
        if ( expr == null )
            return ;
        createWalker(visitor, null).walk(expr);
    }
    
    public static void walk(ExprVisitor visitor, OpVisitor visitorOp, Expr expr) {
        if ( expr == null )
            return ;
        createWalker(visitor, visitorOp).walk(expr);
    }

    public static void walk(ExprVisitor visitor, ExprList exprList) {
        if ( exprList == null )
            return ;
        exprList.forEach(e->walk(visitor, e));
    }
    
    public static void walk(ExprVisitor visitor, OpVisitor visitorOp, ExprList exprList) {
        if ( exprList == null )
            return ;
        exprList.forEach(e->walk(visitor, e));
    }

    public static ExprWalker2 createWalker(ExprVisitor visitorExpr, OpVisitor visitorOp) {
        return new ExprWalker2(visitorExpr, visitorOp)  ;
    }
    
    static class Walker extends ExprVisitorFunction
    {
        private final ExprVisitor visitorExpr ; 
        private final OpVisitor visitorOp ;
        private final boolean topDown ;
        
        Walker(ExprVisitor visitorExpr, OpVisitor visitorOp, boolean topDown) {
            this.visitorExpr = visitorExpr ;
            this.visitorOp = visitorOp ;
            this.topDown = topDown ;
        }
        
        @Override
        protected void visitExprFunction(ExprFunction func) {
            if ( topDown )
                func.visit(visitorExpr) ;    
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
                func.visit(visitorExpr) ;
        }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        {
            // Walk the op
            OpWalker2.walk(funcOp.getGraphPattern(), visitorOp, visitorExpr); 
            funcOp.visit(visitorExpr) ;
        }
        
        @Override
        public void visit(NodeValue nv)         { nv.visit(visitorExpr) ; }
        @Override
        public void visit(ExprVar v)            { v.visit(visitorExpr) ; }
        @Override
        public void visit(ExprAggregator eAgg)    {
            // Unclear
            //eAgg.getAggVar().visit(visitorExpr);
            if ( topDown )
                ExprWalker2.walk(visitorExpr, visitorOp, eAgg.getAggregator().getExprList());
            eAgg.visit(visitorExpr) ; 
            if ( ! topDown )
                ExprWalker2.walk(visitorExpr, visitorOp, eAgg.getAggregator().getExprList());
        }
    }
    
    // Visit current element then visit subelements
    public static class WalkerTopDown extends Walker
    {
        private WalkerTopDown(ExprVisitor visitorExpr, OpVisitor visitorOp)
        { super(visitorExpr,  visitorOp, true) ; }
    }

    // Visit current element then visit subelements
    public static class WalkerBottomUp extends Walker
    {
        private WalkerBottomUp(ExprVisitor visitorExpr, OpVisitor visitorOp)
        { super(visitorExpr,  visitorOp, false) ; }
    }

}