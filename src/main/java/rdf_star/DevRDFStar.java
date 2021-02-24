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

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.sparql.engine.iterator.RX_SA;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.solver.BindingNodeId;
import org.apache.jena.tdb2.solver.SolverLib;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.sys.TDBInternal;

public class DevRDFStar {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {

//        Node n = SSE.parseNode("<< <<:s :p :o>> ?P 123>>");
//        String str = FmtUtils.stringForNode(n, SSE.getPrefixMapRead());
//        System.out.println(str);
//        System.exit(0);

        arq.qexpr.main("TRIPLE(:s, :p, 1+2)");
        //arq.qexpr.main("<<:s :p 1>>");

        System.out.println();
        String s = StrUtils.strjoinNL
                ("PREFIX : <http://example/>"
                ,"SELECT * { "
                ,"    BIND(123 as ?X)"
                ,"    BIND(triple(:s, :p, ?X+1) as ?Y2)"
                ,"}");
        arq.qparse.main("--print=op", "--print=query", "--print=opt", s);
        arq.sparql.main(s);

        System.exit(0);

        // <<>> in expressions
        //   ExprVistors
        //     ExprNoOpVarsWorker
        //     ExprVarsWorker
        //     ApplyTransformVisitor

        // Expr . equalsBySyntax
        // Rename.RenameAnyVars - has to go deep.

        // Rewrite a triple();
        // Prints as:
        // BIND(fnTriple(:s, :p, ?X) AS ?Y)

        // Second time its a NodeValue
        //   Exr parsing of SSE.

        // ==== TDB2
        // Tidy TDB2_dev
        // [x] Use only if there is <<?>>
        // [ ] Code tidy. SolverLib
        // [ ] ReorderTransform.
        // ** [ ] Filter and any graph. ==> basic "solve"
        // [ ] StageMatchTuple -> becomes functions / TupleMatcher.
        // [ ] SolverRX_SA: Pattern tuple and node+triple
        // [ ] Process [RDF-star]

        // ==== TDB1
        // Same.

        // ===
        // Update Turtle parsers
        // Update N-triples, NQ Quads
        //   What is LangNTuple.
        // ===
        // Update SPARQL parsers for exact grammar.



        boolean USE_TDB2_DIRECT = false;
        boolean USE_TDB2_QUERY = true;

        /* ******** */
        RX.MODE_SA = true;
        /* ******** */

        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();

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
        String pattern = "{ ?s ?p ?o {| :q ?z |} }";
        String qs = "PREFIX : <http://example/> SELECT * "+pattern;
        Query query = QueryFactory.create(qs);
        Graph graph = graph2;

        Txn.executeWrite(dsg, ()->{
//          GraphUtil.addInto(dsg.getDefaultGraph(), terms);
//          GraphUtil.deleteFrom(terms, dsg.getDefaultGraph());
            GraphUtil.addInto(dsg.getDefaultGraph(), graph);
//          RDFDataMgr.write(System.out,  dsg,  Lang.TRIG);
//          System.out.println();
            });

//      // In-memory
        {
            System.out.println("== In-memory");
            QueryExecution qExec = QueryExecutionFactory.create(query, DatasetGraphFactory.wrap(graph));
            QueryExecUtils.executeQuery(qExec);
        }

        if ( USE_TDB2_QUERY ) {
            System.out.println("== TDB2/Query");
            Txn.executeRead(dsg, ()->{
                QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
                QueryExecUtils.executeQuery(qExec);
            });
        }


        if ( USE_TDB2_DIRECT ) {
            Txn.executeRead(dsg, ()->{
                NodeTable nodeTable = TDBInternal.getDatasetGraphTDB(dsg).getTripleTable().getNodeTupleTable().getNodeTable();

                ExecutionContext execCxt = new ExecutionContext(ARQ.getContext(), dsg.getDefaultGraph(), dsg, null);
                Iterator<BindingNodeId> chain = Iter.singleton(new BindingNodeId());

                //**//
                Triple tPattern = SSE.parseTriple("(?s :q ?z)");
                // Must have a <<>> terms.
                Iterator<BindingNodeId> out = TDB2_dev.stepOne(chain, Quad.defaultGraphIRI, tPattern, nodeTable, execCxt);
                //**//
                if ( out == null )
                    System.out.println("null");
                else {
                    //out = Iter.log(out);

//                    // Binding (!!) has the real results.
//                    BindingNodeId bn = out.next();
//                    Binding b = SolverLib.convToBinding(bn, nodeTable);
//                    Var var_s = Var.alloc("s");
//                    b.get(var_s);

                    Iterator<Binding> iter =
                            SolverLib.convertToNodes(out, nodeTable);
                    if ( iter.hasNext() )
                        iter.forEachRemaining(System.out::println);
                    else
                        System.out.println("Empty");
                }


            });
        }

    }

    public static void main1(String...a) {
        // To/From NodeId space.
        //SolverRX.

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RX.MODE_SA = false;
        RDFDataMgr.read(dsg, "/home/afs/tmp/D.ttl");
        String PREFIXES = "PREFIX : <http://example/>\n";
        String qs = PREFIXES+"SELECT * { <<:s ?p ?o >> :q :z . BIND('A' AS ?A) }";
        Query query = QueryFactory.create(qs);
        System.out.println("==== Query 1 ====");
        QueryExecution qExec1 = QueryExecutionFactory.create(query, dsg);
        QueryExecUtils.executeQuery(qExec1);

        System.out.println("==== Query 2 ====");
        RX.MODE_SA = true;
        QueryExecution qExec2 = QueryExecutionFactory.create(query, dsg);
        QueryExecUtils.executeQuery(qExec2);


        System.exit(0);

        ARQConstants.getGlobalPrefixMap().setNsPrefix("ex", "http://example/");

//        dwim(":x", "?v");
//        dwim("<< :s :p :o>>", "?v");
//        dwim("<< :s :p :o>>", "<< ?s ?p ?o>>");
//        dwim("<< <<:x :q :z>> :p :o>>", "<< ?s ?p ?o>>");
//        dwim("<< <<:x :q :z>> :p :o>>", "<< <<?x ?q ?z>> ?p ?o>>");

//        dwim("<< :s :p :o>>", "<< ?s :q ?o>>");
//        dwim("<< :s :p :o>>", "<< ?x ?p ?x>>");

//        dwim("<< <<:x :q :z>> :p :z>>", "<< <<?x ?q ?z>> ?p ?z>>");

        dwim("<< <<:x1 :q :z1>> :p  <<:x2 :q :z2>> >>", "<< ?s :p <<?s2 ?q ?o2>> >>");

    }

    private static void dwim(String dataStr, String patternStr) {
        Node data = SSE.parseNode(dataStr);
        Node pattern = SSE.parseNode(patternStr);
        Binding r = RX_SA.match(BindingFactory.root(), data, pattern);
        //r.vars().forEachRemaining(v->
        System.out.println(NodeFmtLib.displayStr(data));
        System.out.println(NodeFmtLib.displayStr(pattern));
        System.out.println("    "+r);
    }
}
