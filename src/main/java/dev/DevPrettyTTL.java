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
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.sparql.util.Context ;

public class DevPrettyTTL {
static { LogCtl.setCmdLogging() ; }

    public static void main(String ... a) {
        
//        ARQ.getContext().set(RIOT.multilineLiterals, true);
//        riotcmd.riot.main("--formatted=TTL", "/home/afs/tmp/D.ttl") ;
        
        Graph g = RDFDataMgr.loadGraph("/home/afs/tmp/D.ttl") ;
        ReaderRIOT r = RDFDataMgr.createReader(Lang.TURTLE) ;
        WriterGraphRIOT w = RDFDataMgr.createGraphWriter(RDFFormat.TURTLE) ;
        Context cxt = RIOT.getContext().copy() ;
        cxt.set(RIOT.multilineLiterals, true);
        w.write(System.out, g, RiotLib.prefixMap(g), null, cxt) ;
        
        
        //ARQ.getContext().set(RIOT.multilineLiterals, false);
        System.out.println("##-----------------------------------") ;
        riotcmd.riot.main("--formatted=TTL", "/home/afs/tmp/D.ttl") ;
        
        System.exit(0) ;
    }
}
