/**
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

//package org.apache.jena.sparql.api;
package dev;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestQueryExecutionTimeout3 {
    static Graph g   = makeGraph(40000) ;
    static DatasetGraph dsg = DatasetGraphFactory.wrap(g) ;
    static Dataset ds  = DatasetFactory.wrap(dsg) ;

    private static final String ns  = "http://example/ns#" ;

    private static final String testQuery =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "SELECT DISTINCT ?item\n" +
            "WHERE\n" +
            "  { ?item  rdf:type  rdfs:Resource\n" +
            "    OPTIONAL { ?item  rdfs:seeAlso  ?l }\n" +
            "  }\n" +
            "ORDER BY ?l ?item\n"
//            +
//            "OFFSET  25\n" +
//            "LIMIT   25"
            ;

    static Graph makeGraph(int size) {
        Graph g = GraphFactory.createDefaultGraph();
        for (int i = 0; i < size; i++) {
            Node r = NodeFactory.createURI(ns + "r" + i);
            Node l = NodeFactory.createURI(ns + "l" + i);
            g.add(r, RDF.type.asNode(), RDFS.Resource.asNode());
            g.add(r, RDFS.seeAlso.asNode(), l);
        }
        return g;
    }

    @Test(expected = QueryCancelledException.class, timeout = 1000)
    public void testPlanTimeout() {
        QueryExec exec = QueryExecDatasetBuilder.create()
                .query(testQuery)
                .dataset(dsg)
                .timeout(200, TimeUnit.MILLISECONDS)
                .build();
        RowSet results = exec.select();
        assertTrue(results.hasNext());
    }
}
