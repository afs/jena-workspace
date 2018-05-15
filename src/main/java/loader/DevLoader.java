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

package loader;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.IOX;
import org.apache.jena.tdb2.sys.TDBInternal;
// Local.
import tdb2.MonitorOutput;
import tdb2.loader.base.LoaderOps;
import tdb2.loader.base.TimerX;

public class DevLoader {
    
    static { JenaSystem.init(); }

    public static LoaderEngine engine = new LoaderEngine(null);
    
    public static MonitorOutput output = engine.output(LoaderOps.outputTo(System.out));
    
    public static void main(String[] args) {
        String DATA = "/home/afs/Datasets/BSBM/bsbm-5m.nt.gz";
        reset("DB3");
        
        // Stream chunker : functions, not queues?
        
        // Temp coordinator . create ( ... parts ... )
        
        // Temp coordinator shutdown -> do not call TransactionalComponentLifecycle.shutdown
        
        //DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph("DB3");
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        dwim(dsg, DATA);
    }

    // DataToPipe -> DataToTuples -> Indexer
    public static void dwim(DatasetGraph dsg, String datafile) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        // Contains a splitter.
        Indexer indexer = new Indexer(output, dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes());
        Consumer<List<Tuple<NodeId>>> functionIndexer = indexer.index();
        
        BlockingQueue<List<Triple>> pipe = new ArrayBlockingQueue<>(LoaderConst.QueueSizeData);
        DataToTuples dtt = new DataToTuples(dsgtdb, pipe, functionIndexer, output);
        
        DataToPipe stream = new DataToPipe(pipe, null, output);
        
        indexer.start();
        dtt.start();
        stream.startBulk();
        
        
        long time = TimerX.time(()->{
            RDFDataMgr.parse(stream, datafile);
            stream.finishBulk();
            //dtt.finish();
            indexer.waitFinish();
        });
        System.out.printf("Total: %.3f s\n", time/1000.0) ;
        System.out.printf("Total: %,d triples\n", stream.getCountTriples());
        //System.out.printf("Total: %d quads\n", stage1.getCountQuads());
        double rate = stream.getCountTriples()/(time/1000.0);
        System.out.printf("Rate: %,.0f TPS\n", rate);
        System.out.println();
        
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });
        
    }
    
    
    //DataInput1 -> Indexer
    public static void dwim1(DatasetGraph dsg, String datafile) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        //BlockingQueue<List<Tuple<NodeId>>> pipe = new ArrayBlockingQueue<>(LoaderConst.QueueSizeTuples);
        
        //indexer is a driver, start/finish threads, a pipe loop and a function to handle chunks loadTuples
        
        Indexer indexer = new Indexer(output, dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes()[0]);
        indexer.start();

        // Splitter.
        Consumer<List<Tuple<NodeId>>> functionIndexer = indexer.index();

        Consumer<List<Tuple<NodeId>>> functionPrint = (x)->System.out.println(x);
        
        // Prefix handler.
        //functionPrefix
        // Should this be a two stage process?
        DataInput inputStage = new DataInput1(dsgtdb, functionIndexer, output);
        //DataInput stage1 = new DataInput2(dsgtdb, functionIndexer, output);

        // Minimum is DataInput+Index to SPO/GSPO.
        
        // Or this thread
        long time = TimerX.time(()->{
            // Drive data into the input stage.
            RDFDataMgr.parse(inputStage, datafile);
            // Wait.
            indexer.waitFinish();
        });
        
        //DatabaseConnection.internalReset();
        
        System.out.printf("Total: %.3f s\n", time/1000.0) ;
        System.out.printf("Total: %,d triples\n", inputStage.getCountTriples());
        //System.out.printf("Total: %d quads\n", stage1.getCountQuads());
        double rate = inputStage.getCountTriples()/(time/1000.0);
        System.out.printf("Rate: %,.0f TPS\n", rate);
        System.out.println();
        
        
        
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });
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

        
        
        
//        try {
//            for ( ;; ) {
//                List<Tuple<NodeId>> tuples;
//                tuples = pipe.take();
//                if ( tuples.isEmpty() )
//                    break;
//                for ( Tuple<NodeId>tuple : tuples ) {
//                    System.out.println(tuple);
//                }
//            }
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    
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
