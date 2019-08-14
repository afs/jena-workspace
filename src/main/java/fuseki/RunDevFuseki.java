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
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
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
        FusekiServer server = FusekiServer.create()
            .parseConfigFile("config.ttl")
            //.add("/ds", DatasetGraphFactory.createTxnMem())
            //.addEndpoint("/ds", "extra", op)
            //.enableStats(true)
            .port(3030)
            //.verbose(true)
            .build();
        try {
            server.start();
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds") ) {
                // Because of Dispatch.findEndpointForOperation #
                System.out.println("dataset");
                QueryExecution qExec = conn.query("ASK{}");
                QueryExecUtils.executeQuery(qExec);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
//            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds/query") ) {
//                System.out.println("ds/query");
//                QueryExecution qExec = conn.query("ASK{}");
//                QueryExecUtils.executeQuery(qExec);
//            } catch (Exception ex) {
//                System.out.println(ex.getMessage());
//            }
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds/sparql") ) {
                System.out.println("ds/sparql");
                QueryExecution qExec = conn.query("ASK{}");
                QueryExecUtils.executeQuery(qExec);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        finally {
            server.stop();
        }
    }
}
