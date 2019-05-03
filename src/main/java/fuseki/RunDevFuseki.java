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

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class RunDevFuseki
{
    public static void main(String ... a) {
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
            //.addServlet("/$/metrics/*", new org.apache.jena.fuseki.ctl.ActionMetrics())
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }

    // Routing for Q+U only.
    public static void mainQueryUpdate(String ... a) {
        FusekiLogging.setLogging();
        
        DataService dSrv = new DataService(DatasetGraphFactory.createTxnMem());
        
        // Unnamed registration.
        dSrv.addEndpoint(Operation.Query, "");
        dSrv.addEndpoint(Operation.Update, "");
        
        Operation oper = Operation.register("q&u", "");
        
        FusekiServer server = FusekiServer.create()
//            .add("/ds", DatasetGraphFactory.createTxnMem())
//            .add("/other", DatasetGraphFactory.createTxnMem())
//            .parseConfigFile("/home/afs/tmp/config.ttl")
            .add("/ds", dSrv)
//            .registerOperation(oper, new ServiceRouterQueryUpdate())
//            .addOperation("/ds", "sparql", oper)

            .enableStats(true)
            
            .addServlet("/direct", new SPARQL_QueryDataset() {
                @Override
                protected Pair<DatasetGraph, Query> decideDataset(HttpAction action, Query query, String queryStringLog) {
                    return Pair.create(dSrv.getDataset(), query); 
                }
            })
        
            .port(3030)
            .verbose(true)
            //.addServlet("/$/metrics/*", new org.apache.jena.fuseki.ctl.ActionMetrics())
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }
}
