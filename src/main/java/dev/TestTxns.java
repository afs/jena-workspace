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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.TxnType;

public class TestTxns {
    public static void main() {
        List<Object[]> tests = new ArrayList<>();
        
        tests.add(test(true,   TxnType.READ,   TxnType.WRITE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ_PROMOTE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ));
    
        tests.add(test(true,   TxnType.READ_PROMOTE,   TxnType.WRITE));
        tests.add(test(false,  TxnType.READ_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(true,   TxnType.READ_PROMOTE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.READ_PROMOTE,   TxnType.READ));
    
        tests.add(test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.WRITE));
        tests.add(test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ));
    
        tests.add(test(true,   TxnType.WRITE,   TxnType.WRITE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ));
        
        for ( Object[] x : tests ) {
            testTxnCompat(x);
        }
    }

    private static Object[] test(boolean expected, TxnType innerTxnType,TxnType outerTxnType) {
        return new Object[] {expected, innerTxnType, outerTxnType}; 
    }

    private static void testTxnCompat(Object[]x ) {
        testTxnCompatibilty((Boolean)x[0], (TxnType)x[1], (TxnType)x[2]);  
    }

    private static void testTxnCompatibilty(boolean expected, TxnType innerTxnType,TxnType outerTxnType) {
        boolean b = Txns.isTxnTypeCompatible(innerTxnType, outerTxnType);
        if ( b != expected )
            System.out.print("**** ");
        System.out.printf("%-5s : inner=%s   :   outer=%s\n", b, innerTxnType, outerTxnType);
    }


}
