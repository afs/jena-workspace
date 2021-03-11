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

package rdf_star;

import java.util.function.Predicate;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphPlain;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.solver.BindingNodeId;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.SystemTDB;

public class DevRDFStar {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
//      runFile();
//      runInline();
//      System.exit(0);
        //runDataAccess();
        runInline();
        System.exit(0);
    }

    public static void runInline(String...a) {

        Dataset dataset = TDB2Factory.createDataset();

        Predicate<Tuple<NodeId>> filter = tuple -> false;
        DatasetGraph dsgtdb2 = dataset.asDatasetGraph();
        dsgtdb2.getContext().set(SystemTDB.symTupleFilter, filter);

        String data = StrUtils.strjoinNL("(dataset"
                                        ,"  (_ <<:a :b :c>> :p 'abc')"
                                        ,")");
        String qs = "SELECT * { <<?a ?b ?c>> ?p 'abc'}";
        Query query = QueryFactory.create(qs);

        Txn.executeWrite(dataset, ()->{
            DatasetGraph dsg = SSE.parseDatasetGraph(data);
            dataset.asDatasetGraph().addAll(dsg);
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
            //qExec.getContext().set(TDB2.symUnionDefaultGraph, true);
            QueryExecUtils.executeQuery(qExec);
        });
    }

    public static void runFile() {
        //Dataset dataset = TDB2Factory.createDataset();
        Dataset dataset = DatasetFactory.create();

//        String DIR = "/home/afs/W3C/rdf-star/tests/sparql/eval/";
//        String DATA = DIR+"data-4.trig";
//        String QUERY = DIR+"sparql-star-graphs-1.rq";

        String DIR = "/home/afs/ASF/afs-jena/jena-arq/testing/ARQ/ExprBuiltIns";
        String DATA = DIR+"/data-builtin-2.ttl";
        String QUERY = DIR+"/q-lang-3.rq"; // Dataset general only?
        Query query = QueryFactory.read(QUERY);
        System.out.println(query);

        Txn.executeWrite(dataset, ()->{

            Graph plain = GraphPlain.plain();
            RDFDataMgr.read(plain, DATA);
            Dataset dataset2 = DatasetFactory.wrap(DatasetGraphFactory.wrap(plain));

            //RDFDataMgr.read(dataset, DATA);
            //Dataset dataset2 = dataset;
            //GraphPlain
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset2);
            QueryExecUtils.executeQuery(qExec);
        });
        arq.sparql.main("--data="+DATA, "--query="+QUERY);
        System.exit(0);
    }

    public static void mainEngine(String...a) {
        boolean USE_TDB2_QUERY = true;
        boolean USE_TDB1_QUERY = true;

        // Temp - force term into the node table.

        String dataStr =
        StrUtils.strjoinNL("(prefix ((: <http://example/>))",
                           "   (graph",
                           "     (:s :p :o )",
                           "     (:s1 :p1 :o1 )",
                           "     (:s2 :p2 :o2 )",
                           "))");

        Graph terms = SSE.parseGraph(dataStr);

        Graph graph1 = SSE.parseGraph("(prefix ((: <http://example/>)) (graph (<<:s :p :o >> :q :z) ))");
        Graph graph2 = SSE.parseGraph("(prefix ((: <http://example/>)) (graph (<<:s0 :p0 :o0 >> :q :z) (<<:s :p :o >> :q :z) ( :s :p :o ) ( :s1 :p1 :o1 ) ))");

        graph1.getPrefixMapping().setNsPrefix("", "http://example/");
        graph2.getPrefixMapping().setNsPrefix("", "http://example/");

        //String pattern = "{ <<:s ?p :o>> ?q :z . BIND('A' AS ?A) }";
        // BIND(<<?s ?p ?o>> AS ?T)
        //String pattern = "{ ?s ?p ?o {| :q ?z |} }";

        String pattern = "{ <<:s :p ?o >> ?q ?z . }";
        String qs = "PREFIX : <http://example/> SELECT * "+pattern;
        Query query = QueryFactory.create(qs);
        Graph graph = graph1;

//      // In-memory
        if ( false )
        {
            // TEST
            System.out.println("== Basic test");
            Triple tData     = SSE.parseTriple("(<<:s :p :o >> :q :z)");
            Triple tPattern  = SSE.parseTriple("(<<:s ?p ?o >> ?q ?z)");

            //Binding input = BindingFactory.binding(Var.alloc("p"), SSE.parseNode(":pz"));
            Binding input = BindingFactory.binding();
            Binding b1 = RX.matchTriple(input, tData, tPattern);
            System.out.println(b1);
            Quad qData = SSE.parseQuad("(:g <<:s :p :o >> :q :z)");
            Node qGraph = SSE.parseNode(":g");
            Triple qPattern = tPattern;
            Binding b2 = RX.matchQuad(input, qData, qGraph, qPattern);
            System.out.println(b2);
            System.exit(0);
        }

        {
            System.out.println("== In-memory");
            QueryExecution qExec = QueryExecutionFactory.create(query, DatasetGraphFactory.wrap(graph));
            QueryExecUtils.executeQuery(qExec);
        }

        if ( USE_TDB2_QUERY ) {
            System.out.println("== TDB2/Query");
            Predicate<BindingNodeId> filter = bnid -> true;

            DatasetGraph dsgtdb2 = DatabaseMgr.createDatasetGraph();
            dsgtdb2.getContext().set(SystemTDB.symTupleFilter, filter);

            Txn.executeWrite(dsgtdb2, ()->GraphUtil.addInto(dsgtdb2.getDefaultGraph(), graph));
            Txn.executeRead(dsgtdb2, ()->{
                QueryExecution qExec = QueryExecutionFactory.create(query, dsgtdb2);
                QueryExecUtils.executeQuery(qExec);
            });
        }

        if ( USE_TDB1_QUERY ) {
            System.out.println("== TDB1/Query");
            try {
                DatasetGraph dsgtdb1 = TDBFactory.createDatasetGraph();
                Txn.executeWrite(dsgtdb1, ()-> GraphUtil.addInto(dsgtdb1.getDefaultGraph(), graph));
                Txn.executeRead(dsgtdb1, ()->{
                    QueryExecution qExec = QueryExecutionFactory.create(query, dsgtdb1);
                    QueryExecUtils.executeQuery(qExec);
                });
            } catch (NotImplemented ex) {
                System.out.println("Exception: "+ex.getClass().getName()+" : "+ex.getMessage());
            }
        }


    }
    // [RDFx]
