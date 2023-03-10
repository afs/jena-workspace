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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.*;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfxml.xmlinput.RDFXMLReader;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

public class Report {
    static {
//        // GeoSPARQL
//        try {
//            org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
//        } catch (Throwable th) {}
        // JenaSystem.DEBUG_INIT = true;
        LogCtl.setLog4j2();
        //FusekiLogging.setLogging();
        //LogCtl.setLevel("org.apache.jena.fuseki.Server", "WARN");
        JenaSystem.init();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...args) {
        System.out.println("DONE");
        System.exit(0);
    }

    public static void mainPath1616(String...args) {
        // Simple version of PathLib - no clever stuff about start points.

        Graph g = SSE.parseGraph("""
                (graph
                    (:s :p 123)
                    (:s :p 456)
                    (:s :q "def")
                    )""");

        DatasetGraph dsg = DatasetGraphFactory.wrap(g);

        String qs = """
                PREFIX : <http://example/>
                SELECT * { ?s :p{0} ?o }
                """;
        Query query = QueryFactory.create(qs);
        Op op = Algebra.compile(query);

        // ----
        //ARQ.getContext().set(ARQ.optPathFlatten, false);
        if ( false ) {
            Op op1a = Algebra.optimize(op);
            System.out.println(op1a);
            System.out.println("==");
            System.out.println();

            with(()->ARQ.getContext().set(ARQ.optPathFlatten, false),
                 ()->ARQ.getContext().set(ARQ.optPathFlatten, true),
                 ()-> {
                     Op op1b = Algebra.optimize(op);
                     System.out.println(op1b);
                     System.out.println("==");
                 });
        }

        // ----

        RowSet rowset = QueryExec.graph(g).query(query)
                .set(ARQ.optPathFlatten, false)
                //.set(ARQ.optPathFlattenExtended, true)
                .select();
        RowSetOps.out(rowset);
        // -- ref eval
        if ( false ) {
            QueryEngineMain.unregister();

            QueryEngineRef.register();
            RowSet rowsetRef = QueryExec.graph(g).query(query)
                    //.set(ARQ.optPathFlatten, false)
                    //.set(ARQ.optPathFlattenExtended, true)
                    .select();
            RowSetOps.out(rowsetRef);
        }
        System.exit(0);
    }

    private static void with(Runnable before, Runnable after, Runnable action) {
        before.run();
        try {
            action.run();
        } finally {
            after.run();
        }
    }

    public static void mainGlob() {
        dwinGlob("abc*", "ABCD");
        dwinGlob("abc*", "abcd");
        dwinGlob("ab*c", "abcd");
        dwinGlob("ab*cd", "abcd");
        dwinGlob("ab*cd", "abxxxcd");
        System.exit(0);
    }

    public static void dwinGlob(String pattern, String str) {
        MatchingEngine m = GlobPattern.compile(pattern);
        System.out.printf("'%s' '%s' -> %s\n", pattern, str, m.matches(str));
        System.out.printf("'%s' '%s' => %s\n", pattern, str, FilenameUtils.wildcardMatch(str,  pattern));
    }

    public static void mainParse(String...args) {

        String s = """
        <rdf:RDF
          xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
          xmlns:j.0="http://jena.hpl.hp.com/test#" >
          <rdf:Description rdf:nodeID="A0">
           <j.0:startTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">2022-09-28T21:31:31Z</j.0:startTime>
          </rdf:Description>
        </rdf:RDF>
        """;
        Model m = ModelFactory.createDefaultModel();
        // null base URI
        StringReader sr = new StringReader(s);
        RDFXMLReader reader = new RDFXMLReader();
        reader.read(m, sr, null );
        System.exit(0);

        FusekiLogging.setLogging();
        FusekiServer server = FusekiServer.construct("-v", "--mem", "/ds").start();
        try {
            QueryExec qExec = QueryExecHTTP
                    .service("http://localhost:"+server.getPort()+"/ds")
                    .queryString("SELECT * { GRAPH ?g {} }")
                    .param("default-graph-uri", "http://example/graph")
                    .param("named-graph-uri",   "http://example/namedGraph")
                    .build();
            RowSet rs = qExec.select();
            RowSetOps.out(rs);
        } finally {
            server.stop();
        }
        System.exit(0);
    }

    public static void mainExplain() {

        FusekiServer server = FusekiServer.construct("--mem", "/ds").start();

        //ARQ.symLogExec = level or true/false.

        ARQ.getContext().set(ARQ.symLogExec, true);
        LogCtl.enable("org.apache.jena.jena.arq.exec");

        try {
            String URL = "http://localhost:"+server.getPort()+"/ds/sparql";
            RowSet rs = QueryExecHTTP.service(URL).queryString("SELECT (BOUND(?c) AS ?X ) {  }").select();
            RowSetOps.out(rs);
        } catch (Throwable th ) {
            th.printStackTrace();
        } finally { System.exit(0); }

//        Context cxt1 = new Context();
//        cxt1.set(ARQ.optReorderBGP, "TRUE");
//        Object x1 = cxt1.get(ARQ.optReorderBGP);
//        System.err.println(x1);
//        System.err.println(cxt1.isTrueOrUndef(ARQ.optReorderBGP));
        System.exit(0);
    }

