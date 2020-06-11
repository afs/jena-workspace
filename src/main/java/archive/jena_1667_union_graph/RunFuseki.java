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

package archive.jena_1667_union_graph;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class RunFuseki
{
    public static void main(String ... a) {
        FusekiLogging.setLogging();
        mainServerClient();
    }

    public static void mainServer(String ... a) {
        FusekiServer server = FusekiServer.create()
            .add("/ds", DatasetGraphFactory.createTxnMem())
            .port(3030)
            .verbose(true)
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }

    public static void mainServerClient() {

        String BASE = "/home/afs/ASF/jena-reports/JENA-1667-union-graph";

        FusekiServer server = FusekiServer.create()
            //.add("/ds", DatasetGraphFactory.createTxnMem())
            .parseConfigFile(BASE+"/config2.ttl")
            .port(3030)
            .build()
            .start();

        // Use it.
        try ( RDFConnection conn = RDFConnectionFactory.connectFuseki("http://localhost:3030/ds") ) {
            conn.querySelect("SELECT * { ?s ?p ?o }", qs->System.out.println(qs));
        }
        finally { server.stop(); }
        System.out.println("DONE");
    }
}
