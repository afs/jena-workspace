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

package tdb2.early;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
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
import tdb2.BulkLoader;
import tdb2.BulkStreamRDF;
import tdb2.TimerX;

public class DevTDB2BulkLoader {
    static {
        JenaSystem.init();
        LogCtl.setLog4j();
        LogCtl.enable(BulkLoader.LOG);
    }

    // Parallel.
    
    // Look for XXX
    // Proper flush (Txn!)
    
    // Tools.
    //   Dump index.
    //   Dump node table.
    
    // -> ProgressMonitor
    //    ProgressMonitor.copy
    //    create(Output)
    //    multistep support.
    
    // Disable transaction-isms?
    // Sorted input.
    
    // Parser to parallel outputs. 
    //   One per index.
    
    public static void main(String ... args) {
        DatasetGraph dsg;
        String data;
        if ( false ) {
            dsg = DatabaseMgr.createDatasetGraph();
            data = "data.ttl"; 
            BulkLoader.DataTickPoint = 1;
            BulkLoader.DataSuperTick = 2;
            BulkLoader.IndexTickPoint = 1;
            BulkLoader.IndexSuperTick = 2;
        } else {
            Path p = Paths.get("DB");
            if ( Files.exists(p) )
                deleteAll(p);
            FileOps.ensureDir("DB");
            //BulkStreamLoader.SplitTriplesIndexes = 1;
            dsg = DatabaseMgr.connectDatasetGraph("DB");
            data = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        }
        
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        Txn.executeWrite(dsgtdb, ()->{
            BulkStreamRDF stream = BulkLoaderJava.create(dsgtdb);
            // Just start/finish?
            stream.startBulk();
            RDFDataMgr.parse(stream, data);
            stream.finishBulk();
        });
        
        Txn.execute(dsgtdb, ()->{
           query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
           query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });
    }

    private static void query(String string, DatasetGraph dsg) {
        TimerX.time(()->{
            Dataset ds = DatasetFactory.wrap(dsg);
            try ( QueryExecution qExec = QueryExecutionFactory.create(string, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        });
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
