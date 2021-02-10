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

package dsg.prefixes;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.index.bplustree.BPlusTree;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb.store.tupletable.TupleTable;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb.transaction.TransactionManager;

public class DevPrefixes {

    public static void main(String...a) {
        DatasetGraph dsg = TDBFactory.createDatasetGraph();
//        //TDBInternal.getBaseDatasetGraphTDB(dsg)
//        DatasetGraphTransaction dsgt = (DatasetGraphTransaction)dsg;
//        DatasetGraphTDB dsgtdb = dsgt.getBaseDatasetGraph();
//
//        PrefixMap pmap = dsgtdb.prefixes();

        TransactionManager tmgt = TDBInternal.getTransactionManager(dsg);

        TransactionManager.QueueBatchSize = 0;

        Txn.executeWrite(dsg, ()->{
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
            //pmap.delete("ex");
            dsg.add(SSE.parseQuad("(_ :s :p :o)"));
            details("WP", dsg);
            //details("WT", nodeTableTriples(dsg));
            dsg.abort();
            //dsg.commit();
        });

        // Node table empty, not the tuples table.
        // Which part?
        Txn.executeRead(dsg, ()->{
            System.out.println("----");
            TupleIndexRecord tir = nodeTablePrefixes(dsg);

            System.out.println("Empty prefixes? "+tir.isEmpty());
            tir.all().forEachRemaining(System.out::println);
            details("RP", dsg);
            //details("RT", nodeTableTriples(dsg));

//            TupleTable ntt2 = nodeTableTriples(dsg);
//            System.out.println("T:"+ntt2);
//            System.out.println("Empty triples? "+ntt2.isEmpty());
//            ntt2.findAll().forEachRemaining(System.out::println);
//            ntt2.getNodeTable().all().forEachRemaining(System.out::println);

            PrefixMap pmap = dsg.prefixes();
            // Here.
            pmap.getMappingCopy();
        });
        System.out.println("DONE");
    }


    // *** TDB1 transaction

    private static void details(String label, DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = TDBInternal.getBaseDatasetGraphTDB(dsg);
        TupleTable tt = dsgtdb.getStoragePrefixes().getNodeTupleTable().getTupleTable();
        details(label, tt);
    }

    private static void details(String label, TupleTable tt) {
        TupleIndexRecord tr =  (TupleIndexRecord)tt.getIndex(0);
        BPlusTree bpt = (BPlusTree)tr.getRangeIndex();

        System.out.println(label+":"+bpt);
//        System.out.println(label+":"+bpt.getNodeManager().getBlockMgr().hashCode());
//        System.out.println(label+":"+bpt.getRecordsMgr().getBlockMgr().hashCode());
    }

        /*
13:36:46 WARN  DatasetPrefixesTDB :: Mangled prefix map: graph name=
java.lang.NullPointerException: null
    at org.apache.jena.tdb.store.DatasetPrefixesTDB.readPrefixMap(DatasetPrefixesTDB.java:120) ~[classes/:?]
    at org.apache.jena.tdb.store.GraphPrefixesProjection.getMapping(GraphPrefixesProjection.java:55) ~[classes/:?]
    at org.apache.jena.riot.system.PrefixMapBase.getMappingCopy(PrefixMapBase.java:46) ~[classes/:?]
    at org.apache.jena.riot.system.PrefixMapWrapper.getMappingCopy(PrefixMapWrapper.java:45) ~[classes/:?]
    at dsg.prefixes.DevPrefixes.lambda$1(DevPrefixes.java:73) ~[classes/:?]
    at org.apache.jena.system.Txn.exec(Txn.java:77) [classes/:?]
    at org.apache.jena.system.Txn.executeRead(Txn.java:115) [classes/:?]
    at dsg.prefixes.DevPrefixes.main(DevPrefixes.java:70) [classes/:?]
    */


    private static TupleIndexRecord nodeTablePrefixes(DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = TDBInternal.getBaseDatasetGraphTDB(dsg);
        return (TupleIndexRecord)dsgtdb.getStoragePrefixes().getNodeTupleTable().getTupleTable().getIndex(0);
    }

    private static TupleTable nodeTableTriples(DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = TDBInternal.getBaseDatasetGraphTDB(dsg);
        return dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable();
    }

}