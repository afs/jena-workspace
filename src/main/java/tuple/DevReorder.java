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

package tuple ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;

public class DevReorder {
        
    public static void main(String ... args) {
        TupleMap tmap = TupleMap.create("GSPO", "POSG") ;
        exec(tmap) ;
    }

    public static void exec(TupleMap tmap) {
        TupleMap tmap2 = tmap.reverse() ;
        
        Node g = SSE.parseNode("':g'") ;
        Node s = SSE.parseNode("':s'") ;
        Node p = SSE.parseNode("':p'") ;
        Node o = SSE.parseNode("':o'") ;
        
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        Quad quad = Quad.create(g, s, p, o) ;
        
        execApply(tmap, quad, (x1, x2, x3, x4)->System.out.printf("add(%s, %s, %s, %s)\n", x1, x2, x3, x4)) ;
        
        // Reorder as if in an index.
        Tuple<Node> indexedQuad = execFunction(tmap, quad, TupleFactory::create4) ;
        Quad quad2 = execFunction(tmap2, indexedQuad.get(0), indexedQuad.get(1), indexedQuad.get(2), indexedQuad.get(3), Quad::create) ; 
        // map in, unmap put.
        // Skew add.  Demo purposes index.
        execApply(tmap, quad, dsg::add) ;
        // Map in, find, unmap out.
        Iterator<Quad> iter = execFunction(tmap, g, s, null, null, dsg::find) ;
        // ideally, the unmap would be as nodes come out of the index. 
        iter = Iter.map(iter,  (q)-> execFunction(tmap2, q, Quad::create) ) ;
        Iter.print(iter);
    }
        
    interface Function3<X,Z>    { Z exec(X x1, X x2, X x3) ; }
    interface Function4<X,Z>    { Z exec(X x1, X x2, X x3, X x4) ; }

    interface Apply3<X>         { void exec(X x1, X x2, X x3) ; }
    interface Apply4<X>         { void exec(X x1, X x2, X x3, X x4) ; }
    
    // ---- Quad / Node 
    
    public static void execApply(TupleMap tupleMap, Quad quad, Apply4<Node> function) {
        Node n1 = quad.getGraph() ;
        Node n2 = quad.getSubject() ;
        Node n3 = quad.getPredicate() ;
        Node n4 = quad.getObject() ;
        execApply(tupleMap, n1, n2, n3, n4, function) ;
    }
    
    public static <Z> Z execFunction(TupleMap tupleMap, Quad quad, Function4<Node,Z> function) {
        Node n1 = quad.getGraph() ;
        Node n2 = quad.getSubject() ;
        Node n3 = quad.getPredicate() ;
        Node n4 = quad.getObject() ;
        return execFunction(tupleMap, n1, n2, n3, n4, function) ;
    }
            
    // ---- Machinery
    
    public static <X> void execApply(TupleMap tupleMap, X x1, X x2, X x3, X x4, Apply4<X> function) {
        X x1a = get(tupleMap.mapIdx(0), x1, x2, x3, x4) ;
        X x2a = get(tupleMap.mapIdx(1), x1, x2, x3, x4) ;
        X x3a = get(tupleMap.mapIdx(2), x1, x2, x3, x4) ;
        X x4a = get(tupleMap.mapIdx(3), x1, x2, x3, x4) ;
        function.exec(x1a, x2a, x3a, x4a) ;
    }
    
    public static <X,Z> Z execFunction(TupleMap tupleMap, X x1, X x2, X x3, X x4, Function4<X,Z> function) {
        X x1a = get(tupleMap.mapIdx(0), x1, x2, x3, x4) ;
        X x2a = get(tupleMap.mapIdx(1), x1, x2, x3, x4) ;
        X x3a = get(tupleMap.mapIdx(2), x1, x2, x3, x4) ;
        X x4a = get(tupleMap.mapIdx(3), x1, x2, x3, x4) ;
        return function.exec(x1a, x2a, x3a, x4a) ;
    }

    private static <X> X get(int i, X x1, X x2, X x3, X x4) {
        switch(i) {
            case 0: return x1 ;
            case 1: return x2 ;
            case 2: return x3 ;
            case 3: return x4 ;
            default: throw new IndexOutOfBoundsException("index = "+i) ;
        }
    }
}

