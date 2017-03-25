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

package binary_protocol;

import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public interface JenaBinaryProtocol {
    // RDFChanges
    // - header
    // + RDFFind + getPrefixes.
    // + removeAll
    
    // StorageRDF remoted.
    // DatasetRDF remoted.
    
    // Patch - add - delete

    public void patch();

    public void add(Collection<Triple> triples);

    public Sink<Triple> add();

    // Delete
    public void delete(Collection<Triple> triples);

    public Sink<Triple> delete();

    public void removeAll(Node g, Node s, Node p, Node o);
    public void removeAll(Node s, Node p, Node o);

    // Remote StreamRDF
    // ??? StreamRDF with add/deleteStreamRD

    public Iterator<Quad> find(Node g, Node s, Node p, Node o);
    public Iterator<Triple> find(Node s, Node p, Node o);

}