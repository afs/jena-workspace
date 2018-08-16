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

package dataset;

import java.util.function.Predicate;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.tdb2.store.NodeId;

/** With a one slot memo-ized result */ 
abstract class AbstractSecurityFilter implements Predicate<Tuple<NodeId>> {
    private long previousGraph = Long.MIN_VALUE;
    private final boolean allowDefaultGraph;
    
    public AbstractSecurityFilter(boolean allowDefaultGraph) {
        this.allowDefaultGraph = allowDefaultGraph;
    }
    
    @Override
    public boolean test(Tuple<NodeId> t) {
        if ( t.len() == 3 ) {
            // Default graph.
            // No need to clear previousGraph - it remains valid.
            return allowDefaultGraph; 
        }
        NodeId g = t.get(0);
        long id = g.getPtrLocation();
        // Check the cache.
        if ( previousGraph != -1 && id == previousGraph ) {
            //System.out.println("Previous");
            return true;
        }

        boolean b = perGraphTest(g);
        if ( b ) {
            previousGraph = id;
            //System.out.println("New");
            return true;
        }
        //System.out.println("Miss");
        return false;
    }

    // The per graph test.
    protected abstract boolean perGraphTest(NodeId g);
}