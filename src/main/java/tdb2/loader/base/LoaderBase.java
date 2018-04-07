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

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import tdb2.loader.Loader;

/** Simple bulk loader framework.
 * It puts a write-transaction around the whole process if {@link #bulkUseTransaction}
 * returns true and then calls abstract {@link #loadOne(StreamRDF, String)}
 * for each file.
 * <p>
 * If a graph name is provided, it converts triples to quads in that named graph.  
 */ 
public abstract class LoaderBase implements Loader {

    protected final DatasetGraph dsg;
    protected final Node graphName;
    private final StreamRDF dest;
    protected final boolean showProgress;
    
    protected LoaderBase(DatasetGraph dsg, Node graphName, boolean showProgress) {
        this.dsg = dsg;
        this.graphName = graphName;
        this.dest = createDest(dsg, graphName);
        this.showProgress = showProgress;
    }
    
    @Override
    public void startBulk() {
        if ( bulkUseTransaction() )
            dsg.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        if ( bulkUseTransaction() ) {
            dsg.commit();
            dsg.end();
        }
    }

    @Override
    public void finishException() {
        if ( bulkUseTransaction() ) {
            dsg.abort();
            dsg.end();
        }
    }

    @Override
    public void load(List<String> filenames) {
        try { 
            filenames.forEach(fn->loadOne(dest, fn));
        } catch (Exception ex) {
            finishException();
            throw ex;
        }
    }

    /** Subclasses must provide a setting. */ 
    @Override
    public abstract boolean bulkUseTransaction();

    protected abstract void loadOne(StreamRDF dest, String filename);
    
    protected abstract StreamRDF createDest(DatasetGraph dsg, Node graphName);
    
}
