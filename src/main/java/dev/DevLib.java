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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.ProgressMonitor ;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.lib.cache.CacheInfo ;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.optimize.Optimize ;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import org.apache.jena.sparql.algebra.optimize.TransformReorder ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.writers.WriterOp ;
import org.apache.jena.sparql.util.QueryExecUtils ;

/** Fragment used in development */
public class DevLib {

    public static void queryExec() {
      String DIR = "/home/afs/ws/jena-1121-perf-regress/" ; 
      Query query = QueryFactory.read(DIR+"Q2.rq") ;
      //Query query = QueryFactory.create("SELECT * { ?s ?p ?o }") ;
      Dataset ds = RDFDataMgr.loadDataset(DIR+"D.ttl") ;
      QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
      QueryExecUtils.executeQuery(qExec);
      System.exit(0);
    }
    
    public static void algebra(String DIR, String queryFile) {
        Query query = QueryFactory.read(DIR+queryFile) ;
        System.out.println(query);
        Op op = Algebra.compile(query) ;
        Op op1 = Algebra.optimize(op) ;
        System.out.println(op1) ;
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
        System.out.println(op) ;
        //Op op1 = Algebra.optimize(op) ;
        Op op1 = Optimize.apply(new TransformFilterPlacement(), op) ;
        System.out.println(op1) ;
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
            System.out.println() ;
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
    
    public static void monitor(ProgressMonitor monitor, Runnable action) {
        if ( monitor != null ) {
            monitor.startMessage(null) ;
            monitor.start() ;
        }
        action.run(); 
        if ( monitor != null ) {
            monitor.finish() ;
            monitor.finishMessage() ;
        }
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