    public static void mainModule(String...args) {
        ArgModuleGeneral aMod = new ArgModuleGeneral() {
            private ArgDecl aDecl1 = new ArgDecl(false, "TEST");
            private ArgDecl aDecl2 = new ArgDecl(true, "VALUE");

            @Override
            public void processArgs(CmdArgModule cmdLine) {
                if ( cmdLine.contains(aDecl1) )
                    System.out.println("aDecl1:"+aDecl1.getKeyName()+"="+cmdLine.getValue(aDecl1));
                if ( cmdLine.contains(aDecl2) )
                    System.out.println("aDecl2:"+aDecl2.getKeyName()+"="+cmdLine.getValue(aDecl2));
            }

            @Override
            public void registerWith(CmdGeneral cmdLine) {
                cmdLine.add(aDecl1);
                cmdLine.add(aDecl2);
                cmdLine.getUsage().addUsage("TEST", "The test arg");
                cmdLine.getUsage().addUsage("VALUE=", "The value arg");
            }
        };

        FusekiModule fmod = new FusekiModule() {
            @Override public String name() { return "Arguments"; }
            //@Override public void start() { org.apache.jena.fuseki.main.cmds.FusekiMain.addArgModule(aMod); }
            @Override public void start() { throw new RuntimeException("Arguments"); }
        };

        FusekiModules.add(fmod);

        //FusekiMain.addArgModule(aMod);
        //FusekiMain.addArgs(new String[] {});

        FusekiMainCmd.main("--TEST", "--VALUE", "123", "--mem", "/ds");
        System.exit(0);

        // FusekiMain.addArgs.

        // FusekiModule command line hackery.
        // Module -> command line modifier object

        // Before -> Extra map entries.
        // After -> access to
        // Before execute
        // After execute (args)

        // 1 - Add to front.
        // 2 - Add separate section
        //FusekiModuleStep.setupCommandLine();
    }

    static interface CmdLineLifecycle {
        public ArgModule extensionModule();
        public void processModule();
    }

    public static void mainFuseki(String...args) {
        try {
            FusekiServer server = FusekiMain.build("--conf", "/home/afs/tmp/opt-none/conf.ttl").start();
            String URL = "http://localhost:"+server.getPort()+"/dataset/sparql";
            RowSet rs = QueryExecHTTP.service(URL).queryString("SELECT * { ?s ?p ?o }").select();
            RowSetOps.out(rs);
        } catch (Throwable th ) {
            th.printStackTrace();
        } finally { System.exit(0); }
    }


    public static void mainHTTP(String...args) {
        String x  =
                """
            GET /hello.txt HTTP/1.1\r
            User-Agent: curl/7.16.3 libcurl/7.16.3 OpenSSL/0.9.7l zlib/1.2.3\r
            Host: www.example.com\r
            Accept-Language: en, mi\r
            \r\n\r
            """;
        String[] lines = x.split("\r\n");

        Map<String, String> headers = new HashMap<>();
        for ( String line : lines ) {
            int idx = line.indexOf(':');
            if ( idx != -1 ) {
                String h = line.substring(0,idx).trim();
                String v = line.substring(idx+1).trim();
//                System.out.println(h+" :: "+v);
//                System.out.println();
                headers.put(h,v);
            }
        }
        System.out.println(headers);
    }

    public static void joinClassification(Query query) {
        Op op = Algebra.compile(query);
        System.out.println(op);
        boolean b = JoinClassifier.print ;
        JoinClassifier.print = true;
        Op op1 = Transformer.transform(new TransformJoinStrategy(), op);
        JoinClassifier.print = b;
        System.out.println(op1);
    }

    public static void mainGraphMem(String...args) {
        Graph g = Factory.createDefaultGraph();
        String DATA = "/home/afs/Datasets/BSBM/bsbm-50m.nt.gz";
        int N = 50_000_000;

        System.out.println("Start...");
        long before = memory();
        long z = Timer.time(()->{
            StreamRDF dest = StreamRDFLib.graph(g);
            AsyncParser.asyncParse(DATA, dest);
        });
        Runtime.getRuntime().gc() ;
        long after = memory();
        long memoryUsed = after - before;

        double seconds = (z/1000.0);
        System.out.printf("Load time  = %,.3f s\n", seconds);
        System.out.printf("Load rate  = %,.3f TPS\n", N/seconds);
        System.out.printf("Load space = %,.0fM \n", memoryUsed/(1_000_000.0));
    }

    public static long memory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
