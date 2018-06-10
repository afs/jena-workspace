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

package tuple;

import org.apache.jena.atlas.lib.tuple.Tuple3 ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;

/** Operation on Triples */
public class TripleOps {
    
    /** This code creates the the "triple as SPO" view */ 
    public static Node getTripleSlot(Triple triple, int i) {
        switch (i) {
            case 0: return triple.getSubject() ;
            case 1: return triple.getPredicate() ;
            case 2: return triple.getObject() ;
            default:
                throw new IndexOutOfBoundsException("index = "+i) ; 
        }
    }

    public static Tuple3<Node> map(TupleMap tupleMap, Triple triple) {
        // Triple not a Tuple3<Node>
        Node x1 = getTripleSlot(triple, tupleMap.mapIdx(0)) ;
        Node x2 = getTripleSlot(triple, tupleMap.mapIdx(1)) ;
        Node x3 = getTripleSlot(triple, tupleMap.mapIdx(2)) ;
        return TupleFactory.create3(x1, x2, x3) ;
    }

    public static Triple unmap(TupleMap tupleMap, Tuple3<Node> nt3) {
        Node x1 = tupleMap.unmapSlot(0, nt3) ;
        Node x2 = tupleMap.unmapSlot(1, nt3) ;
        Node x3 = tupleMap.unmapSlot(2, nt3) ;
        return Triple.create(x1, x2, x3) ;
    }
}
