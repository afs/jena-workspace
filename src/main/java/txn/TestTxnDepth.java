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

package txn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)   
public class TestTxnDepth {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> tests = new ArrayList<>();
        tests.add(t("TDB1", TDBFactory::createDatasetGraph));
        tests.add(t("TDB2", DatabaseMgr::createDatasetGraph));
        //tests.add(t("TIM",  DatasetGraphFactory::createTxnMem));
        //tests.add(t("Link", DatasetGraphFactory::create));
        
        return tests;
    }
    
    //DatasetGraph dsg = TDBFactory.createDatasetGraph();
    
    private static Object[] t(String name, Creator<DatasetGraph> maker) {
        return new Object[] {name, maker};
    }

    private final DatasetGraph dsg; 
    
    public TestTxnDepth(String name, Creator<DatasetGraph> maker) {
        this.dsg = maker.create();
    }
    
    @Test public void read_read() {
        dsg.begin(TxnType.READ);
        dsg.begin(TxnType.READ);
        dsg.end();
        assertTrue(dsg.isInTransaction());
        dsg.end();
        assertFalse(dsg.isInTransaction());
        dsg.end();
    }

    @Test public void write_read() {
        dsg.begin(TxnType.READ);
        dsg.begin(TxnType.READ);
        dsg.commit();
        assertTrue(dsg.isInTransaction());
        dsg.end();
        assertFalse(dsg.isInTransaction());
        dsg.end();
    }

    @Test public void write_read_commit_commit() {
        dsg.begin(TxnType.WRITE);
        dsg.begin(TxnType.READ);
        dsg.commit();
        assertTrue(dsg.isInTransaction());
        dsg.commit();
        assertFalse(dsg.isInTransaction());
        dsg.end();
    }

    @Test public void write_read_end_commit() {
        dsg.begin(TxnType.WRITE);
        dsg.begin(TxnType.READ);
        dsg.end();
        assertTrue(dsg.isInTransaction());
        dsg.commit();
        assertFalse(dsg.isInTransaction());
        dsg.end();
    }

    @Test public void read_write() {
        dsg.begin(TxnType.READ);
        dsg.begin(TxnType.WRITE);
    }
}
