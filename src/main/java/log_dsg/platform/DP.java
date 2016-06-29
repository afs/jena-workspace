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

package log_dsg.platform;

import java.util.concurrent.Callable ;
import java.util.concurrent.atomic.AtomicInteger ;
import java.util.function.Supplier ;

import log_dsg.StreamChanges ;
import log_dsg.Txn ;
import log_dsg.changes.DatasetGraphChanges ;
import log_dsg.changes.PatchReader ;
import log_dsg.changes.StreamChangesApply ;
import log_dsg.platform.LibPatchSender.StreamChangesCollect ;
import org.apache.jena.sparql.core.DatasetGraph ;

public class DP {
    public static final String PatchContainer   = "http://localhost:1066/patch" ;
    
    public static final String _FetchService     = "http://localhost:1066/fetch" ;

    static AtomicInteger lastPatchFetch = new AtomicInteger(0) ;
    
    public static class DatasetGraphChangesVersion extends DatasetGraphChanges {
        // TODO WRONG!!!!
        AtomicInteger localVersion = new AtomicInteger(0) ;
        public final StreamChangesCollect collector ;
        
        public DatasetGraphChangesVersion(DatasetGraph dsg, StreamChangesCollect collector) {
            super(dsg, collector) ;
            this.collector = collector ;
        }
    }
    
    public static DatasetGraph managedDatasetGraph(DatasetGraph dsg, String url) {
        StreamChangesCollect changes = LibPatchSender.create1(url) ;
        DatasetGraph dsg1 = new DatasetGraphChangesVersion(dsg, changes);
        return dsg1 ;
    }
    
    public static void syncExecW(DatasetGraph dsg, Runnable action) {
        DatasetGraphChangesVersion dsgc = (DatasetGraphChangesVersion)dsg ;
        // Update to latest.
        
        for ( ;; ) {
            int x = lastPatchFetch.get()+1 ;
            String url = PatchContainer+"/"+x ;
            PatchReader patchReader = LibPatchFetcher.fetchByPath(url, x) ;
            if ( patchReader == null )
                break ;
            System.out.println("Apply patch "+x);
            lastPatchFetch.incrementAndGet() ;
            dsgc.localVersion.set(lastPatchFetch.get()) ;
            StreamChanges sc = new StreamChangesApply(dsgc) ;
            Txn.execWrite(dsg, ()->patchReader.apply(sc));
        }
        // Prepare for changes.
        
        StreamChangesCollect scc = dsgc.collector ;
         
        Txn.execWrite(dsg, ()-> {
            scc.start();    
            action.run() ;    
            scc.send() ;
            scc.finish();
        }) ;
    }

}
