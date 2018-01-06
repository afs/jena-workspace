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

package dev;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;

public class DevWorkspace
{
    static { LogCtl.setCmdLogging() ; }
    
    public static void main(String ... a) {
        Graph g = RDFDataMgr.loadGraph("/home/afs/tmp/D.nt");
        ExtendedIterator<Triple> iter = g.find(NodeFactory.createBlankNode(),null,null);
//        
////        for(Optional<Triple> opt = iter.nextOptional(); opt.isPresent(); opt = iter.nextOptional() ) {
//        
//            if ( ! opt.isPresent() ) 
//                break;
//            System.out.println(opt.get());
//        }
        System.out.println("DONE");
    }
}
