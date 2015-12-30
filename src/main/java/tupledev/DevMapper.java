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

package tupledev ;

import java.util.Arrays ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.Tuple3 ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.sse.SSE ;

public class DevMapper {
    public static void main(String ... args) {
        Tuple<String> t = TupleFactory.tuple("1", "2", "3") ;
        String[] a = t.asArray(String.class) ;
        System.out.println(Arrays.asList(a)) ;
        
        Tuple<String> t2 = TupleOps.map(t, elt -> ":"+Integer.parseInt(elt)+":") ;
        
        //Tuple<String> t3 = t.map(elt -> ":"+Integer.parseInt(elt)+":") ;
        
        System.out.println(t2) ;
        System.exit(0) ;
        Node s = SSE.parseNode("'S'") ;
        Node p = SSE.parseNode("'P'") ;
        Node o = SSE.parseNode("'O'") ;
        Triple triple = new Triple(s,p,o) ;
        
        dwim(TupleMap.create("SPO", "POS") , triple) ;
        dwim(TupleMap.create("SPO", "OSP") , triple) ;
        dwim(TupleMap.create("SPO", "SPO") , triple) ;
    }
        
    public static void dwim(TupleMap tupleMap,  Triple triple) {
        System.out.println(tupleMap);
        {
            Tuple3<Node> nt3 = TripleOps.map(tupleMap, triple) ;
            Triple t2 = TripleOps.unmap(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        TripleX tx = new TripleX(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;  
        {
            Tuple3<Node> nt3 = TripleXOps.map_(tupleMap, tx) ;
            TripleX t2 = TripleXOps.unmap_(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        {
            Tuple3<Node> nt3 = TripleXOps.map__(tupleMap, tx) ;
            TripleX t2 = TripleXOps.unmap__(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        System.out.println();
        
    }
}
