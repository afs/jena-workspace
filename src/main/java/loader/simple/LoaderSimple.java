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

package loader.simple;

import loader.MonitorOutput;
import loader.base.LoaderBase;
import loader.base.LoaderOps;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFCountingBase;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderSimple extends LoaderBase {

    private static int DataTickPoint = 100_000;
    private static int DataSuperTick = 10;
    private StreamRDFCounting counting;
    private StreamRDF dest;
    
    public LoaderSimple(DatasetGraph dsg, Node graphName, MonitorOutput output, boolean showProgress) {
        super(dsg, graphName, output, showProgress);
        dest = LoaderOps.toNamedGraph(StreamRDFLib.dataset(dsg), graphName);
    }

    @Override
    public boolean bulkUseTransaction() {
        return true;
    }

    @Override
    protected StreamRDF getStream() {
        return new StreamRDFCountingBase(dest);
    }

    @Override
    protected void loadOne(StreamRDF dest, String source) {
        this.counting = new StreamRDFCountingBase(dest);
        LoaderOps.inputFile(counting, source, output, showProgress, DataTickPoint, DataSuperTick);
    }

    @Override
    public long countTriples() {
        return counting.countTriples();
    }

    @Override
    public long countQuads() {
        return counting.countQuads();
    }
}
