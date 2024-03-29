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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

public class DevReorderTDB2 {

    public static void main(String[] args) {
        JenaSystem.init();

        String DIR = "DB";
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);

        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DIR);
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        ReorderTransformation reorder = dsgtdb.getReorderTransform();
        System.out.println(reorder.getClass().getSimpleName());

    }

}
