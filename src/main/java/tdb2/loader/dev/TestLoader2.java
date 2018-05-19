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

package tdb2.loader.dev;

import static org.junit.Assert.assertTrue;

import java.io.PrintStream;

import org.apache.jena.atlas.io.NullOutputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.junit.Test;
import tdb2.loader.Loader;
import tdb2.loader.LoaderFactory;
import tdb2.loader.base.MonitorOutput;
import tdb2.loader.base.ProgressMonitorFactory;

/** Tests of the TDB2 loader.
 * These tests are functional tests, not performance. 
 */
public class TestLoader2 {
    private static PrintStream ps = new PrintStream(NullOutputStream.sink());
    private static MonitorOutput output = ProgressMonitorFactory.outputTo(ps);
    
    private Loader createLoader(DatasetGraph dsg) {
        return LoaderFactory.basicLoader(dsg, output);
    }
    
    @Test public void loader_01() {
        String datafile = "testing/Loader/data-2.nt";
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Loader loader = createLoader(dsg);
        loader.startBulk();
        loader.load(datafile);
        loader.finishBulk();
        Graph g = RDFDataMgr.loadGraph(datafile);
        Txn.executeRead(dsg, ()->
            assertTrue(g.isIsomorphicWith(dsg.getDefaultGraph()))
        );
    }
    
    // Tests prefixes.
    
    // Test load-to-graph.
    
    //
    
}
