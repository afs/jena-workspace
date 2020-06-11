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

package archive.jena_1789_iso.copy;

import static org.apache.jena.atlas.lib.tuple.TupleFactory.tuple;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.sse.SSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsoDev {

    // Check Iso.
    // Iso.nodeISo - blanknode variables.
    // Why test and also NodeIsomorphismMap?

    // ** BNodeIsoWithVar is stateful.
    // EquityTest -> BiPredicate<Node, Node>
    // Only map once.
    //   EqualityTest
    //   Mappable(n1,n2)  : BNodes or variables.

    private static final Logger LOG = LoggerFactory.getLogger(IsoDev.class);

    public static void main(String[] args) {

        List<Tuple<Node>> b1 = Arrays.asList(genTuple("(?A :p1 ?B)") );
        List<Tuple<Node>> b2 = Arrays.asList(genTuple("(?A :p1 ?A)") );
        List<Tuple<Node>> b3 = Arrays.asList(genTuple("(?B :p1 ?B)") );

        List<Tuple<Node>> x1a = Arrays.asList(genTuple("(?A :p1 ?B)"), genTuple("(?B :p2 ?A)") );
        List<Tuple<Node>> x1x = Arrays.asList(genTuple("(?X :p1 ?Y)"), genTuple("(?Y :p2 ?X)") );
        List<Tuple<Node>> x2a = Arrays.asList(genTuple("(?A :p1 ?B)"), genTuple("(?A :p2 ?B)") );
        List<Tuple<Node>> x3x = Arrays.asList(genTuple("(?X :p1 ?Y)"), genTuple("(?Y :p2 ?A)") );

        // Lists reorder
        List<Tuple<Node>> z1a = Arrays.asList(genTuple("(?B :p2 ?A)"), genTuple("(?A :p1 ?B)") );
        List<Tuple<Node>> z1x = Arrays.asList(genTuple("(?Y :p2 ?X)"), genTuple("(?X :p1 ?Y)") );
        List<Tuple<Node>> z2a = Arrays.asList(genTuple("(?A :p2 ?B)"), genTuple("(?A :p1 ?B)") );
        List<Tuple<Node>> z3x = Arrays.asList(genTuple("(?Y :p2 ?A)"), genTuple("(?X :p1 ?Y)") );

        // Cyclic/same predicate
        List<Tuple<Node>> y1a = Arrays.asList(genTuple("(?A :p1 ?B)"), genTuple("(?B :p1 ?A)") );
        List<Tuple<Node>> y1x = Arrays.asList(genTuple("(?X :p1 ?Y)"), genTuple("(?Y :p1 ?X)") );
        List<Tuple<Node>> y2a = Arrays.asList(genTuple("(?A :p1 ?B)"), genTuple("(?A :p1 ?B)") );
        List<Tuple<Node>> y3x = Arrays.asList(genTuple("(?X :p1 ?Y)"), genTuple("(?Y :p1 ?A)") );

        //IsoAlg.DEBUG = true;
        dwim1(b1, b2, false);
        dwim1(b1, b3, false);

//        dwim1(x1a, x1a, true);
//        dwim1(x1a, z1a, true);
//
//        dwim1(x1a, x2a, false);
//        dwim1(x1a, x1x, true);
//        dwim1(x1a, x3x, false);
//
//        dwim1(z1a, z1a, true);
//        dwim1(z1a, z2a, false);
//        dwim1(z1a, z1x, true);
//        dwim1(z1a, z3x, false);
//
//        dwim1(x1a, z1a, true);
//        dwim1(x1a, z2a, false);
//        dwim1(x1a, z1x, true);
//        dwim1(x1a, z3x, false);
//
//        dwim1(z1a, x1a, true);
//        dwim1(z1a, x2a, false);
//        dwim1(z1a, x1x, true);
//        dwim1(z1a, x3x, false);
//
//        dwim1(y1a, y1a, true);
//        dwim1(y1a, y2a, false);
//        dwim1(y1a, y1x, true);
//        dwim1(y1a, y3x, false);
//
//        dwim1(y1a, x1a, false);
//        dwim1(y1a, x2a, false);
//        dwim1(y1a, x1x, false);
//        dwim1(y1a, x3x, false);

        System.out.println("DONE");
        System.exit(0);
    }

    public static Tuple<Node> genTuple(String x) {
        Triple t = SSE.parseTriple(x);
        return tuple(t.getSubject(), t.getPredicate(), t.getObject());
    }

    public static void dwim1(List<Tuple<Node>> x1, List<Tuple<Node>> x2, boolean expected) {
//        Iso.Mappable mappable0 = (n1,n2) -> n1.isBlank() && n2.isBlank();
//        Iso.Mappable mappable1 = (n1,n2) -> ( n1.isBlank() && n2.isBlank()) || ( n1.isVariable() && n2.isVariable() );
//
//        boolean b = IsoAlg.isIsomorphic(x1, x2, mappable1, NodeUtils.sameValue);
//        if ( expected != b ) {
//            System.out.printf("*** IsoMatcher: (%s,%s)\n", expected, b);
//            System.out.println("   "+x1);
//            System.out.println("   "+x2);
//        } else {
//            System.out.printf("+++ IsoMatcher: (%s,%s)\n", expected, b);
//        }
    }
}
