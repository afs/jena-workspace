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

import java.util.Objects ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprTransform ;
import org.apache.jena.sparql.expr.ExprVisitor ;
import walker1.Transformer2 ;

/** Walk the algebra */
public class Walker {
    public static void walk(Op op, OpVisitor opVisitor) {
        walk(op, opVisitor, null);
    }
    
    public static void walk(Op op, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( op == null )
            return ;
        Objects.requireNonNull(opVisitor) ;
        createWalker(opVisitor, exprVisitor).walk(op);
    }
    
    public static void walk(Expr expr, ExprVisitor exprVisitor) {
        walk(expr, null, exprVisitor);
    }
    
    public static void walk(Expr expr, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( expr == null )
            return ;
        Objects.requireNonNull(expr) ;
        Objects.requireNonNull(exprVisitor) ;
        createWalker(opVisitor, exprVisitor).walk(expr);
    }
    
    public static void walk(ExprList exprList, ExprVisitor exprVisitor) {
       walk(exprList, null, exprVisitor);
    }
    
    public static void walk(ExprList exprList, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( exprList == null )
            return ;
        Objects.requireNonNull(exprVisitor) ;
        exprList.forEach(e->walk(e,opVisitor, exprVisitor)) ;
    }

    public static void walk(VarExprList varExprList, ExprVisitor exprVisitor) {
        walk(varExprList, null, exprVisitor);
     }
     
     public static void walk(VarExprList varExprList, OpVisitor opVisitor, ExprVisitor exprVisitor) {
         if ( varExprList == null )
             return ;
         Objects.requireNonNull(exprVisitor) ;
         varExprList.forEach((v,e)->walk(e,opVisitor, exprVisitor)) ;
     }
 
    public static WalkerVisitor createWalker(OpVisitor visitorOp, ExprVisitor visitorExpr) {
        return new WalkerVisitor(visitorOp, visitorExpr)  ;
    }
    
    // --------  Transformer
    

    /** Transform op */
    public static Op transform(Op op, Transform opTransform, ExprTransform exprTransform) {
        ApplyTransformVisitor v = createTransformer(opTransform, exprTransform) ;
        walk(op, v) ;
        return v.opResult() ;
    }
    
    public static Expr transform(Expr expr, Transform opTransform, ExprTransform exprTransform) {
        ApplyTransformVisitor v = createTransformer(opTransform, exprTransform) ;
        walk(expr, v) ;
        return v.exprResult() ;
    }
    
    /** Transform an algebra expression */
    public static Op transform(Op op, Transform transform) {
       return transform(op, transform, null) ;
    }
    
    /** Transform an expression */
    public static Expr transform(Expr expr, ExprTransform exprTransform) {
        return transform(expr, null, exprTransform) ;
    }
    
    private static ApplyTransformVisitor createTransformer(Transform opTransform, ExprTransform exprTransform) {
        return new ApplyTransformVisitor(opTransform, exprTransform) ;
    }
//    
//    
//    
//    /** Transform an algebra expression and the expressions */
//    public static Op transform(Transform transform, ExprTransform exprTransform, Op op)
//    { return get().transformation(transform, exprTransform, op, null, null) ; }
//
//    /**
//     * Transformation with specific Transform and default ExprTransform (apply transform
//     * inside pattern expressions like NOT EXISTS)
//     */
//    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
//        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
//    }
//
//    /** Transformation with specific Transform and ExprTransform applied */
//    public static Op transform(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor,
//                               OpVisitor afterVisitor) {
//        return get().transformation(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
//    }
//
}


