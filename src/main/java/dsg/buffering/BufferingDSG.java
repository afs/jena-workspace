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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads;
import org.apache.jena.sparql.core.Quad;

public class BufferingDSG extends DatasetGraphTriplesQuads {
    
    private DatasetGraph baseDSG;
    protected DatasetGraph get() { return baseDSG; }
    
    private Set<Quad>   addedQuads     = new HashSet<>();
    private Set<Quad>   deletedQuads   = new HashSet<>();
    
    //private BufferingGraph defaultGraph;
    
    
    // True  -> read-optimized.
    // False -> write-optimized
    private static final boolean UNIQUE = true; 
    
    
    private Set<Triple> addedTriples   = new HashSet<>();
    private Set<Triple> deletedTriples = new HashSet<>();
    
    public BufferingDSG(DatasetGraph dsg) {
        baseDSG = dsg;
    }
    
    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        if ( UNIQUE && base.getDefaultGraph().contains(triple) )
            return ;
        addedTriples.add(triple);
        deletedTriples.remove(triple);
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        if ( UNIQUE && base.contains(quad) )
            return ;
        addedQuads.add(quad);
        deletedQuads.remove(quad);
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        Triple triple = Triple.create(s,p,o);
        DatasetGraph base = get();
        if ( UNIQUE && ! base.getDefaultGraph().contains(triple) )
            return ;
        deletedTriples.add(triple);
        addedTriples.remove(triple);
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        Quad quad = Quad.create(g,s,p,o);
        DatasetGraph base = get();
        if ( UNIQUE && ! base.contains(quad) )
            return ;
        deletedQuads.add(quad);
        addedQuads.remove(quad);
    }
    
//    @Override
//    public boolean contains(Quad quad) { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
//
//    @Override
//    public boolean contains(Node g, Node s, Node p, Node o) {
//        return b;
//    }


    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
//        DatasetGraph base = get();
//        Iterator<Quad> extra = addedTriples.find(s,p,o).map(t->Quad.create(Quad.defaultGraphIRI,t));
//        Iter<Quad> iter =
//            Iter.iter(base.find(Quad.defaultGraphIRI, s, p, o))
//                .filter(q->! deletedQuads.contains(q))
//                .append(extra);
//        if ( ! UNIQUE )
//            iter = iter.distinct();
//        return iter;
        return null;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return null;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return null;
    }

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public void begin(TxnType type) {}

    @Override
    public void begin(ReadWrite readWrite) {}

    @Override
    public boolean promote(Promote mode) {
        return false;
    }

    @Override
    public void commit() {}

    @Override
    public void abort() {}

    @Override
    public void end() {}

    @Override
    public ReadWrite transactionMode() {
        return null;
    }

    @Override
    public TxnType transactionType() {
        return null;
    }

    @Override
    public boolean isInTransaction() {
        return false;
    }

}
