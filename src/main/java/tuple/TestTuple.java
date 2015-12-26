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

package tuple;

import static org.junit.Assert.* ;

import java.util.List ;

import org.junit.Test ;

public class TestTuple {
    @Test public void factory_make_0() {
        Tuple<Integer> tuple = TupleFactory.create0() ;
        assertEquals(0, tuple.len()) ;
        check(tuple) ;
    }
    
    @Test public void factory_make_1() {
        Tuple<Integer> tuple = TupleFactory.create1(9) ;
        assertEquals(1, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_2() {
        Tuple<Integer> tuple = TupleFactory.create2(9,8) ;
        assertEquals(2, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_3() {
        Tuple<Integer> tuple = TupleFactory.create3(9,8,7) ;
        assertEquals(3, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_4() {
        Tuple<Integer> tuple = TupleFactory.create4(9,8,7,6) ;
        assertEquals(4, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_5() {
        Tuple<Integer> tuple = TupleFactory.create5(9,8,7,6,5) ;
        assertEquals(5, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_6() {
        Tuple<Integer> tuple = TupleFactory.create6(9,8,7,6,5,4) ;
        assertEquals(6, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_7() {
        Tuple<Integer> tuple = TupleFactory.create7(9,8,7,6,5,4,3) ;
        assertEquals(7, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_8() {
        Tuple<Integer> tuple = TupleFactory.create8(9,8,7,6,5,4,3,2) ;
        assertEquals(8, tuple.len()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N0() {
        Tuple<Integer> tuple = TupleFactory.create() ;
        assertEquals(0, tuple.len()) ;
        assertEquals(Tuple0.class, tuple.getClass()) ;
        check(tuple) ;
    }
    
    @Test public void factory_make_N1() {
        Tuple<Integer> tuple = TupleFactory.create(9) ;
        assertEquals(1, tuple.len()) ;
        assertEquals(Tuple1.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N2() {
        Tuple<Integer> tuple = TupleFactory.create(9,8) ;
        assertEquals(2, tuple.len()) ;
        assertEquals(Tuple2.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N3() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7) ;
        assertEquals(3, tuple.len()) ;
        assertEquals(Tuple3.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N4() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6) ;
        assertEquals(4, tuple.len()) ;
        assertEquals(Tuple4.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N5() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6,5) ;
        assertEquals(5, tuple.len()) ;
        assertEquals(Tuple5.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N6() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6,5,4) ;
        assertEquals(6, tuple.len()) ;
        assertEquals(Tuple6.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N7() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6,5,4,3) ;
        assertEquals(7, tuple.len()) ;
        assertEquals(Tuple7.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N8() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6,5,4,3,2) ;
        assertEquals(8, tuple.len()) ;
        assertEquals(Tuple8.class, tuple.getClass()) ;
        check(tuple) ;
    }

    @Test public void factory_make_N() {
        Tuple<Integer> tuple = TupleFactory.create(9,8,7,6,5,4,3,2,1,0) ;
        assertEquals(10, tuple.len()) ;
        assertEquals(TupleN.class, tuple.getClass()) ;
        check(tuple) ;
    }

    private void check(Tuple<Integer> tuple) {
        int val = 9 ;
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            assertEquals(val-i, tuple.get(i).intValue()) ; 
        }
        List<Integer> list = tuple.asList() ;
        for ( int i = 0 ; i < tuple.len() ; i++ ) {
            assertEquals(val-i, list.get(i).intValue()) ; 
        }
        
        try { tuple.get(-1) ; fail("Index -1 did not throw an exception") ; }
        catch(IndexOutOfBoundsException ex) {}
        try { tuple.get(tuple.len()) ; fail("Index len() did not throw an exception") ; }
        catch(IndexOutOfBoundsException ex) {}
        
    }
}