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

package dsg.graph;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestG {
    @Test public void sameTerm_node_01() {
        same("<x>", "<x>", true, true);
    }

    @Test public void sameTerm_node_02() {
        same("<x>", "<X>", false, false);
    }

    @Test public void sameTerm_node_03() {
        same("1", "'1'^^xsd:integer", true, true);
    }

    @Test public void sameTerm_node_04() {
        same("1", "'01'^^xsd:integer", true, false);
    }

    @Test public void same_triple_01() {
        sameTriple("(<x> :p 1)", "(<x> :p 01)", true, false);
    }

    @Test public void graphAddInto_01() {
        Graph dstGraph = GraphFactory.createTxnGraph();
        graphAddInto(dstGraph);
    }

    @Test public void graphAddInto_02() {
        Graph dstGraph = GraphFactory.createTxnGraph();
        G2.execTxn(dstGraph, ()->graphAddInto(dstGraph));
    }

    private void graphAddInto(Graph dstGraph) {
        G2.addInto(dstGraph, dataGraph);
        assertEquals(6, dataGraph.size());
        assertEquals(dataGraph.size(), dstGraph.size());

        Set<Triple> set1 = dstGraph.find().toSet();
        assertEquals(dstGraph.size(), set1.size());

        Set<Triple> set2 = dataGraph.find().toSet();
        assertEquals(set1, set2);

    }

    private void sameTriple(String s1, String s2, boolean expectedSameValue, boolean expectedSameTerm) {
        Triple t1 = SSE.parseTriple(s1);
        Triple t2 = SSE.parseTriple(s2);
        boolean b1 = t1.getSubject().sameValueAs(t2.getSubject()) &&
                     t1.getPredicate().sameValueAs(t2.getPredicate()) &&
                     t1.getObject().sameValueAs(t2.getObject());
        assertEquals("SameValue?", expectedSameValue, b1);

        boolean b2 = G3.sameTermMatch(t1.getSubject(), t1.getPredicate(), t1.getObject(), t2);
        assertEquals("SameTerm?", expectedSameTerm, expectedSameTerm);
    }

    private static void same(String s1, String s2, boolean expectedSameValue, boolean expectedSameTerm) {
        Node n1 = SSE.parseNode(s1);
        Node n2 = SSE.parseNode(s2);

        boolean b1 = n1.sameValueAs(n2);
        assertEquals("SameValue?", expectedSameValue, b1);

        boolean b2 = G3.sameTermMatch(n1, n2);
        assertEquals("SameTerm?",expectedSameTerm, b2);
    }

    private static String gs = String.join("\n"
                                          ,"(graph"
                                          ,"  (:s :p :o)"
                                          ,"  (:s :p 1)"
                                          ,"  (:s :p 01)"
                                          ,"  (:s :p 'a')"
                                          ,"  (:s :p 'A'@en)"
                                          ,"  (:s :p 'A'@EN)"
                                          ,")"
                                          );
    private static Graph dataGraph = SSE.parseGraph(gs);

}
