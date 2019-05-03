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

import org.apache.jena.fuseki.ctl.ActionDumpRequest;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.util.QueryExecUtils;

public class RunFuseki
{
    public static void main(String ... a) {
        String DIR = "/home/afs/tmp/InfTDB2/";
        FusekiLogging.setLogging();
        mainServerClient();
    }

    public static void mainServer(String ... a) {
        FusekiLogging.setLogging();
        FusekiServer server = FusekiServer.create()
//            .add("/other", DatasetGraphFactory.createTxnMem())
            .parseConfigFile("/home/afs/tmp/config.ttl")
            
            //.passwordFile("/home/afs/tmp/passwd")
            
            .port(3030)
            .verbose(true)
            .build();
        try { server.start().join(); }
        finally { server.stop(); }
    }
    
    public static void mainServerClient() {
        FusekiLogging.setLogging();
        FusekiServer server = FusekiServer.create()
            .parseConfigFile("/home/afs/tmp/config.ttl")
            .port(3030)
            .build()
            .start();
        
        // Use it.
        try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds") ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        } finally {
            server.stop();
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
