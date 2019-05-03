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

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.Tuple3 ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.sse.SSE ;

public class DevMapper {
 
        
    public static void main(String ... args) {
        
        TupleMap tmap = TupleMap.create("SPO", "POS") ;
        
        
        TupleMap tmap2 = antiTupleMap(tmap) ;
        
        Tuple<String> t = TupleFactory.tuple("1", "2", "3") ;
        Tuple<String> t2 = tmap.map(t) ;
        Tuple<String> t3 = tmap2.map(t2) ;
        Triple triple = SSE.parseTriple("(':s' ':p' ':o')") ;
        execApply(tmap, triple, (x1, x2, x3)->System.out.printf("add(%s, %s, %s)\n", x1, x2, x3)) ;
        Tuple<Node> tuple3 = execFunction(tmap, triple, TupleFactory::create3) ;
        Triple triple2 = execFunction(tmap, tuple3.get(0), tuple3.get(1), tuple3.get(2), Triple::create) ; 
        //        Tuple<String> t = TupleFactory.tuple("1", "2", "3") ;
//        Tuple<String> t2 = TupleOps.map(t, elt -> ":"+Integer.parseInt(elt)+":") ;
//        
//        //Tuple<String> t3 = t.map(elt -> ":"+Integer.parseInt(elt)+":") ;
//        
//        //        System.exit(0) ;
//        Node s = SSE.parseNode("'S'") ;
//        Node p = SSE.parseNode("'P'") ;
//        Node o = SSE.parseNode("'O'") ;
//        Triple triple = new Triple(s,p,o) ;
//        
//        dwim(TupleMap.create("SPO", "POS") , triple) ;
//        dwim(TupleMap.create("SPO", "OSP") , triple) ;
//        dwim(TupleMap.create("SPO", "SPO") , triple) ;
    }
        

    interface Apply3<X> {
        void exec(X x1, X x2, X x3) ;
    }
    
    interface Function3<X,Z> {
        Z exec(X x1, X x2, X x3) ;
    }

    static TupleMap antiTupleMap(TupleMap tmap) {
        Tuple<String> t = TupleFactory.tuple("1", "2", "3") ;
        Tuple<String> t2 = tmap.map(t) ;
        TupleMap tmap2 = TupleMap.create("Anti", t2.asList(), t.asList()) ; 
        return tmap2 ; 
    }
    
    public static void execApply(TupleMap tupleMap, Triple triple, Apply3<Node> function) {
        Node n1 = triple.getSubject() ;
        Node n2 = triple.getPredicate() ;
        Node n3 = triple.getObject() ;
        execApply(tupleMap, n1, n2, n3, function) ;
    }
    
    public static <Z> Z execFunction(TupleMap tupleMap, Triple triple, Function3<Node,Z> function) {
        Node n1 = triple.getSubject() ;
        Node n2 = triple.getPredicate() ;
        Node n3 = triple.getObject() ;
        return execFunction(tupleMap, n1, n2, n3, function) ;
    }
            
    public static <X> void execApply(TupleMap tupleMap, X x1, X x2, X x3, Apply3<X> function) {
        X x1a = get(tupleMap.mapIdx(0), x1, x2, x3) ;
        X x2a = get(tupleMap.mapIdx(1), x1, x2, x3) ;
        X x3a = get(tupleMap.mapIdx(2), x1, x2, x3) ;
        function.exec(x1a, x2a, x3a) ;
    }
    
    public static <X,Z> Z execFunction(TupleMap tupleMap, X x1, X x2, X x3, Function3<X,Z> function) {
        X x1a = get(tupleMap.mapIdx(0), x1, x2, x3) ;
        X x2a = get(tupleMap.mapIdx(1), x1, x2, x3) ;
        X x3a = get(tupleMap.mapIdx(2), x1, x2, x3) ;
        return function.exec(x1a, x2a, x3a) ;
    }

    private static <X> X get(int i, X x1, X x2, X x3) {
        if ( i == 0 ) return x1 ;
        if ( i == 1 ) return x2 ;
        if ( i == 2 ) return x3 ;
        throw new IndexOutOfBoundsException("index = "+i) ; 
    }

    public static void dwim(TupleMap tupleMap,  Triple triple) {
        {
            Tuple3<Node> nt3 = TripleOps.map(tupleMap, triple) ;
            Triple t2 = TripleOps.unmap(tupleMap, nt3) ;
            }
        TripleX tx = new TripleX(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;  
        {
            Tuple3<Node> nt3 = TripleXOps.map_(tupleMap, tx) ;
            TripleX t2 = TripleXOps.unmap_(tupleMap, nt3) ;
            }
        {
            Tuple3<Node> nt3 = TripleXOps.map__(tupleMap, tx) ;
            TripleX t2 = TripleXOps.unmap__(tupleMap, nt3) ;
            }
        }
}

