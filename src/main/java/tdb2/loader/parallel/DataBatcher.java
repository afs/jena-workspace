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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import tdb2.loader.BulkStreamRDF;
import tdb2.loader.base.MonitorOutput;

/**
 * A {@link StreamRDF} that groups triples and quads and dispatches them in batches. This
 * class is a {@link StreamRDF} and runs on the calling thread; it does not create any
 * threads.
 */   
public class DataBatcher implements StreamRDF, BulkStreamRDF {
    
    private List<Triple> triples = null;
    private List<Quad> quads = null;
    private final Destination<Triple> destTriples;
    private final Destination<Quad> destQuads;
    private long countTriples;
    private long countQuads;
    private final MonitorOutput output;
    private  BiConsumer<String, String> prefixHandler;
    
    public DataBatcher(Destination<Triple> pipeTriples,
                       Destination<Quad> pipeQuads,
                       MonitorOutput output,
                       BiConsumer<String, String> prefixHandler) {
        this(pipeTriples, pipeQuads, LoaderParallel.DataTickPoint, LoaderParallel.DataSuperTick, output, prefixHandler);
    }
    
    public DataBatcher(Destination<Triple> pipeTriples,
                       Destination<Quad> pipeQuads,
                       int tickPoint, int superTick,
                       MonitorOutput output,
                       BiConsumer<String, String> prefixHandler) {
        this.destTriples = pipeTriples;
        this.destQuads = pipeQuads;
        this.output = output;
        this.prefixHandler = prefixHandler;
    }
    
    @Override
    public void startBulk() {}

    @Override
    public void finishBulk() {
        if ( ! isEmpty(triples) ) {
            dispatchTriples(triples);
            triples = null;
        }
        if ( ! isEmpty(quads) ) {
            dispatchQuads(quads);
            quads = null;
        }
        
        dispatchTriples(null);
        dispatchQuads(null);
    }
    
    private <X> boolean isEmpty(List<X> list) {
        return list == null || list.isEmpty() ;
    }
    
    @Override
    public void start() { }

    @Override
    public void finish() { }

    public long countTriples() { return countTriples; }
    public long countQuads() { return countQuads; }
    
    @Override
    public void triple(Triple triple) {
        if ( triples == null )
            triples = allocChunkTriples();
        triples.add(triple);
        countTriples++;
        maybeDispatch3();
    }

    @Override
    public void quad(Quad quad) {
        if ( quads == null )
            quads = allocChunkQuads();
        quads.add(quad);
        countQuads++;
        maybeDispatch4();
    }

    // Single function output.
//    private void maybeDispatch() {
//        long x = 0;
//        if ( triples != null )
//            x += triples.size();
//        if ( quads != null )
//            x += quads.size();
//        if ( x <= LoaderConst.ChunkSize )
//            return ;
//        if ( x <= LoaderConst.ChunkSize )
//            return;
//        
//        DataBlock block = new DataBlock(triples, quads) ;
//        // Dispatch.
//        dispatch(block);
//        triples = null;
//        quads = null;
//    }
    
    private void maybeDispatch3() {
        if ( triples.size() >= LoaderConst.ChunkSize ) {
            dispatchTriples(triples);
            triples = null;
        }
    }
    
    private void maybeDispatch4() {
        if ( quads.size() >= LoaderConst.ChunkSize ) {
            dispatchQuads(quads);
            quads = null;
        }
    }

    private void dispatchTriples(List<Triple> triples) {
        destTriples.deliver(triples);
    }

    private void dispatchQuads(List<Quad> quads) {
        destQuads.deliver(quads);
    }

    @Override
    public void base(String base) {}

//    // Clean up coordinator setup.
//    NodeTupleTable p = ((DatasetPrefixesTDB)prefixes).getNodeTupleTable();
//    coordinator.add(LoaderOps.ntDataFile(p.getNodeTable()));
//    coordinator.add(LoaderOps.ntBPTree(p.getNodeTable()));
//    NodeTupleTable p = ((DatasetPrefixesTDB)prefixes).getNodeTupleTable();
//    coordinator.add(LoaderOps.ntDataFile(p.getNodeTable()));
//    coordinator.add(LoaderOps.ntBPTree(p.getNodeTable()));
//    for ( TupleIndex pIdx : p.getTupleTable().getIndexes() ) {
//        coordinator.add(LoaderOps.idxBTree(pIdx));
//    }

    @Override
    public void prefix(String prefix, String iri) {
        if ( prefixHandler != null )
            prefixHandler.accept(prefix, iri);
    }

    private List<Triple>  allocChunkTriples() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    } 

    private List<Quad>  allocChunkQuads() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    }

}
