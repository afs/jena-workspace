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

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.expr.* ;

public class ExprTransformer2
{
    private static ExprTransformer2 singleton = new ExprTransformer2();
    
    /** Get the current transformer of expressions */
    public static ExprTransformer2 get() { return singleton; }
    
    /** Transform an expression */
    public static Expr transform(ExprTransform transform, Expr expr)
    { return transform(transform, null, expr) ; }
    
    /** Transform an expression */
    public static ExprList transform(ExprTransform transform, ExprList exprList)
    { return transform(transform, null, exprList) ; }
    
    /** Transform an expression */
    public static Expr transform(ExprTransform transform, Transform opTransform, Expr expr)
    { return get().transformation(transform, opTransform, expr) ; }

    /** Transform an expression list */
    public static ExprList transform(ExprTransform exprTransform, Transform opTransform, ExprList exprList)
    { return get().transformation(exprTransform, opTransform, exprList) ; }
    
    private Expr transformation(ExprTransform exprTransform, Transform opTransform, Expr expr) {
        ApplyExprTransformVisitor v = new ApplyExprTransformVisitor(exprTransform, opTransform) ;
        return transformation(v, expr) ;
    }

    private ExprList transformation(ExprTransform exprTransform, Transform opTransform, ExprList exprList) {
        ApplyExprTransformVisitor v = new ApplyExprTransformVisitor(exprTransform, opTransform) ;
        ExprList exprList2 = new ExprList() ;
        for ( Expr expr : exprList ) {
            Expr expr2 = transformation(v, expr) ;
            exprList2.add(expr2) ;
        }
        return exprList2 ;
    }

    private Expr transformation(ApplyExprTransformVisitor applyVisitor, Expr expr) {
        ExprWalker2.walk(applyVisitor, expr) ;
        return applyVisitor.exprResult() ;
    }
    
    static
    class ApplyExprTransformVisitor implements ExprVisitor
    {
        private ExprTransform exprTransform ;
        private Transform opTransform ;
        private final Deque<Expr> exprStack = new ArrayDeque<>() ;
        
        final Expr exprResult()
        { 
            if ( exprStack.size() != 1 ) {
                Log.warn(this, "Stack is not aligned (size = "+exprStack.size()+")") ;
                if ( exprStack.isEmpty() )
                    return null ;
            }
            return exprStack.pop() ; 
        }

        ApplyExprTransformVisitor(ExprTransform exprTransform, Transform opTransform) {
            this.exprTransform = exprTransform ;
            this.opTransform = opTransform ;
        }

        @Override
        public void visit(ExprFunction0 func)
        {
            Expr e = func.apply(exprTransform) ;
            push(exprStack, e) ;
        }
        
        @Override
        public void visit(ExprFunction1 func)
        {
            Expr e1 = pop(exprStack) ;
            Expr e = func.apply(exprTransform, e1) ;
            push(exprStack, e) ;
        }

        @Override
        public void visit(ExprFunction2 func)
        {
            Expr e2 = pop(exprStack) ;
            Expr e1 = pop(exprStack) ;
            Expr e = func.apply(exprTransform, e1, e2) ;
            push(exprStack, e) ;
        }

        @Override
        public void visit(ExprFunction3 func)
        {
            Expr e3 = pop(exprStack) ;
            Expr e2 = pop(exprStack) ;
            Expr e1 = pop(exprStack) ;
            Expr e = func.apply(exprTransform, e1, e2, e3) ;
            push(exprStack, e) ;
        }

        @Override
        public void visit(ExprFunctionN func)
        {
            ExprList x = process(func.getArgs()) ;
            Expr e = func.apply(exprTransform, x) ;
            push(exprStack, e) ;
        }
        
        private ExprList process(List<Expr> exprList)
        {
            int N = exprList.size() ;
            List<Expr> x = new ArrayList<>(N) ;
            for ( Expr anExprList : exprList )
            {
                Expr e2 = pop( exprStack );
                // Add in reverse.
                x.add( 0, e2 );
            }
            return new ExprList(x) ;
        }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        {
            ExprList x = null ;
            if ( funcOp.getArgs() != null )
                x = process(funcOp.getArgs()) ;
            Op op = funcOp.getGraphPattern() ;
            if ( opTransform != null )
                op = Transformer2.transform(opTransform, exprTransform, op) ;
            Expr e = funcOp.apply(exprTransform, x, op) ;
            push(exprStack, e) ;

        }

        @Override
        public void visit(NodeValue nv)
        {
            Expr e = nv.apply(exprTransform) ;
            push(exprStack, e) ;
        }

        @Override
        public void visit(ExprVar var)
        {
            Expr e = var.apply(exprTransform) ;
            push(exprStack, e) ;
        }
        
        @Override
        public void visit(ExprAggregator eAgg)
        {
            Expr e = eAgg.apply(exprTransform) ;
            push(exprStack, e) ;
        }
        
        private static void push(Deque<Expr> stack, Expr value)
        {
            stack.push(value) ;
        }
        
        private static Expr pop(Deque<Expr> stack)
        {
            try {
            Expr e = stack.pop();
            if ( e == NodeValue.nvNothing )
                e = null ;
            return e ;
            } catch ( EmptyStackException ex)
            {
                System.err.println("Empty stack") ;
                return null ;
            }
        }
    }
    
    
}
