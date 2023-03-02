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

package reorder;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.optimize.TransformReorder;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;
import org.apache.jena.sparql.algebra.walker.WalkerVisitor;
import org.apache.jena.sparql.algebra.walker.WalkerVisitorSkipService;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformBase;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sys.JenaSystem;

public class ReportReorderExists {
    static {
        LogCtl.setLog4j2();
        //FusekiLogging.setLogging();
        LogCtl.setLevel("org.apache.jena.fuseki.Server", "WARN");
        JenaSystem.init();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // Order
    // Filter - expression - exists
    // Filter - subop
    // So it walks down and rebuilds on the way up.

    // What do we need to do to not walk?

    // Check Transformer.

    // ApplyTransformVisitor modification to take "skip expressions"
    // Or the original OpWla
    // Or WalkerSkipExpr

    // Investigate how a expr gets walked into the exists

    // Why do we have "skip service flag" in ApplyTransformVisitor and also WalkerVisitorSkipService?

    public static void main(String...args) {
        // Jena 4.5.0
        String qs1 = """
            PREFIX dash:    <http://tq/dash#>
            PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX sh:      <http://www.w3.org/ns/shacl#>
            PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>

            SELECT *
            WHERE {
                ## ?ps dash:propertyRole dash:IconRole .
                ?ps sh:path ?predicate .
                FILTER EXISTS {
                    ?instance ?predicate ?anyAssertedIcon .
                    ?instance a ?type .
                }
            }
                """;

        String qs2 = """
                PREFIX : <http://example/>
                SELECT * {
                    ?s ?p ?o
                    FILTER EXISTS { ?s ?p ?o . ?s :q :o }
                }
                """;

        String qs = qs1;

        Query query = QueryFactory.create(qs);
        System.out.println(query);
        Op op = Algebra.compile(query);
        System.out.println("Before");
        //System.out.println(op);
        print(op);

        if ( false /* plain */ ) {
            Transform opTransform = new TransformReorder() {
//                @Override
//                public Op transform(OpFilter opFilter, Op subOp) {
//                    System.out.println("Transform ** Filter");
//                    // Already both BGPs visited here.
//                    return super.transform(opFilter, subOp);
//                }
//
//                @Override
//                public Op transform(OpBGP opBGP) {
//                    System.out.println("Transform ** BGP");
//                    System.out.print(opBGP);
//                    System.out.println("Transform ++ BGP");
//                    return super.transform(opBGP);
//                }
            };

            // Stops expressions being it being rewritten : does not affect the walk. BGP rewritten then not used.
            ExprTransform exprTransform = new ExprTransformBase();

            System.out.println();

            /* ********* */
            Op op1 = Transformer.transform(opTransform, exprTransform, op);
            System.out.println("After");
            //System.out.println(op1);
            print(op1);
            System.exit(0);
        }

        // Broken up in constituent parts.
        Transform opTransform = new TransformReorder();
        ExprTransform exprTransform = new ExprTransformCopy();

        // Transformer.transform null -> ExprTransformCopy - copy reconstructs
        ApplyTransformVisitor2 v = new ApplyTransformVisitor2(opTransform, exprTransform, false, false, null,  null);

        // Already pushed onto the expr and op stack by this point.
        // That's done in
//        {
//            @Override
//            public void visit(OpFilter opFilter) {
//                super.visit(opFilter);
//            }
//        };
        // Does null -> ExprTransformCopy - copy reconstructs
        //ApplyTransformVisitor v = Walker.createTransformer(opTransform, exprTransform);
        //WalkerVisitor wv0 = new WalkerVisitorSkipService(v, v, null, null);
        WalkerVisitor wv0 = new WalkerVisitorSkipExpr(v, v, null, null);

        wv0.walk(op);

        /*
         * OpVisitorByTypeAndExpr.visit(OpFilter)
        //==> op.visit(WalkerVisitor) : OpVisitorByTypeAndExpr(WalkerVisitorSkipService)
         *  walk expr
         *  WalkerVisitor :: walk(funcOp.getGraphPattern());
         *  ApplyTransformVisitor.visit(ExprFunctionOp funcOp) {
         *    builds, pushes the new ExprFunctionOp
         *    (1) ExprTransformBase
         *    (2) Or intercept.
         *
         * WalkerVisitor.visit1
         *
    @Override
    public default void visit(OpFilter opFilter) {
        visitExpr(opFilter.getExprs()); -- pushes op
        visit1(opFilter); -- pushes expr
    }
         */

        Op op0 = v.opResult();
        System.out.println("After");
        //System.out.println(op0);
        print(op0);

    }
    public static void main1(String...args) {
        Query query = QueryFactory.read("/home/afs/tmp/Q.rq");
        System.out.println(query);
        Op op = Algebra.compile(query);
        System.out.println("Before");
        System.out.println(op);
        //print(op);

        Op op1 = Algebra.optimize(op);
        System.out.println("Standard");
        print(op1);

        Transform opTransform = new TransformReorder() {
            @Override
            public Op transform(OpFilter opFilter, Op subOp) {
                System.out.println("Transform ** Filter");
                // Already both BGPs visited here.
                return super.transform(opFilter, subOp);
            }

            @Override
            public Op transform(OpBGP opBGP) {
                System.out.println("Transform ** BGP");
                System.out.print(opBGP);
                System.out.println("Transform ++ BGP");
                return super.transform(opBGP);
            }
        };

        // Using ExprTransformBase does not alter expressions but does walk into them.
        // The old OpWalker - does not go into expressions!

        // ApplyTransformVisitor - track down ExprTransform and put if not null
        //    Or just use ExprTransformBase/ExprTransformCopy.

        Transform opTransformPlain = new TransformReorder();
        ExprTransform exprTransform = new ExprTransformBase();
            System.out.println();
        ApplyTransformVisitor v = new ApplyTransformVisitor(opTransform, exprTransform, false,  null,  null);
        // Does null -> ExprTransformCopy - copy reconstructs
        //ApplyTransformVisitor v = Walker.createTransformer(opTransform, exprTransform);
        WalkerVisitor wv0 = new WalkerVisitorSkipService(v, v, null, null) {
            @Override
            public void visit(OpFilter opFilter) {
                System.out.println("Walk ******* Filter");
                visitExpr(opFilter.getExprs());
                visit1(opFilter);
            }
        };
        wv0.walk(op);
        Op op0 = v.opResult();
        System.out.println("After");
        //System.out.println(op0);
        print(op0);
        System.exit(0);
    }

    private static void print(Op op) {
        System.out.println(((OpFilter)op).getExprs());
    }

    public static void main0(String...args) {
//        qparse.main("--print=op", "--print=opt", "--file=/home/afs/tmp/Q.rq");
//        System.exit(0);

        Query query = QueryFactory.read("/home/afs/tmp/Q.rq");
        System.out.println(query);
        Op op = Algebra.compile(query);
        System.out.println("Before");
        System.out.println(op);

//        Op op1 = Algebra.optimize(op);
//        System.out.println("Std optimize");

        Transform opTransform = new TransformReorder();
        ExprTransform exprTransform = new ExprTransformCopy();

        // Where does walking go into expressions?
        /*
         * ApplyTransformVisitor ++
         * Defined in OpVisitorByTypeAndExpr
         *   @Override
         *   public default void visit(OpFilter opFilter) {
         *       visitExpr(opFilter.getExprs());
         *       visit1(opFilter);
         *   }
         *   @Override
         *   public default void visit(OpLeftJoin opLeftJoin) {
         *       visitExpr(opLeftJoin.getExprs());
         *       visit2(opLeftJoin);
         *   }
         *
         *    @Override
         *   public default void visit(OpAssign opAssign) {
         *       visitVarExpr(opAssign.getVarExprList()) ;
         *       visit1(opAssign);
         *   }
         *   OpExtend
         *   OpGrout
         */

        // Plain
        if ( false ) {
            ApplyTransformVisitor v = new ApplyTransformVisitor(opTransform, null, false,  null,  null);
            // Does null -> ExprTransformCopy
            //ApplyTransformVisitor v = Walker.createTransformer(opTransform, exprTransform);
            WalkerVisitor wv0 = new WalkerVisitorSkipService(v, v, null, null);
            wv0.walk(op);
            Op op0 = v.opResult();
            System.out.println("Normal");
            System.out.println(((OpFilter)op0).getExprs());
            System.exit(0);
        }

        // Visits but makes no changes.
        exprTransform = new ExprTransformBase();

        ApplyTransformVisitor v = new ApplyTransformVisitor(opTransform, exprTransform, false,  null,  null);

        // WalkerVisitor.walk(Expr)
        // WalkerVisitor.walk(Exprlist)
        // WalkerVisitor.walk(VarExprList)
        //   exprVisitor == null should work.
        //   does not work uniformly.
        // ApplyTransformVisitor.visit(ExprFunctionOp funcOp)  does not test
        // FIX NEEDED
        // Still does down EXISTS BGP

        System.out.println("Walk");
        WalkerVisitor wv = new WalkerVisitorSkipService(v, v, null, null);
        wv.walk(op);
        Op op2 = v.opResult();
        System.out.println("Modified");
        System.out.println(((OpFilter)op2).getExprs());
        System.out.println();
        System.out.println(op.equals(op2));
        System.exit(0);
    }
}
