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
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;

public class DevFuseki {

    public static void main(String...a) {
        runFuseki();
    }

    private static void runFuseki() {
        FusekiLogging.setLogging();

        //FusekiServer server = FusekiMain.build("--mem", "/ds");
        //FusekiServer server = FusekiMain.build("--port=0", "--file=/home/afs/tmp/D.ttl", "/ds");
        FusekiServer server = FusekiServer.create()
                .port(0)
                //.verbose(true)
                .add("/ds", DatasetGraphFactory.createTxnMem())
                .add("/ds1", DatasetGraphFactory.createTxnMem(), false)
                .add("/ds2", DatasetGraphFactory.createTxnMem(), true)
                .build();
        server.start();
        try {
            String URL1 = "http://localhost:"+server.getPort()+"/ds1/get";
            String x1 = HttpOp.httpOptions(URL1);
            System.out.println(x1);
            String URL2 = "http://localhost:"+server.getPort()+"/ds2/get";
            String x2 = HttpOp.httpOptions(URL2);
            System.out.println(x2);

            //server.join();

            String URL = server.datasetURL("/ds");
            RowSet rs = QueryExecHTTP.service(URL)
                    //.query("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o} } }")
                    .query("SELECT * { <http://data/s1> a ?T }")
                    .select();
            RowSetOps.out(rs);

        } finally { server.stop(); }
        System.exit(0);

        String HOST = "localhost";
        GSP.service("http://"+HOST+":"+server.getHttpPort()).defaultGraph().GET();
        System.exit(0);
    }

    private static void runFusekiFull() {
        String BASE = "/home/afs/tmp";
        // String BASE = "/home/afs/Desktop/JENA-1302";

        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-webapp";
        String fusekiBase = BASE + "/run";

        System.setProperty("FUSEKI_HOME", fusekiHome);
        System.setProperty("FUSEKI_BASE", fusekiBase);

        String runArea = Path.of(fusekiBase).toAbsolutePath().toString();
        FileOps.ensureDir(runArea);
        FileOps.clearAll(runArea);
        FusekiCmd.main("-v", "--mem", "/ds"

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
