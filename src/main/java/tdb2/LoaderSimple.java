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

import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderSimple extends LoaderBase {

    private static int DataTickPoint = 100_000;
    private static int DataSuperTick = 10;
    
    public LoaderSimple(DatasetGraph dsg, Node graphName) {
        super(dsg, graphName);
    }
    
    @Override
    protected void loadOne(StreamRDF dest, String source, boolean showProgress) {
        StreamRDF sink = dest;
        ProgressMonitor monitor = null;
        if ( showProgress ) { 
            String basename = FileOps.splitDirFile(source).get(1);
            monitor = ProgressMonitor.create(BulkLoader.LOG, basename, DataTickPoint, DataSuperTick); 
            sink = new ProgressStreamRDF(sink, monitor);
        }
        if ( monitor!= null )
            monitor.start();
        
        sink.start();
        RDFDataMgr.parse(sink, source);
        sink.finish();
        if ( monitor!= null ) {
            monitor.finish();
            monitor.finishMessage();
        }
    }
    
    private StreamRDF toNamedGraph(StreamRDF dest, String graphName) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(graphName);
        // Rename the default graph triples. Data quads are dropped.
        Node gn = NodeFactory.createURI(graphName);
        return new StreamRDFWrapper(dest) {
            @Override
            public void triple(Triple triple) {
                quad(Quad.create(gn, triple));
            }
            // Drop quads.
            @Override public void quad(Quad quad) {}
        };
    }
}
