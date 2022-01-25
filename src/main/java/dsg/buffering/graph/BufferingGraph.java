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

package dsg.buffering.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dsg.buffering.BufferingCtl;
import dsg.buffering.L;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphPlain;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * A graph that buffers changes (including prefixes changes) until {@link #flush} or
 * {@link #flushDirect} is called.
 */
public class BufferingGraph extends GraphWrapper implements BufferingCtl {

    // Controls whether to check the underlying graph to check whether to record a change or not.
    // It takes more memory but means teh underlying grap is not touched for add() delete().
    private final static boolean CHECK = true;  // Untested for false ATM - e.g. contains.

    private final Graph addedGraph;
    private final Set<Triple> deletedTriples = new HashSet<>();

    private final BufferingPrefixMapping prefixMapping;

    public static BufferingGraph create(Graph graph) {
        if ( graph instanceof BufferingGraph )
            Log.warn(BufferingGraph.class, "Creating a BufferingGraph over a BufferingGraph");
        return new BufferingGraph(graph);
    }

    public BufferingGraph(Graph graph) {
        super(graph);
        prefixMapping = new BufferingPrefixMapping(graph.getPrefixMapping());
        if ( graph.getCapabilities().handlesLiteralTyping())
            addedGraph = Factory.createDefaultGraph();
        else
            addedGraph = GraphPlain.plain();
    }

    /** Flush the changes to the base graph, using a Graph transaction if possible. */
    @Override
    public void flush() {
        Graph base = get();
        L.executeTxn(base, ()-> flushDirect(base));
    }

    /** Flush the changes directly to the base graph. */
    public void flushDirect() {
        // So that get() is called exactly once per call.
        Graph base = get();
        flushDirect(base);
    }

    private void flushDirect(Graph base) {
        deletedTriples.forEach(base::delete);
        addedGraph.find().forEachRemaining(base::add);
        deletedTriples.clear();
        addedGraph.clear();
        prefixMapping.flush();
    }

    @Override
    public void add(Triple t) {
        execAdd(t);
    }

    @Override
    public void delete(Triple t) {
        execDelete(t);
    }

    private void execAdd(Triple triple) {
        Graph base = get();
        deletedTriples.remove(triple);
        if (containsByEquals(addedGraph, triple) )
            return ;
        if ( CHECK && containsByEquals(base, triple) )
            // Already in base gaph
            // No action.
            return;
        addedGraph.add(triple);
    }

    private void execDelete(Triple triple) {
        Graph base = get();
        addedGraph.delete(triple);

        if ( CHECK && ! containsByEquals(base, triple) )
            return;
        deletedTriples.add(triple);
    }

    public Graph getAdded() {
        return addedGraph;
    }

    public Set<Triple> getDeleted() {
        return deletedTriples;
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return contains(Triple.create(s, p, o));
    }

    @Override
    public boolean contains(Triple triple) {
        if ( addedGraph.contains(triple) )
            return true;
        Graph base = get();
        ExtendedIterator<Triple> iter = base.find(triple).filterDrop(t->deletedTriples.contains(t));
        try { return iter.hasNext(); }
        finally { iter.close(); }
    }

    // XXX REPLACE by *Plain
    // contains by "same term" not "same value".
    private static boolean containsByEquals(Graph graph, Triple triple) {
        return containsByEquals(graph, triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    private static boolean containsByEquals(Graph graph, Node s, Node p, Node o) {
        // Do direct for efficiency.
        if ( ! graph.contains(s,p,o) )
            return false;
        // May have matched by value.  Do a term test find to restrict to RDF terms.
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        try {
            return iter.hasNext();
        } finally { iter.close(); }
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        // addedGraph has the same value/term equality as the base graph.
        Iterator<Triple> extra = findInAdded(s, p, o);
        Iter<Triple> iter =
            Iter.iter(get().find(s, p, o))
                .filter(t->! deletedTriples.contains(t))
                .append(extra);
        if ( ! CHECK )
            iter = iter.distinct();
        return WrappedIterator.create(iter);
    }

//    private final Set<Triple> addedTriples = new HashSet<>();
// Term vs value
//    private Iterator<Triple> findInAdded0(Node s, Node p, Node o) {
//        return addedTriples.stream().filter(t->M.match(t,s,p,o)).iterator();
//    }

    private Iterator<Triple> findInAdded(Node s, Node p, Node o) {
        return addedGraph.find(s,p,o);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject());
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public boolean isEmpty() {
        if (!addedGraph.isEmpty())
            return false;
        Graph base = get();
        if (deletedTriples.isEmpty())
            return base.isEmpty();
        // Go through the full machinery.
        return ! contains(Triple.ANY);
    }

    @Override
    public int size() {
        if ( CHECK )
            return super.size() - deletedTriples.size() + addedGraph.size();
        // If we have been recording actions, not changes, need to be more careful.
        return (int)(Iter.count(find(Triple.ANY)));
    }
}
