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

package dsg.indexing;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.sse.SSE;

public class Indexer {
    // SPO, OPS?, POS and no OS_
    // SPO, POS, OSP
    
    
    
    public static void main(String ...a) {
        Node g = SSE.parseNode(":g");
        Node s = SSE.parseNode(":s");
        Node p = SSE.parseNode(":p");
        Node o = SSE.parseNode(":o");
        
        TupleMap primary = TupleMap.create("GSPO", "GSPO");
        TupleMap order = TupleMap.create("GSPO", "GPOS");

        devWeight(TupleFactory.create4("G", "S", "P", "O"), primary, order);
        devWeight(TupleFactory.create4("G", null, "P", "O"), primary, order);
        devWeight(TupleFactory.create4("G", null, "P", null), primary, order);
        devWeight(TupleFactory.create4("G", "S", null, "O"), primary, order);
        devWeight(TupleFactory.create4("G", "S", null, null), primary, order);
        devWeight(TupleFactory.create4(null, "S", "P", "O"), primary, order);
        
        
    }
    
    private static void devWeight(Tuple<String> pattern, TupleMap primary, TupleMap order) {
        int w = weight(pattern, primary, order);
        System.out.printf("%-20s  ->  %s  =>  %d\n", pattern, order.getLabel(), w); 
    }

    private static <X> int weight(Tuple<X> pattern, TupleMap primary, TupleMap order) {
        int N = pattern.len();
        int count = 0 ;
        for ( int i = 0 ; i < N ; i++ ) {
            // Get the i'th mapped slot : The natural to index mapping.
            int j = order.getSlotIdx(i);
            X x = pattern.get(j);
            if ( x == null )
                return count ;
            count++;
        }
        return count;
    }


    public static class Index {
        TupleMap order;
        Index(TupleMap order) {
            this.order = order ;
        }
        
        int weight(Tuple<?>thing) {
            return -1;
        }
        
    }
}

/*
/ * Find all matching tuples - a slot of NodeId.NodeIdAny means match any * 
public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) {
    if ( tupleLen != pattern.len() )
        throw new TDBException(format("Mismatch: finding tuple of length %d in a table of tuples of length %d", pattern.len(), tupleLen));

    int numSlots = 0;
    // Canonical form.
    for ( int i = 0; i < tupleLen ; i++ ) {
        NodeId x = pattern.get(i);
        if ( ! NodeId.isAny(x) )
            numSlots++;
        if ( NodeId.isDoesNotExist(x))
            return Iter.nullIterator();
    }

    if ( numSlots == 0 )
        return scanAllIndex.all();

    int indexNumSlots = 0;
    TupleIndex index = null;
    for ( TupleIndex idx : indexes ) {
        if ( idx != null ) {
            int w = idx.weight( pattern );
            if ( w > indexNumSlots ) {
                indexNumSlots = w;
                index = idx;
            }
        }
    }

    if ( index == null )
        // No index at all.  Scan.
        index = indexes[0];
    return index.find(pattern);
}
*/