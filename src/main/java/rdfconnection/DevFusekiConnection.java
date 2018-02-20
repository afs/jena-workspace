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

package rdfconnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.embedded.FusekiServer ;
import org.apache.jena.graph.*;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;

public class DevFusekiConnection
{
    static { 
        //LogCtl.setJavaLogging("logging.properties");
        LogCtl.setLog4j() ;
    }
    // Better override for string versions of update() and query() (only one intercept)
    
    // SSE <_:abc> [done]
    // RDFConnection2:query -> need Accept setting. [done]
    
    //  "parse local" (optional) and then use string as given. 
    // Single header for any SPARQL query: RDFConnectionRemoteBuilder.acceptHeaderQuery [done]
    //   Check that Thrift;JSON + ASK -> JSON [done]
    
    // Check all query and update routes for string passing.
    
    // Tests:
    
    
    
    //   Components
    //   - RDF Thrift [done]
    //   - parse, query and update, - check for bNodes.  [done]
    //   - Check all query types
    //     - JSON modes? Extra.
    
    // POST not duplicating 
        
    public static void main(String[] args) throws Exception {
        if ( true ) {
            //FusekiLogging.setLogging();
            //LogCtl.setLog4j() ;
            //LogCtl.setJavaLogging();
        }
        
        int PORT = 3037;

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = 
            FusekiServer.create()
                .setPort(PORT)
                //.setStaticFileBase("/home/afs/ASF/jena-fuseki-cmds/sparqler")
                .add("/ds", dsg)
                //.setVerbose(true)
                .build()
                .start();
        Triple triple = SSE.parseTriple("(:s :p <_:b3456>)");
        // Goes in as URI! (pre this PR)
        Model model = ModelFactory.createDefaultModel();
        model.getGraph().add(triple);
        
        String dsURL = "http://localhost:"+PORT+"/ds" ;
        
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(dsURL);
        try {
            if ( ! isFuseki(dsURL) ) 
                System.out.println("*** Not Fuseki *** (by URL)");
            try (RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build()) {
            //try (RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(), Isolation.COPY)){    
                if ( ! isFuseki(conn) ) 
                    System.out.println("*** Not Fuseki *** (by connection)");
            
                System.out.println("== GSP : PUT-GET");
                conn.put(model);
                conn.put(model);
                fetchPrint(conn);
                // Query-update
                
                // This gets into the dest OK.
                System.out.println("== SPARQL Update");
                conn.update("CLEAR DEFAULT" );
                conn.update("INSERT DATA { <x:s> <x:p> <_:b789> }" );
                System.out.println("Remote data ::");
                fetchPrint(conn);
                System.out.println("== SPARQL Query ::");
                conn.querySelect("SELECT * {?s ?p ?o}", x->print(x.getResource("o")));
                conn.querySelect("SELECT * {?s ?p <_:b789>}", x->print(x.getResource("s")));
                //conn.querySelect("SELECT * {?s ?p <_:b789A>}", x->print(x.getResource("s")));
                System.out.println("== SPARQL Query Ask ::");
                try(QueryExecution qExec = conn.query("ASK {?s ?p <_:b789>}")){
                    boolean bool = qExec.execAsk();
                    System.out.println("  "+bool);
                }
            }
        } finally { server.stop(); } 
    }

    private static void fetchPrint(RDFConnection conn) {
        Model model2 = conn.fetch();
        directPrint(model2);
    }
    
    private static void directPrint(Model model) {
        model.getGraph().find().forEachRemaining(x->print(x.getObject()));
    }
    private static void print(Node n) {
        System.out.println("  _:"+n.getBlankNodeLabel());
    }
    
    private static void print(Resource r) {
        if ( r.isURIResource() )
            System.out.println("  <"+r.asNode().getURI()+">");
        else
            System.out.println("  _:"+r.asNode().getBlankNodeLabel());
    }

    /** Test whether a URL identifies a Fuseki server */  
    public static boolean isFuseki(String datasetURL) {
        HttpOptions request = new HttpOptions(datasetURL);
        HttpClient httpClient = HttpOp.getDefaultHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        return isFuseki(request, httpClient, null);
    }
    
    
    /** Test whether a {@link RDFConnectionRemote} connects to a Fuseki server */  
    public static boolean isFuseki(RDFConnectionRemote connection) {
        HttpOptions request = new HttpOptions(connection.getDestination());
        HttpClient httpClient = connection.getHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        HttpContext httpContext = connection.getHttpContext();
        return isFuseki(request, httpClient, httpContext);
    }

    private static boolean isFuseki(HttpOptions request, HttpClient httpClient, HttpContext httpContext) {
        try {
            HttpResponse response = httpClient.execute(request);
            // Fuseki-Request-ID:
            //String reqId = response.getFirstHeader("Fuseki-Request-ID").getValue();
            // Server:
            String serverIdent = response.getFirstHeader("Server").getValue();
            Log.debug(ARQ.getHttpRequestLogger(), "Server: "+serverIdent);
            boolean isFuseki = serverIdent.startsWith("Apache Jena Fuseki");
            if ( !isFuseki )
                isFuseki = serverIdent.toLowerCase().contains("fuseki");
            return isFuseki; // Maybe
        } catch (IOException ex) {
            throw new HttpException("Failed to check for a Fuseki server", ex);
        }
    }
    private static Node n(String str) { return SSE.parseNode(str) ; }
    
    // Check RDF Thrift round-trips blank nodes. 
    private static void testThrift() {
        Triple t = Triple.create(n(":s"), n(":p"), NodeFactory.createBlankNode("ABCD"));
        Node obj = t.getObject(); 
        Graph graph = Factory.createDefaultGraph();
        graph.add(t);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFDataMgr.write(bout, graph, Lang.RDFTHRIFT);
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        Graph graph1 = Factory.createDefaultGraph();
        RDFDataMgr.read(graph1, bin, Lang.RDFTHRIFT);
        Node obj1 = graph1.find().next().getObject();
        System.exit(0);
    }
}
