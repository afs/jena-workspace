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

package z_archive.jena_1746_tdb2_cache;


import static org.apache.jena.tdb2.sys.StoreConnection.connectCreate;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TDB2_Rollbacktest_Original {

      public static final String GRAPH_NAME = "http://virzrt.hu/test-model";
      public static final String FILL_GRAPH_NAME = "http://virzrt.hu/test-model-fill";
      public static final String RES_NAME = "http://virzrt.hu/test-res#";
      public static final String PROP_NAME = "http://virzrt.hu/test-prop#";

      @Test
      public void testRollbackCacheFail() throws IOException {
        final Path tempDirWithPrefix = Files.createTempDirectory("jenaDatabaseRollbackTest");

        // Make the cache small enough

        final StoreParams params =
            StoreParamsBuilder.create(StoreParams.getDftStoreParams())
                .node2NodeIdCacheSize(26)
                .nodeId2NodeCacheSize(26)
                .build();
        final Dataset tdb =
            DatasetFactory.wrap(
                connectCreate(Location.create(tempDirWithPrefix.toAbsolutePath().toString()), params)
                    .getDatasetGraph());

        // Create some fix resources and properties:
        Resource[] resources = new Resource[3];
        for (int i = 0; i < 3; i++) {
          resources[i] = ResourceFactory.createResource(RES_NAME + Integer.toString(i));
        }

        Property[] properties = new Property[2];
        for (int i = 0; i < 2; i++) {
          properties[i] = ResourceFactory.createProperty(PROP_NAME + Integer.toString(i));
        }

        // Create a graph to store and flush cache if necessary

        tdb.begin(TxnType.WRITE);
        Model fillModel = tdb.getNamedModel(FILL_GRAPH_NAME);
        for (int i = 0; i < 40; i++) {
          fillModel.add(fillModel.createResource(), RDFS.comment, Integer.toString(i));
        }
        tdb.commit();

        // ********************************************/
        // Transaction to fill cache, but rollbacked:

        tdb.begin(TxnType.WRITE);

        Model namedModel = tdb.getNamedModel(GRAPH_NAME);
        for (int i = 0; i < resources.length; i++) {
          for (int j = 0; j < properties.length; j++) {

            // This method creates some nodes: resource, property and an anonymous node and the value.
            // It fills Graph_NAME (1), resources (4) and properties (2), anonym (8) and value nodes (8)
            addInfoToModel(namedModel, resources[i], properties[j]);
          }
        }

        assertEquals(namedModel.size(), 2 * resources.length * properties.length);
        // Cache is nearly filled, but we discard the real data
        tdb.abort();

        // *************************
        // Abnormal: the cache in NodeTableCache is filled up with invalid node id-s

        tdb.begin(TxnType.WRITE);

        // Create some more date to make some offset in the node data file. It creates 8 data in cache,
        // but
        // reuses some id.

        fillModel = tdb.getNamedModel(FILL_GRAPH_NAME);
        for (int i = 0; i < 8; i++) {
          fillModel.add(fillModel.createResource(), RDFS.comment, Integer.toString(i));
        }

        namedModel = tdb.getNamedModel(GRAPH_NAME);
        // Rebuild the graph, but the order is backward, and anonym items are new.
        for (int i = resources.length - 1; i >= 0; i--) {
          for (int j = properties.length - 1; j >= 0; j--) {
            addInfoToModel(namedModel, resources[i], properties[j]);
          }
        }
        final long size = namedModel.size();
        assertEquals(size, resources.length * properties.length * 2);

        // There is a big chance to flush out some entry in the cache, but can not read it back
        // as these entries was buggy, and pointed to wrong id.

        fillModel = tdb.getNamedModel(FILL_GRAPH_NAME);
        List<Statement> flushList = new ArrayList<>();
        fillModel.listStatements().forEachRemaining(flushList::add);
        assertEquals(flushList.size(), 48);

        // Cached entries flushed out, check again the size:
        // Nothing changed in namedModel, so size should be unchanged
        assertEquals(namedModel.size(), size); //  *** FAILS! sizes() gives 0!

        // ****** This FAILS too
        List<Statement> readback = new ArrayList<>();
        namedModel.listStatements().forEachRemaining(readback::add);
        assertEquals(readback.size(), size);

        // **** Writing out the model UNEXPECTEDLY causes Thrift error, as it reads the whole model.
        tdb.getUnionModel().write(System.out, "TURTLE");

        tdb.commit();

        tdb.close();
      }

      private void addInfoToModel(
          final Model namedModel, final Resource resource, final Property property) {
        final Resource anon = namedModel.createResource();
        namedModel.add(resource, property, anon);
        namedModel.add(anon, property, UUID.randomUUID().toString());
      }
    }
