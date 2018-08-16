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

package dataset;

import static java.lang.String.format;

import java.util.function.Function;

import javax.servlet.ServletContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class DevSecureNG {
    /*
     * No update requirement.
     * SecurityFilter: 
     *      Naming the default graph to build filters, not just SecurityFilter(,true)
     *      Naming the union to build filters (ie see union only)
     *
     * Security module.
     * SecurityContext - names for "all" and "none".
     * "all but" mode.
     * Either 
     *    set of "yes"
     *    all and set of "no".
     * user!
     * 
     * Check whether NodeId is == across tuples.
     * 
     * user:password file.
     */
    
    // HTTPS
    private static void foo() {
        Server server = new Server();
        
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        
        // === jetty-https.xml ===
        // SSL Context Factory
        
        String jetty_home = "JETTY_HOME";
        
        SslContextFactory sslContextFactory = new SslContextFactory();
        
        sslContextFactory.setKeyStorePath(jetty_home + "/etc/keystore");
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        sslContextFactory.setTrustStorePath(jetty_home + "/etc/keystore");
        sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(8443);
        server.addConnector(sslConnector);
    }
    
    public static void main(String...a) {
        LogCtl.setLog4j();
        try { main$(a); } 
        catch (Exception ex) { ex.printStackTrace(); }
        finally { System.exit(0); }
    }
    
    public static void main$(String...a) {
        int PORT = 1234;
        String dsName = "ds";

        // Turn into an example!
        
        // ---- Setup data.
        Dataset ds = setupDataset("D.trig");
        
        String USER = "user1";
        String PASSWORD = "pw1";

        // ---- Set up the registry.
        SecurityRegistry reg = SecurityRegistry.get();
        reg.put("user1", new SecurityContext("http://example/g1"));
        reg.put("user2", new SecurityContext("http://example/g1", "http://example/g2"));

        // ---- Fuseki.
        // With SecurityHandler if USER != null;
        FusekiServer server = fuseki(PORT, dsName, ds, reg, USER, PASSWORD);
        server.start();
        
        // Security filter?
        
        // ---- Try it.
        // HttpClient with password.
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(USER, PASSWORD);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        
        //HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        HttpClient client = HttpOp.createPoolingHttpClientBuilder().setDefaultCredentialsProvider(credsProvider).build();
        
        String URL = format("http://localhost:%d/%s", PORT, dsName);
        RDFConnection connx = RDFConnectionRemote.create()
            .destination(URL)
            .httpClient(client)
            .build();
            
        try ( RDFConnection conn = connx ) {
            conn.queryResultSet("SELECT * { GRAPH ?g { ?s ?p ?o } }",
                rs->ResultSetFormatter.out(rs)
                );
            // TDB1 or TDB2.
            // Set UnionDefaultGraph (UDG) ... in the server.
            ds.getContext().set(TDB2.symUnionDefaultGraph1, true);
            conn.queryResultSet("SELECT (count(*) AS ?C) { ?s ?p ?o }",
                rs->ResultSetFormatter.out(rs)
                );
//            conn.queryResultSet("SELECT * { ?s ?p ?o }",
//                rs->ResultSetFormatter.out(rs)
//                );
        }
    }
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, SecurityRegistry reg, String user, String password) {
        UserStore userStore = JettyLib.makeUserStore(user, password);
        return fuseki(port, dsName, ds, reg, userStore);
    }
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, SecurityRegistry reg, UserStore userStore) {
        Function<HttpAction, String> determineUser = (action)->action.request.getRemoteUser();
        // Make dynamic.
        ActionService queryServlet2 = new SPARQL_QueryDatasetFiltered(reg, determineUser);

        SecurityHandler sh = null;
        if ( userStore != null )
            sh = JettyLib.makeSecurityHandler("/*", "Daatset:"+dsName, userStore);
        
        // Different name/
        Operation q2 = Operation.register("AltQuery", "Alt Query Service");
        FusekiServer.Builder builder = FusekiServer.create().setPort(port).registerOperation(q2, queryServlet2);
        if ( sh != null )
            builder.setSecurityHandler(sh);
        
        FusekiServer server = builder 
            // Fails due to check that operations are registered once.
            //.registerOperation(Operation.Query, queryServlet2)
            .add(dsName, ds, false)
            .addOperation(dsName, "q", q2)
            .build();
        
        // Same name - replace.
        // ----
        /*  Dispatch in Fuseki works in several ways.
         *   HTTP Requests to the dataset URL go to the ServiceRouter, which can handle  
         *     From SPARQL protocol URL query string like ?query=
         *     From content type.
         *     
         *  From service endpoint names, such as "query" or "sparql".
         *  HTTP Requests to the server endpoint go direct to the right servlet.
         */
        /* 
         * Reconfigure standard Jena Fuseki, replacing the default implementation of "query"
         * with a filtering one.  This for this server only. 
         */
        // The mapping operation to handler is in the ServiceDispatchRegistry and is per server (per servlet context)
        // "registerOrReplace"?
        ServletContext cxt = server.getServletContext();
        ServiceDispatchRegistry.get(cxt).register(Operation.Query, WebContent.contentTypeSPARQLQuery, queryServlet2);
        // ----
        return server;
    }

    private static Dataset setupDataset(String filename) {
        Dataset ds = TDB2Factory.createDataset();
        Txn.executeWrite(ds, () -> RDFDataMgr.read(ds, filename));
        try (RDFConnection conn = RDFConnectionFactory.connect(ds)) {
            conn.queryResultSet("SELECT ?g (count(*) AS ?C) { { ?S ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } GROUP BY ?g",
                rs -> ResultSetFormatter.out(rs));
        }
        return ds;
    }
}

