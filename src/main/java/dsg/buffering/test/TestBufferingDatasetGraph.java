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

package dsg.buffering.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dsg.buffering.BufferingCtl;
import dsg.buffering.BufferingDatasetGraph;
import dsg.buffering.BufferingDatasetGraphQuads;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestBufferingDatasetGraph {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;

        Function<DatasetGraph, DatasetGraph> f1 = BufferingDatasetGraphQuads::new;
        x.add(new Object[] {"BufferingDatasetGraphQuads", f1});

        Function<DatasetGraph, DatasetGraph> f2 = BufferingDatasetGraph::new;
        x.add(new Object[] {"BufferingDatasetGraph", f2});

        return x ;
    }

    private final DatasetGraph base = DatasetGraphFactory.createTxnMem();
    private final DatasetGraph buffered;

    public TestBufferingDatasetGraph(String name, Function<DatasetGraph, DatasetGraph> factory) {
        buffered = factory.apply(base);
    }

    @Test public void basic_1() {
        DatasetGraph dsg = buffered;
        assertTrue(dsg.isEmpty());
    }

    @Test public void basic_2() {
        DatasetGraph dsg = buffered;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        dsg.add(q);
        assertTrue(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }

    @Test public void basic_3() {
        DatasetGraph dsg = buffered;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        dsg.add(q);
        assertTrue(base.isEmpty());
        assertFalse(dsg.isEmpty());
        ((BufferingCtl)dsg).flush();
        assertFalse(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }

    @Test public void basic_4() {
        DatasetGraph dsg = buffered;
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        base.add(q1);
        assertFalse(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }

    @Test public void basic_5() {
        DatasetGraph dsg = buffered;
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        base.add(q1);
        dsg.delete(q1);

        assertFalse(base.isEmpty());
        assertTrue(dsg.isEmpty());

        ((BufferingCtl)dsg).flush();

        assertTrue(base.isEmpty());
        assertTrue(dsg.isEmpty());

        dsg.add(q1);
        dsg.delete(q1);
        ((BufferingCtl)dsg).flush();

        base.isEmpty();

        assertTrue(base.isEmpty());
        assertTrue(dsg.isEmpty());
    }


}
