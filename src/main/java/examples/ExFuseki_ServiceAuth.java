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

package examples;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.engine.http.Service;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

public class ExFuseki_ServiceAuth
{
    public static void main(String ... a) throws Exception {
        FusekiLogging.setLogging();
        try {
            // Password file contains one line:
            // ----
            // user1: pw1
            // ----
            server("passwd");
            runRemoteQuery();
            runServiceQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("DONE");
            System.exit(0);
        }
    }

    /** Make an {@linkHttpClient} with users/password authentication. */
    static HttpClient authHttpClient(String user, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("user1", "pw1");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpClient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        return httpClient;
    }

    /**
     * Execute a query by sending it to the server.
     * <p>
     * Authentication by setting an {@code HttpClient} on the query execution object.
     */
    private static void runRemoteQuery() {
        System.out.println("** Query the server directly");
        HttpClient hc = authHttpClient("user1", "pw1");
        String queryString = "SELECT * { GRAPH ?g { ?s ?p ?o } }";
        Query query = QueryFactory.create(queryString);

        // HTTP QueryExecution
        try ( QueryExecution qExec = new QueryEngineHTTP("http://localhost:3030/ds", query, hc) ) {
            QueryExecUtils.executeQuery(qExec);
        }
    }

    /**
     * Execute a query that contains a SERVICE call.
     * <p>
     * Authentication by setting a content for the SERVICE execution code to pick up
     * - it contains an {@code HttpClient} which is specific to one remote endpoint.
     */
    private static void runServiceQuery() {
        System.out.println("** SERVICE block");

        String queryString = "SELECT * { SERVICE <http://localhost:3030/ds> { GRAPH ?g { ?s ?p ?o }} }";
        Query query = QueryFactory.create(queryString);

        // The HttpClient wit users and password.
        HttpClient hc = authHttpClient("user1", "pw1");

        // Context object to pass details to SERVICE execution.
        Context cxt = new Context();
        cxt.put(Service.queryClient, hc);

        // Associate the context with a specific endpoint
        Map<String, Context> serviceContextMap =  new HashMap<>();
        serviceContextMap.put("http://localhost:3030/ds", cxt);

        // Local dataset.
        DatasetGraph empty = DatasetGraphFactory.createTxnMem();
        // QueryExecution with custom content that holds the SERVICE setup.
        try ( QueryExecution qExec = QueryExecution.create()
                        .context(cxt)
                        .dataset(empty)
                        .query(query)
                        .build() ) {
            QueryExecUtils.executeQuery(qExec);
        } catch (QueryExceptionHTTP ex) {
            System.err.println("QueryExceptionHTTP: "+ex.getMessage());
        }
    }

    /** Run a Fuseki server in-memory for this example */
    public static void server(String passwordFile) {
        // Command line form (no data)
        //FusekiMainCmd.main("--passwd", "--auth=basic", "--mem", "/ds");

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(SSE.parseQuad("(:g :s :p :o)"));

        FusekiServer server = FusekiServer.create()
            .passwordFile(passwordFile)
            .auth(AuthScheme.BASIC)
            .add("/ds", dsg)
            .port(3030)
            .build();
        server.start();
    }
}
