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
    // Thread control.
    // Dft to 2 threads.
    // quads vs triples
    
    // Indexes are a list, chop entries off the list
    //   SPO, POS, OSP so 2T == (data, SPO), (POS,OSP)
    //   GSPO, GPOS, GOSP, SPOG, POSG, OSPG
    
    // 3T
    // (data, GSPO), (GPOS, GOSP, SPOG) (POSG, OSPG)
    // 2T
    // (data, GSPO) (GPOS, GOSP), (SPOG, POSG), (OSPG)
    
    // Tools: 
    //   idx to idx copy
    //   Stream index builder
    
    // CmdBulkLoaderTDB2 : tdb2.loader.
    // Stats gathering StreamRDF
    // All files vs one file.
    
    // Add chunks to a Service Executor/FJ pool.
    // No - overloads RAM use - need phases and fixed threads.
    // Yes - need to control both #CPUs and RAM usage.
    
    // BulkLoader -> "factory".
    // Loader -> counts, and use in overall time/rate.
    
    // Sequential - in sort of exclusive mode.

    // Parallel - change file name during loading.
    // Parallel - CPU and RAM control.
    // LoaderParallel - parse to primary+some parallel indexes // build other indexes in batches.
    // Parallel - parse, NodeTable on one thread? (= sequential phase one, about 
    //                  INFO  Finished: 24,997,044 bsbm-25m.nt.gz 268.00s (Avg: 93,273) -- phase one
    // Parallel - quads not working.  Only reads incoming triples.  
    // See the nodes->triples stage, which does not do quads.

    // *** Need a queue of Triples // Quads. 
    //   Pair on queue?  Separate Node -> NodeId processing (sync/locking looks complicated)

    // Remove, rename Monitor finishe dmessage to be "ended phase
    
    // Expose TransactionalComponents
    //   TupleIndex : TupleIndexRecord < RangeIndex(=BPT)
    // NodeTable : Index(=BPT) and TransBinaryDataFile < BinaryDataFile
    // Index 
    
    // Finished: 100,000,748 EXP 593.59s (Avg: 168,467)
   
    // 2018-04-08:
    // Time to load 25m:
    //   Simple:      Time = 408.455 seconds : Triples = 24,997,044 : Rate = 61,199 /s
    //   Sequential:  Time = 375.059 seconds : Triples = 24,997,044 : Rate = 66,648 /s
    //   Parallel:    Time = 144.567 seconds : Triples = 24,997,044 : Rate = 172,910 /s
    // Time to load 200m: 39G database
    //   Simple:      Time = 3,558.681 seconds : Triples = 200,031,975 : Rate = 56,210 /s
    //   Sequential:  Time = 3,265.094 seconds : Triples = 200,031,975 : Rate = 61,264 /s
    //   Parallel:    Time = 1,641.079 seconds : Triples = 200,031,975 : Rate = 121,891 /s
    //                The rate is dropping to 65K

    // RocksDB
    // Experiment: parse-nodetable only.
    // Parallel:  Time = 134.738 seconds : Triples = 24,997,044 : Rate = 185,523 /s

    // Look for XXX
    
    static class Figures2 {
        // Cmd and Loader, repackaged.
        // 2018-04-07
        
        // Stream loader direct:
        //   ChunkSize = 100,000 : Split = 3 : Queue(triples) = 10 : Queue(tuples) = 10
        //   Finished: 24,997,044 EXP 162.05s (Avg: 154,253)
        // Cmd:
        //   (final, final after flush).
        //   Finished: 24,997,044 EXP 163.13s (Avg: 153,236)
        //   ChunkSize = 100,000 : Split = 3 : Queue(triples) = 10 : Queue(tuples) = 10
        
        
    }
    

    static class Figures1 {
        // Before Cmd writtern and Loader written
        
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
