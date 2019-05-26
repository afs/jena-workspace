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

package fuseki;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class RunDevFuseki
{
    public static void main(String ... a) {
        FusekiLogging.setLogging();
        try { 
            plain();
            //mainQueryUpdate();
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    
    public static void plain() {
        FusekiServer server = FusekiServer.create()
            .add("/ds", DatasetGraphFactory.createTxnMem())
            //.enableStats(true)
            .port(3030)
            .verbose(true)
            .passwordFile("passwd")
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }

    // Routing for Q+U only.
    public static void mainQueryUpdate(String ... a) {
        Operation oper = Operation.register("q&u", "");
        
        FusekiServer server = FusekiServer.create()
            .verbose(false)
//            .add("/ds", DatasetGraphFactory.createTxnMem())
//            .add("/other", DatasetGraphFactory.createTxnMem())
//            .parseConfigFile("/home/afs/tmp/config.ttl")
//            .registerOperation(oper, new ServiceRouterQueryUpdate())
//            .addOperation("/ds", "sparql", oper)

            .enableStats(true)
            .port(3030)
            .verbose(true)
            //.addServlet("/$/metrics/*", new org.apache.jena.fuseki.ctl.ActionMetrics())
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }
}
