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
    // Stream chunker : functions, not queues?
    // Index building monitoring : combined (i.e. min), what about sequential use.  
    // Temp coordinator .create(... parts ...) [add,start]
    
    // Temp coordinator shutdown -> do not call TransactionalComponentLifecycle.shutdown
    
    /*
    // May 15
    // Restructure
    INFO  Data: /home/afs/Datasets/BSBM/bsbm-25m.nt.gz: 24,997,044 bsbm-25m.nt.gz 133.27s (Avg: 187,565)
    INFO  Time = 146.217 seconds : Triples = 24,997,044 : Rate = 170,959 /s
    
    INFO  Data: /home/afs/Datasets/BSBM/bsbm-100m.nt.gz: 100,000,748 bsbm-100m.nt.gz 553.71s (Avg: 180,601)
    INFO  Time = 579.233 seconds : Triples = 100,000,748 : Rate = 172,643 /s
    */

    // Exception handling in bulkload.
    // Remove "showprogress flag. Use ProgressMonitorBasic
}
