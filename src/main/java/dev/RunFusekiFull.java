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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunFusekiFull
{
    static { FusekiLogging.setLogging(); }
    static Logger LOG = LoggerFactory.getLogger("APP");

    public static void main(String ... a) {
        mainWebapp();
        //curl --header 'Content-type: text/turtle'  -XPOST --data-binary @config-inf.ttl 'http://localhost:3030/$/datasets'
    }

    public static void mainWebapp() {
        String BASE = "/home/afs/tmp" ;
        //String BASE = "/home/afs/Desktop/JENA-1302";

        // For the UI files.
        String fusekiHome = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-webapp" ;
        String fusekiBase = "/home/afs/tmp/run" ;

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;

        String runArea = Paths.get(fusekiBase).toAbsolutePath().toString() ;
        FileOps.ensureDir(runArea) ;
        FileOps.clearAll(runArea);
//        FusekiCmd.main(
//            //"-v"
//            //,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
//            //"--conf="+BASE+"/config.ttl"
//            //"--conf=/home/agfs/tmp/conf.ttl"
//            "--mem",  "/ds"
//            //"--update", "--file=/home/afs/tmp/D.ttl",  "/ds"
//            //"--update", "--file=/home/afs/tmp/D.trig",  "/ds"
//            //"--mem",  "/ds"
//            //"--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
//            //--loc=/home/afs/tmp/DB", "/ds"
//            ) ;

        ThreadLib.async(()->FusekiCmd.main("--mem", "/ds"));
        Lib.sleep(1500);
        for(;;) {
            try {
                HttpOp.execHttpPost("http://localhost:3030/$/ping", null);
                break;
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                Lib.sleep(500);
            }
        }

        int W = 1_000;
        int N = 3;

        String HOST = "[::1]";
        HOST = "[fe80::8286:f2ff:fecc:c9]";
        try {
            for ( int i = 0 ; i < N ; i++ ) {
                try {
                    String taskId = execSleepTask("http://"+HOST+":3030/", W);
                    LOG.info("Task = "+taskId);
                } catch (Exception ex) {
                    System.err.println("i = "+i+" : "+ex.getMessage());
                    break;
                }
            }
//            String taskId3 = execSleepTask("http://"+HOST+":3030/", N);
//            LOG.info("Task = "+taskId3);
//            String taskId4 = execSleepTask("http://"+HOST+":3030/", N);
//            LOG.info("Task = "+taskId4);

            HttpOp.execHttpPost("http://"+HOST+":3030/$/backup/ds", null);

//            Lib.sleep(500);
//            execTaskList("http://localhost:3030/");
//            Lib.sleep(250);
//            execTaskList("http://localhost:3030/");


            Lib.sleep(3*W+1000);
        } finally {
            System.exit(0);
        }
   }

    private static String execSleepTask(String serveURL, int millis) {
        String url = serveURL+"$/sleep";

        JsonResponseHandler x = new JsonResponseHandler();
        HttpOp.execHttpPost(url+"?interval="+millis, null, WebContent.contentTypeJSON, x);
        JsonValue v = x.getJSON();
        String id = v.getAsObject().get("taskId").getAsString().value();
        return id;
    }

    private static void execTaskList(String serveURL) {
        String url = serveURL+"$/tasks";

        JsonResponseHandler x = new JsonResponseHandler();
        HttpOp.execHttpGet(url, WebContent.contentTypeJSON, x);
        JsonValue v = x.getJSON();
        System.out.println(v);
    }

    static class JsonResponseHandler implements HttpResponseHandler {

        private JsonValue result = null;

        public JsonValue getJSON() {
            return result;
        }

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            Header location = response.getFirstHeader(HttpNames.hLocation);
//            if ( location != null )
//                System.out.printf("Location: %s\n", location.getValue());
            try ( InputStream in = response.getEntity().getContent() ) {
                result = JSON.parseAny(in);
            }
        }
    }
}
