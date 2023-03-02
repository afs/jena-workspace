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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.lib.cache.CacheInfo ;
import org.apache.jena.graph.Node;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.optimize.Optimize ;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.algebra.optimize.TransformReorder ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.LeftJoinClassifier;
import org.apache.jena.sparql.engine.main.VarFinder;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.writers.WriterOp ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.QueryExecUtils ;

/** Fragment used in development */
public class DevLib {

    public static void queryExec() {
      String DIR = "/home/afs/tmp/" ;
      Query query = QueryFactory.read(DIR+"Q.rq") ;
      //Query query = QueryFactory.create("SELECT * { ?s ?p ?o }") ;
      Dataset ds = RDFDataMgr.loadDataset(DIR+"D.ttl") ;
      QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
      QueryExecUtils.executeQuery(qExec);
      System.exit(0);
    }

    public static void transform() {
        Query query = QueryFactory.create("PREFIX : <http://example/> SELECT * { ?s ?p ?o . ?x ?q :o. ?s :key :value . }") ;
        Op op = Algebra.compile(query) ;
        Op op1 = Transformer.transform(new TransformReorder(), op) ;
        WriterOp.output(IndentedWriter.stdout, op1, query.getPrefixMapping());
        IndentedWriter.stdout.flush() ;
    }

    public static void transformOp(String inputStr) {
        Op op = SSE.parseOp(inputStr) ;
        //Op op1 = Algebra.optimize(op) ;
        Op op1 = Optimize.apply(new TransformFilterPlacement(), op) ;
        }

    public static long time(Runnable action) {
        Timer t = new Timer() ;
        t.startTimer();
        action.run();
        return t.endTimer() ;
    }

    public static void timePrint(Runnable action) {
        timePrint(null, action) ;
    }

    public static void timePrint(String label, Runnable action) {
        long z = time(action) ;
        if ( label != null ) {
            System.out.print(label) ;
            System.out.print(" : ") ;
            System.out.flush() ;
        }
        System.out.printf("Time=%.2fs\n", z/1000.0) ;
    }

    public static long memory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static long space(Runnable action) {
        Runtime.getRuntime().gc() ;
        long before = memory();
        action.run();
        Runtime.getRuntime().gc() ;
        long after = memory();
        return after-before ;
    }

    public static void printReport(String label, ActionReport report) {
        if ( label != null ) {
            System.out.print(label) ;
            System.out.print(" : ") ;
            }
        //System.out.printf("  Space=%7.2f MB  Time=%5.2fs\n", report.spaceUsed/(1000*1000.0), report.timeUsed/1000.0) ;
        System.out.printf("  Space=%.2f MB\n", report.spaceUsed/(1000*1000.0)) ;
        System.out.printf("  Time=%.2fs\n",  report.timeUsed/1000.0) ;
    }

    public static ActionReport spaceTime(String label, Runnable action) {
        Timer t = new Timer() ;
        gc() ;
        long before = memory();
        t.startTimer();
        action.run();
        long time = t.endTimer() ;
        gc() ;
        long after = memory();
        long mem = after-before ;
        return new ActionReport(label, mem, time) ;
    }

    public static void run(String queryFile, String dataFile) {
        Query q = QueryFactory.read(queryFile) ;
        Dataset ds = RDFDataMgr.loadDataset(dataFile) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        QueryExecUtils.executeQuery(q, qExec);
    }

    public static String filename(String dir, String file) {
        if ( dir.endsWith("/") )
            return dir+file ;
        else
            return dir+"/"+file ;
    }

    public static void algebra(String DIR, String queryFile) {
        algebra(filename(DIR, queryFile));
    }

    public static void varfind(String z) {
        Op op = SSE.parseOp(z);
        System.out.println(op);
        VarFinder vf = VarFinder.process(op);
        System.out.println(vf);
    }

    public static Op algebra(String queryFile) {
        Query query = QueryFactory.read(queryFile) ;
        System.out.println(query);
        Op op = Algebra.compile(query) ;
        Op op1 = Algebra.optimize(op) ;
        System.out.println(op1) ;
        return op1 ;
    }

    public static void joinClassification(String... args) {
        String qs = StrUtils.strjoinNL(args);
        Query query = QueryFactory.create(qs);
        Op op = Algebra.compile(query);

        System.out.println(op);
        if ( op instanceof OpJoin ) {
            JoinClassifier.print = true ;
            boolean b = JoinClassifier.isLinear((OpJoin)op);
            System.out.println("Join linear: "+b);
        } else if (op instanceof OpLeftJoin ) {
            LeftJoinClassifier.print = true ;
            boolean b2 = LeftJoinClassifier.isLinear((OpLeftJoin)op);
            System.out.println("LeftJoin linear: "+b2);
        } else
            System.out.println("Not a join Join");
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

    public static Query transform(Query query, QuerySolutionMap qsm) {
        Map<Var, Node> map = new HashMap<>();
        qsm.asMap().forEach((vstr, rdfnode) -> map.put(Var.alloc(vstr), rdfnode.asNode()));
        return QueryTransformOps.transform(query, map);
    }

    public static void spacePrint(Runnable action) {
        spacePrint(null, action) ;
    }

    public static void spacePrint(String label, Runnable action) {
        long z = space(action) ;
        if ( label != null ) {
            System.out.print(label) ;
            System.out.print(" : ") ;
            System.out.flush() ;
        }
        System.out.printf("Space=%.2f MB\n", z/(1000*1000.0)) ;
    }

    public static void gc() {
        Runtime.getRuntime().gc() ;
    }

    public static class ActionReport {
        public final long spaceUsed ;
        public final long timeUsed ;
        public final String label;
        public CacheInfo stats = null ;

        public ActionReport(String label, long spaceUsed, long timeUsed) {
            this.label = label ;
            this.spaceUsed = spaceUsed ;
            this.timeUsed = timeUsed ;
        }
    }


}
