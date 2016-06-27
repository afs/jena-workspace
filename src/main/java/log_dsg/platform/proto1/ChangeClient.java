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

package log_dsg.platform.proto1;

import java.io.IOException ;
import java.net.Socket ;

import log_dsg.DSGMonitor ;
import log_dsg.StreamChanges ;
import log_dsg.StreamChangesWriter ;
import log_dsg.Txn ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ChangeClient {
    static { LogCtl.setLog4j(); }
    
    private static Logger log = LoggerFactory.getLogger("Client") ; 
    
    public static void main(String ...args) {
        int port = 7707 ;
        Dataset ds = DatasetFactory.createTxnMem() ;
        
        // Blank nodes as <_:>
        
        Quad q1 = SSE.parseQuad("(_ :s1 :p1 'One')") ;
        Quad q2 = SSE.parseQuad("(_ :s1 :p1 'Two')") ;
        Quad q3 = SSE.parseQuad("(_ :s2 :p2 'Zero')") ;
        
        Node s1 = SSE.parseNode(":x1") ;
        Node p1 = SSE.parseNode(":s1") ;
        Node p2 = SSE.parseNode(":p2") ;
        Node obj = SSE.parseNode("123") ;
        Node b1 = NodeFactory.createBlankNode() ;
        //System.out.println("Blank node: "+b1.getBlankNodeLabel()) ;
        
        try ( Socket socket = new Socket("localhost", port) ) {
            StreamChanges changes = new StreamChangesWriter(socket.getOutputStream()) ;
            DatasetGraph dsg = new DSGMonitor(ds.asDatasetGraph(), changes) ;
            
            System.out.println("Ready 1");
            System.in.read() ;
            
            Txn.execWrite(dsg, ()-> {
                dsg.add(q1) ;
                dsg.add(q2) ;
                Graph g = dsg.getDefaultGraph() ;
                g.getPrefixMapping().setNsPrefix("", "http://example/") ;
                g.add(Triple.create(s1, p1, b1)) ;
            }) ;
            
            System.out.println("Ready 2");
            System.in.read() ;

            Txn.execWrite(dsg, ()-> {
                dsg.add(q3) ;
                dsg.delete(q2) ;
                Graph g = dsg.getDefaultGraph() ;
                g.add(Triple.create(b1, p2, obj)) ;
            }) ;
            
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
