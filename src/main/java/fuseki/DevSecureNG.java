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
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.jetty.JettyHttps;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.web.AuthSetup;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevSecureNG {

    // Setup
    static String    USER     = "user1";
    static String    PASSWORD = "pw1";
    // When using "digest", this must agree with the password file for MD5 and CRYPT
    // entries.
    static String    REALM    = "TripleStore";
    static String    HOST     = "localhost";
    static int       PORT     = 3443;
    static AuthSetup auth     = new AuthSetup(HOST, PORT, USER, PASSWORD, REALM);

    public static void main(String...a) throws Exception {
        FusekiLogging.setLogging();
        try {
// mainServer();
// mainCode();
// mainHttps();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }

    public static void mainServer() throws IOException {
        // String conf = "config-auth.ttl";
        String conf = "config-access.ttl";
        if ( false )
            LogCtl.enable("org.apache.http.headers");

        // No auth.
        // validation: passwd file if any security allowedUsers or AccessControlledDataset

        FusekiServer server = FusekiServer.create().port(3030).parseConfigFile(conf).build();

        // FusekiServer server = FusekiMain.build("--conf="+conf);
        FusekiLib.modifyForAccessCtl(server, DataAccessCtl.requestUserServlet);
        // DEV

        server.getDataAccessPointRegistry().forEach((name, dap) -> {
            DatasetGraph dsg = dap.getDataService().getDataset();
            if ( dap.getDataService().authPolicy() != null ) {
                System.err.println("AllowedUsers: " + name);
                return;
            }
            if ( DataAccessCtl.isAccessControlled(dsg) ) {
                System.err.println("DataAccessControlled: " + name);
                return;
            }
        });

        server.start();

        boolean fillDirect = true; // TIM
        if ( fillDirect )
            fill(server, "/database", "data-access.trig");
        else
            fill(dsURL(auth, "plain"), "data-access.trig");

        // NO Graph ACL.
        AuthSetup auth = new AuthSetup(HOST, 3030, USER, PASSWORD, REALM);
        LibSec.withAuth("http://localhost:3030/database", auth, conn -> {
            query(conn, "SELECT * { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } }");
        });

        // Lib.sleep(1000);
        System.exit(0);
    }

    public static void mainCode() throws IOException, URISyntaxException {
        // S/basic , C/digest => ends up with "basic"
        // S/digest, C/basic => ends up with "digest"
        // Client side controls first try.

        // BASIC?

        // passwd file must agree.
        LibSec.authMode = AuthScheme.DIGEST;
        JettyLib.authMode = AuthScheme.DIGEST;

// System.err.println("Client: "+LibSec.authMode);
// System.err.println("Server: "+JettyLib.authMode);

// try { FusekiServerSecuredCmd.main("--mem", "/ds"); }
// catch (Throwable ex) { ex.printStackTrace(); }
// finally { System.exit(0); }

        // -----------------
        // ** Replace fuseki() with function calls.
        FusekiLogging.setLogging();
        // LogCtl.enable(Fuseki.requestLog);
        if ( false )
            LogCtl.enable("org.apache.http.headers");

        try {
            FusekiServer server = fusekiServer(auth.port);
            // -- Data
            boolean fillDirect = true; // TIM
            if ( fillDirect )
                fill(server, "/database", "data-access.trig");
            else
                fill(dsURL(auth, "plain"), "data-access.trig");

// LibSec.withAuth("http://"+HOST+":"+PORT+"/database", auth, connx->{
// try ( QueryExecution qExec = connx.query("SELECT * { GRAPH ?g {} }") ) {
// QueryExecUtils.executeQuery(qExec);
// }
// });

// // Update.
// String urlStr = dsURL(auth, "/database");
// LibSec.withAuth(urlStr, auth, connx->{
// connx.query("ASK{}").execAsk();
// try { connx.update("INSERT DATA {}"); } catch (Exception ex) {
// System.err.println(ex.getMessage()); }
// try { connx.loadDataset("data-access.trig"); } catch (Exception ex) {
// System.err.println(ex.getMessage()); }
// });

            client("database");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }

    public static void mainHttps() throws Exception {
        // Server - two connectors on http://8080 and https://8443; 8080 will bounce to
        // https.
        ServletContextHandler contextHandler = new ServletContextHandler();
        DefaultServlet staticServlet = new DefaultServlet();
        ServletHolder staticContent = new ServletHolder(staticServlet);
        staticContent.setInitParameter("resourceBase", ".");
        contextHandler.addServlet(staticContent, "/");

        Server jettyServer = JettyHttps.jettyServerHttps(contextHandler, "certs/mykey.jks", "cert-pw", 8443, 8443);
        jettyServer.start();
        jettyServer.join();
        Lib.sleep(50000);
        System.exit(0);
    }

    public static FusekiServer fusekiServer(int port) {
        FusekiServer server = FusekiLib.fusekiBuilder(DataAccessCtl.requestUserServlet).port(port).parseConfigFile("config-access-all.ttl")
            .build();
        server.start();
        return server;
    }

    public static FusekiServer fusekiServerProg(int port) {
        Logger LOG = LoggerFactory.getLogger("main");

        // -- Security handler.
        // UserStore userStore = JettyLib.makeUserStore(auth.user, auth.password);

        // From a file.
        UserStore userStore = JettyLib.makeUserStore("passwd");

        ConstraintSecurityHandler sh = null;
        if ( userStore != null ) {
            sh = JettyLib.makeSecurityHandler(auth.realm, userStore);
            // JettyLib.addPathConstraint(sh, "/*"); //"/database"
        }

        // -- Create server, "?user=" based. "database" and "plain"
        FusekiServer server = FusekiLib.fusekiBuilder(DataAccessCtl.requestUserServlet)
            // .fusekiBuilder(DataAccessCtl.paramUserServlet)
            .port(port)
            // .verbose(true)
            // .parseConfigFile("/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-access/testing/Access/assem-security-shared.ttl")
            .parseConfigFile("config-access.ttl").securityHandler(sh).build();
        org.eclipse.jetty.server.Server js = server.getJettyServer();
        ServletContextHandler h = (ServletContextHandler)js.getHandler();
        SecurityHandler sh2 = h.getSecurityHandler();

        if ( sh2 != null ) {
            server.getDataAccessPointRegistry().forEach((n, dap) -> {
                DatasetGraph dsg = dap.getDataService().getDataset();
                if ( DataAccessCtl.isAccessControlled(dsg) ) {
                    LOG.info("Controlled " + n);
                    JettyLib.addPathConstraint((ConstraintSecurityHandler)sh2, "/" + n);
                    JettyLib.addPathConstraint((ConstraintSecurityHandler)sh2, "/" + n + "/*");
                }
            });
        }

        // Dynamically setup the security handler
        // rethink
        // FusekiServer server = LibSec.fuseki(PORT, dsName, ds, reg, USER, PASSWORD);

// // -- Info
// DEV
// server.logConfiguration(LOG);
        server.getDataAccessPointRegistry().forEach((n, dap) -> {
            List<String> endpoints = dap.getDataService().getOperations().stream()
                .flatMap((op) -> dap.getDataService().getEndpoints(op).stream()).map(ep -> ep.getName())
                // .map(s->s.isEmpty()?"_":s)
                .map(s -> format("%s/%s", n, s)).collect(Collectors.toList());

            // FmtLog.info(LOG, " %s: %s", n, endpoints);
            FmtLog.info(LOG, "  %s", endpoints);
        });
        server.start();
        return server;
    }

    public static void fill(FusekiServer server, String dsName, String file) {
        DatasetGraph dsg = server.getDataAccessPointRegistry().get(dsName).getDataService().getDataset();
        DatasetGraph basedsg = ((DatasetGraphWrapper)dsg).getBase();
        Txn.executeWrite(dsg, () -> RDFDataMgr.read(basedsg, "data-access.trig"));
    }

    private static void fill(String urlStr, String filename) {
        // -- Add data.
        // Server is password protected so we need to authenticate to access /plain.
        LibSec.withAuth(urlStr, auth, conn -> {
            // Force digest auth setup.
            // [AuthScheme][Digest]
            // conn.query("ASK{}").execAsk();
            conn.putDataset(filename);
        });
    }

    private static void query(RDFConnection conn, String queryString) {
        try {
            QueryExecution qExec = conn.query(queryString);
            QueryExecUtils.executeQuery(qExec);
        } catch (QueryExceptionHTTP ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static String dsURL(AuthSetup auth, String dsName) {
        if ( dsName.startsWith("/") )
            dsName = dsName.substring(1);
        return format("http://%s:%d/%s", auth.host, auth.port, dsName);
    }

    // Client actions on the
    public static void client(String dsName) {
        // ---- Try it.
        String urlStr = dsURL(auth, dsName);
        LibSec.withAuth(urlStr, auth, connx -> {

            // Quads
            if ( false ) {
                Dataset ds1 = connx.fetchDataset();
                RDFDataMgr.write(System.out, ds1, RDFFormat.TRIG_BLOCKS);
                // return;
            }

            // SPARQL Query
            if ( true ) {
// query(connx, "SELECT * FROM <http://host/graphname1> { GRAPH ?g {} }");
// query(connx, "SELECT * FROM NAMED <http://host/graphname1> { { ?s ?p ?o } UNION { GRAPH
// ?g { ?s ?p ?o } } }");
// query(connx, "SELECT * FROM NAMED <http://host/graphname1> { GRAPH
// <http://host/graphname1> { ?s ?p ?o } }");
// query(connx, "SELECT * { GRAPH ?g {} }");
// query(connx, "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }");
// query(connx, "SELECT * FROM <http://host/graphname1> { ?s ?p ?o }");
// query(connx, "SELECT * FROM <http://host/graphname1> { { ?s ?p ?o } UNION { GRAPH ?g {
// ?s ?p ?o } } }");
// query(connx, "SELECT * FROM NAMED <http://host/graphname1> { { ?s ?p ?o } UNION { GRAPH
// ?g { ?s ?p ?o } } }");
// query(connx, "SELECT * { GRAPH <http://host/graphname1> { ?s ?p ?o }}");
//
// query(connx, "SELECT * FROM <http://host/graphname4> { { ?s ?p ?o } UNION { GRAPH ?g {
// ?s ?p ?o } } }");
// query(connx, "SELECT * { GRAPH <http://host/graphname4> { ?s ?p ?o }}");
//
// query(connx, "SELECT * { GRAPH <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o } }");

                // **** Should be empty. No named graphs
                // Sees real unionGraph
                query(connx, "SELECT * FROM <" + Quad.unionGraph.getURI() + "> { ?s ?p ?o }");

// query(connx, "SELECT * { GRAPH <"+Quad.defaultGraphIRI.getURI()+"> { ?s ?p ?o } }");
// query(connx, "SELECT * FROM <"+Quad.defaultGraphIRI.getURI()+"> { ?s ?p ?o }");
// System.exit(0);
            }

            // GSP
            if ( true ) {
                try {
                    Model mg1 = connx.fetch("http://host/graphname1");
                    // Model m = connx.fetch(Quad.unionGraph.getURI());
                    RDFDataMgr.write(System.out, mg1, RDFFormat.TURTLE_FLAT);
                } catch (HttpException ex) {
                    }
                // No!
                try {
                    // Not allowed, user1
                    Model mg2 = connx.fetch("http://host/graphname4");
                    RDFDataMgr.write(System.out, mg2, RDFFormat.TURTLE_FLAT);
                } catch (HttpException ex) {
                    }
                // No!
                try {
                    Model mg2 = connx.fetch("http://host/doesNotExist");
                    RDFDataMgr.write(System.out, mg2, RDFFormat.TURTLE_FLAT);
                } catch (HttpException ex) {
                    }
                try {
                    Model mDft = connx.fetch();
                    RDFDataMgr.write(System.out, mDft, RDFFormat.TURTLE_FLAT);
                } catch (HttpException ex) {
                    }
                try {
                    Model mUnion = connx.fetch(Quad.unionGraph.getURI());
                    RDFDataMgr.write(System.out, mUnion, RDFFormat.TURTLE_FLAT);
                    return;
                } catch (HttpException ex) {
                    }
            }
        });
        }

// // SPARQL Query.
// try ( RDFConnection conn = connx ) {
// conn.queryResultSet("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }",
// rs->ResultSetFormatter.out(rs)
// );
// // TDB1 or TDB2.
// // Set UnionDefaultGraph (UDG) ... in the server.
// ds.getContext().set(TDB2.symUnionDefaultGraph1, true);
// //conn.queryResultSet("SELECT (count(*) AS ?C) { ?s ?p ?o }",
// conn.queryResultSet("SELECT * { ?s ?p ?o }",
// rs->ResultSetFormatter.out(rs)
// );
// }
}
