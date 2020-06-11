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

package dsg.buffering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestBufferingGraph {
    //More needed

    private Graph base = GraphFactory.createGraphMem();
    private BufferingGraph buffered = new BufferingGraph(base);

    @Test public void basic_1() {
        BufferingGraph graph = buffered;
        assertTrue(graph.isEmpty());
    }

    @Test public void basic_2() {
        BufferingGraph graph = buffered;
        Triple t = SSE.parseTriple("(:s :p :o)");
        graph.add(t);
        assertTrue(base.isEmpty());
        assertFalse(graph.isEmpty());
    }

    @Test public void basic_3() {
        BufferingGraph graph = buffered;
        Triple t = SSE.parseTriple("(:s :p :o)");
        graph.add(t);
        assertTrue(base.isEmpty());
        assertFalse(graph.isEmpty());
        graph.flush();
        assertFalse(base.isEmpty());
        assertFalse(graph.isEmpty());
    }

    @Test public void basic_4() {
        Triple t1 = SSE.parseTriple("(:s :p 1)");
        base.add(t1);
        BufferingGraph graph = buffered;
        assertFalse(base.isEmpty());
        assertFalse(graph.isEmpty());
    }

    @Test public void basic_5() {
        Triple t1 = SSE.parseTriple("(:s :p 1)");
        base.add(t1);
        BufferingGraph graph = buffered;
        // New object
        t1 = SSE.parseTriple("(:s :p 1)");
        graph.delete(t1);
        assertFalse(base.isEmpty());
        assertTrue(graph.isEmpty());

        graph.flush();

        assertTrue(base.isEmpty());
        assertTrue(graph.isEmpty());
    }
}
