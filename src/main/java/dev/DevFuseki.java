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

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.Params;

public class DevFuseki
{
    public static void main(String ... a) throws UnsupportedEncodingException {
        try {
            main1(a);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {System.exit(0); 
        }
    }
    
    public static void main1(String ... a) throws UnsupportedEncodingException {
        ThreadLib.async(()-> {
            runFuseki();
        });
        Lib.sleep(500);
        
        // Ping until ready.
        String admin = "http://localhost:3030/$/";
        
        for ( int i = 0 ; i < 5 ; i++ ) {
            try { 
                //FusekiLib.isFuseki("http://localhost:3030/ds");
                HttpOp.execHttpGet(admin+"ping");
                break;
            } catch (HttpException ex) {
                //System.err.println(ex.getMessage());
                Lib.sleep(1000);
                //System.err.printf("Try %d\n", i+1);
            }
        }
        
        //org.apache.jena.fuseki.cmds.FusekiBasicCmd.main("--mem", "/ds");
        
        String URL = "http://localhost:3030/ds";
        
        
        //HttpOp.execHttpDelete(admin+"datasets/ds");
        
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(
            Arrays.asList(new Params.Pair("dbName", "ds"), new Params.Pair("dbType", "tdb"))
            );  
        
        HttpOp.execHttpPost(admin+"datasets", e); 

        RDFConnection conn = RDFConnectionFactory.connect(URL);
        conn.update("INSERT DATA { <x:s> <x:p> 123 }");
        conn.querySelect("SELECT (count(*) AS ?C) { ?s ?p ?o }", (qs)->System.out.println(qs));
        
        HttpOp.execHttpDelete(admin+"datasets/ds");
        
        HttpOp.execHttpPost(admin+"datasets", e);
        conn.querySelect("SELECT (count(*) AS ?C) { ?s ?p ?o }", (qs)->System.out.println(qs));

        System.exit(0);
        
    }
    
    
    private static void runFuseki() {
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
