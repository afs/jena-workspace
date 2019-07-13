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

package dsg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dsg.buffering.BufferingDSG_Q;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestBufferingDatasetGraph {
    

    private DatasetGraph base = DatasetGraphFactory.createTxnMem();
    private BufferingDSG_Q buffered = new BufferingDSG_Q(base);
    
    @Test public void basic_1() {
        BufferingDSG_Q dsg = buffered;
        assertTrue(dsg.isEmpty());
    }
    
    @Test public void basic_2() {
        BufferingDSG_Q dsg = buffered;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        dsg.add(q);
        assertTrue(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }
    
    @Test public void basic_3() {
        BufferingDSG_Q dsg = buffered;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        dsg.add(q);
        assertTrue(base.isEmpty());
        assertFalse(dsg.isEmpty());
        dsg.flush();
        assertFalse(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }

    @Test public void basic_4() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        base.add(q1);
        BufferingDSG_Q dsg = new BufferingDSG_Q(base);
        assertFalse(base.isEmpty());
        assertFalse(dsg.isEmpty());
    }

    @Test public void basic_5() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        base.add(q1);
        BufferingDSG_Q dsg = new BufferingDSG_Q(base);
        dsg.delete(q1);
        
        assertFalse(base.isEmpty());
        assertTrue(dsg.isEmpty());
        
        dsg.flush();
        
        assertTrue(base.isEmpty());
        assertTrue(dsg.isEmpty());
        
        dsg.add(q1);
        dsg.delete(q1);
        dsg.flush();
        
        assertTrue(base.isEmpty());
        assertTrue(dsg.isEmpty());
    }

    
}
