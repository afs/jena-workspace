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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.*;

/** BufferingDatasetGraph - decomposes operations in "triples" (default graph) and "quads" (named graphs)
 *
 * Compare with with {@link BufferingDatasetGraphQuads} which keeps added/deleted by quads.
 */
public class BufferingDatasetGraph extends DatasetGraphTriplesQuads implements BufferingCtl {

    private DatasetGraph baseDSG;
    protected DatasetGraph get() { return baseDSG; }

    private Set<Triple> addedTriples   = new HashSet<>();
    private Set<Triple> deletedTriples = new HashSet<>();

    private Set<Quad>   addedQuads     = new HashSet<>();
    private Set<Quad>   deletedQuads   = new HashSet<>();

    // True  -> read-optimized.
    // False -> write-optimized
    private static final boolean UNIQUE = true;

    public BufferingDatasetGraph(DatasetGraph dsg) {
        baseDSG = dsg;
    }

    @Override
    public void flush() {
        Graph dftGraph = baseDSG.getDefaultGraph();

        addedTriples.forEach(dftGraph::add);
        deletedTriples.forEach(dftGraph::delete);
        addedQuads.forEach(baseDSG::add);
        deletedQuads.forEach(baseDSG::delete);

        addedTriples.clear();
        deletedTriples.clear();
        addedQuads.clear();
        deletedQuads.clear();
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        deletedTriples.remove(triple);
        if ( UNIQUE && base.getDefaultGraph().contains(triple) )
            return ;
        addedTriples.add(triple);
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        deletedQuads.remove(quad);
        if ( UNIQUE && base.contains(quad) )
            return ;
        addedQuads.add(quad);
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        addedTriples.remove(triple);
        if ( UNIQUE && ! base.getDefaultGraph().contains(triple) )
            return ;
        deletedTriples.add(triple);
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        addedQuads.remove(quad);
        if ( UNIQUE && ! base.contains(quad) )
            return ;
        deletedQuads.add(quad);
    }

    // Via find() if not implemented
    @Override
    public boolean contains(Quad quad) { return contains$(quad, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }

    // Via find() if not implemented
    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return contains$(null, g, s, p, o);
    }

    // Avoid recreating quads
    private boolean contains$(Quad quad, Node g, Node s, Node p, Node o) {
        // The find() pattern.
        if ( Quad.isDefaultGraph(g))
            return containedInDftGraph(g, s, p, o) ;
        if ( ! isWildcard(g) )
            return containedInNG(quad, g, s, p, o) ;
        return containedInAny(quad, g, s, p, o) ;
    }

    private boolean containedInDftGraph(Node g, Node s, Node p, Node o) {
        Triple t = Triple.create(s, p, o);
        if ( addedTriples.contains(t) )
            return true;
        if ( deletedTriples.contains(t) )
            return false;
        return get().contains(g,s,p,o);
    }

    private boolean containedInNG(Quad quad, Node g, Node s, Node p, Node o) {
        if ( quad == null )
            quad = Quad.create(g, s, p, o);
        if ( addedQuads.contains(quad) )
            return true;
        if ( deletedQuads.contains(quad) )
            return false;
        return get().contains(g,s,p,o);
    }

    private boolean containedInAny(Quad quad, Node g, Node s, Node p, Node o) {
        // Sufficient work that less point being locally clever.
        Iterator<Quad> iter = findAny(s,p,o);
        return iter.hasNext();
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        DatasetGraph base = get();
        Iterator<Quad> extra = findInAddedTriples(s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.find(Quad.defaultGraphIRI, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

    private Iterator<Quad> findInAddedTriples(Node s, Node p, Node o) {
        return Iter.iter(addedTriples.iterator())
                    .filter(t->M.match(t,s,p,o))
                    .map(t->Quad.create(Quad.defaultGraphIRI,t));
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return findQuads(g, s, p, o);
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return findQuads(Node.ANY, s, p, o);
    }

    protected Iterator<Quad> findQuads(Node g, Node s, Node p, Node o) {
        DatasetGraph base = get();
        Iterator<Quad> extra = findInAddedQuads(g, s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.find(g, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

//    public Iterator<Triple> findInUnionGraph(Node s, Node p , Node o) {
//        return findUnionGraphTriples(s,p,o).iterator() ;
//    }
//
//    public Iterator<Quad> findQuadsInUnionGraph(Node s, Node p , Node o) {
//        return findUnionGraphTriples(s,p,o).map(t -> new Quad(Quad.unionGraph, t)).iterator() ;
//    }

    private Iterator<Quad> findInAddedQuads(Node g, Node s, Node p, Node o) {
        return Iter.iter(addedQuads.iterator())
                    .filter(t->M.match(t,g,s,p,o));
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(baseDSG, graphNode);
    }

    @Override
    public Graph getUnionGraph() {
        return GraphView.createUnionGraph(baseDSG);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    private final Transactional txn                     = TransactionalLock.createMRSW() ;
    protected final Transactional txn()                 { return get(); }
    @Override public void begin()                       { txn().begin(); }
    @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn().begin(mode); }
    @Override public void commit()                      { txn().commit(); }
    @Override public boolean promote(Promote mode)      { return txn().promote(mode); }
    @Override public void abort()                       { txn().abort(); }
    @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
    @Override public void end()                         { txn().end(); }
    @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
    @Override public TxnType transactionType()          { return txn().transactionType(); }
    @Override public boolean supportsTransactions()     { return get().supportsTransactions(); }
    @Override public boolean supportsTransactionAbort() { return get().supportsTransactionAbort(); }
}
