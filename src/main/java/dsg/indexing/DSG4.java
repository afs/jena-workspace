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
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;

public class DSG4 {
    
    public static void main(String ...a) {
        DSG4 dsg = new DSG4();
        Node g = SSE.parseNode(":g");
        Node s = SSE.parseNode(":s");
        Node p = SSE.parseNode(":p");
        Node o = SSE.parseNode(":o");
        Node x = SSE.parseNode(":x");

        dsg.add(g, s, p, o);
        dsg.add(g, s, p, x);

        Iterator<Quad> iter = dsg.find(g, null, null, null);
        List<Quad> list = Iter.toList(iter);

        System.out.println("[");
        list.forEach(t->System.out.println(t));
        System.out.println("]");
    }

    private QuadIndex indexGSPO = makeIndex("GSPO");
    private QuadIndex indexGPOS = makeIndex("GPOS");
    private QuadIndex indexGOSP = makeIndex("GOSP");
    private QuadIndex indexSPOG = makeIndex("SPOG");
    private QuadIndex indexPOSG = makeIndex("POSG");
    private QuadIndex indexOSPG = makeIndex("OSPG");
    
    private QuadIndex indexScan = indexSPOG; 
    private final QuadIndex[] indexes = {indexGSPO, indexGPOS, indexGOSP,
                                         indexSPOG, indexPOSG, indexOSPG};
   
    private static QuadIndex makeIndex(String order) {
        return new QuadIndex(order, TupleMap.create("GSPO", order));
    }
    
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        QuadIndex index = chooseIndex(g, s, p, o);
        return index.find(g, s, p, o);
    }
    
    private QuadIndex chooseIndex(Node g, Node s, Node p, Node o) {
        int weight = -1;
        QuadIndex idx = indexScan;
        for ( int i = 0 ; i < indexes.length ; i++ ) {
            int w = indexes[i].weight(g, s, p, o);
//            if ( w == 4 )
//                return indexes[i]; 
            if ( w > weight ) {
                weight = w ;
                idx = indexes[i];
            }
        }
        return idx;
    }

    
    public void add(Node g, Node s, Node p, Node o) {
        for ( QuadIndex idx : indexes )
            idx.add(g, s, p, o);
    }
    
    public void delete(Node g, Node s, Node p, Node o) {
        for ( QuadIndex idx : indexes )
            idx.delete(g, s, p, o);
    }
}
