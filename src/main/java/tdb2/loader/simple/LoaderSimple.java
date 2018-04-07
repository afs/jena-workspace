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

package tdb2.loader.simple;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import tdb2.loader.base.LoaderBase;
import tdb2.loader.base.LoaderOps;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderSimple extends LoaderBase {

    private static int DataTickPoint = 100_000;
    private static int DataSuperTick = 10;
    
    public LoaderSimple(DatasetGraph dsg, Node graphName, boolean showProgress) {
        super(dsg, graphName, showProgress);
    }

    @Override
    protected StreamRDF createDest(DatasetGraph dsg, Node graphName) {
        return LoaderOps.toNamedGraph(StreamRDFLib.dataset(dsg), graphName);
    }
    
    @Override
    public boolean bulkUseTransaction() {
        return true;
    }

    @Override
    protected void loadOne(StreamRDF dest, String source) {
        LoaderOps.inputFile(dest, source, showProgress, DataTickPoint, DataSuperTick);
    }
}
