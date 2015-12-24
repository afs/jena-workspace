/**
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

public class TupleFactory {
    @SafeVarargs
    public static <X> Tuple<X> create(X ...xs ) {
        if ( xs.length == 3 )
            return create3(xs[0], xs[1], xs[2]) ;
        return new TupleN<>(xs) ;
    }
    
    public static <X> Tuple1<X> create1(X x1) {
        return new Tuple1<>(x1) ;
    }
    
    public static <X> Tuple2<X> create2(X x1, X x2) {
        return new Tuple2<>(x1, x2) ;
    }

    public static <X> Tuple3<X> create3(X x1, X x2, X x3) {
        return new Tuple3<>(x1, x2, x3) ;
    }

    public static <X> Tuple4<X> create4(X x1, X x2, X x3, X x4) {
        return new Tuple4<>(x1, x2, x3,x4) ;
    }
}
