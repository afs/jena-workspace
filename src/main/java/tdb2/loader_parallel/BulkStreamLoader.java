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

package tdb2.loader_parallel;

import static java.lang.String.format;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.atlas.lib.ArrayUtils;
import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.atlas.lib.ProgressMonitor.Output;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.setup.StoreParams;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.BulkLoader;
import tdb2.tools.Tools;

/** Bulk loader stream, parallel */ 
public class BulkStreamLoader implements StreamRDF, BulkStreamRDF {
    public /*private*/ static int ChunkSize = 100_000 ; // 10_000 is about the same.
    
    // TRIPLES
    // Number of indexes in the first stage.  Must be >=1
    public /*private*/ static int SplitTriplesIndexes = 3;
    // Number of indexes in the first stage.  Must be >=1
    public /*private*/ static int SplitQuadsIndexes = 6;
    
    // The exact sizes don't make much difference.
    // Having a fews slots softens jitter but the slowest stage being the node table
    // loader, dominates the speed.
    
    // The parser is faster than nodes ->NodeId stage. 
    public /*private*/ static int QueueSizeData = 10 ;
    // Each indexer alone is faster than the NodeId stage, but the indexer threads contend for system buses and I/O. 
    public /*private*/ static int QueueSizeTuples =  10 ;
    private static final Object outputLock = new Object();
    
    private List<Triple> triples = null;
    private List<Quad> quads = null;
    private final DatasetGraph dsg;
    private final DatasetGraphTDB dsgtdb;
    
    private final NodeTable nodeTableTriples;
    private final NodeTable nodeTableQuads;
    private final TupleIndex primary3Indexes;
    private final TupleIndex primary4Indexes;
    private final TupleIndex[] idxTriples; 
    private final TupleIndex[] idxTriples2;
    private final TupleIndex[] idxQuads; 
    private final TupleIndex[] idxQuads2;
    private final Function<List<Triple>, List<Tuple<NodeId>>> tripleConverter;
    private final Function<List<Quad>, List<Tuple<NodeId>>> quadConverter;
    
    private final BlockingQueue<List<Triple>> pipeTriples;
    private final BlockingQueue<List<Triple>> pipeQuads;
    private final BlockingQueue<List<Tuple<NodeId>>>[] pipesTripleIndexers;
    private final BlockingQueue<List<Tuple<NodeId>>>[] pipesTripleIndexers2;
    private final BlockingQueue<List<Tuple<NodeId>>>[] pipesQuadIndexers;
    private final BlockingQueue<List<Tuple<NodeId>>>[] pipesQuadIndexers2;
    
    private final int waitForCount1;
    private final int waitForCount2;
    private final Semaphore termination = new Semaphore(0);
    private final ProgressMonitor monitor;
    private final Output output;  
    
    // The maximum number of threads to use. 
    private final int threads = Integer.MAX_VALUE;

    // Versions
    // Phased sequential (TDB1 style).
    
    
    // Phase 1.
    
    // 1 -> PahsesSequential => Separate code.
    // 2 -> Parallel parser, SPO loader.
    // 3 -> 2 + 
    
    // Phase 2.
    
    
    // Use of threads is coarse grained so the value of an ExecutorService is limited.
    // Instead, multiple   
    //private ExecutorService executorService = Executors.newFixedThreadPool(??)
    //private ForkJoinPool fjp = new ForkJoinPool(2);

