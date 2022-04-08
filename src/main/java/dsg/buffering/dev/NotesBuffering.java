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

package dsg.buffering.dev;

public class NotesBuffering {
    // ** Graph case
    // [ ] ** Transactions! Need planning for the contract on the underlying graph.
    //   PROMOTE in BufferingDatasetGraph
    // Access cyclein BufferingDatsetGraph
    // [ ] ??"Buffering" = "Buffered"

    // Else ready.

    //  Cases:
    //     No DSG -> raw. Single threaded, no need for TXN.
    //     Companion DSG
    //   And flush points.
    //   e.g. start no txn or start PROMOTE-READ-COMMITED when first used.
    //        or run READ then stop-WRITE-begine(READ) in flush.
    //   On flush, end transaction and restart.

    // ** DatasetGraph case

    // [ ] Graph TransactionHandler adapter not reentrant?

    // [ ] GraphTxn is an interface.
    //   Implemented by GraphView.
    //   GraphTxnWrapper(other graph) Standalone mode = MRSW

    // [ ] flushDirect and flushDirect - different cases!
    //     BufferingGraph to use graph txnhandler? Transactional.


    // ----
    // [ ] Library for "plain" graph operations. Code in BufferingGraph.containsByEquals to G
    // [ ] G-ify BufferingGraph. (Is G complete for DSGs?)
    // [ ] L.executeTxn to G.
    // [ ] test e.g. graph from DSG.
    // [ ] TestBufferingDatasetGraph - no flush tests
    // [ ] TestBufferingGraph - more tests.

    // Graph:
    // BufferingGraph
    // BufferingPrefixMapping

    // ** Maybe: DBOE
    // BufferingStorageRDF
    // BufferingStoragePrefixMap
    //   Port TIM to DBOE?
}
