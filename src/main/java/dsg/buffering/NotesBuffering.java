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

import java.util.Iterator;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class NotesBuffering {

    // BufferingDatasetGraph vs BufferingDatasetGraphQuads. Is it worth it? just Quads version?

    // **** Plan:

    // BufferingDatasetGraph
    // BufferingGraph
    // BufferingStoragePrefixMap
    // BufferingPrefixes


    // ---

    // What about BufferingGraph and a DSG of BufferingGraphs?
    // BufferingPrefixes.

    // BufferingGraphView needed? Prefixes?

    //test for a graph.

    // StorageRDF, StoragePrefixes ??

    /* DatasetGraphTriplesQuads
     *
     */

    // Yes - having the default graph kept as triples is useful.

    // XXX GraphView to take a PrefixMapping object.


    // G S P O
    // Algorithmically determine index.
    // Ask each index to weight a choice, choose best.


    // BufferingDatasetGraph/_Q for now (pure quads) tests test working.

    // StorageRDF?
    // StoragePrefixes

    static class BufferingStorage implements StorageRDF {

        @Override
        public void add(Triple triple) {}

        @Override
        public void add(Node s, Node p, Node o) {}

        @Override
        public void add(Quad quad) {}

        @Override
        public void add(Node g, Node s, Node p, Node o) {}

        @Override
        public void delete(Triple triple) {}

        @Override
        public void delete(Node s, Node p, Node o) {}

        @Override
        public void delete(Quad quad) {}

        @Override
        public void delete(Node g, Node s, Node p, Node o) {}


        @Override
        public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
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
            return false;
        }
    }

    static class BufferingPrefixes implements StoragePrefixes {

        @Override
        public String get(Node graphNode, String prefix) {
            return null;
        }

        @Override
        public Iterator<PrefixEntry> get(Node graphNode) {
            return null;
        }

        @Override
        public Iterator<Node> listGraphNodes() {
            return null;
        }

        @Override
        public void add(Node graphNode, String prefix, String iriStr) {}

        @Override
        public void delete(Node graphNode, String prefix) {}

        @Override
        public void deleteAll(Node graphNode) {}

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }}
}
