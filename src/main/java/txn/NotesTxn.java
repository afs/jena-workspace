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

package txn;

public class NotesTxn {
    
    // Plan:
    //   "Counting transactions" begin-commit.
    //   DatasetGraph.exec, DatasetGraph.calc  
    //   TDB1: DatasetGraphTransaction
    //   TDB2: TransactionalBase
    //
    // problem - end-end-end.
    // TDB1: DatasetGraphTransaction : does finish()->dsg.end
    // TDB2: TransactionalBase.end : does finish()->dsg.end
    
    // Can we do it as a lightweight wrapper?

    // TIM

    // TransactionCoordinatorState - only detach if level==1.

    // Level and end();
    //   end() and READ != end() and WRITE
    
    // Tests!
    // Multiple calls of end()
    // Abort means aborts

    // **** OTHER
    
    // Consumer avoids the need for final outer dsg.
    // DatasetGraph.txn(TxnType, Consumer(dsg)->{})
    // DatasetGraph.txnCalc(TxnType, Consumer(dsg)->{}) -> X

    // TDB2Factory, TIM2Factory -> DatabaseConnection as interface;
    // DatabaseConnectionTDB

    // Tests for nesting.
    // test for Txns.

    // AbstractStoreConnections store_1 and store_4.

    // And also JENA-1667
    // and also GraphUnionRead
    
    
    // **** OTHER
//    DatasetGraph.genin default
//    @Override
//    public void begin(final ReadWrite readWrite) {
//        begin(TxnType.convert(readWrite));
//    }
    
//    // Decide promotion mode before passing on with mode.
//    public default boolean promote() {
//        // Safe choice for READ and WRITE. 
//        Promote promoteMode = Promote.ISOLATED;
//        
//        TxnType txnType = transactionType();
//        if ( txnType == TxnType.READ_COMMITTED_PROMOTE )
//            promoteMode = Promote.READ_COMMITTED;
//        return _promote(promoteMode);
//    }

}
