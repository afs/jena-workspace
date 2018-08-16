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

package dataset;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

/** Library of database retrieval filters for TDB2 datasets */
public class FiltersTDB2 {

    /** Modify the context to add/replace with the given filter predicate */ 
    public static void withFilter(Context context, Predicate<Tuple<NodeId>> filter) {
        context.set(SystemTDB.symTupleFilter, filter);
    }
    
    /** Add a security filter a TDB2 {@link DatasetGraph}.
     * The filter specifies (return true) for Tuples to accept. 
     */ 
    public static SecurityFilterTDB2 securityFilter(DatasetGraph dsg, Node... namedGraphs) {
        return securityFilter(dsg, Arrays.asList(namedGraphs));
    }

    /** Add a security filter a TDB2 {@link DatasetGraph}.
     * The filter specifies (return true) for Tuples to accept. 
     */ 
    public static SecurityFilterTDB2 securityFilter(DatasetGraph dsg, Collection<Node> namedGraphs) {
        if ( ! TDBInternal.isTDB2(dsg) )
            throw new IllegalArgumentException("DatasetGraph is not TDB2-backed");
        List<NodeId> x =  
            Txn.calculateRead(dsg, ()->{
                NodeTable nt = TDBInternal.getDatasetGraphTDB(dsg).getQuadTable().getNodeTupleTable().getNodeTable();
                return 
                    ListUtils.toList(
                        namedGraphs.stream()
                        .map(n->nt.getNodeIdForNode(n))
                        .filter(Objects::nonNull)
                        );
            });
        
        return new SecurityFilterTDB2(x, false);
    }
}
