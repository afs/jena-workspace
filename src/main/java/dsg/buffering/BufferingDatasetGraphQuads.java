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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dsg.buffering.old.DatasetGraphQuads2;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;

/**
 * A {@link DatasetGraph} that provides buffering of changes.
 * It keeps two collections, of added and removed triples/quads, and only passes changes down to the base DatasetGraph
 * when asked to (e.g. in transaction commit).
 * <p>
 * This implementation is designed for small amounts of buffering, in order to capture
 * changes. It can be used to add "abort" capability for small-scale transactions
 * where the underlying dataset does not have "supportsTransactionAbort".
 * <p>
 *
 * See also {@code RDFPatch}, {@code DatasetGraphChanges} for a stream of changes.
 *
 * @see BufferingDatasetGraph
 */
public class BufferingDatasetGraphQuads extends DatasetGraphQuads2 implements BufferingCtl {

    private final DatasetGraph base;

    // A read-optimization. Over indexing?
    // private DatasetGraph addedQuads   = DatasetGraphFactory.create();
    private Set<Quad>    addedQuads   = new HashSet<>();
    private Set<Quad>    deletedQuads = new HashSet<>();

    private static Iterator<Quad> find(Collection<Quad> quads, Node g, Node s, Node p, Node o) {
        return quads.stream().filter(q->M.match(q,g,s,p,o)).iterator();
    }

    private Iterator<Quad> findInAdded(Node g, Node s, Node p, Node o) {
        return addedQuads.stream().filter(q->M.match(q,g,s,p,o)).iterator();
//        return addedQuads.find(g,s,p,o);
    }


    public BufferingDatasetGraphQuads(DatasetGraph dsg) {
         this.base = dsg;
    }

    // True  -> read-optimized.
    // False -> write-optimized
    private static final boolean UNIQUE = true;

    @Override
    public void add(Quad quad) {
        // Whether UNIQUE or not, we to ensure we can commit by adding then deleting new quads.
        // That means added we need keep addedQuads and deletedQuads misjoint.
        deletedQuads.remove(quad);
        if ( UNIQUE && base.contains(quad) )
            return ;
        addedQuads.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        addedQuads.remove(quad);
        if ( UNIQUE && ! base.contains(quad) )
            return ;
        deletedQuads.add(quad);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> extra = findInAdded(g, s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.find(g, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        Iterator<Quad> extra = findInAdded(g, s, p, o);
        Iter<Quad> iter =
            Iter.iter(base.findNG(g, s, p, o))
                .filter(q->! deletedQuads.contains(q))
                .append(extra);
        if ( ! UNIQUE )
            iter = iter.distinct();
        return iter;
    }

    @Override
    public boolean contains(Quad quad) {
        return base.contains(quad) || addedQuads.contains(quad);
    }

    // Prefix Mapping buffering versions.
    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }


    // Leave to DatasetGraphBase.contains/4.

    public Collection<Quad> addedQuads() {
        return addedQuads;
    }

    public Collection<Quad> deletedQuads() {
        return deletedQuads;
    }

    @Override
    public PrefixMap prefixes() {
        throw new NotImplemented();
        //return null;
    }

    @Override
    public void begin() { base.begin(); }

    @Override
    public void begin(TxnType type) { base.begin(type); }

    @Override
    public void begin(ReadWrite readWrite) { base.begin(readWrite); }

    @Override
    public boolean promote() { return base.promote(); }

    @Override
    public boolean promote(Promote mode) { return base.promote(mode); }

    @Override
    public void commit() {
        flush();
        base.commit();
    }

    @Override
    public void flush() {
        addedQuads.forEach(base::add);
        deletedQuads.forEach(base::delete);
        addedQuads.clear();
        deletedQuads.clear();
    }

    @Override
    public void abort() {
        // Base has not been changed yet so abort=commit=no-op
        if ( base.supportsTransactionAbort() )
            base.abort();
        else
            base.commit();
        addedQuads.clear();
        deletedQuads.clear();
    }

    @Override
    public void end() { base.end(); }

    @Override
    public boolean supportsTransactions() {
        return base.supportsTransactions();
    }

    @Override
    public boolean supportsTransactionAbort() {
        return base.supportsTransactionAbort();
    }

    @Override
    public ReadWrite transactionMode() {
        return base.transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return base.transactionType();
    }

    @Override
    public boolean isInTransaction() {
        return base.isInTransaction();
    }
}
