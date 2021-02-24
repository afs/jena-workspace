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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.tdb.setup.StoreParams;
import org.apache.jena.tdb.setup.StoreParamsCodec;
import org.apache.jena.tdb.sys.TDBInternal;

public class RunFusekiSmall
{
    public static void main(String ... a) throws Exception {
        FusekiLogging.setLogging();
        try {
            mainServer();
            runClient();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void runClient() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String queryString = "SELECT * { SERVICE <http://localhost:3030/ds> { GRAPH ?g { ?s ?p ?o }} }";
        Query query = QueryFactory.create(queryString);

        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dsg) ) {
            QueryExecUtils.executeQuery(qExec);
        }
    }

    public static void mainServer(String ... a) {
        //org.apache.jena.fuseki.ctl.ActionMetrics

        //FusekiMainCmd.main("--passwd=/home/afs/tmp/passwd", "--auth=basic", "--mem", "/ds");
        //DatasetGraph dsg = DatasetGraphFactory.createTxnMem();

//        readCacheSize          dft:10000
//        writeCacheSize         dft:2000
//        Node2NodeIdCacheSize   dft:100000
//        NodeId2NodeCacheSize   dft:500000
//        NodeMissCacheSize      dft:100

//        readCacheSize          100
//        writeCacheSize         100
//        Node2NodeIdCacheSize   10000
//        NodeId2NodeCacheSize   10000
//        NodeMissCacheSize      100

        Location location = Location.create("DB");
        FileOps.ensureDir("DB");
        if ( ! FileOps.exists("DB/tdb.cfg") ) {
//        FileOps.clearAll("DB");
            StoreParams params = StoreParams.getSmallStoreParams();
            StoreParamsCodec.write(location, params);
        }

//        Location location = Location.mem("DB");
//        StoreParams params = StoreParams.getSmallStoreParams();
//        StoreConnection.make(location, params);

        DatasetGraph dsg = TDBFactory.createDatasetGraph(location);
        StoreParams params2 = TDBInternal.getStoreConnection(dsg).getBaseDataset().getConfig().params;
        System.out.println(params2);

        dsg.add(SSE.parseQuad("(:g :s :p :o)"));

        FusekiServer server = FusekiServer.create()
            .add("/ds", dsg)
            .port(3030)
            .build();
        server.start();
//        try { server.start().join(); }
//        finally { server.stop(); }
    }
}
