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

import org.apache.jena.sparql.algebra.* ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;

/** Walk algebra, {@link Op}s and {@link Expr}s. */
public class Walker {

    /** Walk visiting every {@link Op} with an {@link OpVisitor},
     * including inside expressions.
     */
    public static void walk(Op op, OpVisitor opVisitor) {
        Objects.requireNonNull(opVisitor) ;
        walk(op, opVisitor, null);
    }
    
    /** Walk visiting every {@link Op} and every {@link Expr},
     *  starting from an {@link Op}.
     */
    public static void walk(Op op, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( op == null )
            return ;
        createWalker(opVisitor, exprVisitor).walk(op);
    }
    
    /** Walk visiting every {@link Expr} with an {@link ExprVisitor},
     * including inside any {@link Op} in expressions.
     */
    public static void walk(Expr expr, ExprVisitor exprVisitor) {
        Objects.requireNonNull(exprVisitor) ;
        walk(expr, null, exprVisitor);
    }
    
    /** Walk visiting every {@link Op} and every {@link Expr},
     *  starting from an {@link Expr}.
     */
    public static void walk(Expr expr, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( expr == null )
            return ;
        Objects.requireNonNull(expr) ;
        createWalker(opVisitor, exprVisitor).walk(expr);
    }
    
    /** Walk visiting every {@link Expr} with an {@link ExprVisitor},
     * including inside any {@link Op} in expressions.
     */
    public static void walk(ExprList exprList, ExprVisitor exprVisitor) {
       walk(exprList, null, exprVisitor);
    }
    
    /** Walk visiting every {@link Op} and every {@link Expr},
     *  starting from an {@link ExprList}.
     */
    public static void walk(ExprList exprList, OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( exprList == null )
            return ;
        Objects.requireNonNull(exprVisitor) ;
        exprList.forEach(e->walk(e,opVisitor, exprVisitor)) ;
    }

    public static void walk(VarExprList varExprList, ExprVisitor exprVisitor) {
        Objects.requireNonNull(exprVisitor) ;
        walk(varExprList, null, exprVisitor);
     }
     
     public static void walk(VarExprList varExprList, OpVisitor opVisitor, ExprVisitor exprVisitor) {
         if ( varExprList == null )
             return ;
         varExprList.forEach((v,e)->walk(e,opVisitor, exprVisitor)) ;
     }
 
    private static OpVisitor   nullOpVisitor   = new OpVisitorBase() ;
    private static ExprVisitor nullExprVisitor = new ExprVisitorBase() ;
     
    public static WalkerVisitor createWalker(OpVisitor opVisitor, ExprVisitor exprVisitor) {
        if ( opVisitor == null )
            opVisitor = nullOpVisitor ;
        if ( exprVisitor == null )
            exprVisitor = new ExprVisitorBase() ;
        return new WalkerVisitor(opVisitor, exprVisitor)  ;
    }
    
     /** Transform an {@link Op}. */
    public static Op transform(Op op, Transform opTransform, ExprTransform exprTransform) {
        ApplyTransformVisitor v = createTransformer(opTransform, exprTransform) ;
        walk(op, v) ;
        return v.opResult() ;
    }
    
    /** Transform an {@link Expr}. */
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
        
    private static Transform     nullOpTransform   = new TransformBase() ;
    private static ExprTransform nullExprTransform = new ExprTransformBase() ;
 
    private static ApplyTransformVisitor createTransformer(Transform opTransform, ExprTransform exprTransform) {
        if ( opTransform == null )
            opTransform = nullOpTransform ;
        if ( exprTransform == null )
            exprTransform = nullExprTransform ;
        return new ApplyTransformVisitor(opTransform, exprTransform) ;
    }
}

