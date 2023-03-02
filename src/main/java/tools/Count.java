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

package tools;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.sys.DatabaseConnection;

public class Count {
    static { LogCtl.setLog4j2(); }

    // Lesson: with a hot node cache, getting nodes + making triples is expensive.
    // Vary node cache size.
    // Reset TDB caches.??

    // These are cache-hot figures!
    // Overhead is elsewhere.

    static String TIM_database = "bsbm-5m.trdf.gz";
    static String TDB1_database = "DB1";
    static String TDB2_database = "DB2";
    static private int N = 5 ;

    static void println() {
        }

    static void println(String x) {
        }

    static void printf(String x, Object...args) {
        System.out.printf(x, args);
    }

    public static void main(String[] args) {
        JenaSystem.init();

        boolean runTDB1 = false;
        boolean runTDB2 = true;
        boolean runTIM = false;

        if ( runTDB1 ) {
            Dataset dataset = makeTDB1();
            runAll("TDB1",  makeTDB1());
            run("Tuple count (tdb1)", N, dataset, (i,ds)->count_tuple1(i, ds));
            println();
        }

        if ( runTDB2 ) {
            Dataset dataset = makeTDB2();
            runAll("TDB2", dataset);
            run("Tuple count (tdb2)", N, dataset, (i,ds)->count_tuple2(i, ds));
            println();
        }

        if ( runTIM ) {
            Dataset dataset = makeTIM();
            runAll("TIM", dataset);
            // Does not apply to TIM. run("Tuple count (tim)", N, dataset, (i,ds)->count_tupleX(i, ds));
            println();
        }
    }

    private static void runAll(String label, Dataset dataset) {
        println("** Database: "+label);

        run("Graph.size()", N, dataset,  (i,ds)->graph_size(i, ds));

        run("Find count", N, dataset,  (i,ds)->count_find(i, ds));

        run("SPARQL count(*)", N, dataset,  (i,ds)->sparql_count_star(i, ds));

        run("SPARQL */materialize", N, dataset,  (i,ds)->sparql_star_consume(i, ds));

        //run("SPARQL */list-size", dataset,  (i,ds)->sparql_star_list_size(i, ds));

        run("SPARQL count(var)", N, dataset,  (i,ds)->sparql_count_var(i, ds));

        run("SPARQL count(materialize)", N, dataset,  (i,ds)->sparql_count_materialize(i, ds));
    }

    private static void run(String label, int N, Dataset ds, BiFunction<Integer, Dataset, Long> r) {
        println();
        println(label);

        long total = 0 ;
        for( int i = 0 ; i < N ; i++ ) {
            total += r.apply(i, ds);
        }
        printf("  Average %.3fs\n", (total/N)/1000.0);

    }

    static boolean verbose() { return true; }

    public static Dataset makeTDB1() {
        return TDBFactory.createDataset(TDB1_database);
    }

    public static Dataset makeTDB2() {
        org.apache.jena.tdb2.params.StoreParams params = org.apache.jena.tdb2.params.StoreParams.getDftStoreParams();
            org.apache.jena.tdb2.params.StoreParams.builder(null, params)
                .nodeId2NodeCacheSize(2*params.getNodeId2NodeCacheSize())
                .build();
        DatabaseConnection conn = DatabaseConnection.connectCreate(Location.create(TDB2_database), params, null);
        return DatasetFactory.wrap(conn.getDatasetGraph());
        //return TDB2Factory.connectDataset(TDB2_database);
    }

    public static Dataset makeTIM() {
        println("Database - TIM");
        Timer timer = new Timer();
        Dataset ds = DatasetFactory.createTxnMem();
        timer.startTimer();
        RDFDataMgr.read(ds, TIM_database);
        long x = timer.endTimer();
        printf("Database - TIM (%.3fs)\n", x/1000.0);
        return ds ;
    }

    public static long count_scan(int i, Dataset ds) {
        long start = System.currentTimeMillis();
        // Materialises nodes.
        //long x = Txn.calculateRead(ds, ()->Iter.count(ds.asDatasetGraph().find()));

        DatasetGraph dsg = ds.asDatasetGraph();
        Graph g = dsg.getDefaultGraph();
        long x = Txn.calculateRead(dsg, ()->g.size());

        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    public static long time(Dataset txn, Supplier<Long> action) {
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(txn, ()->action.get());
        long finish = System.currentTimeMillis();
        return (finish-start);
    }

    public static long graph_size(int i, Dataset ds) {
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->ds.getDefaultModel().size());
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    public static long count_find(int i, Dataset ds) {
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->Iter.count(ds.asDatasetGraph().find()));
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    private static long count_tuple1(int i, Dataset ds) {
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            org.apache.jena.tdb.store.DatasetGraphTDB dsgtdb = org.apache.jena.tdb.sys.TDBInternal.getDatasetGraphTDB(ds);
            return dsgtdb.getTripleTable().getNodeTupleTable().size();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    private static long count_tuple2(int i, Dataset ds) {
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            org.apache.jena.tdb2.store.DatasetGraphTDB dsgtdb = org.apache.jena.tdb2.sys.TDBInternal.getDatasetGraphTDB(ds);
            return dsgtdb.getTripleTable().getNodeTupleTable().size();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }


    public static long sparql_count_star(int i, Dataset ds) {
        String qs = "SELECT (count(*) AS ?c) { ?s ?p ?o }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            return rs.next().get("c").asLiteral().getLong();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    public static long sparql_star_consume(int i, Dataset ds) {
        String qs = "SELECT * { ?s ?p ?o }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            return ResultSetFormatter.consume(rs);
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    public static long sparql_star_list_size(int i, Dataset ds) {
        String qs = "SELECT * { ?s ?p ?o }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            List<QuerySolution> results = ResultSetFormatter.toList(rs);
            return results.size();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    // SPARQL count, no materialization.
    public static long sparql_count_var(int i, Dataset ds) {
        String qs = "SELECT (count(?s) AS ?c) (count(?p) AS ?c2) (count(?o) AS ?c3)  { ?s ?p ?o }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            return rs.next().get("c").asLiteral().getLong();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    // SPARQL count, forcing materialization.
    public static long sparql_count_materialize(int i, Dataset ds) {
        //String qs = "SELECT (count(*) AS ?c) { BIND(BNODE() AS ?B) ?s ?p ?o . FILTER(?s != ?B && ?p != ?B && ?o != ?B) }";
        String qs = "SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s = ?s && ?p = ?p && ?o = ?o) ) }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            return rs.next().get("c").asLiteral().getLong();
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

    // SPARQL count, forcing materialization.
    public static long sparql_count_materialize_X(int i, Dataset ds) {
        //String qs = "SELECT (count(*) AS ?c) { BIND(BNODE() AS ?B) ?s ?p ?o . FILTER(?s != ?B && ?p != ?B && ?o != ?B) }";
        //String qs = "SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s != true && ?p != true && ?o != true) ) }";
        String qs = "SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s = ?s && ?p = ?p && ?o = ?o) ) }";
        if ( i == 0 )
            println("  "+qs);
        Query query = QueryFactory.create(qs);
        long start = System.currentTimeMillis();
        long x = Txn.calculateRead(ds, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(query, ds);
            ResultSet rs = qExec.execSelect();
            ResultSetFormatter.out(rs);
            return 0;
        });
        long finish = System.currentTimeMillis();
        double t = (finish-start)/1000.0;
        if ( verbose() )
            printf("[%d] Count = %,d in %.3fs\n", i, x, t);
        return (finish-start);
    }

}
