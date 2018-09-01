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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.access.SecurityContext;
import org.apache.jena.fuseki.access.AuthorizationService;
import org.apache.jena.fuseki.access.SecurityRegistry;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevSecureNG {
    /*
     * DatasetGraphFilter => jena-arq?
     */
    
    /*
     * 0:: QueryExecutionBase with DatasetGraph directly.
     *     Works but creates a Dataset - tidy up.
     *
     * 1:: https wrapper
     *
     * 9:: FusekiBasicAccessCmd
     * 
     * X:: Documentation
     *    API needs intervention.
     *    QueryExecution is modified.
     *    Example based on assem-access-shared.
     *    jetty: https, and passwords.
     *    Read-only embedding.
     *    
     * Separately.
     *    RDFConnection
     *    Remove deprecated RDFConnectionRemote
     */
    /*
     * "allow" security registries and "deny" registries
     * Jetty: user:password file. https.
     * Shiro
     */
    
    public static void main(String...a) throws IOException {
        try { FusekiServerSecuredCmd.main("--mem", "/ds"); }
        catch (Throwable ex) { ex.printStackTrace(); }
        finally { System.exit(0); }

        // -----------------
        //** Replace fuseki() with function calls.
        FusekiLogging.setLogging();
        
        //LogCtl.setLog4j("log4j.properties");

        try { mainFuseki(a); }
        catch (Throwable ex) { ex.printStackTrace(); }
        finally { System.exit(0); }
    }

    public static void mainFuseki(String...a) {
        //Not printing datasets.  Extract from FusekiBasicServer 
        // Map<String, List<String>> mapDatasetEndpoints = description(DataAccessPointRegistry.get(server.getServletContext()));
        Logger LOG = LoggerFactory.getLogger("main");
        FusekiServer server = DataAccessCtl.fusekiBuilder(DataAccessCtl.paramUserServlet)
            .port(3434)
            //.verbose(true)
            .parseConfigFile("/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-access/testing/Access/assem-security-shared.ttl")
            .build();
        //server.logConfiguration(LOG);
        server.getDataAccessPointRegistry().forEach((n,dap)->{
            List<String> endpoints = dap.getDataService().getOperations().stream()
                .flatMap((op)->dap.getDataService().getEndpoints(op).stream())
                .map(ep->ep.getEndpoint())
                //.map(s->s.isEmpty()?"_":s)
                .map(s->format("%s/%s", n, s))
                .collect(Collectors.toList());
            
            //FmtLog.info(LOG, "  %s: %s", n, endpoints);
            FmtLog.info(LOG, "  %s", endpoints);
        });
        server.start();
        server.join();
    }

    public static void main$(String...a) {
        
    }
        
    public static void mainOld(String...a) {
        int PORT = 1234;
        String dsName = "ds";

        // Turn into an example!
        
        // ---- Setup data.
        Dataset ds = createData("D.trig");
        
        String USER = "user1";
        String PASSWORD = "pw1";

        // ---- Set up the registry.
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("user1", new SecurityContext("http://example/g1", Quad.defaultGraphIRI.getURI()));
        
        //reg.put("user1", new SecurityPolicy("http://example/g1"));
        //reg.put("user2", new SecurityPolicy("http://example/g1", "http://example/g2"));

        // ---- Fuseki.
        // With SecurityHandler if USER != null;
        
        FusekiServer server = fuseki(PORT, dsName, ds, reg, USER, PASSWORD);
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
            
        // Quads
        if ( false ) {
            System.out.println("++ Quads");
            Dataset ds1 = connx.fetchDataset();
            RDFDataMgr.write(System.out, ds1, RDFFormat.TRIG_FLAT);
            //return;
        }
        
        // GSP
        if ( true ) {
            try {
                System.out.println("\n++ GSP : g1");
                Model mg1 = connx.fetch("http://example/g1");
                //Model m = connx.fetch(Quad.unionGraph.getURI());
                RDFDataMgr.write(System.out, mg1, RDFFormat.TURTLE_FLAT);
            } catch (HttpException ex) {
                System.out.println("EXCEPTION: "+ex.getMessage());
            }
            // No!
            try { 
                System.out.println("\n++ GSP : g2");
                Model mg2 = connx.fetch("http://example/g2");
                RDFDataMgr.write(System.out, mg2, RDFFormat.TURTLE_FLAT);
            } catch (HttpException ex) {
                System.out.println("EXCEPTION: "+ex.getMessage());
            }
            // No!
            try { 
                System.out.println("\n++ GSP : doesNotExist");
                Model mg2 = connx.fetch("http://example/doesNotExist");
                RDFDataMgr.write(System.out, mg2, RDFFormat.TURTLE_FLAT);
            } catch (HttpException ex) {
                System.out.println("EXCEPTION: "+ex.getMessage());
            }
            try {
                System.out.println("\n++ GSP : gDft");
                Model mDft = connx.fetch();
                RDFDataMgr.write(System.out, mDft, RDFFormat.TURTLE_FLAT);
            } catch (HttpException ex) {
                System.out.println("EXCEPTION: "+ex.getMessage());
            }
            try {
                System.out.println("\n++ GSP : union");
                Model mUnion = connx.fetch(Quad.unionGraph.getURI());
                RDFDataMgr.write(System.out, mUnion, RDFFormat.TURTLE_FLAT);
                return;
            } catch (HttpException ex) {
                System.out.println("EXCEPTION: "+ex.getMessage());
            }
            if ( true ) return;
            System.out.println();
        }
        
        // SPARQL Query.
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
        }
    }
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, AuthorizationService reg, String user, String password) {
        UserStore userStore = JettyLib.makeUserStore(user, password);
        return fuseki(port, dsName, ds, reg, userStore);
    }
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, AuthorizationService reg, UserStore userStore) {
        Dataset dsx = DataAccessCtl.controlledDataset(ds, reg);
        FusekiServer.Builder builder = DataAccessCtl.fusekiBuilder(DataAccessCtl.requestUserServlet)
            .port(port)
            .add(dsName, dsx, false);
        SecurityHandler sh = null;
        if ( userStore != null )
            sh = JettyLib.makeSecurityHandler("/*", "Dataset:"+dsName, userStore);
        if ( sh != null )
            builder.securityHandler(sh);
        return builder.build();
    }

    private static Dataset createData(String filename) {
        Dataset ds = TDB2Factory.createDataset();
        Txn.executeWrite(ds, () -> RDFDataMgr.read(ds, filename));
        try (RDFConnection conn = RDFConnectionFactory.connect(ds)) {
            conn.queryResultSet("SELECT ?g (count(*) AS ?C) { { ?S ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } GROUP BY ?g",
                rs -> ResultSetFormatter.out(rs));
        }
        return ds;
    }
}

