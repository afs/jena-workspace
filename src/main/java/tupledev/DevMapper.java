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

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.sse.SSE ;
import tuple.Tuple3 ;
import tuple.TupleMap ;

public class DevMapper {
    public static void main(String ... a) {
        Node s = SSE.parseNode("'S'") ;
        Node p = SSE.parseNode("'P'") ;
        Node o = SSE.parseNode("'O'") ;
        TripleX triple = new TripleX(s,p,o) ;
        
        dwim(TupleMap.create("SPO", "POS") , triple) ;
        dwim(TupleMap.create("SPO", "OSP") , triple) ;
        dwim(TupleMap.create("SPO", "SPO") , triple) ;
    }
        
    public static void dwim(TupleMap tupleMap,  TripleX triple) {
        System.out.println(tupleMap);
        {
            Tuple3<Node> nt3 = TripleOps.map(tupleMap, triple) ;
            TripleX t2 = TripleOps.unmap(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        {
            Tuple3<Node> nt3 = TripleOps.map_(tupleMap, triple) ;
            TripleX t2 = TripleOps.unmap_(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        {
            Tuple3<Node> nt3 = TripleOps.map__(tupleMap, triple) ;
            TripleX t2 = TripleOps.unmap__(tupleMap, nt3) ;
            System.out.println(nt3+" -- "+t2) ;
        }
        System.out.println();
        
    }
}
