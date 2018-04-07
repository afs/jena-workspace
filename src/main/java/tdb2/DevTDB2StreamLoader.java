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

package tdb2;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.IOX;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.loader.BulkLoader;
import tdb2.loader.base.TimerX;
import tdb2.loader.parallel.BulkStreamLoader;
import tdb2.loader.parallel.BulkStreamRDF;

// Run the blast loader - breform the command line tool was written 
public class DevTDB2StreamLoader {
    static {
        JenaSystem.init();
        LogCtl.setLog4j();
        LogCtl.enable(BulkLoader.LOG);
    }

    // Expose TransactionalComponents
    //   TupleIndex : TupleIndexRecord < RangeIndex(=BPT)
    // NodeTable : Index(=BPT) and TransBinaryDataFile < BinaryDataFile
    // Index 
    
    // Extract components.
    
    // BPT.nonTransactional
    // Better finishing up - count at end of parse "finishing up".
    
    // SDD version - does this fall off the edge?
    // Quads : split, testing
    
    // Tidy
    // Command line.
    
    // ** Prefixes TransactionalComponents in the data phase.
    
    // Plain.
    // Sequential.
    // Parallel - 2 phase (looping phase).
    
    private static void reset(String DIR) {
        Path p = Paths.get(DIR);
        if ( Files.exists(p) )
            deleteAll(p);
        FileOps.ensureDir(DIR);
    }
    
    public static void main(String ... args) {
        Location location;
        String data;
        if ( false ) {
            location = Location.mem();
            data = "data.ttl"; 
            BulkLoader.DataTickPoint = 1;
            BulkLoader.DataSuperTick = 2;
            BulkLoader.IndexTickPoint = 1;
            BulkLoader.IndexSuperTick = 2;
        } else {
            String DIR = "DB";
            reset(DIR);
            location = Location.create(DIR);
            data = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";
            //BulkStreamLoader.ChunkSize = 10_000;
            //BulkStreamLoader.QueueSizeTriples = 5;
            //BulkStreamLoader.QueueSizeTuples =  5;
            //BulkStreamLoader.SplitTriplesIndexes = 3;
            //BulkStreamLoader.SplitQuadsIndexes = 6;
        }

        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(location);
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        
//        TransactionCoordinator coordinator = dsgtdb.getTxnSystem().getTxnMgr();
//        coordinator.execExclusive(null);
//        coordinator.blockWriters();
        
        // ?? Need to reread the root files.No need.
        // Temporary coordinator:
        //   shutdown == reset in master.
        // Coordinator .reset -> TransactionalComponentLifecycle.clearInternal , not null setting.
        
        BulkStreamRDF stream = new BulkStreamLoader(dsg);
        stream.startBulk();
        RDFDataMgr.parse(stream, data);
        stream.finishBulk();

        // Workaround for no TransactionCoordinator.reset
        
//        TDBInternal.expel(dsg);
//        dsg = DatabaseMgr.connectDatasetGraph(location);
        
        DatasetGraph dsgq = dsg;
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgq) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsgq) ;
        });
        System.exit(0);
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
