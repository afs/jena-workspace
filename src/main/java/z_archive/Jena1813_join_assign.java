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

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.LeftJoinClassifier;
import org.apache.jena.sparql.engine.main.VarFinder;

public class Jena1813_join_assign {
    // Operators to pass through:
    // OpDistinct, OpReduce, OpProject, OpSlice
    // OpGraph, OpFilter,(work), OpTopN

//    /** Find the "effective op" - i.e. the one that may be sensitive to linearization */
//    private static Op effectiveOp(Op op) {
//        for (;;) {
//            if ( op instanceof OpExt )
//                op = ((OpExt)op).effectiveOp() ;
//            else if (safeModifier(op))
//                op = ((OpModifier)op).getSubOp() ;
//            // JENA-1813, temporary fix.
//            else if (op instanceof OpGraph )
//                op = ((OpGraph)op).getSubOp() ;
//            else
//                return op;
//        }


    public static void main(String ...a) {
        String qs = StrUtils.strjoinNL("PREFIX  :  <http://example.com/>"
            ,"SELECT * {"
            ,"  { :s :p ?A }"
            ,"  { GRAPH :g { ?s ?p ?o . BIND(1 AS ?A) } }"
            //,"  { ?s ?p ?o . BIND(1 AS ?Z) }"
            ,"}"
            );
        Query query = QueryFactory.create(qs);
        //Query query = QueryFactory.read("/home/afs/tmp/Q.rq");
        Op op = Algebra.compile(query);

        System.out.println(op);
        if ( op instanceof OpJoin ) {
            VarFinder vf = VarFinder.process(op);
            System.out.println(vf.toString());

            JoinClassifier.print = true ;
            boolean b = JoinClassifier.isLinear((OpJoin)op);
            System.out.println("Join linear: "+b);
        } else if (op instanceof OpLeftJoin ) {
            LeftJoinClassifier.print = true ;
            boolean b2 = LeftJoinClassifier.isLinear((OpLeftJoin)op);
            System.out.println("LeftJoin linear: "+b2);
        } else
            System.out.println("Not a join Join");
        System.exit(0);
    }

}
