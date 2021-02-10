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

package datasetdescription;

import java.util.Set;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.graph.GraphOps ;
import org.apache.jena.sparql.graph.GraphUnionRead ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.NodeUtils ;

/**
 * Given a {@link DatasetDescription} and a {@link DatasetGraph}, create a read-only
 * {@link DatasetGraph} with the graphs taken from the underlying dataset.
 * Use {@code <urn:x-arq:DefaultGraph>} ({@link Quad#defaultGraphIRI} 
 * and {@code <urn:x-arq:UnionGraph>} ({@link Quad#unionGraph}) as needed.
 */
public class DSD_Dataset implements DatasetDescriptionProcessor {
    @Override
    public DatasetGraph process(DatasetDescription description, DatasetGraph dsg, Context context) {
        if ( description.isEmpty() )
            return new DatasetGraphReadOnly(dsg) ;
        
        Set<Node> defaultGraphs = NodeUtils.convertToSetNodes(description.getDefaultGraphURIs()) ; 
        Set<Node> namedGraphs = NodeUtils.convertToSetNodes(description.getNamedGraphURIs()) ;
        
        // The default graph.
        Graph dft = new GraphUnionRead(dsg, defaultGraphs) ;
        // Set the transactional to be the dsg.
        DatasetGraph dsg2 = new DatasetGraphMapLink(dft) {
            @Override public void begin(ReadWrite mode)         { dsg.begin(mode) ; }
            @Override public void commit()                      { dsg.commit(); }
            @Override public boolean isInTransaction()          { return dsg.isInTransaction() ; }
            @Override public void end()                         { dsg.end(); }
            @Override public boolean supportsTransactions()     { return dsg.supportsTransactions() ; }
            @Override public boolean supportsTransactionAbort() { return dsg.supportsTransactionAbort() ; }
        } ;

        // The named graphs.
        for ( Node gn : namedGraphs ) {
            Graph g = GraphOps.getGraph(dsg, gn) ;
            if ( g != null )
                dsg2.addGraph(gn, g) ;
        }

        if ( dsg.getContext() != null )
            dsg2.getContext().putAll(dsg.getContext()) ;

        dsg2 = new DatasetGraphReadOnly(dsg2) ;
        // Record what we've done.
        // ARQConstants.sysDatasetDescription wil have been copied over.
        dsg2.getContext().set(ARQConstants.symDatasetDefaultGraphs, defaultGraphs) ;
        dsg2.getContext().set(ARQConstants.symDatasetNamedGraphs,   namedGraphs) ;
        return dsg2 ;
    }
}
