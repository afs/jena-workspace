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

package fuseki;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Extend another {@link Context}.
 *
 * The extended context is assumed to be unchanging
 * (for tracking removed properties)
 */

public class Context2 extends Context {
    // Needs Tests.
    private final Context left = new Context();
    private final Context right;
    private final Set<Symbol> hidden = new HashSet<>();

    public Context2(Context context) {
        right = context;
    }

    @Override
    protected Object mapGet(Symbol property) {
        Object obj = left.get(property);
        if ( obj != null )
            return obj;
        obj = right.get(property);
        if ( obj == null )
            return null;
        if ( hidden.contains(property) )
            return null;
        return obj;
    }

    @Override
    protected void mapPut(Symbol property, Object value) {
        if ( readonly )
            throw new ARQException("Context is readonly") ;
        if ( property == null )
            throw new ARQException("Context key is null") ;
        if ( value == null ) {
            mapRemove(property) ;
            return ;
        }
        hidden.remove(property);
        left.put(property, value);
    }

    @Override
    protected void mapRemove(Symbol property) {
        left.remove(property);
        if ( right.isDefined(property))
            hidden.add(property);
    }

    @Override
    protected boolean mapContains(Symbol property) {
        return mapGet(property) != null;
    }

    @Override
    protected Set<Symbol> mapKeySet() {
        Set<Symbol> x = new HashSet<>();
        x.addAll(left.keys());
        x.addAll(right.keys());
        x.removeAll(hidden);
        return x;
    }

    @Override
    protected int mapSize() {
        // Assumes right is read-only.
        return left.size()+right.size()-hidden.size();
    }

    @Override
    protected void mapForEach(BiConsumer<Symbol, Object> action) {
        mapKeySet().forEach(k->{
            Object v = get(k);
            if ( v != null )
                action.accept(k, v);
        });
    }
}