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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;

public class BufferingStoragePrefixMap implements StoragePrefixMap {

    private final StoragePrefixMap base;
    private Map<String, String> addedMap = new HashMap<>();
    private Map<String, String> deletedMap = new HashMap<>();

    BufferingStoragePrefixMap(StoragePrefixMap other) {
        this.base = other;
    }

    @Override
    public void put(String prefix, String uriStr) {}

    @Override
    public String get(String prefix) {
        return null;
    }

    @Override
    public void remove(String prefix) {}

    @Override
    public boolean containsPrefix(String prefix) {
        return false;
    }

    @Override
    public void clear() {}

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<PrefixEntry> iterator() {
        return null;
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return null;
    }
}