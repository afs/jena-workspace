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

package eval.bgp;

import java.util.Collection;
import java.util.HashSet;
import java.util.StringJoiner;

import eval.TransformPattern2Join_2;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderFixed;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

public class DevBGP {

    public static void main(String[] args) {
        // Add connectivity** (it already does)
        // Add caching unconnected iterators
        // Add "one binding solver"
        // Entity centric
        // Push expression into triple pattern fetch. (code exists?)

        // Investigate & write-up: BlazeGraph.
        // Read & write-up: Rya.
        //  Placement

        //    P collision (k items in N space) = k^2/2N
        // So P = 1/2 => root(N)
        // So P = 1/256 => root(N)/128

        // NodeId long+int = 8+4 = 12 bytes = 96 bits:
        //  Room for strings!
        //  Hash ids. 11bytes+4 bits 88 bits : root(N) = 2^44 = 2^30 * 2^14 = billion* 16K

        //     2^44 = 17,592,186,044,416
        //  11 bytes+4 bits = 92 bits -> 2^46 = 65K billion.
        //  11 bytes+ 6 bits = 94 bits -> 2^47 = 128K billion

        //  If variable. length, string inline, URIs as ids.
        // int,long,long = 20 bytes = 160 bits
        // long,long,long = 24 bytes = 192 bits String?

        JenaSystem.init();
        ReorderTransformation xform = new ReorderFixed();
        BasicPattern bgp = SSE.parseBGP("(bgp (:s :p ?o) (:s ?p ?v) (?o :q ?x))") ;
        System.out.println(str(bgp));
        System.out.println();
        BasicPattern bgp2 = xform.reorder(bgp);
        System.out.println(str(bgp2));

        System.out.println();
        // Join-ise BGps, place filters.
        Transform tj = new TransformPattern2Join_2();

        Op opBGP = new OpBGP(bgp);
        Expr expr = SSE.parseExpr("(> ?v 56)");
        Op op = OpFilter.filter(expr, opBGP);
        a2(op);

        Op opBGP2 = new OpBGP(bgp2);
        Op op2 = OpFilter.filter(expr, opBGP2);

        a2(op2);

        // Args Decide join type.
    }

    private static void a2(Op op) {
        Transform tj = new TransformPattern2Join_2();
        Op op2 = Transformer.transform(tj, op);
        Op op3 = Transformer.transform(new TransformFilterPlacement(), op2);
        System.out.println(op3);
    }

    public static void main2(String[] args) {
        // BGP -> single long binding.
        BasicPattern bgp = SSE.parseBGP("(bgp (:s :p ?o) (:s :p ?v) (?o :q ?x))") ;
        Collection<Var> x = vars(bgp) ;
        System.out.println(x);
    }

    static String str(BasicPattern bgp) {
        StringJoiner sj = new StringJoiner(" ");
        bgp.forEach(t->sj.add(SSE.str(t)));
        return sj.toString();
    }

    static Collection<Var> vars(BasicPattern bgp) {
        HashSet<Var> x = new HashSet<>();
        bgp.forEach(t->Vars.addVarsFromTriple(x, t));
        return x;
    }
}
