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

import static java.lang.String.format;

import java.io.IOException;
import java.util.function.Function;

import javax.servlet.ServletContext;

import fuseki.security.Filtered_SPARQL_QueryDataset;
import fuseki.security.SecurityPolicy;
import fuseki.security.SecurityRegistry;
import fuseki.security.VocabSecurity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
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
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class DevSecureNG {
    /*
     * No update requirement.
     * 
     * 
     *   SecurityPolicy 
     *   -- default graph is Quad.isDefaultGraph(Node) [DONE]
     *   -- union graph is Quad.isUnionGraph(Node) [Not supported]
     * 
     * "allow" security registries and "deny" registries
     *   registries == realms?
     * 
     * Tests
     *   Assemblers
     *   Fuseki
     *
     * Jetty: user:password file. https.
     * 
     * ** Have an intercepting query operation.
     *    How to distinguish access-controlled and non-controlled datasets? VocabSecurity.symControlledAccess
     * ** Centralize customized DSG.

     * ** TestSecurityAssembler 
     *    ?user!
     *    
     * ** GSP as well.
     *    ** Filtered_REST_Quads_R
     *    ** Filtered_SPARQL_GSP_R
     *      Dataset needs filter!
     *      But affects everything!
     *      DevLocal for constrained dataset. Not API which would break for search-update. 
     * Limitations:
     *    per dataset execution for GSP and Quads. Where is the context?  Add DS wrapper?  
     *    per servlet context dispatch.
     *    
     * Tests!
     *   Local and remote
     * 
     * cmd --args=FILE
     * 
     * Invert : Create a config object, command line is a subset, execute from Config object.
     */

    public static void mainVocab(String...a) {
        VocabSecurity.init();
        //SecurityRegistry sr = (SecurityRegistry)AssemblerUtils.build("assem-security.ttl", VocabSecurity.tSecurityRegistry);
        //System.out.println(sr);
        
        Dataset ds = (Dataset)AssemblerUtils.build("assem-security.ttl", VocabSecurity.tAccessControlledDataset);
        SecurityRegistry sr2 = (SecurityRegistry)ds.getContext().get(VocabSecurity.symSecurityRegistry);
        System.out.println(sr2);
    }

    public static void main(String...a) throws IOException {
        LogCtl.setLog4j();
        try { main$(a); } 
        catch (Exception ex) { ex.printStackTrace(); }
        finally { System.exit(0); }
    }
    
    // Multiple Servlet Contexts per server isn't supported by Fuseki currently.
    // == multiple handlers in Jetty?
    
    static void modify() {
        // ---- Modify
        Dataset ds = TDB2Factory.createDataset();
        Operation operation = Operation.register("AltQuery", "Alt Query Service");
        FusekiServer server = FusekiServer.create().port(1441)
            .registerOperation(operation, (ActionService)null)
            .addOperation("/ds", "query", operation)
            .build();

        server.getDataAccessPointRegistry().forEach((name, sap)->{
            System.out.println("  "+name);
            sap.getDataService().getOperations().forEach(op->{
                System.out.println("    "+op);
            });
        });
        
        // May need to build server via a DataService.
        // Replace query handler for this DataService only.
        DataService dataService = new DataService(ds.asDatasetGraph());
        // AccessControlledQuery.
        dataService.addEndpoint(Operation.Query, "query");
        ServiceDispatchRegistry sdr = new ServiceDispatchRegistry(false);
        sdr.register(Operation.Query, WebContent.contentTypeSPARQLQuery, (ActionService)null);
        // Is ServiceDispatchRegistry per servletContext?
        //ServiceDispatchRegistry.set(cxt, sdr);
        
        // If needed, each FusekiServer is a single ServletContext(Handler);
        // FusekiContext
        // Prepared jetty server, shared across several "FusekiServers"
        
        // statics around:
        // FusekiServer.Builder.
        //   Build around a ServletContext
        // FusekiServer.Builder.buildServletContextHandler() alt to build();
        
        // Multiple servlet contexts => ContextHandlerCollection
        Server jettyServer = server.getJettyServer();
        ServletContextHandler handler1 = new ServletContextHandler();
        /*ServletContext*/ handler1.getServletContext(); 
        handler1.setContextPath("/path1");
        handler1.addServlet((ServletHolder)null, "/action");

        ServletContextHandler handler2 = new ServletContextHandler();
        handler2.setContextPath("/path2");
        handler2.addServlet((ServletHolder)null, "/action");
        
        HandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(handler1);
        handlers.addHandler(handler2);
        jettyServer.setHandler(handlers);
        
//        ServletContext cxt = handler.getServletContext();
//        Server jettyServer = server.getJettyServer();
//        Handler handler = null;
//        jettyServer.setHandler(handler);
//        ServerConnector connector = new ServerConnector(jettyServer);
//        connector.setPort(99);
//        jettyServer.addConnector(connector);
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
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("user1", new SecurityPolicy("http://example/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityPolicy("http://example/g1", "http://example/g2"));

        // ---- Fuseki.
        // With SecurityHandler if USER != null;
        FusekiServer server = fuseki(PORT, dsName, ds, reg, USER, PASSWORD);
        ServletContext cxt = server.getServletContext();
        SecurityRegistry.set(cxt, reg);
        server.start();
        
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
            conn.queryResultSet("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }",
                rs->ResultSetFormatter.out(rs)
                );
            // TDB1 or TDB2.
            // Set UnionDefaultGraph (UDG) ... in the server.
            ds.getContext().set(TDB2.symUnionDefaultGraph1, true);
            //conn.queryResultSet("SELECT (count(*) AS ?C) { ?s ?p ?o }",
            conn.queryResultSet("SELECT * { ?s ?p ?o }",
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
        ActionService queryServlet2 = new Filtered_SPARQL_QueryDataset(determineUser);

        SecurityHandler sh = null;
        if ( userStore != null )
            sh = JettyLib.makeSecurityHandler("/*", "Dataset:"+dsName, userStore);
        
        FusekiServer.Builder builder = FusekiServer.create().port(port);
        
        if ( sh != null )
            builder.securityHandler(sh);
//      // Different name
//        Operation q2 = Operation.register("AltQuery", "Alt Query Service");
//        builder.registerOperation(q2, queryServlet2);
        
        FusekiServer server = builder 
            // Fails due to check that operations are registered once.
            //.registerOperation(Operation.Query, queryServlet2)
            .add(dsName, ds, false)
            //.addOperation(dsName, "q", q2)
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

