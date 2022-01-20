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

package dsg.union;

public class NotesUnionDSG {
    // Other:
    // Lang.getHeaderString -- if used in Accept, no charset?
    //   Probably correct as-is

    // ++ Better to invest in StorageRDF ++
    // Remove plain executor in TDB2 and TDB1.
    // Issue - filter transform insertion.
    // Just have a "don't reorder flag" -- delayed execution break this?
    // Check: filter placement - special case of one triple./quad

    // ** API
    // [ ] Read-only union graph - NGs updatable.
    // [ ] DatasetGraph.withUnionDefaultGraph()-> default impl DatasetGraphAltGraphs.defaultUnionGraph
    // [ ] Context setting.
    // [ ] Documentation

    // ** TIM
    // [ ] Indexing change
    // [ ] ja:unionDefaultGraph
    // [ ] Time to switch the default?
    // [ ] Naming
    //     Rename QuadTable? as QuadStorage, then HexTable -> QuadTable. Ditto Triple/Tri.
    //     Or just DatasetInMemory takes HexTable TriTable.
    // See below about indexing
    // Port to StorageRDF?

    // ** TDB1
    // ** TDB2
    // [ ] Better detection;
    // [ ] ja:unionDefaultGraph in assemblers
    // Currently:
    // :: Query only: QueryEngineTDB(2).isUnionDefaultGraph :
    // :: Rewrite query to union graph form.
    // :: 1/ Becomes Node.ANY in OpExecutorTDB2.decideGraphNode
    // :: 2/ Picked up by PatternMatchTDB2.execute
    //     --> Could leave as union graph
    // :: 3/ SolverRX.matchQuadPattern
    // :. 4/ StageMatchTuple.access
    //       or SolverRX.accessData (in node space; <<>> has a variable).
    // Generally - rework OpExecutorTDBn to be simpler!

    // ** Other DSG
    // [ ] General. DatasetGraphAltGraphs
    // ?? API! DatasetGraph.withUnionDefaultGraph()-> default impl DatasetGraphAltGraphs.defaultUnionGraph

    // ----

    // ** DatasetGraphAltGraphs
    // [ ] Better based on DatasetGraphStorage??
    // [ ] DatasetGraphTriplesQuadsWrapper?
    // [ ] No: Trait=ify DatasetGraphTriplesQuads
    // DatasetGraphStorage < DatasetGraphBaseFind
    //   StorageRDF
    //   DatasetGraphMinimal == add/4, delete/4, find/4
    //
    // [x] TDB1 and TDB2 allow update of the union graph
    //     TDB1 DatasetGraphTriplesQuads
    //     TDB2 DatasetGraphStorage < DatasetGraphBaseFind
    // Should DatasetGraphStorage be based on DatasetGraphTriplesQuads?

    // ** TIM indexing

    // Indexing in QuadTableForm
    //   GSPO  GOPS  SPOG  OSGP  PGSO  OPSG
    // PGSO -- bad for GRAPH ?g { ?s :p ?o } and UNION
    // Duplicates          -     GP:OS/GSP:O
    // Instead of          OSPG  POSG
    // Best order?
    // But? GOSP --> GOPS
    // For union. PSOG, (POSG)

    // Currently, TIM uses getUnionGraph() = GraphView.createUnionGraph(this) = new GraphView(dsg, Quad.unionGraph)
    //

    //    -> NOT DatasetGraphInMemory.findInUnionGraph$

    // ** HexTable.findInUnionGraph only uses SPOG!!!

    //DatasetGraphWrapperView

    // -- OLD

    // Imperfect for things that dive inside implementation.
    // *** What about Context setting? Universal.
    // Push down to storage, but also in OpExecutor.

    // Symbol:  tdb:unionDefaultGraph, tdb2:unionDefaultGraph
    //  ==> arq:unionDefaultGraph : isUnionDftQuery.
    //  ==> ja:unionDefaultGraph

    // Build into OpExecutor.
    //   OpExecutor.executeUnion(OpBGP opBGP, QueryIterator input).
    //   And/Or rewrite to GRAPH <union> quads
    //   Default to quadded, not graph?

    /* TDB1:
     * Quads:
     * OpExecutorTDB1.decideGraphNode
     * BGP:
     * decideGraphNode => Union graph -> Node.ANY
     *
     *
     *
     * SolverRX3:
     * public executes:
     *     graph.getNodeTupleTable()  or ds.chooseNodeTupleTable
     * private SolverLib.execute/NodeTupleTable
     *  if ( Quad.isUnionGraph(graphNode) )
     *       graphNode = Node.ANY ;
     *   if ( Quad.isDefaultGraph(graphNode) )
     *       graphNode = null ;
     *
     * boolean anyGraph = (graphNode==null ? false : (Node.ANY.equals(graphNode))) ;
     * GRAPH <union>
     */

    /* TDB2: same in TDB1 OpExecutorTDB2.
     */


}
