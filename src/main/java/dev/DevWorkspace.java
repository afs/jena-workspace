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

package dev;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Transactional ;

public class DevWorkspace
{
    static { LogCtl.setCmdLogging() ; }
    
    public static void main(String ... a) {
        arq.sparql.main("--desc=/home/afs/tmp/assem.ttl", "ASK{}");
    }        
        
    public static void main0(String ... a) {
        Timer timer = new Timer() ;
        timer.startTimer();

        System.out.println("Start") ;
        for ( int i = 0 ; i < 5 ; i ++ ) {
            long x1 = timer.readTimer() ;
            DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
            Transactional txn = (Transactional)dsg ;
            txn.begin(ReadWrite.WRITE) ;
            RDFDataMgr.read(dsg, "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz") ;
            txn.commit() ;
            txn.end() ;
            long x2 = timer.readTimer() ;
            System.out.printf("%d: %.2f\n", i, (x2-x1)/1000.0) ;
        }
        System.out.println("Finish") ;
        System.exit(0) ;
    }
}
