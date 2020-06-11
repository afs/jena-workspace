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

package tdb2;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.sys.SystemTDB;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder;
import org.apache.jena.tdb2.sys.DatabaseConnection;
import org.apache.jena.tdb2.sys.TDBInternal;

public class DatabaseBuilder {

    public static void main(String ...args) {
        DatasetGraph dsg = DatabaseBuilder.create()
            .location("DB2")
            .nodeCacheSize(2*SystemTDB.Node2NodeIdCacheSize,
                           2*SystemTDB.NodeId2NodeCacheSize)
            .build();
        StoreParams params1 = TDBInternal.getDatasetGraphTDB(dsg).getStoreParams();

        System.out.println(params1);

    }


    private StoreParamsBuilder paramBuilder;
    private Location location;

    public DatabaseBuilder() {
        // All the defaults.
        paramBuilder = StoreParamsBuilder.create();
    }

    public static DatabaseBuilder create() {
        return new DatabaseBuilder();
    }

    public DatabaseBuilder location(String pathName) {
        location(Location.create(pathName));
        return this;
    }

    public DatabaseBuilder location(Location location) {
        this.location = location;
        return this;
    }

    public DatabaseBuilder mem() {
        this.location = Location.mem();
        return this;
    }

    /** Set */
    public DatabaseBuilder small() {
        paramBuilder
            .node2NodeIdCacheSize(10000)
            .nodeId2NodeCacheSize(10000)
            .nodeMissCacheSize(100);
        return this;
    }

    public DatabaseBuilder nodeCacheSize(int node2NodeIdCacheSize, int nodeId2NodeCacheSize) {
        paramBuilder.node2NodeIdCacheSize(node2NodeIdCacheSize);
        paramBuilder.nodeId2NodeCacheSize(nodeId2NodeCacheSize);
        return this;
    }

    enum CacheSize { SMALL(SystemTDB.Node2NodeIdCacheSize/10, SystemTDB.NodeId2NodeCacheSize/10),
                     MEDIUM(SystemTDB.Node2NodeIdCacheSize, SystemTDB.NodeId2NodeCacheSize),
                     LARGE(2*SystemTDB.Node2NodeIdCacheSize, 2*SystemTDB.NodeId2NodeCacheSize)
                     ;
        public final int Node2NodeIdCacheSize;
        public final int NodeId2NodeCacheSize;
        CacheSize(int Node2NodeIdCacheSize, int NodeId2NodeCacheSize) {
            this.Node2NodeIdCacheSize = Node2NodeIdCacheSize;
            this.NodeId2NodeCacheSize = NodeId2NodeCacheSize;
        }
    }

    public int node2NodeIdCacheSize() { return  paramBuilder.getNode2NodeIdCacheSize(); }

    public int nodeId2NodeCacheSize() { return  paramBuilder.getNodeId2NodeCacheSize(); }


    // Just the caches.

    //this.Node2NodeIdCacheSize   = other.Node2NodeIdCacheSize;
    //this.NodeId2NodeCacheSize   = other.NodeId2NodeCacheSize;
    //nodeMissCacheSize


    public DatasetGraph build() {
        // Check if exists.
        // Check if same params.
        DatabaseConnection dbConn = DatabaseConnection.connectCreate(location, paramBuilder.build());
        return dbConn.getDatasetGraph();
    }

    public Dataset dataset() {
        DatasetGraph dsg = build();
        return DatasetFactory.wrap(dsg);
    }

}
