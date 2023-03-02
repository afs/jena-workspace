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

package reorder;

public class NotesReorder {
    // JENA-2317
    // [1] Reorder not taking into account input bindings.
    // [2] Would like a TransformReorder that did not go into EXISTS (i.e. not expressions).
    //     Partial hack: ExprTransformBase which returns its inputs, not ExprTransformCopy
    // ARQ.getContext().set(ARQ.optReorderBGP, false);

    // Not a simple bottom up matter.

    // ElementWalker - the no expressions walker.

    // [2] ==>
    //   How not to walk into expressions
    //   Walker and ApplyTransformVisitor need to cooperate
    //     Assumes push to stack.
    // walkSkipService$ -> walkSkipExprs

    // [0] Switch off TransformReorder

    // Do reorder after OpSequence
    //  Track variables in walker. "Visitor with scope - subclass"

    // ---
    // Related:
    // none.opt, fix.opt
    //     Decide in DatabaseOps.createSwitchable
    //     (it is currently sticky - copied across in a compact)


    /*  TDB2: TDB2StorageBuilder (called from StoreConnection.create from )

TDB2StorageBuilder.build(Location, StoreParams) : DatasetGraphTDB - org.apache.jena.tdb2.store.TDB2StorageBuilder
    TDB2StorageBuilder.build(Location) : DatasetGraphTDB - org.apache.jena.tdb2.store.TDB2StorageBuilder
                Test: before() : void - org.apache.jena.tdb2.store.TestStorageDatasetGraphTests
                Test: createTripleTableMem() : TripleTable - org.apache.jena.tdb2.store.TestTripleTable
    StoreConnection.make(Location, StoreParams) : StoreConnection - org.apache.jena.tdb2.sys.StoreConnection
        StoreConnection.connectCreate(Location, StoreParams) : StoreConnection - org.apache.jena.tdb2.sys.StoreConnection
            StoreConnection.connectCreate(Location) : StoreConnection - org.apache.jena.tdb2.sys.StoreConnection
            DatabaseOps.createSwitchable(Location, StoreParams) : DatasetGraphSwitchable - org.apache.jena.tdb2.sys.DatabaseOps (2 matches)

  TDB1: DatasetBuilderStd, tdb.tdbreorder
     */

}
