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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;

// "Manufacturer"
// "Producer"
// "Assembler2"

// Use SPARQL more in G assemblers to extract shapes? Error messages aren't as good?

public class DevGraphAssemblers {
    public static void main(String...a) {
        Graph descriptionGraph = GraphFactory.createDefaultGraph();
        Node node1 = SSE.parseNode("ex:node");
        Node node2 = SSE.parseNode("ex:nodex");

        BuildContext buildCxt = new BuildContext();
        Manufacturer<DatasetGraph> constructor = new PrototypeTIM();

        Node description1 = node1;
        Node description2 = node1;

        DatasetGraph dsg = constructor.construct(buildCxt, descriptionGraph, description1);
        DatasetGraph dsg2 = constructor.construct(buildCxt, descriptionGraph, description2);

        if ( description1.equals(description2) && dsg != dsg2 )
            System.out.println("*** Unexpected: Same description nodes, different objects");
        if ( ! description1.equals(description2) && dsg == dsg2 )
            System.out.println("*** Unexpected: Different description nodes, same objects");

        System.out.println("DONE");
    }
}