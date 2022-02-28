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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A Graph that provides value semantics even if the base graph is term-semantics
 */
public class GraphBaseValue extends GraphWrapper {

    public GraphBaseValue(Graph graph) {
        super(graph);
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        Graph g = get();
        if ( g.contains(s, p, o) )
            return true;
        if ( ! o.isConcrete() )
            return false;
        // No term match - try values (Node.sameValueAs)
        ExtendedIterator<Triple> iter = g.find(s,p,Node.ANY).filterKeep(t->t.getObject().sameValueAs(o));
        try {
            return iter.hasNext();
        } finally {
            iter.close();
        }
    }

    @Override
    public boolean contains(Triple t) {
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        return get().find(s, p, o);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple t) {
        return find(t.getSubject(), t.getPredicate(), t.getObject());
    }
}