    static public class Builder {
        private DatasetGraph dsg = null;
        
        
        public BulkStreamLoader build() { 
            Objects.requireNonNull(dsg, "Dataset to be loaded not set");
            return new BulkStreamLoader(dsg); 
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public BulkStreamLoader(DatasetGraph dsg) {
        if ( ! TDBInternal.isBackedByTDB(dsg) )
            throw new IllegalArgumentException("Not a TDB2 database");
        
        this.dsg = dsg;
        this.dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        StoreParams params = dsgtdb.getStoreParams();
        
        //params.getPrimaryIndexTriples()
        //params.getPrimaryIndexQuads()
        //params.getPrimaryIndexPrefix()
        
        this.nodeTableTriples = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        this.nodeTableQuads = dsgtdb.getQuadTable().getNodeTupleTable().getNodeTable();
        
        this.tripleConverter = triplesToNodeIds(nodeTableTriples);
        this.quadConverter = quadsToNodeIds(nodeTableQuads);

        // Partition indexes.
        TupleIndex[] dbTriples = dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes();
        TupleIndex[] dbQuads = dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes();
        
        if ( SplitTriplesIndexes < 1 || SplitTriplesIndexes > dbTriples.length )
            throw new IllegalArgumentException(format("SplitTriplesIndexes (%d) must be between %d (inc) and %d (exc)", SplitTriplesIndexes, 1, dbTriples.length));
            
        if ( SplitQuadsIndexes < 1 || SplitQuadsIndexes > dbQuads.length )
            throw new IllegalArgumentException(format("SplitQuadsIndexes (%d) must be between %d and %d (inclusive)", SplitQuadsIndexes, 1, dbQuads.length));
        
        this.idxTriples = Arrays.copyOfRange(dbTriples, 0, SplitTriplesIndexes);
        this.idxTriples2 = Arrays.copyOfRange(dbTriples, SplitTriplesIndexes, dbTriples.length);
        this.idxQuads = Arrays.copyOfRange(dbTriples, 0, SplitQuadsIndexes);
        this.idxQuads2 = Arrays.copyOfRange(dbQuads, SplitQuadsIndexes, dbQuads.length);

        this.primary3Indexes = idxTriples[0];
        this.primary4Indexes = idxQuads[0];
        
        this.output = output();
        this.monitor = new ProgressMonitor("EXP", BulkLoader.DataTickPoint, BulkLoader.DataSuperTick, output);

        this.pipeTriples = new ArrayBlockingQueue<>(QueueSizeData);
        this.pipeQuads = new ArrayBlockingQueue<>(QueueSizeData);
        
        this.pipesTripleIndexers = ArrayUtils.alloc(BlockingQueue.class, idxTriples.length);
        this.pipesTripleIndexers2 = ArrayUtils.alloc(BlockingQueue.class, idxTriples2.length);

        this.pipesQuadIndexers = ArrayUtils.alloc(BlockingQueue.class, 0);
        this.pipesQuadIndexers2 = ArrayUtils.alloc(BlockingQueue.class, 0);

        for ( int i = 0 ; i < idxTriples.length ; i++ ) 
            pipesTripleIndexers[i] = new ArrayBlockingQueue<List<Tuple<NodeId>>>(QueueSizeTuples);
        
        for ( int i = 0 ; i < idxTriples2.length ; i++ ) 
            pipesTripleIndexers2[i] = new ArrayBlockingQueue<List<Tuple<NodeId>>>(QueueSizeTuples);
        
        // XXX Quads
        
        waitForCount1 = 1 + pipesTripleIndexers.length;
        waitForCount2 = pipesTripleIndexers2.length;
    }
    
    static Output output() { 
        return (fmt, args)-> {
            synchronized(outputLock) {
                System.out.printf(fmt, args);
                System.out.println();
//            if ( log != null && log.isInfoEnabled() ) {
//                String str = String.format(fmt, args);
//                log.info(str);
//            }
            }
        } ;
    }

    // ---- Stages
    
    // -- Converted
    
    private static Function<List<Triple>, List<Tuple<NodeId>>> triplesToNodeIds(NodeTable nodeTable) {
        return (List<Triple> triples) -> {
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
    }

    @Override
    public void finishBulk() {
        monitor.finish();
        monitor.finishMessage();
        // Debug
        output.print("ChunkSize = %,d : Split = %d : Queue(triples) = %d : Queue(tuples) = %d", ChunkSize, SplitTriplesIndexes, QueueSizeData, QueueSizeTuples);
    }

    @Override
    public void start() {
        new Thread(()->stageConverter()).start();
        for ( int j = 0 ; j < idxTriples.length ; j++ ) {
            int k = j ;
            TupleIndex idx = idxTriples[k];
            new Thread(()->stageIndex(pipesTripleIndexers[k], idx), "Index: "+idx.getName()).start();
        }
    }

    @Override
    public void finish() {
        if ( triples != null )
            dispatchTriples(triples);
        output.print("Finished reading in data = %,d (triples+quads)", monitor.getTicks());
        dispatchTriples(END_TRIPLES);
        // Essential that the node table and primary have finished their transactions.  
        acquire(termination, waitForCount1);
        if ( waitForCount2 <= 0 )
            return ;
        
        // Phase 2.
        ProgressMonitor monitor2 = new ProgressMonitor("EXP2", BulkLoader.IndexTickPoint, BulkLoader.IndexSuperTick, output);
        //monitor2.startMessage();
        //Monitor is just to indicate progress.
        monitor2.start();
        for ( int j = 0 ; j < idxTriples2.length ; j++ ) {
            int k = j ;
            TupleIndex idx = idxTriples2[k];
            new Thread(()->stageIndex(pipesTripleIndexers2[k], idx),"Index(2): "+idx.getName()).start();
        }
        playIndex(primary3Indexes, monitor2);
        monitor2.finish();
        //monitor2.finishMessage();

        acquire(termination, waitForCount2);
    }

    private static void acquire(Semaphore semaphore, int numPermits) { 
        try { semaphore.acquire(numPermits); }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void quad(Quad quad) {
        if ( quads == null )
            quads = allocChunkQuads();
        quads.add(quad);
        if ( quads.size() >= ChunkSize ) {
            dispatchQuads(quads);
            quads = null;
        }
        monitor.tick();
    }

    @Override
    public void triple(Triple triple) {
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
        try {
            pipeTriples.put(triples);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dispatchQuads(List<Quad> quads) {
        throw new NotImplementedException("Quads");
//        try {
//            pipeQuads.put(quads);
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    // --Stages.
    
    // Pool of workers - more adaptive.
    
    // Txn
    // End maker.
    
    private List<Triple> END_TRIPLES        = new ArrayList<>(); 
    private List<Tuple<NodeId>> END_TUPLES  = new ArrayList<>();
    
    private void stageConverter() {
        // nodeTable
        Journal journal = Journal.create(Location.mem());
        
        TransactionCoordinator coordinator = new TransactionCoordinator(journal);
        coordinator.add(ntDataFile(nodeTableTriples));
        coordinator.add(ntBPTree(nodeTableTriples));
        if ( nodeTableTriples != nodeTableQuads ) {
            coordinator.add(ntDataFile(nodeTableQuads));
            coordinator.add(ntBPTree(nodeTableQuads));
        }
        coordinator.start();
        Transaction transaction = coordinator.begin(TxnType.WRITE);
        try {
            for ( ;; ) {
                List<Triple> triples = pipeTriples.take();
                List<Tuple<NodeId>> x = tripleConverter.apply(triples);
                dispatchBlock(pipesTripleIndexers, x);
                if ( x.isEmpty() ) {
                    break;
                }
            }
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.abort();
        }
        //coordinator.shutdown();
        output.print("Finish - node table");
        termination.release();
    }

    private void stageIndex(BlockingQueue<List<Tuple<NodeId>>> pipe, TupleIndex idx) {
        Journal journal = Journal.create(Location.mem());
        
        TransactionCoordinator coordinator = new TransactionCoordinator(journal);
        coordinator.add(idxBTree(idx));
        coordinator.start();
        Transaction transaction = coordinator.begin(TxnType.WRITE);
        try {
            Consumer<List<Tuple<NodeId>>> loader = loadTuples(idx);
            for (;;) {
                List<Tuple<NodeId>> tuples = pipe.take();
                if ( tuples.isEmpty() )
                    break;
                loader.accept(tuples);
            }
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.abort();
        }
        //coordinator.shutdown();
        output.print("Finish - index "+idx.getName());
        termination.release();
    }

    /** Play the index to the tuples pipes */
    private void playIndex(TupleIndex idx, ProgressMonitor progress) {
        Journal journal = Journal.create(Location.mem());
        TransactionCoordinator coordinator = new TransactionCoordinator(journal);
        coordinator.add(idxBTree(idx));
        coordinator.start();
        Transaction transaction = coordinator.begin(TxnType.READ);
        
        Iterator<Tuple<NodeId>> iter = idx.all();
        List<Tuple<NodeId>> block = null;
        try {
            while( iter.hasNext() ) {
                Tuple<NodeId> t = iter.next();
                if ( block == null )
                    block = new ArrayList<>(ChunkSize);
                block.add(t);
                progress.tick();
                if ( block.size() >= ChunkSize ) {
                    dispatchBlock(pipesTripleIndexers2, block);
                    block = null;
                }
            }
            if ( block != null && block.size() >= 0 ) {
                dispatchBlock(pipesTripleIndexers2, block);
                block = null;
            }
            dispatchBlock(pipesTripleIndexers2, END_TUPLES);
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.abort();
        }
        //coordinator.shutdown();
        output.print("Finish replay - index "+idx.getName());
    }
    
    /** Send an item to several BlockingQueues */
    private static <X> void dispatchBlock(BlockingQueue<X>[] pipes, X block) {
        for ( int i = 0 ; i < pipes.length ; i++ ) {
            try { pipes[i].put(block); }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    private void sequential(List<Triple> triples) {
//        if ( triples.isEmpty() )
//            return;
//        List<Tuple<NodeId>> x = tripleConverter.apply(triples);
//        
//        
//        // Send to next stats
//        for(Tuple<NodeId> tuple : x) {
//            for (TupleIndex idx : idxTriples) {
//                idx.add(tuple);
//            }
//        }
//    }

    /** Copy one index into several others */
    private static void copyIndexes(TupleIndex srcIdx, TupleIndex... dstIndexes) {
        if ( dstIndexes.length == 0 )
            return;
        ProgressMonitor monitor = null;
        StringJoiner sj = new StringJoiner(",");
        for(TupleIndex idx : dstIndexes)
            sj.add(idx.getName());
        String label = sj.toString();
        Tools.copyIndex(srcIdx.all(), dstIndexes, label, monitor); 
    }
    
    // Copy one index.
    private static void copyIndexs(TupleIndex srcIdx, TupleIndex dstIdx) {
        ProgressMonitor monitor = null;
        TupleIndex[] a = { dstIdx } ;
        Tools.copyIndex(srcIdx.all(), a, dstIdx.getName(), monitor); 
    }

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

    private static TransBinaryDataFile ntDataFile(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        BinaryDataFile bdf = ntt.getData();
        TransBinaryDataFile tbdf = (TransBinaryDataFile)bdf;
        return tbdf;
    }
    
    private static BPlusTree ntBPTree(NodeTable nt) {
        NodeTableTRDF ntt = (NodeTableTRDF)(nt.baseNodeTable());
        Index idx = ntt.getIndex();
        return (BPlusTree)idx; 
    }

    private static BPlusTree idxBTree(TupleIndex idx) {
        TupleIndexRecord idxr = (TupleIndexRecord)idx;
        RangeIndex rIndex = idxr.getRangeIndex();
        BPlusTree bpt = (BPlusTree)rIndex;
        return bpt;
    }

}
