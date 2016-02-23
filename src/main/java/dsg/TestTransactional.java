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

package dsg;

import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalLock ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** Tests of standalone Transactionals.
 * (The used one also get tested in other tests)
 */

public class TestTransactional {
    
    // See Test
    // awaitability?
    
    @Test public void trans_mrsw_01() {
        Transactional t = TransactionalLock.createMutex() ;
        testMRSW_W(t) ;
        testMRSW_R(t) ;
        testMRSW_Mix(t) ;
    }

    @Test public void trans_mrsw_02() {
        Transactional t = TransactionalLock.createMRSW() ;
        testMRSW_W(t) ;
        testMRSW_R(t) ;
        testMRSW_Mix(t) ;
    }

    private static ExecutorService execService ;
    @BeforeClass public static void beforeClass() {
        execService = Executors.newFixedThreadPool(4);
    }
    @AfterClass public static void afterClass() {
        execService.shutdownNow() ;
    }
    
    // No parallel testing!
    private synchronized static void testMRSW_W(Transactional t) {
        try {
            Callable<Object> proc1 = callable(ReadWrite.WRITE, t) ;
            Callable<Object> proc2 = callable(ReadWrite.WRITE, t) ;
            Future<Object> f1 = execService.submit(proc1) ;
            Future<Object> f2 = execService.submit(proc2) ;
            f1.get(5, TimeUnit.SECONDS) ;
            f2.get(5, TimeUnit.SECONDS) ;
        } catch (Exception ex) {
            Assert.fail() ;
        }
    }

    private synchronized static void testMRSW_R(Transactional t) {
        try {
            int N = 10 ;
            List<Future<Object>> waiting = new ArrayList<>() ;
            for ( int i = 0 ; i < N ; i++ ) {
                Callable<Object> proc = callable(ReadWrite.READ, t) ;
                Future<Object> f = execService.submit(proc) ;
                waiting.add(f) ;
            }
            for ( int i = 0 ; i < N ; i++ ) {
                // Less than sum all R waits
                waiting.get(i).get(1, TimeUnit.SECONDS) ;
            }
        } catch (Exception ex) {
            Assert.fail() ;
        }
    }
    
    private synchronized static void testMRSW_Mix(Transactional t) {
        try {
            Callable<Object> proc1 = callable(ReadWrite.WRITE, t) ;
            Callable<Object> proc2 = callable(ReadWrite.WRITE, t) ;
            Future<Object> f1 = execService.submit(proc1) ;
            Future<Object> f2 = execService.submit(proc2) ;
            f1.get(5, TimeUnit.SECONDS) ;
            f2.get(5, TimeUnit.SECONDS) ;
            
            int N = 10 ;
            List<Future<Object>> waiting = new ArrayList<>() ;
            for ( int i = 0 ; i < N ; i++ ) {
                Callable<Object> proc = callable(ReadWrite.READ, t) ;
                Future<Object> f = execService.submit(proc) ;
                waiting.add(f) ;
            }
            for ( int i = 0 ; i < N ; i++ ) {
                waiting.get(i).get(5, TimeUnit.SECONDS) ;
            }
        } catch (Exception ex) {
            Assert.fail() ;
        }
    }

    
    private static AtomicInteger counter = new AtomicInteger(0) ; 
    private static Callable<Object> callable(ReadWrite mode, Transactional t) {
        switch (mode) {
            case READ :
                return () -> {
                    t.begin(ReadWrite.READ);
                    // Counter constant.
                    int x1 = counter.get();
                    Lib.sleep(100);
                    int x2 = counter.get();
                    t.commit();
                    t.end();
                    Assert.assertEquals(x1, x2);
                    return null ;
                };
            case WRITE :
                return () -> {
                    // Counter incremented on entry.
                    t.begin(ReadWrite.WRITE);
                    int x1 = counter.incrementAndGet();
                    Lib.sleep(100);
                    int x2 = counter.get();
                    t.commit();
                    t.end();
                    Assert.assertEquals(x1, x2);
                    return null ;
                };
        }
        return null ;    
    }
}
