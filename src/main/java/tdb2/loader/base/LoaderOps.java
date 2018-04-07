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

package tdb2.loader.base;

import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;
import tdb2.MonitorOutput;
import tdb2.loader.BulkLoader;

/** Operations for the Loader processes */ 
public class LoaderOps {
    
    /** Parse one file, with an optional progress monitor */ 
    public static void inputFile(StreamRDF dest, String source, MonitorOutput output, boolean showProgress, int DataTickPoint, int DataSuperTick) {
        StreamRDF sink = dest;
        ProgressMonitor2 monitor = null;
        if ( showProgress ) { 
            String basename = FileOps.splitDirFile(source).get(1);
            monitor = ProgressMonitor2.create(BulkLoader.LOG, basename, DataTickPoint, DataSuperTick); 
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
    
    /** 
     * Convert to quads: triples from the default graph of parsing become quads using the {@code graphName}. 
     * If {@code graphName} is null, return the {@code stream} argument.
     */ 
    public static StreamRDF toNamedGraph(StreamRDF stream, Node graphName) {
        Objects.requireNonNull(stream);
        if ( graphName == null )
            return stream;
        // Rename the default graph triples. Data quads are dropped.
        return new StreamRDFWrapper(stream) {
            @Override
            public void triple(Triple triple) {
                quad(Quad.create(graphName, triple));
            }
            // Drop quads.
            @Override public void quad(Quad quad) {}
        };
    }
}
