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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.graph.Node;

public class BufferingStoragePrefixes implements StoragePrefixes {

    private final StoragePrefixes base;
    // Map of maps.
    private Map<Node, Map<String, String>> addedMap = new ConcurrentHashMap<>();
    // Deleted by prefix.
    private MultiValuedMap<Node, String> deletedMap = new HashSetValuedHashMap<>();

    BufferingStoragePrefixes(StoragePrefixes other) {
        this.base = other;
    }

    @Override
    public String get(Node graphNode, String prefix) {
        Map<String, String> x = addedMap.get(graphNode);
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
    public void add(Node graphNode, String prefix, String iriStr) {

    }


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
    }
}