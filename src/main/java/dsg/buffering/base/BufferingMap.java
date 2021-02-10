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

package dsg.buffering.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.NotImplemented;

public class BufferingMap<K, V> implements Map<K,V> {

    private Map<K, V> base;
    private Map<K, V> added = new HashMap<>();
    private Map<K, V> deleted = new HashMap<>();

    BufferingMap(Map<K,V> other) {
        this.base = other;
    }

    @Override
    public int size() {
        return base.size() + added.size() - deleted.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 ;
    }

    @Override
    public boolean containsKey(Object key) {
        if ( added.containsKey(key) )
            return true;
        if ( deleted.containsKey(key) )
            return false;
        return base.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new NotImplemented();
        //return base.containsValue(value);
    }

    @Override
    public V get(Object key) {
        V v = added.get(key);
        if ( v != null )
            return v;
        return base.get(key);
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        if ( deleted.containsKey(key) )
            return null;
        V vAdd = added.get(key);
        if ( vAdd != null )
            return added.remove(key);

        V v = base.get(key) ;
        if ( v == null )
            return null;
        deleted.put((K)key, v);
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((k,v)->put(k,v));
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    // PlanB : AbstractMap and entrySet()

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}

