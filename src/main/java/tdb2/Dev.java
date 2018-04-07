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

public class Dev {
    // CmdBulkLoaderTDB2 : tdb2.dataloader.
    //   Complete replacmeent for tdb2.tdbloader.
    // Stats gathering StreamRDF
    // All files vs one file.
    
    // Add chunks to a Service Executor/FJ pool.
    // No - overloads RMA use - need phases and fixed threads.
    // Yes - need to control both CPUs and RAM usage.
    
    // ProgressMonitor.Output -> LineOutput
    
    // BulkLoader -> "factory".
    
    // Sequential - in exclusive mode.
    // Sequential - BuilderSecondaryIndexesSequential index tick
    // Sequential - Own package? 

    // Parallel - change file name during loading.
    // Finished: 100,000,748 EXP 593.59s (Avg: 168,467)
    
    
    // Loader 
    // LoaderSimple - parse-dsg.add
    // LoaderSequential - parse to primary // build indexes one by one.  TDB1
    // LoaderParallel - parse to primary+some parallel indexes // build other indexes in batches.
    //   == LoaderSequential? with phase 1 = no indexes, and  other in batches of 1.
    
    // ProgressMonitor - interface; format the output?
    //   Common - the data phase, including file name.
    //   "label" of pregress monitor? --> cope with null.
    //   Timer.time.

    // No BulkStreamRDF.
    //   bulkStart/bulkFinish is in Loader.
    
    // The Load cycle.
    //    Get the loader.
    //    stream.startBulk()
    //    for each file:
    //       loadOneFile.
    //    stream.finishBulk(); stream.finishErrorBulk()
    


    static class Figures {
        // ** tdb1 tdbloader
        // INFO  ** Completed: 100,000,748 triples loaded in 1,454.53 seconds [Rate: 68,751.20 per second]
        
        // ** tdb2.tdbloader (single pass over triples into SPO) 
        // INFO  Finished: 5,000,599 bsbm-5m.nt.gz 80.25s (Avg: 62,314)
        // INFO  Finished: 24,997,044 bsbm-25m.nt.gz 406.72s (Avg: 61,460)
        // INFO  Finished: 100,000,748 bsbm-100m.nt.gz 1792.76s (Avg: 55,780)
        
        // ----
        // ** New loader / java TDB1 style
        // Finished: 5,000,599 data 73.81s (Avg: 67,752)
        // Finished: 24,997,044 data 369.38s (Avg: 67,673)
        // Finished: 100,000,748 data 1591.74s (Avg: 62,824)
    
        // New loader. nodes only: Peak average: 112K 
        // Finished: 24,997,044 data 239.02s (Avg: 104,583)
        
        // New loader. SPO only: Peak 98K
        // Finished: 24,997,044 data 270.47s (Avg: 92,419)
        
        // ** Pure parse.
        // riot --sink --time ~/Datasets/BSBM/bsbm-25m.nt.gz
        //   Creates triples, sends to a null StreamRDF (= StreamRDFBase).
        // /home/afs/Datasets/BSBM/bsbm-25m.nt.gz : 110.64 sec : 24,997,044 Triples : 225,923.18 per second
        //
        // riot --time ~/Datasets/BSBM/bsbm-25m.nt.gz > /dev/null
        //   Writes N-triples - sort of like TDB1 encoding.
        // /home/afs/Datasets/BSBM/bsbm-25m.nt.gz : 122.85 sec : 24,997,044 Triples : 203,482.77 per second
    
        // Dev Stream loader: 
        // 5m - 91K to SPO only.
        // 5m - NodeTable only.     (Avg: 103,971)
        // 5m - Parse throughput. : 222,337 (no NT, no index)
        // 5m - 3 indexes : Finished: 5,000,599 EXP 71.28s (Avg: 70,152)
        // 25m - 3 indexes : Finished: 24,997,044 EXP 369.21s (Avg: 67,703)
        
        // Parallel loader
        // 100m SSD  : 
        // 100m Disk : 
        
        // Parallel loader : 1m conversion only
        // Finished: 1,000,312 EXP 6.57s (Avg: 152,277)
        // 25m: 3 indexes : 
        // Finished: 24,997,044 EXP 150.50s (Avg: 166,090)
        // 100m: 3 indexes :
        // Finished: 100,000,748 EXP 607.25s (Avg: 164,677)
        
        // Chunk sizes: (single runs)
        // 100000, Finished: 24,997,044 EXP 153.19s (Avg: 163,175)
        // 10000, Finished: 24,997,044 EXP 154.74s (Avg: 161,544)
        // 1000, Finished: 24,997,044 EXP 156.69s (Avg: 159,529)
        
        // Queue sizes (chunk 100k)
        // 100/100 as above
        // 10/10: Finished: 24,997,044 EXP 152.28s (Avg: 164,151)
        // 2/1 Finished: 24,997,044 EXP 147.51s (Avg: 169,461)
        // 10/10 Finished: 24,997,044 EXP 148.27s (Avg: 168,587)
        
        // 100m: SSD
        // Finished: 100,000,748 EXP 587.29s (Avg: 170,275)
        // ChunkSize = 100,000 : Queue(triples) = 10 : Queue (tuples) = 10
        
        // 100m: disk
        //Finished: 100,000,748 EXP 1582.56s (Avg: 63,189)
        //ChunkSize = 100,000 : Queue(triples) = 10 : Queue (tuples) = 10
        
        // 25m: Disk.
        // Finished: 24,997,044 EXP 174.68s (Avg: 143,098)
        // ChunkSize = 100,000 : Queue(triples) = 10 : Queue (tuples) = 10
        
        // 25m: SSD, 2/1 index split.
        //Finished: 24,997,044 EXP 196.50s (Avg: 127,212)
        // ChunkSize = 100,000 : Queue(triples) = 10 : Queue(tuples) = 10
        
        // 25m: SSD, 1/2 index split.
        // Finished: 24,997,044 EXP 190.44s (Avg: 131,261)
    
        // Spliter - running unsplit:
        // 25m: SSD, 3/0 index split.
        // Finished: 24,997,044 EXP 151.07s (Avg: 165,471)
        // ChunkSize = 100,000 : Split = 3 : Queue(triples) = 10 : Queue(tuples) = 10
    
        // ---- Final design testing (quads not ready)
        //    Finished: 24,997,044 EXP 146.70s (Avg: 170,400)
        //    ChunkSize = 100,000 : Split = 3 : Queue(triples) = 10 : Queue(tuples) = 10
    }    
}
