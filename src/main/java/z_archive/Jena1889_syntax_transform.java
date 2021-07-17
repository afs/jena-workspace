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

package z_archive;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.QueryCompare;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.syntaxtransform.*;
import org.apache.jena.sparql.util.ExprUtils;

public class Jena1889_syntax_transform {

    private static void clearResultVars(Query query) {
        query.setQueryResultStar(true);
        query.getProject().clear();
    }

    private static void print(VarExprList varExprList) {
        StringJoiner sj = new StringJoiner(" ");
        varExprList.forEachVarExpr((v,e)->{
            if ( e == null ) {
                sj.add(String.format("%s", v));
            } else {
                sj.add(String.format("(%s AS %s)", v,e));
            }
        });
        System.out.print(sj.toString());
        System.out.print(" :: "+varExprList);
    }

    public static void main(String... args) {
        mainParse();
        //mainExprTransform();
        //mainQueryTransform();
    }

    public static void mainParse() {
        Query query = QueryFactory.create("SELECT ?x { SELECT ?o {} }");
        System.out.println(query.isQueryResultStar());
        System.out.print(query);
    }

    public static void mainExprTransform(String... args) {
        VarExprList varExprList = new VarExprList();
//        varExprList.add(Var.alloc("x"));
//        varExprList.add(Var.alloc("y"), ExprUtils.parse("2"));
        // Choice point.
        varExprList.add(Var.alloc("o"));
        //varExprList.add(Var.alloc("o"), ExprUtils.parse("2"));
        varExprList.add(Var.alloc("z"), ExprUtils.parse("2+?o"));

        Map<Var, Node> substitutions = new HashMap<>() ;
        substitutions.put(Var.alloc("o"), SSE.parseNode("1")) ;

        ElementTransform eltrans = new ElementTransformSubst(substitutions);
        NodeTransform nodeTransform = new NodeTransformSubst(substitutions);
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans);

        dwim(varExprList, exprTrans);
        System.exit(0);
    }

    public static void mainQueryTransform(String... args) {
        String[] inputs =    {"SELECT * { ?s ?p ?o }", "SELECT ?s ?o { ?s ?p ?o }"};
        String[] expecteds = {"SELECT * { ?s ?p 1 }", "SELECT ?s { ?s ?p 1 }"};

        for ( int i = 0 ; i < inputs.length ; i++ ) {

            String qs = inputs[i];
            String qExpectedStr = expecteds[i];

            Map<Var, Node> substitutions = new HashMap<>() ;
            substitutions.put(Var.alloc("o"), SSE.parseNode("1")) ;

            Query qExpected = QueryFactory.create(qExpectedStr);

            Query q1 = QueryFactory.create(qs);
            q1.setResultVars();
            Query q2 = QueryTransformOps.transform(q1, substitutions);
            System.out.println("Q2: "+q2.getProject());

            boolean b  = QueryCompare.equals(qExpected, q2);
            if ( b ) {
                System.out.println("SAME");
                System.out.println(q2);
            } else {
                System.out.println("DIFFERENT");
                // Because the (internal) project varExprList has (1 as ?o)
                // If we remove that, works-- but it is iterating over variables :-(
                System.out.println(qExpected.getProject());
                System.out.println(q2.getProject());
                System.out.println(qExpected);
                System.out.println(q2);
            }
        }

        System.exit(0);
    }

    private static void dwim(VarExprList varExprList, ExprTransform exprTransform) {
        System.out.print("Input:  ");
        print(varExprList);
        System.out.println();
        VarExprList varExprList2 = calcTransformVarExprList(varExprList, exprTransform);
        System.out.print("Output: ");
        print(varExprList2);
        System.out.println();
        transformVarExprList(varExprList, exprTransform);
        System.out.print("Output: ");
        print(varExprList);
        System.out.println();
    }

    private static void transformVarExprList(VarExprList varExprList, ExprTransform exprTransform) {
        VarExprList x = calcTransformVarExprList(varExprList, exprTransform);
        varExprList.clear();
        varExprList.addAll(x);
    }

    private static VarExprList calcTransformVarExprList(VarExprList varExprList, ExprTransform exprTransform) {
        VarExprList varExprList2 = new VarExprList();
        boolean changed = false;

        for (Var v : varExprList.getVars()) {
            Expr e = varExprList.getExpr(v);
            // Transform variable.
            ExprVar ev = new ExprVar(v);
            Expr ev2 = exprTransform.transform(ev);
            if (ev != ev2)
                changed = true;

            if ( e == null ) {
                // Variable only.
                if ( ev2.isConstant() ) {
                    // Skip or old var, assign so it become (?old AS substitute)
                    // Skip .
                    // Require transform to add back substitutions "for the record";
                    varExprList2.remove(v);
                    varExprList2.add(v, ev2);
                }
                else if ( ev2.isVariable() ) {
                    varExprList2.add(ev2.asVar());
                } else {
                    throw new ARQException("Can't substitute " + v + " because it's not a simple value: " + ev2);
                }
                continue;
            }

            // There was an expression.
            Expr e2 = ExprTransformer.transform(exprTransform, e);
            if ( e2 != e )
                changed = true;
            if ( ! ev2.isVariable() )
                throw new ARQException("Can't substitute ("+v+", "+e+") as ("+ev2+", "+e2+")");
            varExprList2.add(ev.asVar(), e2);

        }
        return varExprList2;
    }
}
