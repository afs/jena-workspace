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
    // JENA-1550.1552.
    // Binary rt input?

    
    // tdb2.tdbloader: Check load from "-- -"
    // --syntax
    
    // Remove "Loader =" at level info.
    
    // "Tuples[3]" output.
    
    // + Documentation page
    // + Parallel and phased.
    
    static class ExperimentalResults {
        /* 2018-06-03
INFO  Time = 1,671.482 seconds : Triples = 200,031,975 : Rate = 119,673 /s
INFO  Time = 1,624.729 seconds : Triples = 200,031,975 : Rate = 123,117 /s

qexpr '(1671.482 +1624.729)/2' 
Average: 121395.0

INFO  Loader = LoaderPhased (as LoaderParallel)
INFO  Time = 1,143.754 seconds : Triples = 200,031,975 : Rate = 174,891 /s
INFO  Loader = LoaderParallel
INFO  Time = 1,156.762 seconds : Triples = 200,031,975 : Rate = 172,924 /s
INFO  Loader = LoaderPhased
INFO  Time = 1,154.409 seconds : Triples = 200,031,975 : Rate = 173,277 /s
INFO  Loader = LoaderParallel
INFO  Time = 1,134.725 seconds : Triples = 200,031,975 : Rate = 176,282 /s

qexpr '(1143.754+1156.762+1154.409+1134.725)/4'

1147.4
Average: 174, 343 TPS

         | TDB2 phased   | 200m | SSD  | 1,646 | 121,395 |
         | TDB2 parallel | 200m | SSD  | 1,147 | 174,343 |
         | TDB2 phased   | 200m | Disk | 2,899 |  69,010 |

         */
        
        /*
        | | | | | |
        |TDB2 parallel|25m|SSD|143|175,434|
        |TDB2 sequential|25m|SSD|393|112,985|
        |TDB2 basic|25m|SSD|422|67,642|
        */
        // Integrated:
        
        // INFO  Time = 1,640.104 seconds : Triples = 200,031,975 : Rate = 121,963 /s
        // INFO  Time = 213.627 seconds : Triples = 24,997,044 : Rate = 117,013 /s
        // INFO  Time = 2,898.585 seconds : Triples = 200,031,975 : Rate = 69,010 /s
        /*
        | | | | | |
        
            |TDB2 phased|200m|disk|2,899 |69,010 |

         |TDB2 phased|200m|SSD|1,640|121,963|
         |TDB2 parallel|200m|SSD|1,640|174, 343|

        |TDB2 phased|25m|SSD|214|117,013|
        
        
        | TDB2 phased  |  200m  | Disk  |  2,899  |   69,010  |
        | TDB2 phased  |  200m  | SSD   |  1,640  |  121,963  |
        | TDB2 phased  |  25m   | SSD   |  214    |  117,013  |

        */
        
        /*
        // 2018-05-15:
        // Restructure
        INFO  Data: /home/afs/Datasets/BSBM/bsbm-25m.nt.gz: 24,997,044 bsbm-25m.nt.gz 133.27s (Avg: 187,565)
        INFO  Time = 146.217 seconds : Triples = 24,997,044 : Rate = 170,959 /s
        
        INFO  Data: /home/afs/Datasets/BSBM/bsbm-100m.nt.gz: 100,000,748 bsbm-100m.nt.gz 553.71s (Avg: 180,601)
        INFO  Time = 579.233 seconds : Triples = 100,000,748 : Rate = 172,643 /s
        
        5m: parallel:   149,481 TPS
        5m: sequential:  60,851 TPS
        5m: basic:       61,318 TPS
        
        25m: parallel:  169,223 TPS
        25m: sequential: 67,338 TPS
        25m: basic:      61,529 TPS

        SATA SSD, 32G RAM: Quad core i7

        2018-05:
        Simple/original/disk:
          INFO  Finished: 200,031,975 data.nt.gz 6045.02s (Avg: 33,090)
          
        Parallel:
          Time = 1,180.566 seconds : Triples = 200,031,975 : Rate = 169,437 /s
        Sequential:
          Time = 3,227.839 seconds : Triples = 200,031,975 : Rate = 61,971 /s
        Basic:
          Time = 3,507.557 seconds : Triples = 200,031,975 : Rate = 57,029 /s
        
        ****
        Final Parallel: 200m: SSD
          Time = 1,179.543 seconds : Triples = 200,031,975 : Rate = 169,584 /s
        Final Parallel: 25m: SSD
          Time =   487 seconds     : Triples = 24,997,044 : Rate = 175,434 /s
        Final Sequential: 25m: SSD
          Time =   392.504 seconds : Triples = 24,997,044 : Rate = 63,686 /s
        Final Basic: 25m: SSD
          Time = 422.049 seconds : Triples = 24,997,044 : Rate = 59,228 /s
        ****

        Phased TDB parallel loader / SSD
              Time = 1,745.185 seconds : Triples = 200,031,975 : Rate = 114,619 /s
        Phased TDB parallel loader / Disk
              Time = 1,300.145 seconds : Triples = 100,000,748 : Rate = 76,915 /s
              Time = 2,957.218 seconds : Triples = 200,031,975 : Rate = 67,636 /s

        TDB2/parallel/Disk:
          INFO  Time =   171.863 seconds : Triples = 24,997,044  : Rate = 145,448 /s
          INFO  Time =   885.078 seconds : Triples = 100,000,748 : Rate = 112,985 /s
          INFO  Time = 3,515.494 seconds : Triples = 200,031,975 : Rate = 56,900 /s
        TDB2/sequential/disk    
           INFO  Time = 4,793.281 seconds : Triples = 200,031,975 : Rate = 41,732 /s
     
          
        TDB1: loader1/SSD
        INFO  ** Completed: 200,031,975 triples loaded in 3,333.00 seconds [Rate: 60,015.54 per second]

        TDB1: loader2 3078 => 200,031,975/3078  Rate = 64,987

    -------------
    | C         |
    =============
    | 200031975 |
    -------------
    66.081 seconds
    -----------
    | C       |
    ===========
    | 2412628 |
    -----------

        TDB1
        INFO  ** Data: 5,000,599 triples loaded in 53.55 seconds [Rate: 93,383.61 per second]
        ** Completed: 5,000,599 triples loaded in 75.55 seconds [Rate: 66,188.39 per second]
        */
    }

}

