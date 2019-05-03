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
import org.apache.jena.fuseki.system.FusekiLogging;

public class RunFusekiFull
{

    public static void main(String ... a) {
        FusekiLogging.setLogging();
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
}
