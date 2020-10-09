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

import java.nio.file.Paths;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;

public class DevFuseki
{
    // -- Dispatch

    // Dispatcher.chooseEndpoint, no name maytch.
    //   if 404

    // Old style test in TTestMai

    // ** /ds/MyServlet : interacts with dropping into Jetty's default 404 handler.
    // ** Does database name "/" work?
    // ---

    // JENA-1854
    // ActionLib.parse - throw the RiotException?
    //   SPARQL query parsing

//    Upload.incomingData (GSP and GSP Quads)
//    ActionLib.parse [DONE]
//              ActionDatasets.bodyAsGraph
//              Upload.fileUploadWorker
//              Upload.incomingData.
//              Upload.multipartUploadWorker
//              ActionLib.readFromRequest

//    SPARQL Queries SPARQLQueryProcessor.executeBody – this is OK - the whole string is read before parsing. [DONE]
//    SPARQL Update requests SPARQL_Update.executeBody – may need to read whole string. [DONE]
//        if we are always consuming the input, which not JDI.
//        Ideally, switch depending on presence of Content-Length so large updates stream.

    // ==> UploadDetails -> "was error"

    //static { FusekiLogging.setLogging(); }

    public static void main(String ... a) {
        try {
            //System.setProperty("fuseki.loglogging", "true");
            FusekiLogging.setLogging();

            UserStore userStore = JettyLib.makeUserStore("u", "p");
            SecurityHandler sh = JettyLib.makeSecurityHandler("TripleStore",  userStore, AuthScheme.BASIC);

            FusekiServer server = FusekiServer.create()
                .add("/ds", DatasetGraphFactory.createTxnMem())
                //.verbose(true)
//                .serverAuthPolicy(Auth.ANY_USER)
//                .securityHandler(sh)
                .build();
            server.start();

            String URL = "http://localhost:3330/ds";
            try ( RDFConnection conn = RDFConnectionFactory.connectPW(URL, "u", "p") ) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK="+b);
            }

            //server.join();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally { System.exit(0); }
    }

    private static void runFusekiFull() {
        String BASE = "/home/afs/tmp" ;
        //String BASE = "/home/afs/Desktop/JENA-1302";

        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core" ;
        String fusekiBase = BASE+"/run" ;

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;

        String runArea = Paths.get(fusekiBase).toAbsolutePath().toString() ;
        FileOps.ensureDir(runArea) ;
        FileOps.clearAll(runArea);
        FusekiCmd.main(
            //"-v"
            //,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
            //"--conf="+BASE+"/config.ttl"
            //"--conf=/home/afs/tmp/conf.ttl"
            //, "--mem",  "/ds"
            //"--update", "--file=/home/afs/tmp/D.ttl",  "/ds"
            //"--update", "--file=/home/afs/tmp/D.trig",  "/ds"
            //"--mem",  "/ds"
            //"--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
            //--loc=/home/afs/tmp/DB", "/ds"

            ) ;
    }
}
