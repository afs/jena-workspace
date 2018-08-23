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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.access.SecurityPolicy;
import org.apache.jena.fuseki.access.SecurityRegistry;
import org.apache.jena.fuseki.access.VocabSecurity;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;

public class DevSecureNG {
    /*
     * DatasetGraphAccess only?
     */
    
    /*
     * 1:: Assembler => Still std Fuseki, not query ops. 
     *       [Check]
     *       DataAccessCtl.fusekiBuilder() with Fuseki+filter actions.
     *       DataAccessCtl.fuseki - replaces the services 
     *       DataAccessCtl.fusekiReadOnly- only read/access services. (FusekiServer.createReadOnly())
     * 2:: GSP
     *
     * 3:: Init via ServiceLoader.
     *     Test logging.
     *
     * 6:: Check wrappers on tdb. 
     *     Filtered_SPARQL_QueryDataset
     *     "Find with filter" -> a masking find operation.
     * 
     * 8:: Look for X XX.
     * 
     * 9:: FusekiBasicAccessCmd
     * 
     * X:: Documentation
     *    API needs intervention.
     *    QueryExecution is modified.
     *    Example based on assem-access-shared.
     *    jetty: https, and passwords.
     *    Read-only embeddeding.
     *    
     * Z:: jena-fuseki-embedded depending on (optional) log4j via parent.
     *     Remove from parent.
     */
    /*
     * "allow" security registries and "deny" registries
     * Jetty: user:password file. https.
     */
    
    public static void main(String...a) throws IOException {
        //** Replace fuseki() with function calls.
        LogCtl.setLog4j();
        try { main$(a); }
        catch (Exception ex) { ex.printStackTrace(); }
        finally { System.exit(0); }
    }

    public static void mainSetup(String...a) {
        
        Dataset ds = (Dataset)AssemblerUtils.build("assem-security.ttl", VocabSecurity.tAccessControlledDataset);
        SecurityRegistry sr1 = (SecurityRegistry)ds.getContext().get(DataAccessCtl.symSecurityRegistry);
        SecurityRegistry sr2 = (SecurityRegistry)AssemblerUtils.build("assem-security.ttl", VocabSecurity.tSecurityRegistry);
        
        System.out.println(sr2);
        
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("user1", new SecurityPolicy("http://example/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityPolicy("http://example/g1", "http://example/g2"));
        
        // --------------
        
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        /*DataAccessCtl.*/DataAccessCtl.controlledDataset(dsg, reg);

        //or
//        DatasetGraph dsg0 = DatabaseMgr.createDatasetGraph();
//        DatasetGraph dsg1 = wrapControlledDataset(dsg0, reg);
//        dsg = dsg1;
        
        FusekiServer server = DataAccessCtl.fusekiBuilder(DataAccessCtl.requestUserServlet)
            .port(3434)
            .add("/ds", dsg)
            .build();

        server.start();
    }

    public static void main$(String...a) {
        int PORT = 1234;
        String dsName = "ds";

        // Turn into an example!
        
        // ---- Setup data.
        Dataset ds = createData("D.trig");
        
        String USER = "user1";
        String PASSWORD = "pw1";

        // ---- Set up the registry.
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("user1", new SecurityPolicy("http://example/g1", Quad.defaultGraphIRI.getURI()));
        
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
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, SecurityRegistry reg, String user, String password) {
        UserStore userStore = JettyLib.makeUserStore(user, password);
        return fuseki(port, dsName, ds, reg, userStore);
    }
    
    private static FusekiServer fuseki(int port, String dsName, Dataset ds, SecurityRegistry reg, UserStore userStore) {
        Dataset dsx = DataAccessCtl.wrapControlledDataset(ds, reg);
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

