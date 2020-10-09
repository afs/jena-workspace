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

package dsg.prefixes;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.*;
import org.apache.jena.dboe.storage.simple.StoragePrefixesMem;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;

public class NotesPrefixes {

    // Plan
    //    PrefixMap with dboe:PrefixMapI stuff
    //    Turtle rules unless otherwise stated.
    // [ ] Add get(String prefix) -> URI to PrefixMap
    // [ ] Add match(String uri) -> prefix or null

    // [ ] Add getPrefixMap to Graph - means moving PrefixMap(I) and memory impl PrefixMapStd (as PrefixMapMem())
    // [ ] Graph.getPrefixMapping wraps a PrefixMap.    Library function for getPrefixMap(Graph)->PrefixMap.
    // [ ] Remove dboe:PrefixMappingOverPrefixMapI, use PrefixMappingAdapter
    //        PrefixMapFactory.create(PrefixMapping pmap) -> PrefixMap
    //        see src/test/.../TestPrefixMapOverPrefixMapping
    // [ ] XML rules for

    // [ ] Add basic implementations as default methods into PrefixMap

    // ?? PrefixMapping
    // MonitorModel, MonitorGraph - deprecate.
    // sparql.Prologue - remove! Merge into a Query, UpdateRequest base.
    // WriterSSE - uses sparql.Prologue

    // Current:
    // 1--
    //    PrefixMapping over PrefixMap
    //      dboe:PrefixMappingOverPrefixMapI
    //          pkg org.apache.jena.dboe.storage.prefixes
    //      PrefixMappingAdapter <<**** "PrefixMappingOverPrefixMap"
    //          pkg org.apache.jena.sparql.graph
    //        then interface PrefixMapI extends Iterable<PrefixEntry>, PrefixMap

    //    Add PrefixMapI.get to PrefixMap
    //       issue: public StoragePrefixMap getPrefixMapStorage(); <- graph/storage.
    //       Why do we need this?
    //       Only called by StoragePrefixesMem.iterator.
    //
    // 2--
    // PrefixMapping
    //    pkg org.apache.jena.sparql.graph
    //  with GraphPrefixesProjection over DatasetPrefixStorage
    //   DatasetPrefixStorage > DatasetPrefixStorageInMemory, DatasetPrefixesTDB

    // dboe:StoragePrefixMap -- graph view
    // dboe:StoragePrefixes -- database view.

    //
    // 3--
    //  org.apache.jena.dboe.storage.prefixes
    //    PrefixLib
    // org.apache.jena.util.SplitIRI
    //
    // 4-- PrefixMapI2 : PrefixMapI and PrefixMapping
    //   No PrefixMapI2 . Do by adapter.

    // 5-- PrefixMapOverPrefixMapping

    // dboe::StoragePrefixes
    //   StoragePrefixes(database), StoragePrefixesMap(graph)

    // -- Plan
    // PrefixMapI vs PrefixMap
    //   PrefixMapI.get
    //   PrefixMapI.prefixes
    //   PrefixMapI.stream()
    //   PrefixMapI.iterator() via default stream().iterator();

    // and default methods down to
    //  add(p,u), delete(p), conatinsPrefix and StoragePrefixMap getPrefixMapStorage()
    // ==>. Merge PrefixMapI into PrefixMap.
    //   Is there a storage interface?   Yes - dboe:StoragePrefixes


    // (ARQ) StoragePrefixMap/Graph vs (DBOE) DatasetPrefixStorage
    ///


    // NOW PrefixMapI: replaces PrefixMap: (with @deprecated?)

    // -------------- OLD NOTES
    // This package
    //   PrefixMapI2 -- PrefixMapI2 extends PrefixMapI, PrefixMapping

    // Steps/Now to Jena.
    // What about XML? PrefixMappingImpl.

    // ----
    // Migrate Graph from PrefixMapping to PrefixMap.

    // PrefixMapIBase -- no copy getMapping.
    // ParserProfile.makeIRI

    //   Combine with iri4ld?
    // PrefixMapTDB2 - isn't StoragePrefixes switching/txn safe?

    // -------------------------

    /* From PrefixMapI
     *  * @implNote The package {@code org.apache.jena.dboe.storage.prefixes} in module
     *     {@code jena-dboe-storage} provides implementations that work with
     *     {@code StoragePrefixes} which is dataset provision of prefixes on per-named
     *     graph basis.
     */


    // Store4 plan.
    // NOW PrefixMapI: replaces PrefixMap: (with @deprecated?)


    // Prefixes
    //   StoragePrefixes - dataset prefixes storage.
    //   StoragePrefixMap - graph prefix storage (StoragePrefixesView:  StoragePrefixes -> StoragePrefixMap)

    // PrefixesFactory
    //   PrefixMapI: replaces PrefixMap: API focused.  (PrefixMapBase:  StoragePrefixMap -> PrefixMapI)
    //   PrefixMapping: old API only: PrefixMappingOverPrefixMapI: PrefixMap to ? PrefixMapping)

    // PrefixesFactory -> "Prefixes"?
    // Migrate Graph from PrefixMapping to PrefixMap.
    // Too many layers? Or use final classes


    public static void main(String ...a) {
        {
            //DatasetGraph dsg = TDBFactory.createDatasetGraph();
            DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
            Graph graph = dsg.getDefaultGraph();

            RDFWriter.create().format(RDFFormat.TURTLE_PRETTY).source(graph).build()
                .asString();

        }


        // Too many layers!
        // StoragePrefixesView - add direct to PrefixMapping? = projection, PrefixMapping API.

        // Dataset storage.
        StoragePrefixes sPrefixes = new StoragePrefixesMem();

        // Storage oriented view for a single graph : StoragePrefixesView
        StoragePrefixMap spv = StoragePrefixesView.viewDefaultGraph(sPrefixes);

        // Build PrefixMapI into StoragePrefixMap?

        // PrefixMapI impl is PrefixMapBase > PrefixMapTDB2
        // PrefixMap : combine PrefixMapI into StoragePrefixMap? :
        // Add PrefixMapping to ? PrefixMapOverPrefixMapI
        //   PrefixMapBase = PrefixMapI over StoragePrefixMap

        PrefixMapI pm = PrefixesFactory.newPrefixMap(spv);

        // Model/graph PrefixMapping
        //   PrefixMappingOverPrefixMapI
        PrefixMapping pmap = PrefixesFactory.newPrefixMappingOverPrefixMapI(pm);
    }

}