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

package tdb2.early;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.atlas.lib.ProgressMonitor.Output;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.BulkLoader;
import tdb2.BulkStreamRDF;

/** Bulk loader stream - sequential (simple) - chunks and processes on a single thread */ 
public class BulkLoader_Chunked_Sequential implements StreamRDF, BulkStreamRDF {
    int ChunkSize = 100000 ;
    
    private List<Triple> triples = null;
    private List<Quad> quads = null;

    private final DatasetGraph dsg;
    private final DatasetGraphTDB dsgtdb;
    private final NodeTable nodeTable;
    private final TupleIndex[] indexes; 
    private final Function<List<Triple>, List<Tuple<NodeId>>> chunkProcessor;
    private ProgressMonitor monitor;

    public BulkLoader_Chunked_Sequential(DatasetGraph dsg) {
        if ( ! TDBInternal.isBackedByTDB(dsg) ) {
            throw new IllegalArgumentException("Not a TDB2 database");
        }
        
        this.dsg = dsg;
        this.dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        TupleIndex[] indexes = {
            dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndex(0)
        };

//        TupleIndex[] indexes =
//            dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes();
        
        this.nodeTable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        this.indexes = indexes;
        this.chunkProcessor = nodes();
        this.monitor = new ProgressMonitor("EXP1", BulkLoader.DataTickPoint, BulkLoader.DataSuperTick, output());
    }
    
    static Output output() { 
        return (fmt, args)-> {
            System.out.printf(fmt, args);
            System.out.println();
//            if ( log != null && log.isInfoEnabled() ) {
//                String str = String.format(fmt, args);
//                log.info(str);
//            }
        } ;
    }

    // ---- Stages
    
    // -- Converted
    
    private Function<List<Triple>, List<Tuple<NodeId>>> nodes() {
        return (triples) -> {
            List<Tuple<NodeId>> x = new ArrayList<>(triples.size()); 
            for(Triple triple: triples) {
                x.add(nodes(nodeTable, triple));
            }
            return x;
        };
    }
    
    private static Tuple<NodeId> nodes(NodeTable nt, Triple triple) {
        NodeId s = idForNode(nt, triple.getSubject());
        NodeId p = idForNode(nt, triple.getPredicate());
        NodeId o = idForNode(nt, triple.getObject());
        return TupleFactory.tuple(s,p,o);
    }
    
    private static final NodeId idForNode(NodeTable nodeTable, Node node) {
        return nodeTable.getAllocateNodeId(node);
    }

    // --  Tuple Loader
    private static Consumer<List<Tuple<NodeId>>> loadTuples(TupleIndex index) {
        return (List<Tuple<NodeId>> tuples) -> {
            for(Tuple<NodeId> tuple : tuples)
                index.add(tuple);
        };
    }
    
    // ----
    
    @Override
    public void startBulk() {
        monitor.startMessage();
        monitor.start();
        dsg.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        dsg.commit();
        dsg.end();
        
        monitor.finish();
        monitor.finishMessage();
    }

    @Override
    public void start() {
    }

    @Override
    public void finish() {
        if ( triples != null )
            dispatchTriples(triples);
        // No need to signal the end.
        //dispatchTriples(END_TRIPLES);
    }

    @Override
    public void triple(Triple triple) {
        // ProgressMonitor.
        if ( triples == null )
            triples = allocChunkTriples();
        triples.add(triple);
        if ( triples.size() >= ChunkSize ) {
            dispatchTriples(triples);
            triples = null;
        }
        monitor.tick();
    }

    private void dispatchTriples(List<Triple> triples) {
        sequential(triples);
    }

    private void sequential(List<Triple> triples) {
        if ( triples.isEmpty() )
            return;
        List<Tuple<NodeId>> x = chunkProcessor.apply(triples);
        for(Tuple<NodeId> tuple : x) {
            for (TupleIndex idx : indexes) {
                idx.add(tuple);
            }
        }
    }

    @Override
    public void quad(Quad quad) {}

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    private List<Triple>  allocChunkTriples() {
        return new ArrayList<>(ChunkSize); 
    } 

    private List<Quad>  allocChunkQuads() {
        return new ArrayList<>(ChunkSize); 
    }
}
