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

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.sse.SSE;

public class G3 {
    
    public static void main(String ...a) {
        G3 graph = new G3();
        Node s = SSE.parseNode(":s");
        Node p = SSE.parseNode(":p");
        Node o = SSE.parseNode(":o");
        Node x = SSE.parseNode(":x");
        
        graph.add(s, p, o);
        graph.add(s, p, x);
//        graph.delete(s, p, o);
//        graph.delete(s, p, x);
        
        Iterator<Triple> iter = graph.find(null, null, null);
        List<Triple> list = Iter.toList(iter);
        
        System.out.println("[");
        list.forEach(t->System.out.println(t));
        System.out.println("]");
   }
    
    private TripleIndex indexSPO = makeIndex("SPO");
    private TripleIndex indexPOS = makeIndex("POS");
    private TripleIndex indexOSP = makeIndex("OSP");
    
    private TripleIndex indexScan = indexSPO; 
    private TripleIndex[] indexes = {indexSPO, indexPOS, indexOSP};
   
    private static TripleIndex makeIndex(String order) {
        return new TripleIndex(order, TupleMap.create("SPO", order));
    }
    
    public Iterator<Triple> find(Node s, Node p, Node o) {
        TripleIndex index = chooseIndex1(s, p, o);
        return index.find(s, p, o);
    }
    
    private TripleIndex chooseIndex1(Node s, Node p, Node o) {
        // For triples, we can write out the decision tree for std indexing. 
        int w1 = indexSPO.weight(s, p, o);
        if ( w1 == 3 || w1 == 2 )
            // w1 == 2 because better is 3, i.e. concrete. 
            return indexSPO;
        
        // 2?
        // Still may be 1 and SPO 
        int w2 = indexPOS.weight(s, p, o);
        if ( w2 == 2 )
            return indexPOS;
        int w3 = indexOSP.weight(s, p, o);
        if ( w3 == 2 )
            return indexOSP;
        // 1?
        if ( w1 == 1 )
            return indexSPO;
        if ( w2 == 1 )
            return indexPOS;
        if ( w3 == 1 )
            return indexOSP;
        return indexScan;
    }

    private TripleIndex chooseIndex2(Node s, Node p, Node o) {
        // For triples, we can write out the decision tree for std indexing. 
        int w1 = indexSPO.weight(s, p, o);
        if ( w1 == 3 || w1 == 2 )
            // w1 == 2 because better is 3, i.e. concrete. 
            return indexSPO;
        
        // 2?
        // Still may be 1 and SPO 
        int w2 = indexPOS.weight(s, p, o);
        if ( w2 == 2 )
            return indexPOS;
        int w3 = indexOSP.weight(s, p, o);
        if ( w3 == 2 )
            return indexOSP;
        // 1?
        if ( w1 == 1 )
            return indexSPO;
        if ( w2 == 1 )
            return indexPOS;
        if ( w3 == 1 )
            return indexOSP;
        return indexSPO;
    }

    
    public void add(Node s, Node p, Node o) {
        for ( TripleIndex idx : indexes )
            idx.add(s, p, o);
    }
    
    public void delete(Node s, Node p, Node o) {
        for ( TripleIndex idx : indexes )
            idx.delete(s, p, o);
    }
}

