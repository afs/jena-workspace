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

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

/** Triples etc to queue of work. */ 

public class LoaderStreamRDF implements StreamRDF {
    // Prefixes and (base)
    
    public /*private*/ static int ChunkSize = 100_000 ; // 10_000 is about the same.

    private long countTriples;
    private long countQuads;

    private List<Triple> triples = null;
    private List<Quad> quads = null;
    
    private final BlockingQueue<List<Triple>> pipeTriples;
    private final BlockingQueue<List<Quad>> pipeQuads;

    LoaderStreamRDF(BlockingQueue<List<Triple>> pipeTriples,
                    BlockingQueue<List<Quad>> pipeQuads) {
        this.pipeTriples = pipeTriples;
        this.pipeQuads = pipeQuads;
    }
    
    // StreamRDF
    @Override
    public void start() {}

    @Override
    public void quad(Quad quad) {
        if ( quads == null )
            quads = allocChunkQuads();
        quads.add(quad);
        countQuads++;
        if ( quads.size() >= ChunkSize ) {
            dispatchQuads(quads);
            quads = null;
        }
    }

    @Override
    public void triple(Triple triple) {
        if ( triples == null )
            triples = allocChunkTriples();
        triples.add(triple);
        countTriples++;
        if ( triples.size() >= ChunkSize ) {
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
        try {
            pipeQuads.put(quads);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Triple>  allocChunkTriples() {
        return new ArrayList<>(ChunkSize); 
    } 

    private List<Quad>  allocChunkQuads() {
        return new ArrayList<>(ChunkSize); 
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    @Override
    public void finish() {}


}
