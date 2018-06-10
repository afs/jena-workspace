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

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;

public class DevPhased {
    static { JenaSystem.init(); }

    public static void main(String[] args) {
        
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-200m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        //String DATA = "/home/afs/tmp/D.trig";
        
        String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        String datafile = args.length > 0 ? args[0] : DATA ;
        
        //String DB = "home/afs/disk1/tmp/DB3";
        String DB = "DB3";
        LoaderDevTools.reset(DB);

        tdb2.tdbloader.main("--loader=phased", "--loc="+DB, datafile);

//        MonitorOutput output = LoaderOps.outputTo(System.out);  
//
//        DataLoader loader = new LoaderPhased(dsg, output);
//       
//        long totalElapsed = Timer.time(()->{
//            loader.startBulk();
//            loader.load(datafile);
//            loader.finishBulk();
//        }); 
//            
//        if ( output != null ) {
//            long count = loader.countTriples();
//            String label = "Triples";
//            double seconds = totalElapsed/1000.0;
//            if ( seconds > 1 )
//                output.print("Time = %,.3f seconds : %s = %,d : Rate = %,.0f /s", seconds, label, count, count/seconds);  
//        }
        
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DB);
        // Check answers!
        Txn.execute(dsg, ()->{
            LoaderDevTools.query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
            LoaderDevTools.query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
        });
    }
}
