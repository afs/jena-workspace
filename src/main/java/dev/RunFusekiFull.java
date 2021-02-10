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

import static org.apache.jena.fuseki.mgt.ServerMgtConst.opCompact;
import static org.apache.jena.riot.web.HttpOp.execHttpPost;

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
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.ctl.JsonConstCtl;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunFusekiFull
{
    static { FusekiLogging.setLogging();
        LogCtl.enable(Fuseki.requestLog);
    }
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

        // FusekiCmd.main("--loc=/home/afs/tmp/DB", "/ds");

        if ( false ) {
            // Text dataset.
            ThreadLib.async(()->
            FusekiCmd.main("--conf=/home/afs/ASF/afs-jena/jena-fuseki2/examples/config-text-tdb2.ttl")
                );
            Lib.sleep(5000);
            // Compact
            JsonResponseHandler x = new JsonResponseHandler();
            execHttpPost("http://localhost:3030/$/" + opCompact + "/dataset", null, WebContent.contentTypeJSON, x);
            JsonValue v = x.getJSON();
            String id = v.getAsObject().getString(JsonConstCtl.taskId);
            Lib.sleep(5000);
            System.exit(0);
        }

        int port = WebLib.choosePort();
        ThreadLib.async(()->
            FusekiCmd.main(
                "--port="+port,
                //"-v"
                //,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
                //"--conf="+BASE+"/config.ttl"
                //"--conf=/home/agfs/tmp/conf.ttl"
                "--mem",  "/ds"
                //"--update", "--file=/home/afs/tmp/D.ttl",  "/ds"
                //"--update", "--file=/home/afs/tmp/D.trig",  "/ds"
                //"--mem",  "/ds"
                //"--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
                //--loc=/home/afs/tmp/DB", "/ds"
                ));

        Lib.sleep(2000);
        String x = HttpOp.execHttpGetString("http://localhost:"+port+"/$/datasets/ds");
        System.exit(0);
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
