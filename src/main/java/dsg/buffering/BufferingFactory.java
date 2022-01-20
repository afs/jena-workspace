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

package dsg.buffering;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixesDboeFactory;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixMap;
import org.apache.jena.dboe.storage.prefixes.StoragePrefixesView;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;

public class BufferingFactory {

    public static PrefixMapping xcreate(StoragePrefixes storagePrefixes, Node graphNode) {
        // Too many layers!
        // Dataset storage : storagePrefixes

//        StoragePrefixMap spv = ( graphNode == null || Quad.isDefaultGraph(graphNode) )
//                ? StoragePrefixesView.viewDefaultGraph(storagePrefixes)
//                : StoragePrefixesView.viewGraph(storagePrefixes, graphNode);

        StoragePrefixMap spv =  StoragePrefixesView.viewDataset(storagePrefixes);
        PrefixMap pmap = PrefixesDboeFactory.newPrefixMap(spv);
        return Prefixes.adapt(pmap);
    }
}
