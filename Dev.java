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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder;
import org.apache.jena.tdb2.sys.DatabaseConnection;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class Dev {
    static {
        LogCtl.setLog4j();
    }

    public static void main(String[] args) {
        // Does it matter that in the node table, there are nodes from the future?
        // Are the caches correct between storage and cache?

    }

    public static void mainDelete(String[] args) {

        String testString = "Größe";

        String encoded = IRILib.encodeNonASCII(testString);
        String decoded = IRILib.decode(encoded);
        if ( ! testString.equals(decoded) ) {
            System.out.println(encoded);
        }

        System.out.println(testString);
        System.out.println(encoded);
        System.out.println(decoded);
        assertEquals(testString, decoded);
        System.exit(0);

        System.out.println(encoded);
        assertEquals("Gr%C3%B6%C3%9Fe", encoded);// <-- passes
        //String decoded = IRILib.decode(encoded);

        String encoded1 = IRILib.encodeNonASCII("Größe");
        System.out.println(encoded1);
        System.out.println(decoded);

        assertEquals("Größe", decoded);// <-- fails
        System.exit(0);

        /*
         * RDFConnectionRemote - override queryAsk etc.
         * Pull up RDFConnection impls, add a "with type" version. Needs public in RDFConnection (its an interface).
         * public ResultSet querySelect(?) -- no, API is designed for streamed
         */

        /* update strings aren't passed through */

        RDFConnection c =
            RDFConnectionRemote.create()
            .destination("http://localhost:3030/ds")
            .acceptHeaderAskQuery("application/sparql-results+json, application/sparql-results+xml;q=0.9")
            .acceptHeaderSelectQuery("HelloWorld")
            .build();

        boolean b1 = c.queryAsk("ASK{}");
        c.querySelect("SELECT * {}", qs->{});
        System.out.println(b1);
        System.exit(0);

        // TDB1 :
        // W: 27 ms/txn (97 ms/txn for QueueBatchSize = 0)
        // R: 0.08 ms/txn
        // P: 0.1 ms/txn

        // TDB2 :
        // W: 350 ms/txn (N=100); 178 ms/txn for one quad, the same added; or no quads.
        // R: 1.04ms/txn
        // P: 0.1

        // Optimize empty transaction! No? - that's a promote.

        boolean execTDB1 = false;
        boolean execTDB2 = ! execTDB1 ;

        if ( execTDB1 ) {
            FileOps.clearAll("DB1");
            DatasetGraph dsg1 = TDBFactory.createDatasetGraph("DB1");
            org.apache.jena.tdb.transaction.TransactionManager.QueueBatchSize = 0 ;
            dwim("TDB1", dsg1);
        }

        if ( execTDB2 ) {
            FileOps.clearAll("DB2");
            DatasetGraph dsg2 = DatabaseMgr.connectDatasetGraph("DB2");
            dwim("TDB2", dsg2);
        }
    }

    private static void dwim(String label, DatasetGraph dsg) {
        AtomicInteger counter = new AtomicInteger(0);
        System.out.println(label+" Start");
        int N = 50;
        Runnable w = ()->{
            for ( int i = 0 ; i < N; i++ ) {
                Txn.executeWrite(dsg, () ->{
                    Quad q = SSE.parseQuad("(:g :s :p "+counter.getAndIncrement()+")");
                    dsg.add(q);
                });
            }
        };
        Runnable r = ()->{
            for ( int i = 0 ; i < N; i++ ) {
                Txn.executeRead(dsg, () ->{});
            }
        };

        long z = Timer.time(w);

        System.out.printf("%s N=%d : Total %d ms : %.3f ms/txn\n", label, N, z, z/((double)N));

    }

    public static void mainIdx(String... args) {
        RDFDataMgr.loadGraph("/home/afs/tmp/D.ttl");

        System.out.println("DONE");
        System.exit(0);

        String[] tripleIndxes = { "POS", "PSO" };
        StoreParams storeParams = StoreParamsBuilder.create()
            .tripleIndexes(tripleIndxes)
            .primaryIndexTriples("POS")
            .build();
        Location loc = Location.create("DBX");
        DatabaseConnection dbConn = DatabaseConnection.connectCreate(loc, storeParams);
        DatasetGraph dsg = dbConn.getDatasetGraph();
        Dataset ds = DatasetFactory.wrap(dsg);
        System.out.println("DONE");
    }

    public static void mainSchiphop(String... args) throws Exception {
        UpdateRequest update = UpdateFactory.read("/home/afs/Desktop/Schiphol/U.ru");
        UpdateRequest insert = UpdateFactory.read("/home/afs/Desktop/Schiphol/U2.ru");
        Model model = RDFDataMgr.loadModel("/home/afs/Desktop/Schiphol/C.ttl");

        try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/ds") ) {
            conn.put("http://graph/", model);
            for ( int i = 0 ; i < 10 ; i++ ) {

                long x1 = Timer.time(()->{
                    conn.update(update);
                });
                String xs1 = Timer.timeStr(x1);
                System.out.printf("DEL = %sms\n",xs1);

                long x2 = Timer.time(()->{
                    conn.update(insert);
                });
                String xs2 = Timer.timeStr(x2);
                System.out.printf("INS  %sms\n",xs2);
            }
        }
    }
}
