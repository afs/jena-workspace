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

package dsg.buffering.old;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dsg.buffering.M;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

// Uses a Set for addedTriples
//   Graph probably better for find().
//   See BufferingGraph0

// Nearly "Don't bother and use BufferingDatasetGraph" but details differ:
// + push down (flush) is different.
// + Prefixes.

public class BufferingGraph_GraphBase extends GraphBase {

    private Set<Triple> addedTriples = ConcurrentHashMap.newKeySet();
    private Set<Triple> deletedTriples = ConcurrentHashMap.newKeySet();
    private final Graph base;
    private final boolean UNIQUE = false;

    public BufferingGraph_GraphBase(Graph other) {
        this.base = other;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triple) {
        Iterator<Triple> extra = findInAdded(triple);
        Iter<Triple> iter =
            Iter.iter(base.find(triple))
                .filter(t->! deletedTriples.contains(t))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return WrappedIterator.create(iter);
    }

    private Iterator<Triple> findInAdded(Triple triple) {
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();
        return addedTriples.stream().filter(t->M.match(t,s,p,o)).iterator();
    }

    public void flush() {
        // causes events on the underlying graphs.
        addedTriples.forEach(base::add);
        deletedTriples.forEach(base::delete);
    }

    // Events. Delay until flush.

    @Override
    public void performAdd( Triple triple ) {
        deletedTriples.remove(triple);
        if ( UNIQUE && base.contains(triple) )
            return;
        addedTriples.add(triple);
    }

    @Override
    public void performDelete( Triple triple ) {
        deletedTriples.remove(triple);
        if ( UNIQUE && ! base.contains(triple) )
            return;
        deletedTriples.add(triple);
    }

    @Override
    public void remove( Node s, Node p, Node o ) {
        super.remove(s, p, o);
    }

    @Override
    public GraphEventManager getEventManager() {
        // Fresh set of event managers so app can listen to changes.
        // Events also get fired when "flush" happens.
        return super.getEventManager();
    }

    // ----

    @Override
    public PrefixMapping getPrefixMapping() { return super.getPrefixMapping(); }

    @Override
    public PrefixMapping createPrefixMapping() { return super.createPrefixMapping(); }


    @Override
    protected boolean graphBaseContains(Triple triple) { return super.graphBaseContains(triple); };

    @Override
    protected int graphBaseSize() { return super.graphBaseSize(); }

    @Override
    public boolean isEmpty() { return super.isEmpty(); }

}