//    
//    //    private Map<Node, Map<Node, Map<Node, Triple>>> indexSPO = new HashMap<>();
////    private Map<Node, Map<Node, Map<Node, Triple>>> indexPOS = new HashMap<>();
////    private Map<Node, Map<Node, Map<Node, Triple>>> indexOSP = new HashMap<>();
//    private Index3 indexSPO = new Index3();
//    private Index3 indexPOS = new Index3();
//    private Index3 indexOSP = new Index3();
//    
//    // Either return a tuple or pass in a maker but the maker needs to carry the
//    // inputs and if it is making objects", we might as well return a Tuple3.
//    private Map<Node, Index2> index = new HashMap<>();
//    
//    // Store as an sorted set?
//    
//    public void add(Node s, Node p, Node o) {
//        Triple triple = Triple.create(s, p, o); 
//        add(indexSPO, s, p, o, triple);
//        add(indexPOS, p, o, s, triple);
//        add(indexOSP, o, s, p, triple);
//    }
//
//    public void delete(Node s, Node p, Node o) {
//        delete(indexSPO, s, p, o);
//        delete(indexPOS, p, o, s);
//        delete(indexOSP, o, s, p);
//    }
//    
//    private void delete(Index3 index3, Node n1, Node n2, Node n3) {
//        Index2 index2 = index3.get(n1);
//        if ( index2 == null )
//            return;
//        Index1 index1 = index2.get(n2);
//        if ( index1 == null )
//            return;
//        index1.remove(n3);
//        //Clear up.
//        boolean e1 = index1.isEmpty();
//        if ( e1 )
//            index2.remove(n2);
//        boolean e2 = index2.isEmpty();
//        if ( e2 )
//            index3.remove(n1);
//    }
//
//    private void add(Index3 index3, Node n1, Node n2, Node n3, Triple t) {
//        Index2 index2 = ensure(index3, n1, ()->new Index2());
//        Index1 index1 = ensure(index2, n2, ()->new Index1());
//        index1.put(n3, t);
//    }
//
//    private <X> X ensure(Map<Node, X> index, Node n, Supplier<X> creator) {
//        return index.computeIfAbsent(n, (k)->creator.get());
//    }
//
//    
//    // Triple -> Tuple3 or a remapper.
//    private static Function< Index1, Iterator<Triple> > level2to1 = m -> m.values().iterator();
//    private static Function< Index2, Iterator<Index1> > level3to2 = m -> m.values().iterator();
//    private static Function< Index3, Iterator<Index2> > level4to3 = m -> m.values().iterator();
//    
//    private static Iterator<Triple> access(Index3 index3) {
//        Iterator<Index2> iter3 = index3.values().iterator();
//        Iterator<Index1> iter2 = Iter.flatMap(iter3, level3to2);
//        return  Iter.flatMap(iter2, level2to1);
//    }
//
//    private static Iterator<Triple> access(Index3 index3, Node x) {
//        Index2 m2 = index3.get(x);
//        Iterator<Index1> m1 = m2.values().iterator();
//        return  Iter.flatMap(m1, it->level2to1.apply(it));
//    }
//
//    private static Iterator<Triple> access(Index3 index3, Node x1, Node x2) {
//        Index2 m2 = index3.get(x1);
//        Index1 m1 = m2.get(x2);
//        if ( m1 == null )
//            return noTriples;
//        return m1.values().iterator();
//    }
//
//    private static Iterator<Triple> noTriples = Iter.nullIterator(); 
//    private static Iterator<Triple> access(Index3 index3, Node x1, Node x2, Node x3) {
//        Index2 m2 = index3.get(x1);
//        Index1 m1 = m2.get(x2);
//        Triple t = m1.get(x3);
//        if ( t == null )
//            return noTriples;
//        return Iter.singleton(t);
//    }
//    
//    public Iterator<Triple> find(Node s, Node p, Node o) {
//        // Hardwiring is (just about) practical for a graph.
//        // A dataset (and we want to have settable index choices), it isn't.
//        if ( concrete(s) ) {
//            if ( concrete(p) ) {
//                if ( concrete(o) )
//                    return access(indexSPO, s, p, o); 
//                else
//                    return access(indexSPO, s, p);
//            } else {
//                // p null
//                if ( concrete(o) )
//                    return access(indexOSP, o, s); 
//                else
//                    return access(indexSPO, s);
//            }
//        }
//        if ( concrete(p) ) {
//            // and s == null;
//            if ( concrete(o) )
//                return access(indexPOS, p, o);
//            else
//                return access(indexPOS, p);
//        }
//        // p == null s == null;
//        if ( concrete(o) )
//            return access(indexOSP, o);
//        else
//            return access(indexSPO);
//    }
//    
//    private static boolean concrete(Node x) { return x != null && x != Node.ANY; }
//    private static boolean wildcard(Node x) { return x == null || x == Node.ANY; }
//    
//    private static <X> Iterator<X> flatten(Map<Node, X> map) {
//        return map.values().iterator();
//    }
//}
