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

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunFusekiFull
{
    static {
        //System.setProperty("fuseki.loglogging", "true");
        FusekiLogging.setLogging();
        //LogCtl.enable(Fuseki.requestLog);
    }
    static Logger LOG = LoggerFactory.getLogger("APP");

    // RunFusekiFull
    // [x] - not defaulting to read-only.
    //     - switch at templates stage. WebappDSGSetup.setup(+update flag)
    // [ ] Document in ANN
    // [ ] Website : fuseki-security.md
    // [x] Github issue
    // [x] --localhost => update
    // [ ] Drop legacy
    // [ ] Test --desc. (check for service)
    // [ ] Start up tests (shareable with main?)
    // [ ] Make consistent with Fuseki Main.

    // Main and ModAdmin.


    public static void main(String ... a) {

        // Action datasets - read-only versions
        //   Including plain file.


        plainRun();
        //mainWebapp();
        //curl --header 'Content-type: text/turtle'  -XPOST --data-binary @config-inf.ttl 'http://localhost:3030/$/datasets'
    }

    public static void plainRun() {
        String fusekiHome = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-webapp" ;
        String fusekiBase = "/home/afs/tmp/Fuseki/run" ;

        IOX.deleteAll(fusekiBase);
        FileOps.ensureDir(fusekiBase);

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;
        //FusekiCmd.main("--update", "--tdb2", "--loc=/home/afs/tmp/DB2", "/ds");
        FusekiCmd.main("--mem", "/db");

        System.exit(0);
    }

    public static void mainWebapp() {
        String BASE = "/home/afs/tmp" ;
        //String BASE = "/home/afs/Desktop/JENA-1302";

        // For the UI files.
        String fusekiHome = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-webapp" ;
        String fusekiBase = "/home/afs/tmp/run" ;

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;

        String runArea = Path.of(fusekiBase).toAbsolutePath().toString() ;
        FileOps.ensureDir(runArea) ;
        FileOps.clearAll(runArea);

        int port = 3030; //WebLib.choosePort();
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

        try ( RDFConnection conn = RDFConnection.connect("http://localhost:"+port+"/ds") ) {
            String queryString = "CONSTRUCT WHERE { ?s ?p ?o}";
            Model m = conn.queryConstruct(queryString);
            RDFDataMgr.write(System.out, m, Lang.TTL);

//            String queryString = "SELECT * { ?s ?p ?o }";
//            conn.queryResultSet(queryString, rs->ResultSetFormatter.out(rs));

        }

        System.exit(0);
   }
}
