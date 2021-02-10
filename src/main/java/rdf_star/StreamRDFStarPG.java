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

package rdf_star;

import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.CacheSet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;

/**
 * Generate the target triple for each RDF-star triple term.
 */
public class StreamRDFStarPG extends StreamRDFBase {

    // TODO Remember recently generated to suppress duplicates.

    private final CacheSet<Triple> cacheTriples;
    private final CacheSet<Quad>   cacheQuads;

    private static final Node dftGraph = Quad.defaultGraphNodeGenerated;

    public StreamRDFStarPG(int cacheSize) {
        this.cacheTriples = CacheFactory.createCacheSet(cacheSize);
        this.cacheQuads = CacheFactory.createCacheSet(cacheSize);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void finish() {
        cacheTriples.clear();
        cacheQuads.clear();
        super.finish();
    }

    @Override
    public void triple(Triple triple) {
        Triple ts = Node_Triple.tripleOrNull(triple.getSubject());
        Triple to = Node_Triple.tripleOrNull(triple.getObject());
        gen(null, ts);
        gen(null, to);
        super.triple(triple);
    }

    private void gen(Node graph, Triple starTriple) {
        if ( starTriple == null )
            return ;
        //Node g = ( graph == null ) ? dftGraph : graph;

        // NB: XXX Recurse!
        if ( graph == null || Quad.isDefaultGraph(graph) ) {
            if ( ! cacheTriples.contains(starTriple) ) {
                triple(starTriple);
                cacheTriples.add(starTriple);
            }
        } else {
            Quad starQuad = Quad.create(graph, starTriple);
            if ( ! cacheQuads.contains(starQuad) ) {
                quad(starQuad);
                cacheQuads.add(starQuad);
            }
        }
    }

    @Override
    public void quad(Quad quad) {
        Triple ts = Node_Triple.tripleOrNull(quad.getSubject());
        Triple to = Node_Triple.tripleOrNull(quad.getObject());
        gen(quad.getGraph(), ts);
        gen(quad.getGraph(), to);
        super.quad(quad);
    }
}
