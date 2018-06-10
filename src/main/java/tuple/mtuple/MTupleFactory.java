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

package tuple.mtuple;

import java.util.Arrays;
import java.util.List ;

/** {@code MTuple} creation */
public class MTupleFactory {

    private MTupleFactory() {}

    /** Create a Tuple */ 
    @SafeVarargs
    public static <X> MTuple<X> tuple(X...xs) {
        switch(xs.length) {
            case 0 : return create0() ;
            case 1 : return create1(xs[0]) ;
            case 2 : return create2(xs[0], xs[1]) ;
            case 3 : return create3(xs[0], xs[1], xs[2]) ;
            case 4 : return create4(xs[0], xs[1], xs[2], xs[3]) ;
//            case 5 : return create5(xs[0], xs[1], xs[2], xs[3], xs[4]) ;
//            case 6 : return create6(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5]) ;
//            case 7 : return create7(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6]) ;
//            case 8 : return create8(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6], xs[7]) ;
            default:
                return asTuple(xs) ;
        }
    }

    /** Create a Tuple from an array */ 
    public static <X> MTuple<X> create(X[] xs) {
        switch(xs.length) {
            case 0 : return create0() ;
            case 1 : return create1(xs[0]) ;
            case 2 : return create2(xs[0], xs[1]) ;
            case 3 : return create3(xs[0], xs[1], xs[2]) ;
            case 4 : return create4(xs[0], xs[1], xs[2], xs[3]) ;
//            case 5 : return create5(xs[0], xs[1], xs[2], xs[3], xs[4]) ;
//            case 6 : return create6(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5]) ;
//            case 7 : return create7(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6]) ;
//            case 8 : return create8(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6], xs[7]) ;
            default:
                return MTupleN.create(xs) ;
        }
    }

    /** Treat an array as a Tuple.  The array must not be mutated */ 
    public static <X> MTuple<X> asTuple(X[] xs) {
        X[] xs2 = Arrays.copyOf(xs, xs.length);
        return MTupleN.wrap(xs2) ;
    }

    /** Create a MTuple from a list */ 
    public static <X> MTuple<X> create(List<X> xs) {
        @SuppressWarnings("unchecked")
        X[] xa = (X[])(new Object[xs.size()]) ;
        return MTupleFactory.tuple(xs.toArray(xa)) ;
    }

    /** Create a MTuple of length 0 */
    public static <X> MTuple0<X> create0() {
        return new MTuple0<>() ;
    }

    /** Create a MTuple of length 1 */
    public static <X> MTuple1<X> create1(X x1) {
        return new MTuple1<>(x1) ;
    }

    /** Create a MTuple of length 2 */
    public static <X> MTuple2<X> create2(X x1, X x2) {
        return new MTuple2<>(x1, x2) ;
    }

    /** Create a MTuple of length 3 */
    public static <X> MTuple3<X> create3(X x1, X x2, X x3) {
        return new MTuple3<>(x1, x2, x3) ;
    }

    /** Create a MTuple of length 4 */
    public static <X> MTuple4<X> create4(X x1, X x2, X x3, X x4) {
        return new MTuple4<>(x1, x2, x3, x4) ;
    }

//    /** Create a MTuple of length 5 */
//    public static <X> MTuple5<X> create5(X x1, X x2, X x3, X x4, X x5) {
//        return new MTuple5<>(x1, x2, x3, x4, x5) ;
//    }
//
//    /** Create a MTuple of length 6 */
//    public static <X> MTuple6<X> create6(X x1, X x2, X x3, X x4, X x5, X x6) {
//        return new MTuple6<>(x1, x2, x3, x4, x5, x6) ;
//    }
//
//    /** Create a MTuple of length 7 */
//    public static <X> MTuple7<X> create7(X x1, X x2, X x3, X x4, X x5, X x6, X x7) {
//        return new MTuple7<>(x1, x2, x3, x4, x5, x6, x7) ;
//    }
//
//    /** Create a MTuple of length 8 */
//    public static <X> MTuple8<X> create8(X x1, X x2, X x3, X x4, X x5, X x6, X x7, X x8) {
//        return new MTuple8<>(x1, x2, x3, x4, x5, x6, x7, x8) ;
//    }
}
