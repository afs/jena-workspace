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

package loader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetPrefixStorage;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.DatasetPrefixesTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import tdb2.MonitorOutput;
import tdb2.loader.base.ProgressMonitor2;

/** Triple to chunks of Tuples : 2 theraded version
 * Also loads prefixes.
 */ 
public class DataInput1 implements DataInput {
    private long countTriples;
    private long countQuads;

    private final Consumer<List<Tuple<NodeId>>> dest;
    private final DatasetGraphTDB dsgtdb;
    private final NodeTable nodeTable;
    private final DatasetPrefixStorage prefixes;

    // Chunk accumulators.
    private List<Tuple<NodeId>> quads = null;
    private List<Tuple<NodeId>> triples = null;
    private final MonitorOutput output;
    private final ProgressMonitor2 progress;

    // Prefix handler.
    public DataInput1(DatasetGraphTDB dsgtdb, Consumer<List<Tuple<NodeId>>> function, MonitorOutput output) {
        this.dsgtdb = dsgtdb;
        this.dest = function;
        this.nodeTable = dsgtdb.getQuadTable().getNodeTupleTable().getNodeTable();
        this.prefixes = dsgtdb.getPrefixes();
        this.output = output;
        
        this.progress = new ProgressMonitor2("Data", LoaderConst.DataTickPoint, LoaderConst.DataSuperTick, output);
        
        NodeTable nodeTable2 = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        if ( nodeTable != nodeTable2 )
            throw new BulkLoaderException("Different node tables");
    }
    
    // StreamRDF
    private TransactionCoordinator coordinator;
    private Transaction transaction; 
    @Override
    public void start() {
        // Dummy transaction coordinator because TDB2 is transactional only.
        Journal journal = Journal.create(Location.mem());
        coordinator = new TransactionCoordinator(journal);
        coordinator.add(LoaderOps.ntDataFile(nodeTable));
        coordinator.add(LoaderOps.ntBPTree(nodeTable));
        
        // Clean up coordinator setup.
        NodeTupleTable p = ((DatasetPrefixesTDB)prefixes).getNodeTupleTable();
        coordinator.add(LoaderOps.ntDataFile(p.getNodeTable()));
        coordinator.add(LoaderOps.ntBPTree(p.getNodeTable()));
        // Only has one index.
        coordinator.add(LoaderOps.idxBTree(p.getTupleTable().getIndex(0)));
        coordinator.start();
        transaction = coordinator.begin(TxnType.WRITE);
        progress.startMessage();
        progress.start();
    }

    @Override
    public void finish() {
        if ( triples != null && ! triples.isEmpty() ) {
            dispatch(triples);
            triples = null;
        }
        dispatch(LoaderConst.END_TUPLES);
        transaction.commit();
        progress.finish();
        progress.finishMessage("triples/quads");
        //transaction.end();
        //coordinator.shutdown();
    }

    @Override
    public long getCountTriples()   { return countTriples; }
    @Override
    public long getCountQuads()     { return countQuads; }

    @Override
    public void triple(Triple triple) {
        countTriples++;
        progress.tick();
        if ( triples == null )
            triples = allocChunkTriples();
        accTuples(triple, nodeTable, triples);
        if ( triples.size() >= LoaderConst.ChunkSize ) {
            dispatch(triples);
            triples = null;
        }
    }

    private void dispatch(List<Tuple<NodeId>> chunk) {
        dest.accept(chunk);
    }

    @Override
    public void quad(Quad quad) {
        countQuads++;
        if ( quads == null )
            quads = allocChunkQuads();
        accTuples(quad, nodeTable, quads);
        if ( quads.size() >= LoaderConst.ChunkSize ) {
            dispatch(quads);
            quads = null;
        }
    }
    
    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {
        // Clean constant handling.
        prefixes.insertPrefix("", prefix, iri);
    }

    private static void accTuples(Triple triple, NodeTable nodeTable, List<Tuple<NodeId>> acc) {
        acc.add(nodes(nodeTable, triple));
    }
    
    private static void accTuples(Quad quad, NodeTable nodeTable, List<Tuple<NodeId>> acc) {
        acc.add(nodes(nodeTable, quad));
    }
    
    // Recycle?
    private List<Tuple<NodeId>> allocChunkTriples() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    } 

    private List<Tuple<NodeId>> allocChunkQuads() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    }

    private static Tuple<NodeId> nodes(NodeTable nt, Triple triple) {
        NodeId s = idForNode(nt, triple.getSubject());
        NodeId p = idForNode(nt, triple.getPredicate());
        NodeId o = idForNode(nt, triple.getObject());
        return TupleFactory.tuple(s,p,o);
    }
    
    private Function<List<Quad>, List<Tuple<NodeId>>> quadsToNodeIds(NodeTable nodeTable) {
        return (List<Quad> quads) -> {
            List<Tuple<NodeId>> x = new ArrayList<>(quads.size()); 
            for(Quad quad: quads) {
                x.add(nodes(nodeTable, quad));
            }
            return x;
        };
    }

    private static Tuple<NodeId> nodes(NodeTable nt, Quad quad) {
        NodeId g = idForNode(nt, quad.getGraph());
        NodeId s = idForNode(nt, quad.getSubject());
        NodeId p = idForNode(nt, quad.getPredicate());
        NodeId o = idForNode(nt, quad.getObject());
        return TupleFactory.tuple(g,s,p,o);
    }
    
    private static final NodeId idForNode(NodeTable nodeTable, Node node) {
        return nodeTable.getAllocateNodeId(node);
    }
    
}
