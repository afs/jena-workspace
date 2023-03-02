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

package z_archive.jena1815_var_type;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class Dev1815 {

    // VarExprList : simplify forEach*
    // Check XXX
    // UNION can substitute if can substitute each side. Maybe for different reasons!
    //   Change from "is it set?" to "is is substitutable?"

    public static void main(String ...a) {
        String tests[] = {
//            ":s :p ?A"
//            , "OPTIONAL { :s :q ?B }"
//            , ":s :p ?A OPTIONAL { :s :q ?B }"
//            , ":s :p ?A OPTIONAL { :s :q ?A }"
//            , "{ GRAPH :g { BIND(1 AS ?A) } }"
//            , " ?x :p ?z BIND ((?x + ?y) AS ?Y) ?x ?y ?z "
//            , "{ ?x :p ?z } UNION { ?x :q 123 BIND ((?x + ?y) AS ?Y) }"
//            ,
            "?s ?p ?o  { ?x :p ?z } UNION { ?x :q 123 BIND ((?x + ?y) AS ?Y) }",
        };


        for ( String s : tests ) {
            String qs = StrUtils.strjoinNL
                ("PREFIX  :  <http://example.com/>"
                ,"SELECT * {"
                , s
                ,"}"
                );
            System.out.println(s);
            Query query = QueryFactory.create(qs);
            Op op = Algebra.compile(query);

            VarFinder2 vf = VarFinder2.process(op);
            vf.printFlat(System.out);
            System.out.println();
        }

//        System.out.println(op);
//        if ( op instanceof OpJoin ) {
//            JoinClassifier.print = true ;
//            boolean b = JoinClassifier.isLinear((OpJoin)op);
//            System.out.println("Join linear: "+b);
//        } else if (op instanceof OpLeftJoin ) {
//            LeftJoinClassifier.print = true ;
//            boolean b2 = LeftJoinClassifier.isLinear((OpLeftJoin)op);
//            System.out.println("LeftJoin linear: "+b2);
//        } else
//            System.out.println("Not a join Join");
        System.exit(0);
    }
}
