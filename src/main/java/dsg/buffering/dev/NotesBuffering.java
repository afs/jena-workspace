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

    // [ ] TestBufferingDatasetGraph - no flush tests
    // [ ] TestBufferingGraph - more tests.
    // [ ] Transaction journal. RDF Patch + single file.

    // ** Graph case
    // [ ] ** Transactions! Need planning for the contract on the underlying graph.
    //   PROMOTE in BufferingDatasetGraph
    // Access cycle in BufferingDatsetGraph
    // [ ] ??"Buffering" = "Buffered"

    // Else ready.

    // =====================

    // ** DatasetGraph case

    // [ ] Graph TransactionHandler adapter not reentrant?

    // No -- BufferingDatasetGraphQuads
    //   Not worth the trouble.
    //   Only advantage is that it does not take default quads apart.
    //   But graph views may be better as BufferingDatasetGraph
    //   Needs the transaction lifecycle but can't share a super class

    // Graph:
    // BufferingGraph
    // BufferingPrefixMapping

    // ** Maybe: DBOE
    // BufferingStorageRDF
    // BufferingStoragePrefixMap
    //   Port TIM to DBOE?
}
