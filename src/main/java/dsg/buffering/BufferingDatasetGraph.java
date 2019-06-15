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
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.Quad;


//See BufferingDSG_Q -- good enough for now.

// DatasetGraphTriplesQuads
// See BufferingDSG

public class BufferingDatasetGraph extends DatasetGraphBase {

    private DatasetGraph other;
    protected DatasetGraph get() { return other; }


    // Implementation : BufferingDSG_Q
    
    // DatasetGraphChanges +  support? + prefixes?
    
    private Set<Quad>   addedQuads     = new HashSet<>();
    private Set<Quad>   deletedQuads   = new HashSet<>();
    private Set<Triple> addedTriples   = new HashSet<>();
    private Set<Triple> deletedTriples = new HashSet<>();    
    
    public BufferingDatasetGraph(DatasetGraph dsg) {
        other = dsg;
    }
    
    @Override
    public void add(Quad quad) { throw new UnsupportedOperationException("DatasetGraph.add(Quad)") ; } 
    
    @Override
    public void delete(Quad quad) { throw new UnsupportedOperationException("DatasetGraph.delete(Quad)") ; }

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return null;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
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

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {}

    @Override
    public void removeGraph(Node graphName) {}
    
//    containsGraph(Node)
    
//    getDefaultGraph()
//    getUnionGraph()
//    getGraph(Node)
//    addGraph(Node, Graph)
//    removeGraph(Node)
//    setDefaultGraph(Graph)
//    ** getLock()
//    listGraphNodes()
    
//    add(Quad)
//    delete(Quad)
//    add(Node, Node, Node, Node)
//    delete(Node, Node, Node, Node)
//    deleteAny(Node, Node, Node, Node)
//    clear()
//    isEmpty()
    
//    find()
//    find(Quad)
//    find(Node, Node, Node, Node)
//    findNG(Node, Node, Node, Node)
//    contains(Quad)
//    contains(Node, Node, Node, Node)
    
//    getContext()
//    size()
//    close()
//    toString()
//    sync()
    
//    begin()
//    transactionMode()
//    transactionType()
//    begin(TxnType)
//    begin(ReadWrite)
//    promote()
//    promote(Promote)
//    commit()
//    abort()
//    end()
//    isInTransaction()
//    supportsTransactions()
//    supportsTransactionAbort()

}
