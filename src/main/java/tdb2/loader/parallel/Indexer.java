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

package tdb2.loader.parallel;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.jena.atlas.lib.ArrayUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.query.TxnType;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import tdb2.loader.base.LoaderOps;
import tdb2.loader.base.MonitorOutput;
import tdb2.loader.base.TimerX;

/**
 * Build index(es).
 * Provides a function {@link #index()} that should be called from another thread
 * to deliver chunks of tuples ({@code  List<Tuple<NodeId>>}).
 * Each chunk should be the same Tuyple length and this must correspond to the length of the {@link TupleIndex}s being loaded. 
 * <p>
 * This class creates one thread per {@link TupleIndex}.  
 */
class Indexer {

    private BlockingQueue<List<Tuple<NodeId>>>[] pipesTripleIndexers;
    private final int N;
    private final MonitorOutput output;
    private TupleIndex[] indexes;
    private final Semaphore termination = new Semaphore(0);
    
    @SuppressWarnings("unchecked")
    public Indexer(MonitorOutput output, TupleIndex... idxTriples) {
        pipesTripleIndexers = ArrayUtils.alloc(BlockingQueue.class, idxTriples.length);
        this.N = idxTriples.length;
        this.indexes = Arrays.copyOf(idxTriples, N); 
        this.output = output; 
            
        for ( int i = 0 ; i < N ; i++ ) {
            pipesTripleIndexers[i] = new ArrayBlockingQueue<List<Tuple<NodeId>>>(LoaderConst.QueueSizeTuples);
        }
    }
    
    private static long acquire(Semaphore semaphore, int numPermits) {
        return TimerX.time(()->{
            try { semaphore.acquire(numPermits); }
            catch (InterruptedException e) { e.printStackTrace(); }
        });
    }
    
    /** Return a function that delivers multiple {@code List<Tuple<NodeId>>>} to this indexer */
    public Destination<Tuple<NodeId>> index() {
        return this::index; 
    }
    
    private void index(List<Tuple<NodeId>> chunk) {
        for ( int i = 0 ; i < N ; i++ ) {
            try {
                pipesTripleIndexers[i].put(chunk);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Start the threads that will do the indexing */ 
    public void start() {
        for ( int i = 0 ; i < N ; i++ ) {
            TupleIndex idx = indexes[i];
            BlockingQueue<List<Tuple<NodeId>>> pipe = pipesTripleIndexers[i];
            new Thread(()->stageIndex(pipe, idx)).start();
        }
    }
    
    /** Wait for all the indexing threads to complete. */ 
    public void waitFinish() {
        System.out.println("Wait for "+N+" indexers");
        acquire(termination, N);
    }
    
    private void stageIndex(BlockingQueue<List<Tuple<NodeId>>> pipe, TupleIndex idx) {
        Journal journal = Journal.create(Location.mem());
        
        TransactionCoordinator coordinator = new TransactionCoordinator(journal);
        coordinator.add(LoaderOps.idxBTree(idx));
        coordinator.start();
        Transaction transaction = coordinator.begin(TxnType.WRITE);
        try {
            Destination<Tuple<NodeId>> loader = loadTuples(idx);
            for (;;) {
                List<Tuple<NodeId>> tuples = pipe.take();
                if ( tuples.isEmpty() )
                    break;
                loader.deliver(tuples);
            }
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.abort();
        }
        // Do not call - this would shutdown the TransactionCompoents for the temporary coordinator
        //coordinator.shutdown();
        output.print("Finish - index "+idx.getName());
        termination.release();
    }
    
    private static Destination<Tuple<NodeId>> loadTuples(TupleIndex index) {
        return (List<Tuple<NodeId>> tuples) -> {
            for(Tuple<NodeId> tuple : tuples)
                index.add(tuple);
        };
    }
}
