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

package dsg.buffering.graph;

import java.util.HashSet;
import java.util.Set;

import dsg.buffering.BufferingCtl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

/** A prefixMap that buffers changes until {@link #flush()} is called. */
public class BufferingPrefixMapping extends PrefixMappingImpl implements BufferingCtl {
    private static final boolean CHECK = false;
    // Depends on the fact that PrefixMappingImpl maps everything get/set/remove.
    private final PrefixMapping added = new PrefixMappingImpl();
    private final Set<String> deleted = new HashSet<>();
    private final PrefixMapping other;

    public BufferingPrefixMapping(PrefixMapping other) {
        this.other = other;
    }

    @Override
    public void flush() {
        deleted.forEach(prefix->other.removeNsPrefix(prefix));
        // Copy :-(
        other.setNsPrefixes(added);

        deleted.clear();
        added.clearNsPrefixMap();
    }

    public PrefixMapping getAdded() {
        return added;
    }

    public Set<String> getDeleted() {
        return deleted;
    }

    @Override
    protected void set(String prefix, String uri) {
        if ( CHECK ) {
            String u = other.getNsPrefixURI(prefix);
            if ( uri.equals(u) )
                return;
        }
        added.setNsPrefix(prefix, uri);
    }

    @Override
    protected String get(String prefix) {
        if ( deleted.contains(prefix) )
            return null;
        String uri = added.getNsPrefixURI(prefix);
        if ( uri != null )
            return uri;
        return other.getNsPrefixURI(prefix);
    }

    @Override
    protected void remove(String prefix) {
        deleted.add(prefix);
        added.removeNsPrefix(prefix);
    }

    @Override
    public int numPrefixes() {
        return added.numPrefixes() - deleted.size() + other.numPrefixes();
    }

    @Override
    public boolean hasNoMappings() {
        return numPrefixes() == 0 ;
    }
}
