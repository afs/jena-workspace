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

import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.sse.SSE;

public class Skolemizer {

    private static Node skolemize(Node n) {
        return NodeFunctions.blankNodeToIri(n); //RiotLib.blankNodeToIri(n);
    }

    public static void main(String...args) {
        Graph g = SSE.parseGraph("(graph" + "(:s :p   _:a)" + "(:s1 :p1 _:a)" + ")");
        StreamRDF out = StreamRDFLib.writer(System.out);
        out.start();
        StreamRDF skol = new StreamRDFNodeExec(out, Skolemizer::skolemize);
        StreamOps.graphToStream(g, skol);
        out.finish();
        System.out.println("DONE");
    }

    /**
     * Execution a function on nodes in triples and quads. 
     * If the function returns null, drop the triple or 
     * quad being processed.
     */
    static class StreamRDFNodeExec extends StreamRDFWrapper {
        private Function<Node, Node> function;

        public StreamRDFNodeExec(StreamRDF output, Function<Node, Node> function) {
            super(output);
        }

        @Override
        public void triple(Triple triple) {
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();

            Node s1 = function.apply(s);
            if ( s1 == null )
                return;
            Node p1 = function.apply(p);
            if ( p1 == null )
                return;
            Node o1 = function.apply(o);
            if ( o1 == null )
                return;
            if ( s1 == s && p1 == p && o1 == o )
                super.triple(triple);
            else
                super.triple(Triple.create(s1, p1, o1));
        }

        @Override
        public void quad(Quad quad) {
            Node g = quad.getGraph();
            Node s = quad.getSubject();
            Node p = quad.getPredicate();
            Node o = quad.getObject();

            Node g1 = function.apply(g);
            if ( g1 == null )
                return;
            Node s1 = function.apply(s);
            if ( s1 == null )
                return;
            Node p1 = function.apply(p);
            if ( p1 == null )
                return;
            Node o1 = function.apply(o);
            if ( o1 == null )
                return;
            if ( g1 == g && s1 == s && p1 == p && o1 == o )
                super.quad(quad);
            else
                super.quad(Quad.create(g1, s1, p1, o1));
        }
    }
}
