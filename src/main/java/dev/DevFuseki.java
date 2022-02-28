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

import java.nio.file.Path;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class DevFuseki {

    public static void main(String...a) {

        mainGeneral();
    }

    public static void mainGeneral() {
        try {
            // What about "/" and "/sparql"?

            /* cases:
             *   /dataset
             *   /dataset/sparql
             *   /dataset/does-not-exist
             *
             *   /
             *   /sparql
             *   /does-not-exist
             *
             *   /path/dataset
             *   /path/dataset/sparql
             *   /path/dataset/does-not-exist
             *
             *   /path1/path2/dataset
             *   /path1/path2/dataset/sparql
             *   /path1/path2/dataset/does-not-exist
             */

            // TestDispatch: request URI + registry -> result.

            //String ROOT = "/path/dataset";
            String ROOT = "/";
            //String ROOT = "/sparql";
            DataService dataService = DataService
                    .newBuilder(DatasetGraphFactory.createTxnMem())
                    .addEndpoint(Operation.Query)
                    .addEndpoint(Operation.Query, "query2")
                    .build();

            FusekiLogging.setLogging();
            FusekiServer server = FusekiServer.create()
                                              // .jettyServerConfig("jetty.xml")
                                              //.add("/ds", DatasetGraphFactory.createTxnMem())
                    .add(ROOT, dataService)
                    .port(3333)
                    .build();
            server.start();

            //String URL = "http://localhost:"+server.getHttpPort()+ROOT;
            String URL = "http://localhost:"+server.getHttpPort()+ROOT+"query2";

            try (RDFConnection conn = RDFConnection.connect(URL)) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("\nASK=" + b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }


    private static void runFusekiFull() {
        String BASE = "/home/afs/tmp";
        // String BASE = "/home/afs/Desktop/JENA-1302";

        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core";
        String fusekiBase = BASE + "/run";

        System.setProperty("FUSEKI_HOME", fusekiHome);
        System.setProperty("FUSEKI_BASE", fusekiBase);

        String runArea = Path.of(fusekiBase).toAbsolutePath().toString();
        FileOps.ensureDir(runArea);
        FileOps.clearAll(runArea);
        FusekiCmd.main(
        // "-v"
        // ,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
        // "--conf="+BASE+"/config.ttl"
        // "--conf=/home/afs/tmp/conf.ttl"
        // , "--mem", "/ds"
        // "--update", "--file=/home/afs/tmp/D.ttl", "/ds"
        // "--update", "--file=/home/afs/tmp/D.trig", "/ds"
        // "--mem", "/ds"
        // "--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
        // --loc=/home/afs/tmp/DB", "/ds"

        );
    }
}
