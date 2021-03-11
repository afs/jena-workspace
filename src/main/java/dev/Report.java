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

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.*;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.listeners.NullListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.riot.system.*;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sparql.util.graph.GraphListenerBase;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class Report {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        {
            Var var = Var.alloc("p");
            Op op1 = SSE.parseOp("(service <g> (bgp (:s ?p ?o)))");
            Op op2 = new OpProject(op1, Arrays.asList(var));
            Query query = OpAsQuery.asQuery(op2);

            System.out.println(query);
            System.exit(0);
        }



        String qs1 = "PREFIX : <http://example/> select * { bind(:xxx as ?s) ?s :ppp* ?o }";
        String qs2 = "PREFIX : <http://example/> select * { :xxx :ppp* ?o }";
        Query q = QueryFactory.create(qs1);
        Op op = Algebra.compile(q);
        System.out.println(op);
        Dataset ds = DatasetFactory.createTxnMem();
        QueryEngineRef.register();
        QueryExecution qExec = QueryExecutionFactory.create(q, ds);
        QueryExecUtils.executeQuery(qExec);
    }

    public static void mainXMLesc(String...a) {
        String updateString = "PREFIX : <http://example.org/> INSERT DATA { :s :p  'foo\u001Bbar' }" ;
        Dataset ds = DatasetFactory.createTxnMem();
        RDFConnection conn = RDFConnectionFactory.connect(ds);
        conn.update(updateString);

        {
            QueryExecution qExec = conn.query("SELECT ?c WHERE { ?a ?b ?c }");
            ResultsWriter.create().lang(ResultSetLang.SPARQLResultSetXML).write(System.out, qExec.execSelect());
        }
        {
            QueryExecution qExec = conn.query("SELECT ?c WHERE { ?a ?b ?c }");
            ResultsWriter.create().lang(ResultSetLang.SPARQLResultSetJSON).write(System.out, qExec.execSelect());
        }
        {
//        org.apache.jena.rdf.model.impl.Util.substituteEntitiesInElementContent
            try {
                QueryExecution qExec = conn.query("CONSTRUCTWHERE { ?a ?b ?c }");
                RDFDataMgr.write(System.out, qExec.execConstruct(), RDFFormat.RDFXML_PLAIN);
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
                //ex.printStackTrace();
            }
        }
   }

    public static void mainListener(String...a) {
        if ( false ) {
            ModelChangedListener listener = new  NullListener() {
                @Override
                public void addedStatement( Statement s ) {
                    System.out.println(s);
                }
            };
            Dataset dataset = TDB2Factory.createDataset();
            //Dataset dataset = TDBFactory.createDataset();
            dataset.getNamedModel("MY_MODEL").register(listener);
            Triple t1 = SSE.parseTriple("(:s :p 1)");
            Triple t2 = SSE.parseTriple("(:s :p 2)");
            Triple t3 = SSE.parseTriple("(:s :p 3)");

            dataset.begin(TxnType.WRITE);
            System.out.println("1");
            dataset.getNamedModel("MY_MODEL").getGraph().add(t1); // Callback successfully received
//            System.out.println("2");
//            dataset.getNamedModel("TEMP_MODEL").getGraph().add(t2);
            System.out.println("3");
            dataset.getNamedModel("MY_MODEL").getGraph().add(t3); // No Callback received. If comment out previous line then callback is received.
            dataset.abort();
            System.out.println("DONE");
            System.exit(0);
        }

        GraphListener gl = new GraphListenerBase() {
            @Override protected void addEvent(Triple t) { System.out.println("ADD: "+t); }
            @Override protected void deleteEvent(Triple t) { System.out.println("DEL: "+t); }
        };

        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Node gn1 = SSE.parseNode(":gn1");
        Node gn2 = SSE.parseNode(":gn2");
        Graph g = dsg.getGraph(gn1);
        g.getEventManager().register(gl);

        Txn.executeWrite(dsg, ()->{
            Triple t1 = SSE.parseTriple("(:s :p 1)");
            Triple t2 = SSE.parseTriple("(:s :p 2)");
            Triple t3 = SSE.parseTriple("(:s :p 3)");
            System.out.println("1");
            dsg.getGraph(gn1).add(t1); // Callback successfully received
//            System.out.println("2");
//            dsg.getGraph(gn2).add(t2);
            System.out.println("3");
            dsg.getGraph(gn1).add(t3);
        });



        System.exit(0);

        Graph graph = GraphFactory.createGraphMem();
        System.out.println("DONE");
        System.exit(0);
}

    public static void mainAdam(String...a) throws Exception {

        OutputStream out = System.out;

        // Stream writer.
        StreamRDF dest =
            StreamRDFWriter.getWriterStream(out, RDFFormat.TURTLE_BLOCKS);

        Graph graph1 = RDFDataMgr.loadGraph("/home/afs/tmp/D1.ttl");


        // Send prefixes.
        dest.start();
        PrefixMap prefixMap = PrefixMapFactory.create(graph1.getPrefixMapping()) ;
        StreamRDFOps.sendPrefixesToStream(prefixMap, dest);

        StreamRDFOps.sendGraphToStream(graph1, dest, null, null) ;
        StreamRDFOps.sendGraphToStream(graph1, dest, null, null) ;
        dest.finish();
//        // Process models.
//
//        for each model: {
//           // Don't send the prefx map
//           StreamRDFOps.sendGraphToStream(model.getGraph, stream, null, null) ;
//          }

        IO.flush(out);
    }

    private static void parseRule(String str) {
        System.out.println(str);
        try {
            Rule rule = Rule.parseRule(str);
            System.err.flush();
            System.out.println(rule);
        } catch (Rule.ParserException ex) {
            System.err.println(str);
            ex.printStackTrace();
        }
        System.err.flush();
        System.out.flush();
    }

    /*
Thread [main] (Suspended (breakpoint at line 108 in RX))
    RX.matchTripleStar(QueryIterator, Var, Triple, ExecutionContext) line: 108
    RX.preprocessForTripleTerms(QueryIterator, Triple, ExecutionContext) line: 129
    RX.rdfStarTripleSub(QueryIterator, Triple, ExecutionContext) line: 90
    RX.rdfStarTriple(QueryIterator, Triple, ExecutionContext) line: 69
    QueryIterBlockTriplesStar.<init>(QueryIterator, BasicPattern, ExecutionContext) line: 47
    QueryIterBlockTriplesStar.create(QueryIterator, BasicPattern, ExecutionContext) line: 36
     */

    public static void main2(String...a) throws Exception {
        boolean USE_TDB2 = true;
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Graph graph1 = SSE.parseGraph("(prefix ((: <http://example/>)) (graph (<<:s :p :o >> :q :z) ))");
        Graph graph2 = SSE.parseGraph("(prefix ((: <http://example/>)) (graph (<<:s :p :o >> :q :z) ( :s :p :o )))");

        String pattern = "{ <<:s :p :o>> :q :z BIND('A' AS ?A) }";
        String qs = "PREFIX : <http://example/> SELECT * "+pattern;
        Query query = QueryFactory.create(qs);
        Graph graph = graph1;

        if ( USE_TDB2 ) {
            Txn.executeWrite(dsg, ()->{
                GraphUtil.addInto(dsg.getDefaultGraph(), graph);
                QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
                QueryExecUtils.executeQuery(qExec);
            });

        } else {
            QueryExecution qExec = QueryExecutionFactory.create(query, DatasetGraphFactory.wrap(graph));
            QueryExecUtils.executeQuery(qExec);
        }
        System.exit(0);
    }

    public static void mainOnt(String...a) throws Exception {
        var leafDocMgr = new OntDocumentManager();
        leafDocMgr.setFileManager();
        leafDocMgr.setProcessImports(false);
        leafDocMgr.setCacheModels(false);
        var leafModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
        leafModelSpec.setDocumentManager(leafDocMgr);
        // OntModel leafOntModel = ModelFactory.createOntologyModel(leafModelSpec);
        Graph graph = GraphFactory.createDefaultGraph();
        graph = new GraphWrapper(graph) {
            @Override
            public ExtendedIterator<Triple> find(Triple triple) {
                System.out.println("findT(" + triple + ")");
                return super.find(triple);
            }

            @Override
            public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
                System.out.println("findN(" + s + ", " + p + ", " + o + ")");
                return super.find(s, p, o);
            }

            @Override
            public boolean contains(Node s, Node p, Node o) {
                System.out.println("containsN(" + s + ", " + p + ", " + o + ")");
                return super.contains(s, p, o);
            }

            @Override
            public boolean contains(Triple t) {
                System.out.println("containsT(" + t + ")");
                return super.contains(t);
            }
        };

        Model baseModel = ModelFactory.createModelForGraph(graph);
        OntModel wrapperModel = ModelFactory.createOntologyModel(leafModelSpec, baseModel);

        // wrapperModel.listStatements().toList();

        System.out.println("DONE");
        System.exit(0);

        var shapes = Shapes.parse("/home/afs/tmp/D1.ttl", true);

        RDFDataMgr.write(System.out, shapes.getGraph(), Lang.SHACLC);

        ShLib.printShapes(shapes);
        System.exit(0);
        // mainJSONLD();
    }

    public static void mainJSONLD() throws Exception {
        /* { "@context": { "@base": "http://example.org/", "tag:p": { "@type": "@id"
         * } }, "tag:p": [ "foo", 42, false ] } */

        String str = String.join("\n", "{", "  \"@context\": {", "      \"@base\": \"http://example.org/\" ,",
                                 "      \"tag:p1\": { \"@type\": \"@id\" } ,", "      \"tag:p2\": { \"@type\": \"@id\" }", "    }",
                                 " , \"tag:p1\": 41", " , \"tag:p2\": [42]", " , \"tag:q1\": 43", " , \"tag:q2\": [44]", "}");
// String FN = "/home/afs/tmp/D.jsonld";
// InputStream inputStream = new FileInputStream(FN);
// Object jsonObject = JsonUtils.fromInputStream(inputStream);
// Read the file into an Object (The type of this object will be a List, Map, String,
// Boolean,
// Number or null depending on the root object in the file).

        System.out.println(str);
        System.out.println();
        Object jsonObject = JsonUtils.fromString(str);
        RDFDataset x = (RDFDataset)JsonLdProcessor.toRDF(jsonObject);
        @SuppressWarnings("unchecked")
        List<RDFDataset.Quad> quads = (List<RDFDataset.Quad>)x.get("@default");
        quads.stream().map(q -> q.getObject()).forEach(System.out::println);

        // Print out the result (or don't, it's your call!)
        // System.out.println(JsonUtils.toPrettyString(x));

        // riotcmd.riot.main("--base=http://ex/","/home/afs/tmp/D.jsonld");
        System.exit(0);
    }
}
