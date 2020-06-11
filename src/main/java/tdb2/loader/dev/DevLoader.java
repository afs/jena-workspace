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

import static tdb2.loader.dev.LoaderDevTools.*;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.loader.DataLoader;
import org.apache.jena.tdb2.loader.LoaderFactory;
import org.apache.jena.tdb2.loader.base.LoaderOps;

/** code in support of TDB2 loader development */
public class DevLoader {
    
    static { JenaSystem.init(); LogCtl.setLogging(); }
    
    // Tests of DataLoader.
    
    // Loader time keeping.
    // start on timer at the true start needed.
    //   ProgressMonitorContext.start(0 - counter
    
    
    public static void main(String[] args) {
        // [A] Put section in [%d] after name.
        // [B] FINISHED:
        // [C] Any XXX
        // [D]Check from stdin.
        // [E] "Start: <unset>" -> file name or load X files.
        
        // tdbloader : check files exist 
        
        // Look for XXX
        
        {
            DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
            //DataLoader loader = LoaderFactory.createLoader(LoaderPlans.loaderPlanLight, dsg, LoaderOps.outputToLog());
            /* SET ME */
            //DataLoader loader = LoaderFactory.parallelLoader(dsg, LoaderOps.outputToLog());
            DataLoader loader = LoaderFactory.sequentialLoader(dsg, LoaderOps.outputToLog());
            //DataLoader loader = LoaderFactory.basicLoader(dsg, LoaderOps.outputToLog());
            
            loader.startBulk();
            // "bsbm-10k.nt"
            loader.load("/home/afs/Datasets/BSBM/bsbm-250k.nt.gz", "/home/afs/Datasets/BSBM/bsbm-50k.nt.gz");
            loader.finishBulk();
            Txn.executeWrite(dsg, () -> {
                //RDFDataMgr.write(System.out, dsg, Lang.NQ);
                });
            System.exit(0);
        }

        //String DATA = "/home/afs/Datasets/BSBM/bsbm-200m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        String DATA = "/home/afs/tmp/D.trig";
        reset("DB3");
        
        tdb2.cmd.load.main("--loc=DB3", "--loader=parallel", DATA);
        System.exit(0);
        
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph("DB3");
        
        List<String> urls = Arrays.asList(DATA);
        
        if ( false ) {
            //inputThreaded(dsg, urls);
            inputInline(dsg, urls);
            System.exit(0);
        }
        
        
        load(
            ()->LoaderFactory.parallelLoader(dsg, baseMonitorOutput),
            //()->LoaderFactory.sequentialLoader(dsg, baseMonitorOutput),
            //()->LoaderFactory.simpleLoader(dsg, baseMonitorOutput),
            urls);
        
        // Check answers!
        Txn.execute(dsg, ()->{
            query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });

        System.exit(0);
    }

}
