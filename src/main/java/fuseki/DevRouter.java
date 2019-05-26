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
public class DevRouter {}

//import static org.junit.Assert.fail;
//
//import java.util.function.Consumer;
//
//import org.apache.jena.atlas.json.JSON;
//import org.apache.jena.atlas.json.JsonObject;
//import org.apache.jena.atlas.web.HttpException;
//import org.apache.jena.atlas.web.WebLib;
//import org.apache.jena.fuseki.main.FusekiServer;
//import org.apache.jena.fuseki.server.DataService;
//import org.apache.jena.fuseki.server.Operation;
//import org.apache.jena.fuseki.system.FusekiLogging;
//import org.apache.jena.query.Dataset;
//import org.apache.jena.query.QueryExecution;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdfconnection.LibSec;
//import org.apache.jena.rdfconnection.RDFConnection;
//import org.apache.jena.rdfconnection.RDFConnectionFactory;
//import org.apache.jena.riot.RDFDataMgr;
//import org.apache.jena.riot.web.HttpOp;
//import org.apache.jena.sparql.core.DatasetGraph;
//import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
//import org.apache.jena.sparql.util.QueryExecUtils;
//import org.apache.jena.web.AuthSetup;
//import org.apache.jena.web.HttpSC;
//import org.junit.Assert;
//
///** Change Fuseki configuration on-the-fly */
//public class DevRouter {
//    public static void main(String...a) {
//        //JenaSystem.init();
//        FusekiLogging.setLogging();
//        mainStdConfig();
//    }
//    
//    public static void mainStdConfig() {
//s        int port = WebLib.choosePort();
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        
//        try {
//            FusekiServer server = FusekiServer.create()
//                //.verbose(true)
//                //.parseConfigFile("config-std-mem.ttl")
//                //.add("/ds", dsg)
//                .add("/ds", dsg, true)
//                .port(port)
//                .build();
//            server.start();
//            //server.join();
//
//            Model data = RDFDataMgr.loadModel("D.ttl");
//            Dataset dataset = RDFDataMgr.loadDataset("D.ttl");
//            
//            String URL = "http://localhost:" + port + "/ds";
//            
//            execDataset(URL, conn->conn.queryAsk("ASK{}") );
//            execDataset(URL, conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }") );
//            execDataset(URL, conn->conn.fetch());
//            execDataset(URL, conn->conn.fetchDataset());
//            execDataset(URL, conn->conn.put("http://example", data));
//            execDataset(URL, conn->conn.putDataset(dataset));
//            
//            svcExec(URL, "/query",   conn->conn.queryAsk("ASK{}") );
//            svcExec(URL, "/sparql",  conn->conn.queryAsk("ASK{}") );
//            svcExec(URL, "/update",  conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }") ); 
//            svcExec(URL, "/get",     conn->conn.fetch());
//            svcExec(URL, "/data",    conn->conn.fetch());
//            svcExec(URL, "/data",    conn->conn.put(data));
//            svcExec(URL, "/data",    conn->conn.putDataset(dataset));
//            
//            // Fails.
//            svcExecFail(URL, "/get",    (RDFConnection conn)->conn.put(data));
//            svcExecFail(URL, "/query",  (RDFConnection conn)->conn.update("INSERT DATA { <x:s> <x:p> 123 }") );
//            svcExecFail(URL, "/update", (RDFConnection conn)->conn.queryAsk("ASK{}") );
//            
//            svcExecFail(URL, "/doesNotExist", (RDFConnection conn)->conn.queryAsk("ASK{}") );
//            svcExecFail(URL+"2", "", (RDFConnection conn)->conn.queryAsk("ASK{}") );
//
//
//        } catch (Throwable th) {
//            th.printStackTrace();
//        }
//        finally {
//            System.exit(0);
//        }
//    }
//    
//    private static void svcExec(String url, String ep, Consumer<RDFConnection> action) {
//        exec(url, ep, action);
//    }
//    
//    private static void svcExecFail(String url, String ep, Consumer<RDFConnection> action) {
//        execFail(url, ep, action);
//    }
//
//    private static void execDataset(String url, Consumer<RDFConnection> action) {
//        exec(url, null, action);
//    }
//
//    private static void execDatasetFail(String url, Consumer<RDFConnection> action) {
//        execFail(url, null, action);
//    }
//
//    private static void exec(String url, String ep, Consumer<RDFConnection> action) {
//        try {
//            execEx(url, ep, action);
//        } catch (HttpException ex) {
//            printException(ex, ex.getStatusCode(), ex.getMessage());
//        } catch (QueryExceptionHTTP ex) {
//            printException(ex, ex.getStatusCode(), ex.getMessage());
//        }
//    }
//            
//    private static void execEx(String url, String ep, Consumer<RDFConnection> action) {       
//        String dest;
//        if ( ep == null || ep.isEmpty() ) {
//            dest = url;
//        } else {
//            if ( ! url.endsWith("/") )
//                url = url+"/";
//            if ( ep.startsWith("/") )
//                ep = ep.substring(1);
//            dest = url+ep;
//        }
//        try ( RDFConnection conn = RDFConnectionFactory.connect(dest) ) {
//            action.accept(conn);
//        } 
//    }
//    
//    private static void printException(Exception ex, int responseCode, String message) {
//        System.out.flush();
//        System.err.println("**** "+message);
//    }
//
//    private static void execFail(String url, String ep, Consumer<RDFConnection> action) {
//        try {
//            execEx(url, ep, action);
//            System.err.println("Expected an exception");
//        } 
//        catch (HttpException ex) {}
//        catch (QueryExceptionHTTP ex) {}
//    }
//
//    
//    /// ----
//    
//    public static void mainNoName() {
//        FusekiLogging.setLogging();
//        int port = WebLib.choosePort();
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        
//        DataService dSrv1 = new DataService(DatasetGraphFactory.createTxnMem());
//        // Unnamed registration.
//        //dSrv1.addEndpointNoName(Operation.Query);
//        //dSrv1.addEndpoint(Operation.Query, "query");
//        dSrv1.addEndpointNoName(Operation.Update);
//
//        // Stats not updating!
//        
////        dataService.addEndpointNoName(Operation.Query);
////        dataService.addEndpointNoName(Operation.Update);
//
//        try {
//            FusekiServer server = FusekiServer.create()
//                //.verbose(true)
//                .enableStats(true)
//                .enablePing(true)
//                .port(port)
//                .add("/ds", dSrv1)
//                .addOperation("/ds", Operation.Query)
//                //.addServlet("/sparql", new SPARQL_QueryGeneral())
//                .build();
//            server.start();
//            //server.join();
//
//            RDFConnection conn1 = RDFConnectionFactory.connect("http://localhost:" + port + "/ds");
//            conn1.queryAsk("ASK{}");
//            
//            RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/ds");
//            try (conn) {
//                //conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
//                try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
//                    QueryExecUtils.executeQuery(qExec);
//                }
//
//              String x = HttpOp.execHttpGetString("http://localhost:" + port + "/$/stats/ds");
//              //System.out.println(x);
//
//              JsonObject obj = JSON.parse(x);
//              int good = obj.getObj("datasets").getObj("/ds").getNumber("RequestsGood").intValue();
//              int bad  = obj.getObj("datasets").getObj("/ds").getNumber("RequestsBad").intValue();
//
//              System.out.printf("Good=%d, Bad=%d\n", good, bad);
//
//              JsonObject endpoints = obj.get("datasets").getAsObject().get("/ds").getAsObject().get("endpoints").getAsObject();
//              //System.out.println(endpoints);
//              endpoints.keys().forEach(k->{
//                  int g = endpoints.getObj(k).getNumber("RequestsGood").intValue();
//                  int b = endpoints.getObj(k).getNumber("RequestsBad").intValue();
//                  System.out.printf("  %-10s : good=%d bad=%d\n", "'"+k+"'", g, b);
//              });
//            }
//
////            expect403(() -> {
////                RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/ds");
////                try (conn) {
////                    conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
////                    try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
////                        QueryExecUtils.executeQuery(qExec);
////                    }
////                }
////            });
////            expect403(() -> {
////                RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/ds/query");
////                try (conn) {
////                    conn.queryAsk("ASK{}");
////                }
////            });
////            
////            expect403(() -> {
////                String x = HttpOp.execHttpGetString("http://localhost:" + port + "/$/stats");
////                System.out.println(x);
////            });
//
//        } catch (Throwable th) {
//            th.printStackTrace();
//        }
//        finally {
//            System.exit(0);
//        }
//    }
//    
////    public static JsonValue path(JsonObject obj, String ... keys) {
////        for ( int i = 0 ; i < keys.length-1 ; i++ )
////            obj = obj.get(keys[i]).getAsObject();
////        return obj.get(keys[keys.length-1]);
////    }
//    
//    public static void mainAuth() {
//        int port = WebLib.choosePort();
//
//        try {
//            FusekiServer server = FusekiServer.create()
//                //.verbose(true)
//                .enableStats(true)
//                .enablePing(true)
//                .port(port)
//                //.add("/ds", dataService)
//                .parseConfigFile("config-auth.ttl")
//                //.port(3000)
//                //.addServlet("/sparql", new SPARQL_QueryGeneral())
//                .build();
//            server.start();
//            //server.join();
//            
//            AuthSetup auth1 = new AuthSetup("localhost", port, "user1", "pw1", null);
//            AuthSetup auth2 = new AuthSetup("localhost", port, "user2", "pw2", null);
//            AuthSetup auth3 = new AuthSetup("localhost", port, "user3", "pw3", null);
//            
//            expect401(() -> {
//                // No login 401
//                System.out.println("Auth: query - 401");
//                RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/db");
//                try (conn) {
//                    //conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
//                    try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
//                        QueryExecUtils.executeQuery(qExec);
//                    }
//                }
//            });
//            
//            expectOK(()->{
//                System.out.println("Auth: query - 200");
//                LibSec.withAuth("http://localhost:"+port+"/db", auth1, conn -> {
//                    try (conn) {
//                        // Update
//                        conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
//                        // Query
//                        try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
//                            QueryExecUtils.executeQuery(qExec);
//                        }
//                    }});
//            });
//
//            expect403(()->{
//                System.out.println("Auth: query - 403");
//                // No
//                LibSec.withAuth("http://localhost:"+port+"/db", auth3, conn -> {
//                    try (conn) {
//                        try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
//                            QueryExecUtils.executeQuery(qExec);
//                        }
//                    }});
//            });
//        } catch (Throwable th) {
//            th.printStackTrace();
//        }
//        finally {
//            System.exit(0);
//        }
//    }
//    
//    /** Assert that an {@code HttpException} ias an authorization failure.
//     * This is normally 403.  401 indicates no retry with credentials.
//     */
//    public static HttpException assertAuthHttpException(HttpException ex) {
//        int rc = ex.getStatusCode();
//        Assert.assertTrue(rc == HttpSC.FORBIDDEN_403 || rc == HttpSC.UNAUTHORIZED_401 );
//        return ex;
//    }
//    
//    private static void expect403(Runnable runnable) {
//        try {
//            runnable.run();
//            fail("Expected 403");
//        } catch (QueryExceptionHTTP ex) {
//            if ( ex.getStatusCode() == HttpSC.FORBIDDEN_403 )
//                return;
//            throw ex;
//        }
//    }
//    
//    private static void expect401(Runnable runnable) {
//        try {
//            runnable.run();
//            fail("Expected 401");
//        } catch (QueryExceptionHTTP ex) {
//            if ( ex.getStatusCode() == HttpSC.UNAUTHORIZED_401 )
//                return;
//            throw ex;
//        }
//    }
//
//    private static void expectOK(Runnable runnable) {
//            runnable.run();
//    }
//}
