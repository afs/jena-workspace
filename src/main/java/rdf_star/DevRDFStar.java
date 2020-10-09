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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.solver.BindingNodeId;
import org.apache.jena.tdb.solver.SolverLib;
import org.apache.jena.tdb.solver.SolverRX;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.vocabulary.RDF;

public class DevRDFStar {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // OpQuad in OpExecutorTDB1

    static Graph data(String dataStr) {
        Graph g = SSE.parseGraph(dataStr);
        g.getPrefixMapping().setNsPrefix("", "http://example/");
        g.getPrefixMapping().setNsPrefix("rdf", RDF.getURI());
        return g;
    }

    public static void main(String...a) {

        String str = "(graph (<< <<:s :p :o>> :r :z>> :q :a) )";
        //String str = "(graph (<<:s :p :o>> :q :z) (:s1 :p1 :o1) )";
        Graph g = data(str);
        Graph g1 = RDFX.encodeRDFStar(g);


        //Reification missing base triple!
        Graph g2 = data(str);
        RDFX.encodeRDFStarInPlace(g2);



        //System.out.println("-- Mismatch");
        RDFDataMgr.write(System.out, g, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-- Encode");
        RDFDataMgr.write(System.out, g1, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-- Encode in place");
        RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS);

//        Graph g3 = RDFX.decodeRDFStar(g);
//        System.out.println("-- Decode");
//        RDFDataMgr.write(System.out, g3, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-----");


        System.exit(0);
        main1();

        String queryString = StrUtils.strjoinNL
            ("PREFIX ex:      <http://example/>"
            ,"PREFIX :        <http://example/ns#>"
            ,"PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
            ,""
            //,"SELECT * { FIND(<< <<?s ?p ?o>> ?q ?z >> AS ?t) }"
            ,"SELECT * { { FIND(<< <<?s ?p ?o>> ?q ?z >> AS ?t) } UNION { <<?s ?p ?o>> ?q ?z } }"
            //,"SELECT ?t { VALUES ?t { << ?var :p :o >> } }" // BAD
            );
//        PF_Find.init();

        Query query1 = QueryFactory.create(queryString);
        Query query2 = QueryFactory.create(queryString);
        System.out.println(query1.equals(query2));

//        System.out.println(query1.getResultVars());
//
//        Op op = Algebra.compile(query);
//        VarFinder vf = VarFinder.process(op);
//        System.out.println(vf);

        arq.qparse.main("-v", "--syntax=arq", "--print=op", "--print=query", queryString);
        System.out.println();
        arq.sparql.main("--syntax=arq", "--data=D.ttl", queryString);
        System.exit(0);
    }

    public static void main1() {
        Triple[] patterns =
            { SSE.parseTriple("(<<?s :p ?o>> :q1 ?z)")
            , SSE.parseTriple("(<<?s :p ?o>> :q ?z)") // Miss
            , SSE.parseTriple("(<< <<?s :p ?o>> :q1 ?z1 >> :q2 ?z2)")
            //, SSE.parseTriple("(?s ?p ?o)")
        };

        for ( Triple pattern : patterns) {
            System.out.println("==== MEM");
            mem(pattern);
            System.out.println("==== TDB1");
            tdb1(pattern);
            System.out.println("==== TDB2");
            tdb2(pattern);
        }
      System.out.println("DONE");
      System.exit(0);
    }

    private static void common(DatasetGraph dsg) {
        Triple t1 = SSE.parseTriple("(:s :p :o)");
        Triple t2 = SSE.parseTriple("(<<:s :p :o>> :q1 :z1)");
        Triple t3 = SSE.parseTriple("(<< <<:s :p :o>> :q1 :z1 >> :q2 :z2)");
        //System.out.println("SETUP");
        Txn.executeWrite(dsg, ()->{
            dsg.getDefaultGraph().add(t1);
            dsg.getDefaultGraph().add(t2);
            dsg.getDefaultGraph().add(t3);
            dsg.getDefaultGraph().add(SSE.parseTriple("(:plain :q :z)"));
        });
    }

    /** Parse a Node in TTL syntax. */
    public static Node ttlNode(String string) {
        Tokenizer tok = TokenizerText.fromString(string);
        PrefixMap pmap = PrefixMapFactory.create(SSE.getPrefixMapRead());
        return ttlNode(tok, pmap);
    }

    /** Parse a Node in TTL syntax. */
    public static Node ttlNode(Tokenizer tok, PrefixMap pmap) {
        if ( ! tok.hasNext() )
            throw new RiotException("String too short");
        Token token = tok.next();
        if ( ! token.hasType(TokenType.LT2) )
        if ( ! token.hasType(TokenType.LT2) )
            return token.asNode(pmap);
        Node s = ttlNode(tok, pmap);
        Node p = ttlNode(tok, pmap);
        Node o = ttlNode(tok, pmap);
        token = tok.peek();
        if ( ! token.hasType(TokenType.GT2) )
            throw new RiotException(String.format("Expected >>, found %s", token.text())) ;
        tok.next();
        return NodeFactory.createTripleNode(s, p, o);
    }

    public static void mem(Triple pattern) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        common(dsg);

        Txn.executeRead(dsg, ()->{
//            RDFDataMgr.write(System.out, dsg, Lang.TRIG);
//            System.out.println("--");
            ExecutionContext execCxt = new ExecutionContext(ARQ.getContext().copy(), dsg.getDefaultGraph(), dsg, null);
            System.out.println("SOLVE "+NodeFmtLib.str(pattern));
            QueryIterator qIter = RX.rdfStarTriple(QueryIterRoot.create(execCxt), pattern, execCxt);
            print(System.out, qIter);
        });
    }

