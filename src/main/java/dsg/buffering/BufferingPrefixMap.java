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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.jena.riot.system.PrefixEntry;
import org.apache.jena.riot.system.PrefixLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;

/**
 *
 */
public class BufferingPrefixMap extends PrefixMapBase {

    private Map<String, String> addedMappings = new HashMap<>();
    private Set<String> deletedMappings = new HashSet<>();
    private final PrefixMap base;

    public BufferingPrefixMap(PrefixMap prefixes) {
        this.base = prefixes;
    }

    @Override
    public Map<String, String> getMapping() {
        return null;
    }

    @Override
    public Map<String, String> getMappingCopy() {
        return null;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {}

    @Override
    public Stream<PrefixEntry> stream() {
        return null;
    }

    @Override
    public String get(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        if ( addedMappings.containsKey(prefix) ) {
            return addedMappings.get(prefix);
        }
        if ( deletedMappings.contains(prefix) )
            return null;
        return base.get(prefix);
    }

    @Override
    public void add(String prefix, String iriString) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        if ( base.containsPrefix(prefix) ) {
            String x = get(prefix);
            if ( Objects.equals(x, iriString) ) {
                // No change.
                return ;
            }
        }
        addedMappings.put(prefix, iriString);
        deletedMappings.remove(prefix);
    }

//    @Override
//    public void putAll(PrefixMap pmap) {}
//
//    @Override
//    public void putAll(PrefixMapping pmap) {}
//
//    @Override
//    public void putAll(Map<String, String> mapping) {}

    @Override
    public void delete(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        addedMappings.remove(prefix);
        if ( base.containsPrefix(prefix) )
            deletedMappings.add(prefix);
        else
            deletedMappings.remove(prefix);
    }

    @Override
    public void clear() {}

    @Override
    public boolean containsPrefix(String prefix) {
        return false;
    }

//    @Override
//    public String abbreviate(String uriStr) {
//        return null;
//    }
//
//    @Override
//    public Pair<String, String> abbrev(String uriStr) {
//        return null;
//    }
//
//    @Override
//    public String expand(String prefixedName) {
//        return null;
//    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        // XXX Wrong for added same prefix, different iri.
        return base.size() + addedMappings.size() - deletedMappings.size();
    }

    public void flush() {
        addedMappings.forEach(base::add);
        deletedMappings.forEach(base::delete);
        addedMappings.clear();
        deletedMappings.clear();

    }

}

