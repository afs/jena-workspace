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

import org.apache.jena.tdb2.TDB2;
import org.slf4j.Logger;

public abstract class BulkLoader {

    /** Tick point for messages during loading of data */
    public static long      DataTickPoint         = 500_000 ;
    /** Number of ticks per super tick when first loading the data */
    public static int       DataSuperTick         = 10 ;
    
    /** Tick point for messages during secondary index creation */
    public static long      IndexTickPoint        = 5_000_000 ;
    /** Number of ticks per super tick when indexing */
    public static int       IndexSuperTick        = 10 ;

    public static Logger   LOG                    = TDB2.logLoader ;
//    
//    /** Load files. */
//    public void load(String ... filenames) {
//        load(Arrays.asList(filenames));
//    }
//    
//    /** Load files. */
//    public abstract void load(List<String> filenames);
    
//    public abstract void loadTriples(Iterator<Triple> triples) ;
//    public abstract void loadTriples(Stream<Triple> triples) ;
//    public abstract void loadQuads(Iterator<Quad> triples) ;
//    public abstract void loadQuads(Stream<Quad> triples) ;
    
}
