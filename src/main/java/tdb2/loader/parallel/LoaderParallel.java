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

package tdb2.loader.parallel;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import tdb2.loader.Loader;
import tdb2.loader.base.LoaderOps;

/** Bulk loader stream, parallel */ 
public class LoaderParallel implements Loader {
    protected final DatasetGraph dsg;
    protected final Node graphName;
    private final StreamRDF dest;
    protected final boolean showProgress;
    
    public LoaderParallel(DatasetGraph dsg, Node graphName, boolean showProgress) {
        this.dsg = dsg;
        this.graphName = graphName;
        // We don't do graphName
        this.dest = createDest(dsg, graphName);
        this.showProgress = showProgress;
    }
    
    private StreamRDF createDest(DatasetGraph dsg, Node graphName) {
        StreamRDF s = StreamRDFLib.dataset(dsg);
        return LoaderOps.toNamedGraph(s, graphName);
    }
    
    @Override
    public void startBulk() {
        // Move some BulkStreamLoader actions here. 
    }

    @Override
    public void finishBulk() {}

    @Override
    public void finishException() {}

    @Override
    public boolean bulkUseTransaction() {
        return false;
    }

    @Override
    public void load(List<String> filenames) {
        BulkStreamRDF stream = new BulkStreamLoader(dsg);
        stream.startBulk();
        // XXX Change the monitor to reflect the filename. 
        filenames.forEach(fn->RDFDataMgr.parse(stream, fn));
        stream.finishBulk();
    }
}
