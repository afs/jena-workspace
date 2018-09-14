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
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphZero;

public class RunFuseki
{
    public static void main(String ... a) {
        main3();
    }
    
    public static void main1(String ... a) {
        String BASE = "/home/afs/tmp" ;
        //String BASE = "/home/afs/Desktop/JENA-1302";

        // For the UI files.
        String fusekiHome = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-core" ;
        String fusekiBase = "/home/afs/tmp/run" ;

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;

        String runArea = Paths.get(fusekiBase).toAbsolutePath().toString() ;
        FileOps.ensureDir(runArea) ;
        FileOps.clearAll(runArea);
        FusekiCmd.main(
            //"-v"
            //,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
            //"--conf="+BASE+"/config.ttl"
            //"--conf=/home/agfs/tmp/conf.ttl"
            //, "--mem",  "/ds"
            //"--update", "--file=/home/afs/tmp/D.ttl",  "/ds"
            //"--update", "--file=/home/afs/tmp/D.trig",  "/ds"
            //"--mem",  "/ds"
            //"--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
            //--loc=/home/afs/tmp/DB", "/ds"

            ) ;
    }
    
    
    public static void main2(String ... a) {
        FusekiLogging.setLogging(); 
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsgRO = new DatasetGraphZero();
        FusekiServer.create()
             .port(4040)
             .add("/ds", dsg)
             .addServlet("/sparql",  new SPARQL_QueryGeneral())
             // Instead: /empty
             //.add("", new DatasetGraphSink())
             .staticFileBase("/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-basic/sparqler/pages")
             .build()
             .start()
             .join();
        
    }
    public static void main3(String ... a) {
        // --sparqler pages/ == --empty
        org.apache.jena.fuseki.main.cmds.FusekiMainCmd.main(
            "--sparqler=/home/afs/Jena/jena-fuseki2/jena-fuseki-basic/sparqler/pages"
            );
    }

}
