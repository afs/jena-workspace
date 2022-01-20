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

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixesDboeFactory;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixesView;
import org.apache.jena.dboe.storage.simple.StoragePrefixesSimpleMem;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;

public class DevBuffering {

    // NotesBuffering

    public static PrefixMapping create() {

        Graph graph;

        // Too many layers!
        // StoragePrefixesView - add direct to PrefixMapping? = projection, PrefixMapping API.

        // Dataset storage.
        StoragePrefixes sPrefixes = new StoragePrefixesSimpleMem();

        // Storage oriented view for a single graph : StoragePrefixesView
        StoragePrefixMap spv = StoragePrefixesView.viewDataset(sPrefixes);

        // Build PrefixMapI into StoragePrefixMap?

        // PrefixMapI impl is PrefixMapBase > PrefixMapTDB2
        // PrefixMap : combine PrefixMapI into StoragePrefixMap? :
        // Add PrefixMapping to ? PrefixMapOverPrefixMapI
        //   PrefixMapBase = PrefixMapI over StoragePrefixMap

        PrefixMap pm = PrefixesDboeFactory.newPrefixMap(spv);
        return Prefixes.adapt(pm);
    }

}