//    public static void main1(String...a) {
//        // To/From NodeId space.
//        //SolverRX.
//
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        RX.MODE_SA = false;
//        RDFDataMgr.read(dsg, "/home/afs/tmp/D.ttl");
//        String PREFIXES = "PREFIX : <http://example/>\n";
//        String qs = PREFIXES+"SELECT * { <<:s ?p ?o >> :q :z . BIND('A' AS ?A) }";
//        Query query = QueryFactory.create(qs);
//        System.out.println("==== Query 1 ====");
//        QueryExecution qExec1 = QueryExecutionFactory.create(query, dsg);
//        QueryExecUtils.executeQuery(qExec1);
//
//        System.out.println("==== Query 2 ====");
//        RX.MODE_SA = true;
//        QueryExecution qExec2 = QueryExecutionFactory.create(query, dsg);
//        QueryExecUtils.executeQuery(qExec2);
//
//
//        System.exit(0);
//
//        ARQConstants.getGlobalPrefixMap().setNsPrefix("ex", "http://example/");
//
////        dwim(":x", "?v");
////        dwim("<< :s :p :o>>", "?v");
////        dwim("<< :s :p :o>>", "<< ?s ?p ?o>>");
////        dwim("<< <<:x :q :z>> :p :o>>", "<< ?s ?p ?o>>");
////        dwim("<< <<:x :q :z>> :p :o>>", "<< <<?x ?q ?z>> ?p ?o>>");
//
////        dwim("<< :s :p :o>>", "<< ?s :q ?o>>");
////        dwim("<< :s :p :o>>", "<< ?x ?p ?x>>");
//
////        dwim("<< <<:x :q :z>> :p :z>>", "<< <<?x ?q ?z>> ?p ?z>>");
//
//        dwim("<< <<:x1 :q :z1>> :p  <<:x2 :q :z2>> >>", "<< ?s :p <<?s2 ?q ?o2>> >>");
//
//    }
//
//    private static void dwim(String dataStr, String patternStr) {
//        Node data = SSE.parseNode(dataStr);
//        Node pattern = SSE.parseNode(patternStr);
//        Binding r = RX_SA.match(BindingFactory.root(), data, pattern);
//        //r.vars().forEachRemaining(v->
//        System.out.println(NodeFmtLib.displayStr(data));
//        System.out.println(NodeFmtLib.displayStr(pattern));
//        System.out.println("    "+r);
//    }
}
