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

package dsg;


public class DevDSG {
    // From TDB2.2
    // Require transaction checks .. in StorageTDB.find 
    
    // Jena-1695.
    // << MUST
    // >> MUST

    // << Tasks
    
    // Check
    //   Backwards and forwards compatibility.
    
    // Task: 
    // (necessary? or can it wait?)
    // PrefixMapI is a replacement for PrefixMap
    // PrefixMapOverPrefixMapI
    //
    // PrefixMap
    //   PrefixMapBase (A)
    //     FastAbbreviatingPrefixMap
    //     PrefixMapExtended
    //     PrefixMapStd
    //   PrefixMapNull
    //   PrefixMapWrapper
    //     PrefixMapUnmodifiable
    
    // Task: union prefixes.
    //   GraphViewSwitchable -- union prefix copy.  [Later]

    // >> Tasks

    // StorageLib.removeTriples, removeQuads -- used? without node materialization.

    // -----

    // Check performance.
    // Check DB compatibility.

    // Revisit TDB2 POM.
    // TDBInternal.isBackedByTDB - test switchable thing

    // migrate

    /** Integration tests.
     * @see AbstractDatasetGraphFind
     * @see AbstractDatasetGraphFindPatterns
     * @see AbstractDatasetGraphTests
     * @see AbstractTestGraphOverDatasetGraph
     *
     * + TestStorageDatasetGraphTests
     */
    
    // **** LoaderNotes
    
    // MULTI: executeData uses DataBatcher uses PrefixHandler
    // PARSE_NODE: executeDataParseId -> Directly in DataToTuplesInline. prefixes.add
    // PARSE_NODE_INDEX: executeDataOneThread -> DataToTuplesInlineSingle prefixes.add

    // InputStage.MULTI
    //   loaderPlanPhased 
    //   loaderPlanParallel

    // InputStage.PARSE_NODE
    //   loaderPlanLight

    // InputStage.PARSE_NODE_INDEX
    //   loaderPlanMinimal 
    //   loaderPlanSimple

    // LoaderMain issue, prefixes.
    //   ** Prefixes use the Coordinator as setup.
    //   ** Why isn't indexing affected?

    // Prefixes are always done...

    // StoragePrefixes: need to take apart and use directly

    // MULTI: class Indexer - DataToTuples (then batcher).
    // PARSE_NODE: class Indexer - DataToTuplesInline
    // PARSE_NODE_INDEX: class Indexer - DataToTuplesInlineSingle

}
