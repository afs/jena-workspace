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

package tupledev;

import org.apache.jena.graph.Node ;
import tuple.Tuple ;
import tuple.Tuple3 ;
import tuple.TupleFactory ;
import tuple.TupleMap ;

/** Operation on Triples */
public class TripleOps {
    
    public static Node get(TripleX triple, int i) {
        switch (i) {
            case 0: return triple.getSubject() ;
            case 1: return triple.getPredicate() ;
            case 2: return triple.getObject() ;
            default:
                throw new IndexOutOfBoundsException("index = "+i) ; 
        }
    }
    

    // Triple not a Tuple
    public static Tuple3<Node> map(TupleMap tupleMap, TripleX triple) {
        Node x1 = get(triple, tupleMap.mapSlotIdx(0)) ;
        Node x2 = get(triple, tupleMap.mapSlotIdx(1)) ;
        Node x3 = get(triple, tupleMap.mapSlotIdx(2)) ;
//        Node x1 = tupleMap.mapSlot(0, triple) ;
//        Node x2 = tupleMap.mapSlot(1, triple) ;
//        Node x3 = tupleMap.mapSlot(2, triple) ;
        return TupleFactory.create3(x1, x2, x3) ;
    }

    public static TripleX unmap(TupleMap tupleMap, Tuple3<Node> nt3) {
        Node x1 = tupleMap.unmapSlot(0, nt3) ;
        Node x2 = tupleMap.unmapSlot(1, nt3) ;
        Node x3 = tupleMap.unmapSlot(2, nt3) ;
        return new TripleX(x1, x2, x3) ;
    }

    // Index mapping.
    public static Tuple3<Node> map_(TupleMap tupleMap, TripleX triple) {
        Node x1 = triple.get(tupleMap.mapSlotIdx(0)) ;
        Node x2 = triple.get(tupleMap.mapSlotIdx(1)) ;
        Node x3 = triple.get(tupleMap.mapSlotIdx(2)) ;
        return TupleFactory.create3(x1, x2, x3) ;
    }

    public static TripleX unmap_(TupleMap tupleMap, Tuple3<Node> nt3) {
        Node x1 = nt3.get(tupleMap.unmapSlotIdx(0)) ;
        Node x2 = nt3.get(tupleMap.unmapSlotIdx(1)) ;
        Node x3 = nt3.get(tupleMap.unmapSlotIdx(2)) ;
        return new TripleX(x1, x2, x3) ;
    }
    
    // Functional style.
    public static Tuple3<Node> map__(TupleMap tupleMap, TripleX triple) {
        return doEverything(tupleMap::mapSlot, triple, TupleFactory::create3) ;
    }

    public static TripleX unmap__(TupleMap tupleMap, Tuple3<Node> nt3) {
        return doEverything(tupleMap::unmapSlot, nt3, TripleX::new) ;
    }
    
    private static <T> T doEverything(AtoB<Node> f, Tuple<Node> nt3, Maker3<Node, T> maker) {
        Node x1 = applyAndGet(f, nt3, 0) ;
        Node x2 = applyAndGet(f, nt3, 1) ;
        Node x3 = applyAndGet(f, nt3, 2) ;
        return maker.make(x1, x2, x3) ;
    }
    
    private interface Maker3<X, T> {
        T make(X x1, X x2, X x3) ;
    }
    
    private interface AtoB<X> {
        X access(int i, Tuple<X> tuple) ;
    }

    private static <X> X applyAndGet(AtoB<X> f, Tuple<X> nt3, int i) {
        return f.access(i, nt3) ; 
    }

}
