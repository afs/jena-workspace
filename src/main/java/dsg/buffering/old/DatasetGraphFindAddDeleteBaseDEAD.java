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

package dsg.buffering.old;

import java.util.Iterator;

import dsg.buffering.AbstractDatasetGraphAddDelete;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;

/** DatasetGraphWrapper, structured decomposition of methods. */

public class DatasetGraphFindAddDeleteBaseDEAD extends AbstractDatasetGraphAddDelete {

    public DatasetGraphFindAddDeleteBaseDEAD(DatasetGraph dsg) {
        super(dsg);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    @Override
    protected void actionAdd(Node g, Node s, Node p, Node o) {}

    @Override
    protected void actionDelete(Node g, Node s, Node p, Node o) {}

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
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

    //
    private final Transactional txn                     = TransactionalLock.createMRSW() ;
    protected final Transactional txn()                 { return get(); }
    @Override public void begin()                       { txn().begin(); }
    @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn().begin(mode); }
    @Override public void commit()                      { txn().commit(); }
    @Override public boolean promote(Promote mode)      { return txn().promote(mode); }
    @Override public void abort()                       { txn().abort(); }
    @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
    @Override public void end()                         { txn().end(); }
    @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
    @Override public TxnType transactionType()          { return txn().transactionType(); }
    @Override public boolean supportsTransactions()     { return get().supportsTransactions(); }
    @Override public boolean supportsTransactionAbort() { return get().supportsTransactionAbort(); }

    @Override
    public PrefixMap prefixes() {
        return null;
    }
}
