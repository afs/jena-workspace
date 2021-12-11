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
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class DevFuseki {

    public static void main(String...a) {
//        FusekiMainCmd.main("--port=", "--https=certs/https-details", "--mem", "/ds");
//        System.exit(0);

        System.exit(0);
        Graph graph = Factory.createDefaultGraph();
        ExtendedIterator<Triple> x1 = graph.find(null,null,null);
        ExtendedIterator<Triple> x2 = WrappedIterator.create(x1.toList().iterator());
    }

    public static void mainGeneral() {
        try {
            FusekiLogging.setLogging();
            FusekiServer server = FusekiServer.create()
                                              // .jettyServerConfig("jetty.xml")
                                              .add("/ds", DatasetGraphFactory.createTxnMem())
                                              .port(3333)
                                              .build();
            server.start();
            String URL = server.datasetURL("/ds");
            try (RDFConnection conn = RDFConnection.connect(URL)) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK=" + b);
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
