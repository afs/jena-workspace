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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TBS4190_Test {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testTBS4190() throws IOException {

        Location loc = Location.mem("4190");

        String replacementChar = "\uFFFD";

        Path tdbLocation = tmp.getRoot().toPath().resolve("TBS-4190");
        loc = Location.create(tdbLocation.toString());
        Files.createDirectories(tdbLocation);

        Dataset tdbDataset = TDBFactory.createDataset(loc);
        tdbDataset.executeWrite(()->{});

        try {
            Model m = tdbDataset.getDefaultModel();
            m.add(RDF.Bag, RDFS.label, replacementChar);
        } finally {
            tdbDataset.close();
        }

        //TDBInternal.expel(loc);

        Dataset sameDatasetReopened = TDBFactory.createDataset(loc);
        try {
            Model m = sameDatasetReopened.getDefaultModel();
            String literalFromTdb = m.listStatements().next().getString();
            assertEquals(replacementChar, literalFromTdb);
        } finally {
            sameDatasetReopened.close();
        }
    }
}
