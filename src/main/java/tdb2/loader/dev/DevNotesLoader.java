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

public class DevNotesLoader {
    // quads
    // triples + quads.
    
    // Triples - concurrent - limits on size? log every 1 million? Log on driver size.
    // Quads - phase loading
    
    // parallel 1:
    // parallel 1:
    
    // API : Loader, LoaderFactory
    
    // Phase 1 : data (2 threads), primary1, primary2 (2 threads, likely 1 inactive)
    // Phase 2 : 2 indexes : POS, SPO
    // Phase 3 : GPOS, GOSP 
    // Phase 4 : SPOG, POSG, OSPG 
    
    // excessive indexing?
    // Break up!

    // Temp coordinator .create(... parts ...) [add,start]
    // Temp coordinator shutdown -> do not call TransactionalComponentLifecycle.shutdown
    
    /*
    // May 15
    // Restructure
    INFO  Data: /home/afs/Datasets/BSBM/bsbm-25m.nt.gz: 24,997,044 bsbm-25m.nt.gz 133.27s (Avg: 187,565)
    INFO  Time = 146.217 seconds : Triples = 24,997,044 : Rate = 170,959 /s
    
    INFO  Data: /home/afs/Datasets/BSBM/bsbm-100m.nt.gz: 100,000,748 bsbm-100m.nt.gz 553.71s (Avg: 180,601)
    INFO  Time = 579.233 seconds : Triples = 100,000,748 : Rate = 172,643 /s
    
    5m: parallel:   149,481 TPS
    5m: sequential:  60,851 TPS
    5m: simple:      61,318 TPS
    
    25m: parallel:  169,223 TPS
    25m: sequential: 67,338 TPS
    25m: simple:     61,529 TPS

    TDB1
    INFO  ** Data: 5,000,599 triples loaded in 53.55 seconds [Rate: 93,383.61 per second]
    ** Completed: 5,000,599 triples loaded in 75.55 seconds [Rate: 66,188.39 per second]
    */

    
    // Exception handling in bulkload.
    // Remove "showprogress flag. Use ProgressMonitorBasic
}
