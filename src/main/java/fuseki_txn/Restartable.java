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

package fuseki_txn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinatorState;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.TransactionManager;

public class Restartable {
    // For Fuseki.
    
    // 1 - Integrate TIM into DBOE.
    // 2 - StorageRDF first.
    
    // Parked transactions.
    static Map<String, TransactionCoordinatorState> transactions = new ConcurrentHashMap<>();
    
    public static void main(String...a) {
        Dataset ds = TDBFactory.createDataset();
        DatasetGraph dsg = ds.asDatasetGraph();
        
        if ( org.apache.jena.tdb.sys.TDBInternal.isTDB1(dsg) ) {
            TransactionManager tm = org.apache.jena.tdb.sys.TDBInternal.getTransactionManager(dsg);
        }
        
        if ( org.apache.jena.tdb2.sys.TDBInternal.isTDB2(dsg) ) {
            TransactionCoordinator txnCoord = org.apache.jena.tdb2.sys.TDBInternal.getTransactionCoordinator(dsg);
            
            org.apache.jena.dboe.transaction.txn.Transaction txn = txnCoord.begin(TxnType.READ_PROMOTE);
            txn.getTxnId().bytes();
            TransactionCoordinatorState state = txnCoord.detach(txn);
            txnCoord.attach(state);
            txn.commit();
            txn.end();
        }
    }
}
