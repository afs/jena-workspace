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

package assembler2;

import static org.apache.jena.assembler.JA.data;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class PrototypeTIM implements Manufacturer<DatasetGraph>{

    @Override
    public DatasetGraph newItem(BuildContext cxt, Graph descriptionGraph, Node description) {
        /*
         *  :dataset rdf:Type ja:InMemory...;
         *      ja:context [ ja:cxtName ?name ; ja:cxtValue ?value ] }" ;
         *      :data "data.ttl";
         *      :data "data.trig";
         *      :nameGraph [ :graphName <SomeName> ;
         *                   :data <file1.ttl>;
         *                   :data <file2.ttl> ];
         *      .
         */

        final DatasetGraph dataset = DatasetGraphFactory.createTxnMem();

        ConstructorLib.mergeContext(descriptionGraph, description, dataset.getContext());

        GNav.multiValueAsString(descriptionGraph, description, data.asNode() )
            .forEach(dataURI -> RDFDataMgr.read(dataset, dataURI));

        GNav.multiValue(descriptionGraph, description, pNamedGraph.asNode())
            .forEach(ngDesc -> {
                Node graphName = GNav.getAsNode(descriptionGraph, ngDesc, pGraphName.asNode());
                Graph g = dataset.getGraph(graphName);
                GNav.multiValueAsString(descriptionGraph, ngDesc, data.asNode())
                        .forEach(dataURI -> RDFDataMgr.read(g, dataURI));
            });

        return dataset;
    }
}
