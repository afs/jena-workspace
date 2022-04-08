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

package eval;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.sse.SSE;

public class DevReorder {
    // DevJoin
    public static void main(String... args) {
//        optionalEqualityScope_02();
//        optionalEqualitySubQuery_01();
//        optimize_02();
        mainReorder();
    }

    public static void mainReorder() {
        String s = """
              (filter (|| (= ?o1 <abc>) (< ?o1 123))
                (bgp
                   (?s :q1 ?o1)
                   (?s :p ?o )
                   (?s :q2 ?o2 )
                   (?o :r 123 )
                ))
                """;
        Op op = SSE.parseOp(s);
        //Op op1 = Transformer.transform(new TransformFilterPlacement(true), op);
        //Op op1 = Transformer.transform(new TransformFilterDisjunction(), op);
        Op op1 = Algebra.optimize(op);
        System.out.println(op1);
    }

    // 1: Reorder BGPs.
    // 2:


    //New Op : triple+filter.
    // CHECK
    static public void optimize_02() {
        String in = StrUtils.strjoinNL
            (
             "(filter (exprlist (|| (= ?var3 'ABC') (= ?var3 'XYZ')) (&& (regex ?var4 'pat1') (!= ?VAR 123)))"
             ,"    (bgp"
             ,"      (triple ?var2 :p1 ?var4)"
             ,"      (triple ?var2 :p2 ?var3)"
             ,"    ))") ;

        // TransformFilterDisjunction(inc equality push down), then filter replacement, the reorder,  (no-op)
        String out = StrUtils.strjoinNL
            ("(filter (!= ?VAR 123)"
             ," (disjunction"
             ,"   (assign ((?var3 'ABC'))"
             ,"     (sequence"
             ,"       (filter (regex ?var4 'pat1')"
             ,"         (bgp (triple ?var2 :p1 ?var4)))"
             ,"       (bgp (triple ?var2 :p2 'ABC'))))"
             ,"   (assign ((?var3 'XYZ'))"
             ,"     (sequence"
             ,"       (filter (regex ?var4 'pat1')"
             ,"         (bgp (triple ?var2 :p1 ?var4)))"
             ,"       (bgp (triple ?var2 :p2 'XYZ'))))))"
             ) ;
        Op op = SSE.parseOp(in);
        Op op1 = Algebra.optimize(op);
        System.out.println(op1);

        // Alternative answer :
        // TransformFilterDisjunction(inc equality push down), the reorder, then filter replacement

        String out1 = StrUtils.strjoinNL
            ("(filter (!= ?VAR 123)"
            ,"  (disjunction"
            ,"      (assign ((?var3 'ABC'))"
            ,"        (filter (regex ?var4 'pat1')"
            ,"          (bgp"
            ,"            (triple ?var2 <http://example/p2> 'ABC')"
            ,"            (triple ?var2 <http://example/p1> ?var4)"
            ,"          )))"
            ,"      (assign ((?var3 'XYZ'))"
            ,"        (filter (regex ?var4 'pat1')"
            ,"          (bgp"
            ,"           (triple ?var2 <http://example/p2> 'XYZ')"
            ,"            (triple ?var2 <http://example/p1> ?var4)"
            ,"         )))))"
            );
    }

    static public void optionalEqualitySubQuery_01() {
        // Presence of ?test in the projection blocks the rewrite.
        // (this is actually over cautious).
        String qs = StrUtils.strjoinNL
            ( "SELECT *"
              , "WHERE {"
              , "    ?test ?p1 ?X."
              , "    FILTER ( ?test = <http://localhost/t1> )"
              , "    { SELECT ?s1 ?test { ?test ?p2 ?o2 } }"
              , "}") ;

        String ops = StrUtils.strjoinNL
            ("(sequence"
             ,"   (assign ((?test <http://localhost/t1>))"
             ,"     (bgp (triple <http://localhost/t1> ?p1 ?X)))"
             ,"   (project (?s1 ?test)"
             ,"     (bgp (triple ?test ?/p2 ?/o2))))"
                ) ;
        Query query = QueryFactory.create(qs);
        Op op = Algebra.compile(query);
        Op op1 = Algebra.optimize(op);
        System.out.println(op1);
        //TestOptimizer.check(qs, ops) ;
    }

 // JENA-294 part II
    static public void optionalEqualityScope_02() {
        // Safe to transform:  ?x is fixed.
        String qs = StrUtils.strjoinNL
            ( "PREFIX : <http://example/> SELECT * {"
              , "    ?x :p ?o2"
              , "    OPTIONAL { ?x :q ?o }"
              , "    FILTER(?x = :x)"
              , "}"
                ) ;
        // JENA-616
        // Answer if FILTER equality optimization done only after FILTER placement.
        String ops = StrUtils.strjoinNL
            ( "(conditional"
              , "  (assign ((?x <http://example/x>))"
              , "     (bgp (triple <http://example/x> <http://example/p> ?o2)))"
              , "  (bgp (triple ?x <http://example/q> ?o))"
              , "  )"
                ) ;

        Query query = QueryFactory.create(qs);
        Op op = Algebra.compile(query);
        Op op1 = Algebra.optimize(op);
        System.out.println(op1);


        // Answer if FILTER equality optimization done before FILTER placement
        // (and possible afterwards as well).
        //        String ops = StrUtils.strjoinNL
        //            ( "(assign ((?x <http://example/x>))"
        //            , "  (conditional"
        //            , "    (bgp (triple <http://example/x> <http://example/p> ?o2))"
        //            , "    (bgp (triple <http://example/x> <http://example/q> ?o))"
        //            , "  ))"
        //            ) ;
        //TestOptimizer.check(qs, ops) ;
    }

}
