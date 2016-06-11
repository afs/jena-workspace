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

package log_dsg.v1;

import log_dsg.Txn ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDBFactory ;

public class DevTrack1 {

    public static void main(String[] args) {
        Dataset ds2 = DatasetFactory.createTxnMem() ;
        Dataset ds1 = TDBFactory.createDataset() ;
        
        DatasetChangesTxn monitor = new DatasetChangesTxnLogger() ;
        
        Dataset dsMaster = DatasetFactory.wrap
           (new DatasetGraphMonitorTxn(ds1.asDatasetGraph(),
            monitor)) ;
        
        Quad q1 = SSE.parseQuad("(:g1 :s1 :p1 :o1)") ;
        Quad q2 = SSE.parseQuad("(_ :s1 :p1 :o1)") ;
        Quad q3 = SSE.parseQuad("(:g2 :s1 :p1 :o1)") ;
        Node s1 = SSE.parseNode(":s1") ;   
        
        Txn.execWrite(dsMaster, ()-> {
            dsMaster.asDatasetGraph().add(q1) ;   
            dsMaster.asDatasetGraph().add(q2) ;
            dsMaster.asDatasetGraph().add(q3) ;
            dsMaster.asDatasetGraph().deleteAny(null, s1, null, null);
        }) ;
        
        
    }
}


