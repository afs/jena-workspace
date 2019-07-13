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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public class QuadIndex implements QuadIndexI {
    
    private static class Index4 extends HashMap<Node, Index3> {}
    private static class Index3 extends HashMap<Node, Index2> {}
    private static class Index2 extends HashMap<Node, Index1> {}
    private static class Index1 extends HashMap<Node, Quad> {}
    
    private TupleMap order;
    private String name;
    private Index4 index;
    
    public QuadIndex(String name, TupleMap order) {
        this.name = name ;
        this.order = order ;
        this.index = new Index4() ;
    }
    
    @Override
    public void add(Node g, Node s, Node p, Node o) {
        Tuple<Node> pattern = TupleFactory.create4(g,s,p,o);
        Tuple<Node> storage = order.map(pattern);
        add(index, storage.get(0), storage.get(1), storage.get(2), storage.get(3), Quad.create(g, s, p, o));
    }
    
    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        Tuple<Node> pattern = TupleFactory.create4(g,s,p,o);
        Tuple<Node> storage = order.map(pattern);
        delete(index, storage.get(0), storage.get(1), storage.get(2), storage.get(3));
    }
    
    @Override
    public Iterator<Quad> find(Node x1, Node x2, Node x3, Node x4) { 
        // Assumes correct order.
        if ( x4 != null )
            return access(index, x1, x2, x3, x4);
        if ( x3 != null )
            return access(index, x1, x2, x3);
        if ( x2 != null )
            return access(index, x1, x2);
        if ( x1 != null )
            return access(index, x1);
        return access(index);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public int weight(Node g, Node s, Node p, Node o) {
        //XXX Avoid an Object - hard code the "4"?
        Tuple<Node> pattern = TupleFactory.create4(g,s,p,o); 
        return weight(pattern, order);
    }

    private static <X> int weight(Tuple<X> pattern, TupleMap order) {
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

    private void add(Index4 index4, Node n1, Node n2, Node n3, Node n4, Quad quad) {
        Index3 index3 = ensure(index4, n1, ()->new Index3());
        Index2 index2 = ensure(index3, n2, ()->new Index2());
        Index1 index1 = ensure(index2, n3, ()->new Index1());
        index1.put(n4, quad);
    }

    private <X> X ensure(Map<Node, X> index, Node n, Supplier<X> creator) {
        return index.computeIfAbsent(n, (k)->creator.get());
    }
    
    private Quad delete(Index4 index4, Node n1, Node n2, Node n3, Node n4) {
        Index3 index3 = index4.get(n1);
        if ( index3 == null )
            return null;

        Index2 index2 = index3.get(n2);
        if ( index2 == null )
            return null;
        Index1 index1 = index2.get(n3);
        if ( index1 == null )
            return null;
        Quad x = index1.remove(n4);
        //Clear up.
        if ( index1.isEmpty() )
            index2.remove(n3);
        if ( index2.isEmpty() )
            index3.remove(n2);
        if ( index3.isEmpty() )
            index4.remove(n1);
        return x;
    }
    
    private static Iterator<Quad> noQuads = Iter.nullIterator();
    
    private static Function< Index1, Iterator<Quad> >   level2to1 = m -> m.values().iterator();
    private static Function< Index2, Iterator<Index1> > level3to2 = m -> m.values().iterator();
    private static Function< Index3, Iterator<Index2> > level4to3 = m -> m.values().iterator();

    private static Iterator<Quad> access(Index4 index4) {
        Iterator<Index3> iter3 = index4.values().iterator();
        if ( ! iter3.hasNext() )
            return noQuads;
        Iterator<Index2> iter2 = Iter.flatMap(iter3, level4to3);
        if ( ! iter2.hasNext() )
            return noQuads;
        Iterator<Index1> iter1 = Iter.flatMap(iter2, level3to2);
        if ( ! iter1.hasNext() )
            return noQuads;
        return Iter.flatMap(iter1, level2to1);
    }

    private static Iterator<Quad> access(Index4 index4, Node x) {
        Index3 m3 = index4.get(x);
        if ( m3 == null )
            return noQuads;
        Iterator<Index2> iter3 = m3.values().iterator();
        if ( ! iter3.hasNext() )
            return noQuads;
        Iterator<Index1> iter2 = Iter.flatMap(iter3, level3to2);
        if ( ! iter2.hasNext() )
            return noQuads;
        return Iter.flatMap(iter2, level2to1);
    }

    private static Iterator<Quad> access(Index4 index4, Node x1, Node x2) {
        Index3 m3 = index4.get(x1);
        if ( m3 == null )
            return noQuads;
        Index2 m2 = m3.get(x2);
        if ( m2 == null )
            return noQuads;
        Iterator<Index1> iter2 = m2.values().iterator();
        if ( ! iter2.hasNext() )
            return noQuads;
        return Iter.flatMap(iter2, level2to1);
    }

    private static Iterator<Quad> access(Index4 index4, Node x1, Node x2, Node x3) {
        Index3 m3 = index4.get(x1);
        if ( m3 == null )
            return noQuads;
        Index2 m2 = m3.get(x2);
        if ( m2 == null )
            return noQuads;
        Index1 m1 = m2.get(x3);
        if ( m1 == null )
            return noQuads;
        return m1.values().iterator();
    }
    
    private static Iterator<Quad> access(Index4 index4, Node x1, Node x2, Node x3, Node x4) {
        Index3 m3 = index4.get(x1);
        if ( m3 == null )
            return noQuads;
        Index2 m2 = m3.get(x2);
        if ( m2 == null )
            return noQuads;
        Index1 m1 = m2.get(x3);
        if ( m1 == null )
            return noQuads;
        Quad q = m1.get(x4);
        if ( q == null )
          return noQuads;
        return Iter.singleton(q);
    }
}
