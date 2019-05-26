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
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;

public class BufferingDatasetGraph extends DatasetGraphWrapper {

    // Implementation : BufferingDSG_Q
    
    // DatasetGraphChanges +  support? + prefixes?
    
    private Set<Quad>   addedQuads     = new HashSet<>();
    private Set<Quad>   deletedQuads   = new HashSet<>();
    private Set<Triple> addedTriples   = new HashSet<>();
    private Set<Triple> deletedTriples = new HashSet<>();    
    
    public BufferingDatasetGraph(DatasetGraph dsg) {
        super(dsg);
    }
    
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
