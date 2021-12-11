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

import fuseki.builders.DataServiceBuilder;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.QueryExecUtils;

public class RunDevFuseki
{
    public static void main(String ... a) {
        FusekiLogging.setLogging();
        try {
            run();
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static void run() {
        // New
        DataServiceBuilder builder = new DataServiceBuilder();

        Endpoint ep = Endpoint.create().endpointName("QUERY").operation(Operation.Query).build();

        // Remove .addEndpoint(* AuthPolicy)
        DataService dSrc = builder
            .dataset(DatasetGraphFactory.createTxnMem())
            .addEndpoint(ep)
            .addEndpoint(Operation.Query)          // check dispatchable. Do this on any overloaded EP.
            .addEndpoint(Operation.Shacl, "shacl") // Inaccessible.
            .build();

        FusekiServer server = FusekiServer.create()
            //.parseConfigFile("config.ttl")
            //.add("/ds", DatasetGraphFactory.createTxnMem())
            //.addEndpoint("/ds", "extra", op)
            //.enableCors(true)
            //.enableStats(true)
            .add("/ds", dSrc)
            .port(3030)
            //.verbose(true)
            .build();
        try {
            server.start();

            try ( RDFConnection conn = RDFConnection.connect("http://localhost:3030/ds") ) {
                QueryExecution qExec = conn.query("ASK{}");
                QueryExecUtils.executeQuery(qExec);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            //String x = HttpOp.httpGetString("http://localhost:3030/$/stats");
            //System.out.println(x);

        }
        finally {
            server.stop();
        }
    }
}
