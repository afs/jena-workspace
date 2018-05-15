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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.IOX;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.cmd.load;
import tdb2.loader.MonitorOutput;
import tdb2.loader.TimerX;
import tdb2.loader.base.LoaderOps;
import tdb2.loader.parallel.LoaderParallel;

public class DevLoader {
    
    static { JenaSystem.init(); }
    
    public static void main(String[] args) {
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-100m.nt.gz";
        String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        reset("DB3");
        
        load.main("--loader=para", "--loc=DB3", DATA);

        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph("DB3");
        // Check answers!
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });

        System.exit(0);
        
        //DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        dwim(dsg, DATA);
    }

    // DataToPipe -> DataToTuples -> Indexer
    public static void dwim(DatasetGraph dsg, String datafile) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        TransactionCoordinator txnCoord =TDBInternal.getTransactionCoordinator(dsg);
        txnCoord.execExclusive(()->load(dsgtdb, datafile));
        
        // Check answers!
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });
    }
    
    private static final Object outputLock = new Object();
    private static final MonitorOutput baseMonitorOutput = LoaderOps.outputTo(System.out);
    private static final MonitorOutput output = (fmt, args)-> {
        synchronized(outputLock) {
            baseMonitorOutput.print(fmt, args);
        }
    };
    
    public static void load(DatasetGraphTDB dsgtdb, String datafile) {
        LoaderParallel parallel = new LoaderParallel(dsgtdb, output, true);
        
//        BiConsumer<String, String> prefixHandler = (prefix, uristr) -> {
//            // Transactions
//            output.print("PREFIX %s: %s\n", prefix, uristr); 
//        };
//        
//        // Contains a splitter of Tuples -> Tuples per index.
//        Indexer indexer = new Indexer(output, dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes());
//        Destination<Tuple<NodeId>> functionIndexer = indexer.index();
//        
//        DataToTuples dtt = new DataToTuples(dsgtdb, functionIndexer, output);
//        Destination<Triple> dest = dtt.data();
//
//        DataBatcher stream = new DataBatcher(dest, null, output, prefixHandler);
//        
//        indexer.start();
//        dtt.start();
//        stream.startBulk();
        
        long time = TimerX.time(()->{
            parallel.startBulk();
            parallel.load(Arrays.asList(datafile));
            parallel.finishBulk();
        });
        System.out.printf("Total: %.3f s\n", time/1000.0) ;
        System.out.printf("Total: %,d triples\n", parallel.countTriples());
        //System.out.printf("Total: %d quads\n", stage1.countQuads());
        double rate = parallel.countTriples()/(time/1000.0);
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
