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

package rdf_star;

public class NotesRDFStar {

    // Revisit jena-site RDF-star doc

    // [ ] Copy CG tests, Add to Scripts_*
    // [ ] ReorderTransform?
    // [ ] EARL report.

    // == Part 2
    // [ ] BindingNodeId costs.
    //     Check BindingNodeIds - get empty ones?
    //     BindingNodeId->BindingX
    //     BindingNodeId.root;
    //     Can we replace by a "get from parent, use NodeTable"? (Functions<Node, X)?
    // ??? Isomorphism match

    // [ ] StageMatchTuple -> becomes functions -> TupleMatcher.
    // [ ] Rewrite QueryIterTriplePattern et al.

    // [ ] Var.lookup ;  Substitute.substitute

    // ==== Done

    // *** SolverRX public static boolean DATAPATH = true;
    // org.apache.jena.tdb.solver.SolverRX;
    // org.apache.jena.tdb2.solver.SolverRX;
    // org.apache.jena.sparql.engine.iterator.RX_SA.DATAPATH

    // [x] ORDER BY
    //     NodeUtils.compareRDFTerms
    //     Find comparator tests : TestOrdering
    // [x] First class Node_Triple
    // [x] Isomorphism (with bnodes)

    // [x] RDF-star tests -> mem
    // [x] mem : ready
    // [x] TDB2 : filter
    // [x] TDB1 update
    // [x] Solver RX.DATAPATH=true, RX_SA.DATAPATH
    // [x] Tests : Quad Filter and <<>> TestQuadFilter *2
    // [x] Code DRY in SolverRX

    // SA cleanup.
    // == Mem
    // **
    // [x] Check all SA for in-memory :: RX_SA.rdfStarTriple_SA
    // [x] Cleanup RX_SA
    //
    // ==== TDB2

    // Work:
    // [x] Review and clean code. (TDB2)
    // [x] SolverRX, StageMatchTuple cleanup. (part 2)
    // [x] Test without fast path :: SolverRX.matchQuadPattern
    // [x] accessData1 for filter.
    // [x] test cases to work for TDB1/2 injection Creator<DatasetGraph>
    //     Done: RunnerSPARQL_TDB2 etc.
    //     qtest --tdb2, --tdb1? (Does not use RunnerSPARQL?)


    // ==== TDB1
    // [x] Resync (last: 2021-03-08)

    // ===
    // Update SPARQL parser for exact grammar.

    // ** Test and Migrate RDFX to org.apache.jena.system (in jena-arq) JENA-1903

    // SHACL, SHACLC, ShEx
}
