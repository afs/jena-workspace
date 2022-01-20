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

import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** StorageRDF for union default graph */
public class StorageRDF_Union implements StorageRDF {

    // Already triple/quad split.

    @Override
    public void add(Node s, Node p, Node o) {}

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        // XXX
        if ( Quad.isUnionGraph(g) ) {}
    }

    @Override
    public void delete(Node s, Node p, Node o) {}

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        // XXX
        if ( Quad.isUnionGraph(g) ) {}
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        // XXX
        if ( Quad.isUnionGraph(g) ) {}
        return null;
    }

    @Override
    public Iterator<Triple> find(Node s, Node p, Node o) {
        return null;
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return false;
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        // XXX
        if ( Quad.isUnionGraph(g) ) {}
        return false;
    }

}
