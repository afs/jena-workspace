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

package dsg.union;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads;
import org.apache.jena.sparql.core.Quad;

public abstract class DSG_Alt extends DatasetGraphTriplesQuads {

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    @Override
    public PrefixMap prefixes() {
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
    protected void addToDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {}

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {}

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

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

}
