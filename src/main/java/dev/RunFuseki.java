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
import org.apache.jena.fuseki.ctl.ActionDumpRequest;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class RunFuseki
{
    public static void main(String ... a) {
        //System.setProperty("fuseki.loglogging", "true");
        FusekiLogging.setLogging();
//        org.apache.jena.fuseki.main.cmds.FusekiMainCmd.main("--mem", "/ds");
//        System.exit(0);

        mainFusekiText();
        mainServer();
    }

    public static void mainServer(String ... a) {
        FusekiServer server = FusekiServer.create()
            //.parseConfigFile("config.ttl")
            .add("/ds", DatasetGraphFactory.createTxnMem())
            .port(3030)
            //.verbose(true)
            //.staticFileBase("Files")
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
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
