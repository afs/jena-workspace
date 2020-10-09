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
import org.apache.jena.dboe.storage.prefixes.PrefixMapI;
import org.apache.jena.dboe.storage.prefixes.PrefixesFactory;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixesView;
import org.apache.jena.dboe.storage.simple.StoragePrefixesMem;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;

public class DevPrefixes {

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