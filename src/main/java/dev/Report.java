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
import java.util.List;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.util.iterator.ExtendedIterator;

public class Report {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) throws Exception {

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
