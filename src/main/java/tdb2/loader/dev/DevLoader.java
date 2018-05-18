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

package tdb2.loader.dev;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.IOX;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.loader.Loader;
import tdb2.loader.LoaderFactory;
import tdb2.loader.base.*;
import tdb2.loader.parallel.DataBatcher;
import tdb2.loader.parallel.DataToTuples;
import tdb2.loader.parallel.DataToTuplesInline;
import tdb2.loader.parallel.Destination;

public class DevLoader {
    
    static { JenaSystem.init(); }
    
    public static void main(String[] args) {
        String DATA = "/home/afs/Datasets/BSBM/bsbm-200m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        reset("DB3");
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph("DB3");
        
        List<String> urls = Arrays.asList(DATA);
        
        if ( false ) {
            //inputThreaded(dsg, urls);
            inputInline(dsg, urls);
            System.exit(0);
        }
        
        
        load(
            //()->LoaderFactory.parallelLoader(dsg, baseMonitorOutput),
            ()->LoaderFactory.sequentialLoader(dsg, baseMonitorOutput),
            //()->LoaderFactory.simpleLoader(dsg, baseMonitorOutput),
            urls);
        
        // Check answers!
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });

        System.exit(0);
    }

    private static final Object outputLock = new Object();
    private static final MonitorOutput baseMonitorOutput = ProgressMonitorFactory.outputTo(System.out);
    private static final MonitorOutput output = (fmt, args)-> {
        synchronized(outputLock) {
            baseMonitorOutput.print(fmt, args);
        }
    };

    public static void inputInline(DatasetGraph dsg, List<String> data) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        Semaphore sema = new Semaphore(0);
        Destination<Tuple<NodeId>> functionIndexer3 = (x)->{
            if ( x.isEmpty() )
                sema.release(1);
        };
        Destination<Tuple<NodeId>> functionIndexer4 = (x)->{};
        DataToTuplesInline dtt = new DataToTuplesInline(dsgtdb, functionIndexer3, functionIndexer4, output);
        long time = TimerX.time(()->{
            dtt.startBulk();
            data.forEach(fn-> {
                ProgressMonitor monitor = ProgressMonitorOutput.create(output, "data", 100_000, 10);
                StreamRDF stream = new ProgressStreamRDF(dtt, monitor); 
                monitor.startMessage();
                monitor.start();
                
                RDFDataMgr.parse(stream, fn);
                monitor.finish();
                monitor.finishMessage("data");
            });
            dtt.finishBulk();
            System.out.println("Wait for DataToTuples"); 
            try {
                sema.acquire(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
          });
        System.out.println("Finished"); 
        long c = dtt.getCountTriples();
        double t = time/1000.0;
        double r = c/t; 
        
        System.out.printf("DataToTuplesInline: Triples = %,d : time = %,.2f : rate = %,.3f\n", c,t,r);  

    }
    
    public static void inputThreaded(DatasetGraph dsg, List<String> data) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        
        BiConsumer<String, String> prefixHandler = (prefix, uristr) -> {
            // Transactions
            output.print("PREFIX %s: %s\n", prefix, uristr); 
        };
        
        Semaphore sema = new Semaphore(0);
        Destination<Tuple<NodeId>> functionIndexer3 = (x)->{
            if ( x.isEmpty() )
                sema.release(1);
        };
        Destination<Tuple<NodeId>> functionIndexer4 = (x)->{};
        
        DataToTuples dtt = new DataToTuples(dsgtdb, functionIndexer3, functionIndexer4, output);
        Destination<Triple> dest3 = dtt.dataTriples();
        Destination<Quad> dest4 = dtt.dataQuads();
        
        DataBatcher dataBatcher = new DataBatcher(dest3, dest4, output, prefixHandler);
        
        dtt.start();
        long time = TimerX.time(()->{
            dataBatcher.startBulk();
            data.forEach(fn-> {
                ProgressMonitor monitor = ProgressMonitorOutput.create(output, "data", 100_000, 10);
                StreamRDF stream = new ProgressStreamRDF(dataBatcher, monitor); 
                monitor.startMessage();
                monitor.start();
                
                RDFDataMgr.parse(stream, fn);
                monitor.finish();
                monitor.finishMessage("data");
            });
            dataBatcher.finishBulk();
            System.out.println("Wait for DataToTuples"); 
            try {
                sema.acquire(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
          });
        System.out.println("Finished"); 
        long c = dataBatcher.countTriples();
        double t = time/1000.0;
        double r = c/t; 
        
        System.out.printf("DataToTuples: Triples = %,d : time = %,.2f : rate = %,.3f\n", c,t,r);  
        
    }
    
    public static void load(Creator<Loader> creator, List<String> data) {
        // The core of the command line "load"
        //load.main("--loader=para", "--loc=DB3", DATA);
        // << The core of "tdb2.load" 
        Loader loader = creator.create();
        output.print("Loader = "+Lib.className(loader));
        long time = TimerX.time(()->{
          loader.startBulk();
          loader.load(data);
          loader.finishBulk();
        });
        // >>
        
        System.out.printf("Total: %.3f s\n", time/1000.0) ;
        System.out.printf("Total: %,d triples\n", loader.countTriples());
        //System.out.printf("Total: %d quads\n", stage1.countQuads());
        double rate = loader.countTriples()/(time/1000.0);
        System.out.printf("Rate: %,.0f TPS\n", rate);
        System.out.println();
    }
    
    private static void query(String string, DatasetGraph dsg) {
        long x = TimerX.time(()->{
            Dataset ds = DatasetFactory.wrap(dsg);
            try ( QueryExecution qExec = QueryExecutionFactory.create(string, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        });
        System.out.println(Timer.timeStr(x)+" seconds");
    }
    
    private static void reset(String DIR) {
        Path p = Paths.get(DIR);
        if ( Files.exists(p) )
            deleteAll(p);
        FileOps.ensureDir(DIR);
    }

    // ==> IO
    /** Delete everything from a {@code Path} start point, including the path itself.
     * Works on files or directories.
     * Walks down the tree and deletes directories on the way backup.
     */  
    public static void deleteAll(Path start) {
        try { 
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        }
        catch (IOException ex) { throw IOX.exception(ex) ; }
    }
}
