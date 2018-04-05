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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** Simple bulk loader framework. Calls abstract loadOne */ 
public abstract class LoaderBase implements Loader {

    private final DatasetGraph dsg;
    private final Node graphName;
    private final StreamRDF dest;
    
    protected LoaderBase(DatasetGraph dsg, Node graphName) {
        this.dsg = dsg;
        this.graphName = graphName;
        this.dest = toNamedGraph(StreamRDFLib.dataset(dsg), graphName);
    }
    
    @Override
    public void startBulk() {
        if ( inTransaction() )
            dsg.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        if ( inTransaction() ) {
            dsg.commit();
            dsg.end();
        }
    }

    @Override
    public void finishException() {
        if ( inTransaction() ) {
            dsg.abort();
            dsg.end();
        }
    }

    @Override
    public boolean inTransaction() {
        return false;
    }

    @Override
    public void loadOne(String filename) {
        loadOne(dest, filename, true);
    }
    
    protected abstract  void loadOne(StreamRDF dest, String filename, boolean showProgress); 
    
    private static StreamRDF toNamedGraph(StreamRDF dest, Node graphName) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(graphName);
        // Rename the default graph triples. Data quads are dropped.
        return new StreamRDFWrapper(dest) {
            @Override
            public void triple(Triple triple) {
                quad(Quad.create(graphName, triple));
            }
            // Drop quads.
            @Override public void quad(Quad quad) {}
        };
    }
}
