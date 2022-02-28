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

package dev;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateProcessor;
import org.junit.Test;

public class TestSPARQLUpdate {
//    3.1.4 LOAD
//    3.1.5 CLEAR
//3.2 Graph Management
//    3.2.1 CREATE
//    3.2.2 DROP
//    3.2.3 COPY
//    3.2.4 MOVE
//    3.2.5 ADD

    @Test
    public void COPY_toNonExistingGraph_isError() {
        UpdateProcessor processor = prepare(
                                            datasetWithoutNamedGraphs(),
                "COPY DEFAULT TO <http://example.org/no-such-graph>");

        UpdateException thrown = assertThrows(UpdateException.class,
                                              () -> processor.execute());
        assertThat(thrown.getMessage(), containsString("No such graph"));
    }

    @Test
    public void COPY_SILENT_toNonExistingGraph_isSuccess() {
        UpdateProcessor processor = prepare(
                                            datasetWithoutNamedGraphs(),
                "COPY SILENT DEFAULT TO <http://example.org/no-such-graph>");

        processor.execute();
    }

    @Test
    public void MOVE_toNonExistingGraph_isError() {
        UpdateProcessor processor = prepare(
                                            datasetWithoutNamedGraphs(),
                "MOVE DEFAULT TO <http://example.org/no-such-graph>");

        UpdateException thrown = assertThrows(UpdateException.class,
                                              () -> processor.execute());
        assertThat(thrown.getMessage(), containsString("No such graph"));
    }

    @Test
    public void MOVE_SILENT_toNonExistingGraph_isSuccess() {
        UpdateProcessor processor = prepare(
                                            datasetWithoutNamedGraphs(),
                "MOVE SILENT DEFAULT TO <http://example.org/no-such-graph>");

        processor.execute();
    }

    private DatasetGraph datasetWithoutNamedGraphs() {
        return DatasetGraphOne.create(GraphFactory.createGraphMem());
    }

    private UpdateProcessor prepare(DatasetGraph dataset, String updateRequest) {
        return UpdateExec.dataset(dataset).update(updateRequest).build();
    }
}
