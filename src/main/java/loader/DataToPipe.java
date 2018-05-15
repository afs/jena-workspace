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
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import tdb2.MonitorOutput;
import tdb2.loader.base.ProgressMonitor2;

/** A {@link StreamRDF} that groups it's inputs */ 
public class DataToPipe implements BulkStreamRDF {
    
    private List<Triple> triples = null;
    private List<Quad> quads = null;
    private final BlockingQueue<List<Triple>> pipeTriples;
    private final BlockingQueue<List<Quad>> pipeQuads;
    private long countTriples;
    private long countQuads;
    private final MonitorOutput output;
    private ProgressMonitor2 progress;
    
    public DataToPipe(BlockingQueue<List<Triple>> pipeTriples,
                      BlockingQueue<List<Quad>> pipeQuads,
                      MonitorOutput output) {
        this.pipeTriples = pipeTriples;
        this.pipeQuads = pipeQuads;
        this.output = output;
        this.progress = new ProgressMonitor2("Data", LoaderConst.DataTickPoint, LoaderConst.DataSuperTick, output);
    }
    
    @Override
    public void startBulk() {
        progress.startMessage();
        progress.start();
    }

    @Override
    public void finishBulk() {
        if ( triples != null && ! triples.isEmpty() ) {
            dispatchTriples(triples);
            triples = null;
        }
        dispatchTriples(LoaderConst.END_TRIPLES);
        progress.finish();
        progress.finishMessage("triples");
    }
    
    @Override
    public void start() {}

    @Override
    public void finish() {}

    public long getCountTriples() { return countTriples; }
    public long getCountQuads() { return countQuads; }
    
//    private static long acquire(Semaphore semaphore, int numPermits) {
//        return TimerX.time(()->{
//            try { semaphore.acquire(numPermits); }
//            catch (InterruptedException e) { e.printStackTrace(); }
//        });
//    }
    
    @Override
    public void quad(Quad quad) {
        if ( quads == null )
            quads = allocChunkQuads();
        quads.add(quad);
        countQuads++;
        if ( quads.size() >= LoaderConst.ChunkSize ) {
            dispatchQuads(quads);
            quads = null;
        }
    }

    @Override
    public void triple(Triple triple) {
        if ( triples == null )
            triples = allocChunkTriples();
        progress.tick();
        triples.add(triple);
        countTriples++;
        if ( triples.size() >= LoaderConst.ChunkSize ) {
            dispatchTriples(triples);
            triples = null;
        }
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

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    private List<Triple>  allocChunkTriples() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    } 

    private List<Quad>  allocChunkQuads() {
        return new ArrayList<>(LoaderConst.ChunkSize); 
    }

}
