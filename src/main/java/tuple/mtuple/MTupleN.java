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

import java.util.Arrays ;
import java.util.List;
import java.util.function.Function;

/** A Tuple of N items */
public class MTupleN<X> extends MTupleBase<X> {
    private final X[] tuple ;

    /** Create a TupleN - safely copy the input */ 
    @SafeVarargs
    public static <X> MTupleN<X> create(X... xs) {
        X[] xs2 = Arrays.copyOf(xs, xs.length) ;
        return new MTupleN<>(xs2) ;
    }

    // When the array will not be modified.
    /*package*/ static <X> MTupleN<X> wrap(X[] xs) {
        return new MTupleN<>(xs) ;
    }

    /** Put a TupleN wrapper around a X[].
     *  The array must not be subsequently modified. 
     *  The statics {@link #create} and {@link wrap} determine whether to copy or not.
     */
    protected MTupleN(X[] xs) {
        tuple = xs ;
    }

    @Override
    public final X get(int i) {
        return tuple[i] ;
    }

    @Override
    public int len() {
        return tuple.length;
    }

    @Override
    public List<X> asList() {
        return null;
    }

    @Override
    public void set(int index, X x) {
        tuple[index] = x;
    }

    @Override
    public <Y> MTuple<Y> map(Function<X, Y> function) {
        int N = len();
        @SuppressWarnings("unchecked")
        Y[] xs2 = (Y[])new Object[N];
        for ( int i = 0 ; i < N ; i++ ) {
            xs2[i] = function.apply(tuple[i]);
        }
        return wrap(xs2); 
    }

    @Override
    protected X[] asArray() {
        return Arrays.copyOf(tuple, tuple.length);
    }
}