    public static void tdb1(Triple pattern) {
        DatasetGraph dsg = TDBFactory.createDatasetGraph();
        common(dsg);
        Tuple<Node> tuplePattern = TupleFactory.create3(pattern.getSubject(), pattern.getPredicate(),pattern.getObject());
        Txn.executeRead(dsg, ()->{
            ExecutionContext execCxt = new ExecutionContext(ARQ.getContext().copy(), dsg.getDefaultGraph(), dsg, null);
            DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
            System.out.println("SOLVE "+tuplePattern);
            NodeTupleTable ntt = dsgtdb.chooseNodeTupleTable(null);
            Iterator<BindingNodeId> rootIter = Iter.singleton(new BindingNodeId());
            Iterator<BindingNodeId> iter =
                SolverRX.solveRX(ntt, tuplePattern, false, rootIter, /*filter*/null, execCxt);
            Iterator<Binding> iter2 = SolverLib.convertToNodes(iter, ntt.getNodeTable());
            QueryIterator qIter = new QueryIterPlainWrapper(iter2);
            print(System.out, qIter);
        });
    }

    public static void tdb2(Triple pattern) {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        common(dsg);
        Tuple<Node> tuplePattern = TupleFactory.create3(pattern.getSubject(), pattern.getPredicate(),pattern.getObject());
        Txn.executeRead(dsg, ()->{
            ExecutionContext execCxt = new ExecutionContext(ARQ.getContext().copy(), dsg.getDefaultGraph(), dsg, null);
            org.apache.jena.tdb2.store.DatasetGraphTDB dsgtdb = org.apache.jena.tdb2.sys.TDBInternal.getDatasetGraphTDB(dsg);
            System.out.println("SOLVE "+tuplePattern);
            org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable ntt = dsgtdb.chooseNodeTupleTable(null);
            Iterator<org.apache.jena.tdb2.solver.BindingNodeId> rootIter = Iter.singleton(new org.apache.jena.tdb2.solver.BindingNodeId());
            Iterator<org.apache.jena.tdb2.solver.BindingNodeId> iter =
                org.apache.jena.tdb2.solver.SolverRX.solveRX(ntt, tuplePattern, false, rootIter, /*filter*/null, execCxt);
            Iterator<Binding> iter2 = org.apache.jena.tdb2.solver.SolverLib.convertToNodes(iter, ntt.getNodeTable());
            QueryIterator qIter = new QueryIterPlainWrapper(iter2);
            print(System.out, qIter);
        });
    }

    private static QueryIterator print(PrintStream out, QueryIterator qIter) {
        List<Binding> x = Iter.toList(qIter);
        out.println("[");
        boolean first = true;
        for ( Binding b : x) {
            if ( ! first )
                out.println("--");
            DevRDFStar.print(System.out, b);
            first = false;
        }
        out.println("]");
        return new QueryIterPlainWrapper(x.iterator());
    }

    private static void print(PrintStream out, Binding binding) {
        Iterator<Var> vIter = binding.vars();
        while(vIter.hasNext()) {
            Var v = vIter.next();
            if ( v.getName().startsWith("*") )
                continue;
            Node n = binding.get(v);
            out.printf("  %-5s  %s\n", v, NodeFmtLib.str(n));
        }
        //out.println();
    }
}
