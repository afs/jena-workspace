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

package dev;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFBase ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;

public class Skolemizer extends StreamRDFBase {
    
    
    public static void main(String...args) {
        Graph g = SSE.parseGraph("(graph"+
            "(:s :p   _:a)"+
            "(:s1 :p1 _:a)"+
            ")") ;
        StreamRDF out = StreamRDFLib.writer(System.out) ;
        out.start() ;
        StreamRDF skol = new Skolemizer(out) ;
        StreamOps.graphToStream(g, skol);
        out.finish() ;
        System.out.println("DONE");
    }
    
    private StreamRDF output ;
    
    public Skolemizer(StreamRDF output) {
        this.output = output ;
    }
    
    @Override
    public void triple(Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        if ( !s.isBlank() && !p.isBlank() && ! o.isBlank() ) {
            output.triple(triple) ;
            return ;
        }
        s = skolemize(s) ;
        p = skolemize(p) ;
        o = skolemize(o) ;
        output.triple(Triple.create(s, p, o)) ;
    }

    @Override
    public void quad(Quad quad)
    {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        if ( !g.isBlank() && !s.isBlank() && !p.isBlank() && ! o.isBlank() ) {
            output.quad(quad) ;
            return ;
        }
        g = skolemize(g) ;
        s = skolemize(s) ;
        p = skolemize(p) ;
        o = skolemize(o) ;
        output.quad(Quad.create(g, s, p, o)) ;
    }

    private static Node skolemize(Node n) {
        if ( ! n.isBlank() )
            return n ;
        // Put somewhere!
        String x = "_:"+NodeFmtLib.encodeBNodeLabel(n.getBlankNodeLabel()) ;
        return NodeFactory.createURI(x) ;
    }
}
