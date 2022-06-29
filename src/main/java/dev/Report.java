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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class Report {
    static {
        try {
            // GeoSPARQL
            //org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        } catch (Throwable th) {}
        // JenaSystem.DEBUG_INIT = true;
        JenaSystem.init();
        //LogCtl.setLog4j2();
        FusekiLogging.setLogging();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    private static final String ns  = "http://example/ns#" ;
    static Graph makeGraph(int size) {
        Graph g = GraphFactory.createDefaultGraph();
        for (int i = 0; i < size; i++) {
            Node r = NodeFactory.createURI(ns + "r" + i);
            Node l = NodeFactory.createURI(ns + "l" + i);
            g.add(r, RDF.type.asNode(), RDFS.Resource.asNode());
            g.add(r, RDFS.seeAlso.asNode(), l);
        }
        return g;
    }

    public static void main(String...args) {
        Query query = QueryFactory.create("ASK { <urn:uuid:1234> <x:p> 123 }");
        //Query query = QueryFactory.create("ASK { <http://^^^/> <x:p> 123 }");
        System.out.println(query);
    }

    private static Symbol TEST = Symbol.create("TEST") ;

    public static void mainServiceCxt(String...args) {
        FusekiLogging.setLogging();
        FusekiServer server = FusekiServer.create()
                .port(0)
                .add("/ds",  DatasetGraphFactory.empty())
                .start();
        try {
            String SERVICE = "http://localhost:"+server.getPort()+"/ds";
            Node expected = NodeFactory.createLiteral("28181", XSDDatatype.XSDinteger);
            String queryString = "SELECT * { SERVICE <"+SERVICE+"> { VALUES ?x { 28181 } } }";
            // Empty context
            Context cxt = new Context();
            //Context cxt = ARQ.getContext().copy();
            RowSet rs = QueryExec.dataset(DatasetGraphFactory.empty())
                    .query(queryString)
                    .context(cxt)
                    .select()
                    .materialize();
            assertTrue(rs.hasNext());
            Node n = rs.next().get("x");
            assertEquals(expected, n);
        } finally { server.stop(); }
    }

    public static void mainSerivceJoin(String...args) {
        String s = """
                SELECT * {
                  SERVICE <https://dbpedia.org/sparql>
                  { SELECT * { ?s a <http://dbpedia.org/ontology/MusicalArtist> } LIMIT 5 }

                  SERVICE <https://dbpedia.org/sparql>
                  { SELECT * { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?x } LIMIT 1 }
                }
                """;

        Query query = QueryFactory.create(s) ;
        joinClassification(query);
        System.out.println();
        System.out.println(query);
        Op op = Algebra.compile(query) ;
        Op op1 = Algebra.optimize(op) ;
        System.out.println(op1) ;
        System.out.println("DONE");
        System.exit(0);
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


    public static void mainGrapMem(String...args) {
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

    public static void mainJSONLD(String...args) {
        String s = """
                PREFIX ex: <http://example/ex#>
                PREFIX : <http://example/>
                :s ex:p 124 ;
                   ex:q ex:o .
                """;

        Graph g = RDFParser.fromString(s).lang(Lang.TTL).toGraph();
        RDFWriter.source(g).lang(Lang.JSONLD11).output(System.out);
    }
}
