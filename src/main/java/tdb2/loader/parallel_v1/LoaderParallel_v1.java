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

package tdb2.loader.parallel_v1;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.base.LoaderBase;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

// TEMPORARY - kept for timing experiments.

/** Bulk loader stream, parallel */ 
public class LoaderParallel_v1 extends LoaderBase {
    public static final int DataTickPoint   = 100_000;
    public static final int DataSuperTick   = 10;
    public static final int IndexTickPoint  = 1_000_000;
    public static final int IndexSuperTick  = 10;
    
    private final DatasetGraphTDB dsgtdb;
    private final StreamRDF stream;
    private BulkStreamLoader_v1 bulkLoader;
    
    public LoaderParallel_v1(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        // XXX Calls createDest :-(
        super(dsg, graphName, output);
        dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        this.bulkLoader = new BulkStreamLoader_v1(dsg, output);
        this.stream = LoaderOps.toNamedGraph(bulkLoader, graphName);  
    }
    
    @Override
    public StreamRDF stream() {
        return stream;
    }
    
    @Override
    public boolean bulkUseTransaction() {
        // Manipulate the transactions directly by component. 
        return false;
    }

    @Override
    public void startBulk() {
        // Lock everyone else out while we multithread.
        dsgtdb.getTxnSystem().getTxnMgr().startExclusiveMode();
        super.startBulk();
        bulkLoader.startBulk();
    }

    @Override
    public void finishBulk() {
        bulkLoader.finishBulk();
        super.finishBulk();
        dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }
    
    @Override
    public void finishException(Exception ex) {
        dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }
    
    @Override
    public long countTriples() {
        return bulkLoader.getCountTriples();
    }

    @Override
    public long countQuads() {
        return bulkLoader.getCountQuads();
    }

    @Override
    protected void loadOne(String filename) {
        LoaderOps.inputFile(stream, filename, output, DataTickPoint, DataSuperTick);
    }
}
