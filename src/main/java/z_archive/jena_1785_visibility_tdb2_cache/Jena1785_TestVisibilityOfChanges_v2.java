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

package z_archive.jena_1785_visibility_tdb2_cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// JENA-1785

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class Jena1785_TestVisibilityOfChanges_v2 {
//    private Dataset dataset;
//
//    @Before
//    public void before() {
//        dataset = TDB2Factory.connectDataset(Location.mem());
//    }
//
//    @After
//    public void after() {
//        dataset.close();
//    }

//    @Test
//    public void visibilityOfNewlyCreatedResources() {
//        Dataset dataset = TDB2Factory.connectDataset(Location.mem());
//        Resource resource = ResourceFactory.createResource();
//        assertFalse(Txn.calculateRead(dataset, () -> dataset.getDefaultModel().containsResource(resource)));
//        Txn.executeWrite(dataset, () -> {
//            dataset.getDefaultModel().add(resource, RDFS.label, "I exist!");
//            assertTrue(dataset.getDefaultModel().containsResource(resource));
//            int cacheSize = 2000; // A magic constant from NodeTableCache
//            for (int i = 0; i < cacheSize; i++) {
//                Resource newResource = ResourceFactory.createResource();
//                dataset.getDefaultModel().add(newResource, RDFS.label, "I'm here to pollute the cache");
//            }
//            assertTrue("Resource should be visible within the write transaction",
//                    dataset.getDefaultModel().containsResource(resource));
//        });
//        assertTrue("Resource should be visible after commit",
//                Txn.calculateRead(dataset, () -> dataset.getDefaultModel().containsResource(resource)));
//    }

    // Check NodeTableCache.getNodeForNodeIdCache etc

    @Test // Node version
    public void visibilityOfNewlyCreatedResources_Graph() {
        DatasetGraph dataset = DatabaseMgr.connectDatasetGraph(Location.mem());
        //Node x = NodeFactory.createBlankNode();
        Node x = NodeFactory.createURI("http://example/x-node");

        System.err.println("Test for the node - dataset is empty");
        assertFalse(Txn.calculateRead(dataset, () -> dataset.find(Quad.defaultGraphIRI, x, null, null).hasNext()));
        Txn.executeWrite(dataset, () -> {
            System.err.println("W: add node");
            dataset.add(Quad.defaultGraphIRI, x, RDFS.Nodes.label, NodeFactory.createLiteral("I exist!"));
            assertTrue(dataset.contains(Quad.defaultGraphIRI, x, RDFS.Nodes.label, NodeFactory.createLiteral("I exist!")));
            System.err.println("W: loop");
            int cacheSize = 20000;
            for (int i = 0; i < cacheSize; i++) {
                Node x1 = NodeFactory.createBlankNode();
                //System.err.println("W: loop "+i);
                dataset.add(Quad.defaultGraphIRI, x1, RDFS.Nodes.label, NodeFactory.createLiteral("I'm here to pollute the cache"));
                // Makes it work!
                //dataset.contains(Quad.defaultGraphIRI, x, null, null);
            }
            System.err.println("W: end loop");
            assertTrue("Resource should be visible within the write transaction ",
                dataset.contains(Quad.defaultGraphIRI, x, null, null));
        });
        System.err.println("After TxnW");
        assertTrue("Resource should be visible after commit",
                Txn.calculateRead(dataset, () -> dataset.contains(Quad.defaultGraphIRI, x, null, null)));
    }

}
