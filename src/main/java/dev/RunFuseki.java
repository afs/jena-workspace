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

package dev;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.ctl.ActionDumpRequest;
import org.apache.jena.fuseki.ctl.ActionMetrics;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;

public class RunFuseki
{
    public static void main(String ... a) throws Exception {
        FusekiLogging.setLogging();
        try {
            mainExternal();
//            mainServer();
//            runClient();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void mainExternal() {
        FusekiMainCmd.main("-v", "--mem", "/ds");
    }

    private static void runClient() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String queryString = "SELECT * { SERVICE <http://localhost:3030/ds> { GRAPH ?g { ?s ?p ?o }} }";
        Query query = QueryFactory.create(queryString);


        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dsg) ) {
            QueryExecUtils.executeQuery(qExec);
        }
    }

    public static void mainServer(String ... a) {
        //org.apache.jena.fuseki.ctl.ActionMetrics

        //FusekiMainCmd.main("--passwd=/home/afs/tmp/passwd", "--auth=basic", "--mem", "/ds");


        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(SSE.parseQuad("(:g :s :p :o)"));

        FusekiServer server = FusekiServer.create()
            .passwordFile("/home/afs/tmp/passwd")
            .auth(AuthScheme.BASIC)

//            .enableMetrics(true)
//            .parseConfigFile("/home/afs/ASF/Examples/config-1-mem.ttl")
//            .parseConfigFile("/home/afs/ASF/Examples/config-2-mem-old.ttl")

            //** Authorization is the "and" of named services.

            .add("/ds", dsg)
//            //.addOperation("/ds", Operation.Shacl)
//            .addEndpoint("/ds", "shacl", Operation.Shacl)

            .port(3030)
            //.verbose(true)
            //.staticFileBase("Files")
            .build();
        server.start();



//        try { server.start().join(); }
//        finally { server.stop(); }
    }

    public static void mainServerRunExit(String ... a) {
        FusekiServer server = FusekiServer.create()
            .add("/ds", DatasetGraphFactory.createTxnMem())
            .port(3030)
            .verbose(true)
            .addServlet("/$/metrics", new ActionMetrics())
            .build();
        server.start();

        String str = HttpOp.execHttpGetString("http://localhost:3030/$/metrics");
        System.out.println(str);
        System.exit(0);


        //RDFConnection c = RDFConnectionFactory.connectFuseki("http://localhost:3030/ds");
        RDFConnection c =  RDFConnectionFuseki.create()
            .destination("http://localhost:3030/ds").build();

        Model m = RDFDataMgr.loadModel("/home/afs/tmp/D.ttl");
        c.put(m);
        c.put(m);

        RDFDataMgr.write(System.out, c.fetchDataset(), Lang.TRIG);

//        server.stop();
        System.out.println("DONE");
        System.exit(0);
    }


    public static void mainFusekiText() {
        FileOps.ensureDir("Lucene");
        //FileOps.clearAll("Lucene");
        // Convert to build programmatically
        FusekiServer server = FusekiServer.create()
            //.add("/ds", DatasetGraphFactory.createTxnMem())
            .parseConfigFile("fuseki-text-config.ttl")
            .port(3333)
            .build()
            .start();

        // TextIndexLucene.deleteEntity
//        if (docDef.getUidField() == null)
//            return;

        String P = StrUtils.strjoinNL(
            "PREFIX text: <http://jena.apache.org/text#>"
            , "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
            ,""
            );

        // Use it.
        String data = P+"INSERT DATA { <x:s1> rdfs:label 'dog cat' . <x:s2> rdfs:label 'cat'}";
        String qs = P+"SELECT * { (?s ?score ?lit ) text:query 'cat' . }";
        String qs1 = P+"SELECT * { (?s ?score ?lit ) text:query '*:*' . }";
        String qs2 = P+"SELECT * { ?s ?p ?o }";


        try ( RDFConnection conn = RDFConnectionFactory.connectFuseki("http://localhost:3333/ds") ) {
            for ( int i = 0 ; i < 5 ; i++ ) {
                conn.delete();
                conn.update(data);
                queryExec(conn, qs);
            }
//            queryExec(conn, qs1);
            conn.delete();
            conn.update(data);
            queryExec(conn, qs);
//            conn.put("D.ttl");
//            queryExec(conn, qs2);
//            queryExec(conn, qs);
//            conn.put("D.ttl");
//            queryExec(conn, qs);
        }
        finally { server.stop(); }
        System.out.println("DONE");
        System.exit(1);
    }

    private static void queryExec(RDFConnection conn, String queryString) {
        Query query = QueryFactory.create(queryString);
        try ( QueryExecution qExec = conn.query(queryString) ) {
            ResultSetFormatter.out(qExec.execSelect(), query.getPrologue());
        }
    }

    public static void mainWebapp() {
        RunFusekiFull.mainWebapp();
    }

    public static void mainSparqler() {
        // --sparqler pages/ == --empty
//        FusekiMainCmd.main(
//            "--sparqler=/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-main/sparqler/pages"
//            );

        FusekiLogging.setLogging();
        FusekiServer.create()
            .addServlet("/dump", new ActionDumpRequest())
            // -- SPARQLer.
            .addServlet("/sparql",  new SPARQL_QueryGeneral())
            .staticFileBase("/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-main/sparqler/pages")
            .addServlet("/validate/query",  new QueryValidator())
            .addServlet("/validate/update", new UpdateValidator())
            .addServlet("/validate/iri",    new IRIValidator())
            .addServlet("/validate/data",   new DataValidator())
            // -- SPARQLer.
            .build().start().join();
    }

}
