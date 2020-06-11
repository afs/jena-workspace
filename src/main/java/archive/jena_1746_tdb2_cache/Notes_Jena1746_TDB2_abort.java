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

package archive.jena_1746_tdb2_cache;

import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionListener;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;

public class Notes_Jena1746_TDB2_abort {

    // Need tests.
    // Debug - why so many calls?

    // ** System abort (OK?)
    // Think about not-present.

    // Not-present is shrinking.

    // Case 1: Not in main "not-present"
    //  Add to local "not-present", fluish down.

    // Case 2: In main "not-present"
    //   May be goes into local cache.
    //   Write back updates "not-present"
    //   Depends on "not-rpesent" used to protect the underlying "not-present"

    // Better to layer NodeTableCache as NodeTableBuffering?? (long term)
    //    OK if checks on node2Id/id2node first.

    // NodeTableCache
    // + Use "notPresent" to stop going to baseNodeTable (only).
    // + Check tests for "not present then present"



    // Need full testing environment.

    // [1746]
    // ****
    // forEachComponent - add listeners
    // components.forEachComponent becomes forEachComponent -  components then listeners (before and after!)


    static class TxnTrace implements TransactionListener {

        static boolean FLAG = true;

        @Override
        public void notifyTxnStart(Transaction transaction) { if ( FLAG ) System.out.println("TxnStart"); }

        @Override
        public void notifyPromoteStart(Transaction transaction) { if ( FLAG ) System.out.println("PromoteStart"); }
        @Override
        public void notifyPromoteFinish(Transaction transaction) { if ( FLAG ) System.out.println("PromoteFinish"); }

        @Override
        public void notifyPrepareStart(Transaction transaction) { if ( FLAG ) System.out.println("PrepareStart"); }
        @Override
        public void notifyPrepareFinish(Transaction transaction) { if ( FLAG ) System.out.println("PrepareFinish"); }

        @Override
        public void notifyCommitStart(Transaction transaction) { if ( FLAG ) System.out.println("CommitStart"); }
        @Override
        public void notifyCommitFinish(Transaction transaction) { if ( FLAG ) System.out.println("CommitFinish"); }

        @Override
        public void notifyAbortStart(Transaction transaction) { if ( FLAG ) System.out.println("AbortStart"); }
        @Override
        public void notifyAbortFinish(Transaction transaction) { if ( FLAG ) System.out.println("AbortFinish"); }

        @Override
        public void notifyEndStart(Transaction transaction) { if ( FLAG ) System.out.println("EndStart"); }

        @Override
        public void notifyEndFinish(Transaction transaction) { if ( FLAG ) System.out.println("EndFinish"); }

        @Override
        public void notifyCompleteStart(Transaction transaction) { if ( FLAG ) System.out.println("CompleteStart"); }
        @Override
        public void notifyCompleteFinish(Transaction transaction) { if ( FLAG ) System.out.println("CompleteFinish"); }

        @Override
        public void notifyTxnFinish(Transaction transaction) { if ( FLAG ) System.out.println("TxnEnd"); }

    }

    public static void main(String...args) {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        TransactionCoordinator coord =  TDBInternal.getTransactionCoordinator(dsg);
        coord.modifyConfig(()->coord.addListener(new TxnTrace()));

        dsg.begin(TxnType.WRITE);
//        dsg.promote();
        dsg.abort();
        dsg.end();


        System.out.println("--DONE--");

    }

}
