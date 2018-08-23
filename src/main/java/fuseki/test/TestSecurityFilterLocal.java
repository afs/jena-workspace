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

package fuseki.test;

import static fuseki.test.GraphData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fuseki.security.DataAccessCtl;
import fuseki.security.SecurityPolicy;
import fuseki.security.SecurityRegistry;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSecurityFilterLocal {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        Creator<DatasetGraph> c1 = TDBFactory::createDatasetGraph;
        Object[] obj1 = { "TDB", c1};
        Creator<DatasetGraph> c2 = DatabaseMgr::createDatasetGraph;
        Object[] obj2 = { "TDB2", c2 };
        return Arrays.asList(obj1, obj2);
    }
    
    private DatasetGraph testdsg;
    private SecurityRegistry reg = new SecurityRegistry();
    
    public TestSecurityFilterLocal(String name, Creator<DatasetGraph> source) {
        testdsg = source.create();
        fill(testdsg);
        reg.put("userNone", SecurityPolicy.NONE);
        reg.put("userDft", SecurityPolicy.DFT_GRAPH);
        reg.put("user0", new SecurityPolicy(Quad.defaultGraphIRI.getURI()));
        reg.put("user1", new SecurityPolicy("http://test/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityPolicy("http://test/g1", "http://test/g2", "http://test/g3"));
        DataAccessCtl.controlledDataset(testdsg, reg);
        //testdsg = DataAccessCtl.wrapControlledDataset(testdsg, reg);
    }
    
    private static void assertSeen(Set<Node> visible, Node ... expected) {
        Set<Node> expectedNodes = new HashSet<>(Arrays.asList(expected));
        assertEquals(expectedNodes, visible);
    }

    private static String queryAll        = "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
    private static String queryDft        = "SELECT * { ?s ?p ?o }";
    private static String queryNamed      = "SELECT * { GRAPH ?g { ?s ?p ?o } }";

    private static String queryG2         = "SELECT * { GRAPH <http://test/graph2> { ?s ?p ?o } }";
    private static String queryGraphNames = "SELECT * { GRAPH ?g { } }";

    private Set<Node> subjects(DatasetGraph dsg, String queryString, Consumer<QueryExecution> modifier) {
        Dataset ds = DatasetFactory.wrap(dsg);
        return
            Txn.calculateRead(ds, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryString, ds)) {
                    if ( modifier != null )
                        modifier.accept(qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream()
                        .map(qs->qs.get("s"))
                        .filter(Objects::nonNull)
                        .map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }
    
    private Set<Node> graphs(DatasetGraph dsg, Consumer<QueryExecution> modifier) {
        Dataset ds = DatasetFactory.wrap(dsg);
        return
            Txn.calculateRead(ds, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryGraphNames, ds)) {
                    if ( modifier != null )
                        modifier.accept(qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream().map(qs->qs.get("g")).filter(Objects::nonNull).map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }

    @Test public void filter_setup() {
        Set<Node> visible = subjects(testdsg, queryAll, null);
        assertEquals(5, visible.size());
        assertSeen(visible, s0, s1, s2, s3, s4);
    }

    private static Consumer<QueryExecution> qExecAddFiler(DatasetGraph dsg, SecurityPolicy sCxt) {
        return qExec->sCxt.filterTDB(dsg, qExec);
    }

    // QueryExecution
    private void filter_user(String user, Node ... expected) {
        SecurityPolicy sCxt = reg.get(user);
        Set<Node> visible = subjects(testdsg, queryAll, qExecAddFiler(testdsg, sCxt));
        assertSeen(visible, expected);
    }

    @Test public void filter_userNone() {
        filter_user("userNone");
    }

    @Test public void filter_userDft() {
        filter_user("userDft", s0);
    }

    @Test public void filter_user0() {
        filter_user("user0", s0);
    }

    @Test public void filter_user1() {
        filter_user("user1", s0, s1);
    }

    @Test public void filter_user2() {
        filter_user("user2", s1, s2, s3);
    }

    @Test public void filter_userX() {
        filter_user("userX");
    }

    // "Access Denied"
    @Test public void no_access_user1() {
        SecurityPolicy sCxt = reg.get("user1");
        Set<Node> visible = subjects(testdsg, queryG2, qExecAddFiler(testdsg, sCxt));
        assertTrue(visible.isEmpty());
    }

    @Test public void graph_names_userNone() {
        SecurityPolicy sCxt = reg.get("userNone");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible);
    }
    
    @Test public void graph_names_userDft() {
        SecurityPolicy sCxt = reg.get("userDft");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible);
    }
    
    @Test public void graph_names_user0() {
        SecurityPolicy sCxt = reg.get("user0");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible);
    }
    
    @Test public void graph_names_user1() {
        SecurityPolicy sCxt = reg.get("user1");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible, g1);
    }

    @Test public void graph_names_user2() {
        SecurityPolicy sCxt = reg.get("user2");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible, g1, g2, g3);
    }

    @Test public void graph_names_userX() {
        SecurityPolicy sCxt = reg.get("userX");
        Set<Node> visible = graphs(testdsg, qExecAddFiler(testdsg, sCxt)); 
        assertSeen(visible);
    }

    // QueryExecution w/ Union default graph
    private void filter_union_user(String user, Node ... expected) {
        SecurityPolicy sCxt = reg.get(user);
        Consumer<QueryExecution> modifier = qExec-> {
            qExec.getContext().set(TDB.symUnionDefaultGraph, true);
            qExec.getContext().set(TDB2.symUnionDefaultGraph, true);    // Not strictly necessary.
            sCxt.filterTDB(testdsg, qExec); 
        };
        Set<Node> visible = subjects(testdsg, queryDft, modifier);
        assertSeen(visible, expected);
    }
    
    @Test public void filter_union_userNone() {
        filter_union_user("userNone");
    }
    
    @Test public void filter_union_userDft() {
        // Storage default graph not visible with a union query.
        filter_union_user("userDft");
    }

    @Test public void filter_union_user0() {
        // Storage default graph not visible with a union query.
        filter_union_user("user0");
    }
    
    @Test public void filter_union_user1() {
        filter_union_user("user1", s1);
    }
    
    @Test public void filter_union_user2() {
        filter_union_user("user2", s1, s2, s3);
    }
    
    @Test public void filter_union_userX() {
        filter_union_user("userX");
    }
    
    private Set<Node> subjects(Graph graph, String queryString, Consumer<QueryExecution> modifier) {
        Model model = ModelFactory.createModelForGraph(graph);
        return
            Txn.calculateRead(testdsg, ()->{
                try(QueryExecution qExec = QueryExecutionFactory.create(queryString, model)) {
                    if ( modifier != null )
                        modifier.accept(qExec);
                    List<QuerySolution> results = Iter.toList(qExec.execSelect());
                    Stream<Node> stream = results.stream().map(qs->qs.get("s")).filter(Objects::nonNull).map(RDFNode::asNode);
                    return SetUtils.toSet(stream);
                }
            });
    }

    // Graph/Model
    @Test public void query_model_userNone() {
        query_model_user(testdsg.getDefaultGraph(), "userNone");
    }
    
    @Test public void query_model_userDft() {
        query_model_user(testdsg.getDefaultGraph(), "userDft", s0);
    }

    @Test public void query_model_user0() {
        query_model_user(testdsg.getDefaultGraph(), "user0", s0);
    }

    @Test public void query_model_user1() {
        query_model_user(testdsg.getDefaultGraph(), "user1", s0);
    }

    @Test public void query_model_user2() {
        query_model_user(testdsg.getDefaultGraph(), "user2");
    }

    @Test public void query_model_ng_userNone() {
        query_model_user(testdsg.getGraph(g1), "userNone");
    }

    @Test public void query_model_ng_user11() {
        query_model_user(testdsg.getGraph(g1), "user1", s1);
    }

    @Test public void query_model_ng_user21() {
        query_model_user(testdsg.getGraph(g1), "user2", s1);
    }

    @Test public void query_model_ng_user12() {
        query_model_user(testdsg.getGraph(g2), "user1");
    }

    @Test public void query_model_ng_user22() {
        query_model_user(testdsg.getGraph(g2), "user2", s2);
    }
    
    @Test public void query_model_userXa() {
        query_model_user(testdsg.getDefaultGraph(), "userX");
    }

    @Test public void query_model_userXb() {
        query_model_user(testdsg.getGraph(g1), "userX");
    }

    private void query_model_user(Graph g, String user, Node ... expected) {
        SecurityPolicy sCxt = reg.get(user);
        Set<Node> visible = subjects(g, queryDft, qExecAddFiler(testdsg, sCxt));
        assertSeen(visible, expected);
    }
}
