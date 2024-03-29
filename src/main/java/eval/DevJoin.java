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
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.OptimizerStd;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.util.Context;

public class DevJoin {

    // Filter not pushed into joins.
    // OpVars fixes.
    //ends up in OpVarsPattern?

    // DevJoin
    public static void main(String... args) {
        mainJoin();
    }

    public static void mainJoin() {
        String QS = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,"SELECT * {"
            ,"  ?s :p1 ?o ."
            ,"  ?s :p2 ?o ."
            ,"  ?o :q ?x ."
//            ,"FILTER(?x < 5 )"
//            ,"FILTER(?o > 9)"
            ,"}");

        // Sequence.
        // Filter - can push two ways or one because a join is an equality test.

        Query query = QueryFactory.create(QS);
        Op op = Algebra.compile(query);
        //op = Algebra.toQuadForm(op);
        System.out.println("---- algebra ----");
        System.out.print(op);

        {
            Transform transforms[] = {new TransformPattern2Join_2(), new TransformFilterPlacement()};
            Op op1 = op;
            for ( Transform t : transforms ) {
                op1 = Transformer.transform(t, op1);
            }
            System.out.println("---- joins+place ----");
            System.out.print(op1);
        }

        Op opx = optimize(op);
        System.out.println("---- std ----");
        System.out.print(opx);
        System.out.println("---- std+joins ----");
        opx = Transformer.transform(new TransformPattern2Join_2(), opx);
        System.out.print(opx);
    }

    static Op optimize(Op op) {
        return new Optimize2().rewrite(op);
    }

    // also breaking up BGPs..
    static class Optimize2 extends OptimizerStd {

        static Context cxt() {
            Context cxt = ARQ.getContext().copy();
            //cxt.set(ARQ.optFilterPlacementBGP, false);
            return cxt;
        }

        public Optimize2() {
            super(cxt());
        }

//        @Override
//        protected Op transformJoinStrategy(Op op) { return op ; }


    }

}
