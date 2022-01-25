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

package z_archive;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class DevGeoAssembler {

    //http://www.opengis.net/def/crs/EPSG/0/27700

    // Tests
    // Documentation
    //   Example of all defaults.

    static String DIR = "/home/afs/ASF/afs-jena/jena-integration-tests/src/test/files/GeoAssembler/";

    public static void main(String[] args) {
        mainMem();
        System.exit(0);
    }

    public static void mainTDB2() {
        //** Apache SIS j.u.l logging redirection.

        if ( false ) {
            try {
                //Just jul-to-slf4j is not enough
                // Is this an initialization order thing?
                // But this wipes out messages sent on the preconfigured loggers.
                Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
                SLF4JBridgeHandler.removeHandlersForRootLogger();
                SLF4JBridgeHandler.install();
            } catch (ClassNotFoundException ex) {}
        }



        JenaSystem.init();
        FusekiLogging.setLogging();

        //http://www.opengis.net/def/crs/EPSG/0/27700
        FileOps.ensureDir("DB2");
//        FileOps.clearDirectory("DB2");
//        FileOps.delete("spatial.index");
        Dataset ds = TDB2Factory.connectDataset("DB2");
        ds.executeWrite(()->{
            RDFDataMgr.read(ds, DIR+"geosparql_test.rdf");
        });


        FusekiServer server =
            FusekiServer.create()
                //.parseConfigFile("file:GeoAssembler/geo-config.ttl")
                .parseConfigFile("file:"+DIR+"geo-config-ex.ttl")
                .build();
        server.start();

        try {
            String queryStr = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                    + "\n"
                    + "SELECT *\n"
                    + "WHERE{\n"
                    //  "{ <http://example.org/Geometry#PolygonH> ?P ?O } UNION \n"
                    + " {  <http://example.org/Geometry#PolygonH> geo:sfContains ?obj }\n"
                    //+ "    <http://example.org/Geometry#PolygonH> ?P ?obj .\n"
                    + "} ORDER by ?obj";

            RowSet rs = QueryExec.service("http://localhost:3330/ds").query(queryStr).select();
            RowSetOps.out(rs);
            server.stop();
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            System.exit(0);
        }

    }

    public static void mainMem() {

        if ( false ) {
            try {
                //** Apache SIS j.u.l logging redirection.
                //Just jul-to-slf4j is not enough
                // Is this an initialization order thing?
                // But this wipes out messages sent on the preconfigured loggers.
                Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
                SLF4JBridgeHandler.removeHandlersForRootLogger();
                SLF4JBridgeHandler.install();
            } catch (ClassNotFoundException ex) {}
        }

        JenaSystem.init();
        FusekiLogging.setLogging();

        FusekiServer server =
            FusekiServer.create()
                .parseConfigFile("file:"+DIR+"geo-config-mem.ttl")
                .build();
        server.start();

        try {
            String queryStr = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                    + "\n"
                    + "SELECT *\n"
                    + "WHERE{\n"
                    //  "{ <http://example.org/Geometry#PolygonH> ?P ?O } UNION \n"
                    + " {  <http://example.org/Geometry#PolygonH> geo:sfContains ?obj }\n"
                    //+ "    <http://example.org/Geometry#PolygonH> ?P ?obj .\n"
                    + "} ORDER by ?obj";

            RowSet rs = QueryExec.service("http://localhost:3330/dsm").query(queryStr).select();
            RowSetOps.out(rs);
            server.stop();
        } catch (Throwable th) {
            th.printStackTrace();
        } finally { System.exit(0); }
    }

}
