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

package archive;

import java.util.Arrays;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class Jena1861_expr_hash {

    public static void main(String... args) {
        testRaceCondition();
    }

    //@Test
    public static void testRaceCondition() {
        if ( false )
        {
            String qs = "SELECT * { BIND(SHA256('foo') AS ?bar) }";
            Query q = QueryFactory.create(qs);
            Op op = Algebra.compile(q);
            System.out.println(op);
//            Op op1 = Algebra.optimize(op);
//            System.out.println(op1);
        }

        Model model = ModelFactory.createDefaultModel();

        // setResultVars() in SPARQLParser -> test fails in TestSyntaxTransform
        // SELECT * case.
        //   IF result vars set, then transform turns ?o into ?o // {?o = 1}
        //   IF result vars not set, then ?o is not in the post-transofrm resultvars

        // Q is whether (1 AS ?o) should be in the trandform and why does it not show up?
        //    Does not show because not in the trasnform query.
        //    Need to add later.?

        /*
Caused by: java.lang.NullPointerException
    at java.base/java.util.ArrayDeque.addFirst(ArrayDeque.java:287)
    at java.base/java.util.ArrayDeque.push(ArrayDeque.java:580)
    at org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor.push(ApplyTransformVisitor.java:447)
    at org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor.visit(ApplyTransformVisitor.java:384)
    at org.apache.jena.sparql.expr.ExprFunction1.visit(ExprFunction1.java:96)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.visitExprFunction(WalkerVisitor.java:265)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.visit(WalkerVisitor.java:246)
    at org.apache.jena.sparql.expr.ExprFunction1.visit(ExprFunction1.java:96)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.walk(WalkerVisitor.java:91)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.lambda$1(WalkerVisitor.java:107)
    at org.apache.jena.sparql.core.VarExprList.lambda$0(VarExprList.java:82)
    at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
    at org.apache.jena.sparql.core.VarExprList.forEachVarExpr(VarExprList.java:79)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.walk(WalkerVisitor.java:105)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.visitVarExpr(WalkerVisitor.java:121)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.visit(WalkerVisitor.java:213)
    at org.apache.jena.sparql.algebra.op.OpExtend.visit(OpExtend.java:101)
    at org.apache.jena.sparql.algebra.walker.WalkerVisitor.walk(WalkerVisitor.java:81)
    at org.apache.jena.sparql.algebra.walker.Walker.walk$(Walker.java:71)
    at org.apache.jena.sparql.algebra.walker.Walker.walk$(Walker.java:62)
    at org.apache.jena.sparql.algebra.walker.Walker.walk(Walker.java:51)
    at org.apache.jena.sparql.algebra.walker.Walker.transform(Walker.java:182)
    at org.apache.jena.sparql.algebra.walker.Walker.transform(Walker.java:172)
    at org.apache.jena.sparql.algebra.Transformer.transformation$(Transformer.java:107)
    at org.apache.jena.sparql.algebra.Transformer.transformation(Transformer.java:103)
    at org.apache.jena.sparql.algebra.Transformer.transform(Transformer.java:46)
    at org.apache.jena.sparql.algebra.optimize.OptimizerStd.transformExprConstantFolding(OptimizerStd.java:180)
         */
        // 2 -- Query.setResultVars not thread safe

        // TransformOps : a mess : transformVarExprList
        // SPARQLParser.parse :; query.setResults.

        // setResultVars synchronized -> vs calculate - set model (atomic pointer change, no harm multiple times.
        // Seems to fix but imperfect

        // resultVarsSet handled badly.
        // findAndAddNamedVars  : return fresh array.
        // memory barriers
        /*
Caused by: java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0
    at java.base/java.util.ArrayList.add(ArrayList.java:486)
    at java.base/java.util.ArrayList.add(ArrayList.java:498)
    at org.apache.jena.sparql.core.VarExprList.add(VarExprList.java:125)
    at org.apache.jena.query.Query._addVar(Query.java:493)
    at org.apache.jena.query.Query._addResultVar(Query.java:475)
    at org.apache.jena.query.Query.addResultVar(Query.java:434)
    at org.apache.jena.query.Query.findAndAddNamedVars(Query.java:736)
    at org.apache.jena.query.Query.setResultVars(Query.java:689)
         */

        for (;;) {
            Query q = QueryFactory.create("SELECT * { BIND(SHA256('foo') AS ?bar) }");
            Arrays.asList(q, q, q, q, q, q, q, q).parallelStream()
            .forEach(query -> {
                try(QueryExecution qe = QueryExecutionFactory.create(query, model)) {
                    ResultSetFormatter.consume(qe.execSelect());
                }
            });
        }


    }
}
