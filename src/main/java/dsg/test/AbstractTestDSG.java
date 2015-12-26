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

package dsg.test;

import static org.apache.jena.atlas.iterator.Iter.toList ;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.util.Arrays ;
import java.util.List ;
import java.util.function.Supplier ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

public abstract class AbstractTestDSG {
    static Node s = SSE.parseNode(":s") ;
    static Node p = SSE.parseNode(":p") ;
    static Node o = SSE.parseNode(":o") ;
    static Node g1 = SSE.parseNode(":g1") ;
    
    static Quad q1  = Quad.create(Quad.defaultGraphIRI,  s,  p, o ) ;
    static Quad q2  = Quad.create(Quad.defaultGraphIRI,  s,  p, NodeConst.nodeZero ) ;
    
    static Quad q3  = SSE.parseQuad("(:g1 :s :p :o)") ;
    static Quad q4  = SSE.parseQuad("(:g1 :s :p 1)") ;
    
    static Quad q5  = SSE.parseQuad("(:g2 :s :p :o)") ;
    static Quad q6  = SSE.parseQuad("(:g2 :s :p 1)") ;
    static Quad q7  = SSE.parseQuad("(:g2 :s :p 2)") ;

    static Quad q8  = SSE.parseQuad("(:g3 :s :p :o)") ;
    static Quad q9  = SSE.parseQuad("(:g3 :s :p 1)") ;
    static Quad q10 = SSE.parseQuad("(:g3 :s :p 2)") ;
    
    static List<Quad> data = Arrays.asList(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10) ;

    private Supplier<DatasetGraph> source;

    protected AbstractTestDSG(Supplier<DatasetGraph> source) {
        this.source = source ;
    }

    private DatasetGraph setup(List<Quad> data) {
        DatasetGraph dsg = source.get() ;
        data.forEach(dsg::add) ;
        return dsg ;
    }
    
    @Test public void find_quad_01() {
        DatasetGraph dsg = setup(data) ;
        List<Quad> x = toList(dsg.find()) ;
        assertEquals(10, x.size()) ;
        assertTrue(x.contains(q1)) ;
        assertTrue(x.contains(q5)) ;
    }
    
    
    
}
