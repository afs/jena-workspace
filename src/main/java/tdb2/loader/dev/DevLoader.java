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

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import tdb2.loader.LoaderFactory;

/** code in support of TDB2 loader development */
public class DevLoader {
    
    static { JenaSystem.init(); }
    
    public static void main(String[] args) {
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-200m.nt.gz";
        String DATA = "/home/afs/Datasets/BSBM/bsbm-25m.nt.gz";
        //String DATA = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz";
        //String DATA = "/home/afs/tmp/D.trig";
        reset("DB3");
        
        tdb2.cmd.load.main("--loc=DB3", "--graph=http://example/graph", "-loader=parallel", DATA);
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
