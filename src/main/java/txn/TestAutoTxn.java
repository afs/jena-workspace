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

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.system.TxnCounter;
import org.junit.Test;

public class TestAutoTxn {
    private TxnCounter counter = new TxnCounter(0);
    
    static class ExceptionFromTest extends RuntimeException {}

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_basic_0() {
        // Verify the TxnCounter is txn-sensitive.
        counter.inc();
    }

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_basic_1() {
        // Verify the TxnCounter is txn-sensitive.
        // NB TxnCounter.get is not.
        counter.read();
    }

    @Test public void autoTxn_read_1() {
        AutoTxn.execRead(counter, counter::read);
    }

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_read_2() {
        AutoTxn.execRead(counter, counter::inc);
    }

    @Test
    public void autoTxn_write_1() {
        AutoTxn.execWrite(counter, counter::inc);
    }

    @Test public void autoTxn_calc_1() {
        long x =
            AutoTxn.calcRead(counter, () -> {
                assertEquals("In R, value()", 0, counter.value());
                assertEquals("In R, get()", 0, counter.get());
                return counter.read();
            });
        assertEquals("Outside R", 0, x);
    }

    @Test public void autoTxn_calc_2() {
        AutoTxn.execWrite(counter, counter::inc);
        long x = AutoTxn.calcRead(counter, counter::read);
        assertEquals("Outside R", 1, x);
    }

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_lifecycle_1() {
        // Can't explicitly commit
        AutoTxn.execRead(counter, counter::commit);
    }

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_lifecycle_2() {
        // Can't explicitly abort
        AutoTxn.execRead(counter, counter::abort);
    }

    @Test(expected=JenaTransactionException.class)
    public void autoTxn_lifecycle_3() {
        // Can't explicitly end
        AutoTxn.execRead(counter, counter::end);
    }

    @Test
    public void autoTxn_exception_01() {
        try {
            AutoTxn.execWrite(counter, () -> {
                counter.inc();
                throw new ExceptionFromTest();
            });
        } catch (ExceptionFromTest ex) {}

        assertEquals("Outside W - get", 0, counter.get());
        assertEquals("Outside W - value", 0, counter.value());
    }

    // Compare to TestTxn.txn_nested_* tests

    @Test
    public void autoTxn_nested_01() {
        AutoTxn.exec(counter, TxnType.READ, ()->{
            AutoTxn.exec(counter, TxnType.READ, counter::read);
        });
    }

    @Test
    public void autoTxn_nested_02() {
        AutoTxn.exec(counter, TxnType.WRITE, ()->{
            AutoTxn.exec(counter, TxnType.READ, counter::read);
        });
    }

    @Test(expected=JenaTransactionException.class)
    public void txn_nested_05() {
        AutoTxn.exec(counter, TxnType.READ, ()->{
            AutoTxn.exec(counter, TxnType.WRITE, counter::inc);
        });
    }
}
