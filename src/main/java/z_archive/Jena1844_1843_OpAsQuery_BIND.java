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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.sse.SSE;

public class Jena1844_1843_OpAsQuery_BIND {

 // TODO Optimization formats like OpTopN (which is an optimizer additional algebra operator).
    // TODO More group flattening.
    // OPTIONAL (LeftJoin) unbundles the LHS to avoid { { P OPTIONAL{} } OPTIONAL{} }
    // This is actually a general situation.
    // Adding onto the end of a group when the item added can not merge into the existing last element.
    // e.g. BIND, VALUES.

    // Remove {} before BIND
    // TODO: AFS: SELECT AS vs BIND -- flag.

//    /** Extract the VarExprList from a stack of OpExtend in apply order (deepest first)*/
//    private static Op processExtend(Op op, List<VarExprList> assignments) {
//        while ( op instanceof OpExtend ) {
//            OpExtend opExtend = (OpExtend)op ;
//            assignments.add(opExtend.getVarExprList());
//            op = opExtend.getSubOp() ;
//        }
//        return op ;
//    }


    public static void main(String ...a) {

        String qsa[] = {
            ""
            /*yes*/            //,"SELECT ?s (?o + 5 AS ?B) { ?s ?p ?o }"
            /*yes*/            //, "SELECT ?s ?B { ?s ?p ?o BIND(?o + 5 AS ?B) }"

            /*yes*///            // https://issues.apache.org/jira/browse/JENA-1843
            /*yes*///            , "SELECT * { ?s ?p ?o BIND(?o + 1 AS ?a1) BIND(?v+2 as ?a2) }"
            /*yes*///            , "SELECT * { BIND(?o + 1 AS ?a1) BIND(?v+2 as ?a2) }"
            /*yes*///            // https://issues.apache.org/jira/browse/JENA-1844
            /*yes*///            , "SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) } ORDER BY ?s"

            //, "SELECT * { ?s ?p ?o FILTER(?o+1) ?x ?q ?v FILTER(2) } ORDER BY ?s"

            , "SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) ?x ?q ?v BIND(2 AS ?a2) } ORDER BY ?s"
            , "SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) ?x ?q ?v BIND(?v+2 AS ?a2) }"
        };


//        dwimQ(qs1);
//        System.out.println("- - - - - - - - - - - - -");
//        dwimQ(qs2);
//        System.out.println("- - - - - - - - - - - - -");
        boolean divider = false ;
        int x = 0 ;
        for(String qs : qsa ) {
            if ( qs.isEmpty() )
                continue;
            x++;
            divider |= dwimQ(x, qs, divider);
        }
        System.out.println("DONE");
        System.exit(0);
    }

    private static boolean dwimQ(int x, String qs, boolean divider) {
        Query expected = QueryFactory.create(qs);
        Op op = Algebra.compile(expected);
        Query actual = OpAsQuery.asQuery(op);
        Op op2 = Algebra.compile(actual);
//        if ( op.equals(op2) )
//            return true;
        if ( expected.equals(actual) )
            return true;
        String marker = op.equals(op2) ? "++ ++ ++ ++ ++" : "** ** ** ** **";
        System.out.println(x+" "+marker);
        String s = expected.toString().replace("\n", " ").replaceAll("  *", " ");
        System.out.println(s);
        //System.out.println(op);
        s = actual.toString().replace("\n", " ").replaceAll("  *", " ");
        System.out.println(s);
        //System.out.println(op2);
        System.out.println(x+" "+marker);
        return false ;
    }

    private static void dwimOp(String opStr) {
        Op op = SSE.parseOp(opStr);
        Query actual = OpAsQuery.asQuery(op);
        System.out.println(op);
        System.out.println(actual);
    }
}
