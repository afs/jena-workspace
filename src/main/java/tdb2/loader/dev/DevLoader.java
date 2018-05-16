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

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.IOX;
import tdb2.loader.Loader;
import tdb2.loader.LoaderFactory;
import tdb2.loader.MonitorOutput;
import tdb2.loader.TimerX;
import tdb2.loader.base.LoaderOps;

public class DevLoader {
    
    static { JenaSystem.init(); }
    
    public static void main(String[] args) {
        
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-100m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz";
        String DATA = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";
        reset("DB3");
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph("DB3");
        
        List<String> urls = Arrays.asList(DATA);
        
        load(
            ()->LoaderFactory.parallelLoader(dsg, baseMonitorOutput),
            urls);
        
        // Check answers!
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });

        System.exit(0);
    }

    private static final Object outputLock = new Object();
    private static final MonitorOutput baseMonitorOutput = LoaderOps.outputTo(System.out);
    private static final MonitorOutput output = (fmt, args)-> {
        synchronized(outputLock) {
            baseMonitorOutput.print(fmt, args);
        }
    };
    
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
