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

import java.util.Arrays ;

/** A Tuple is the same class of item */
public class TupleN<X> implements Tuple<X> {
    private final X[] tuple ;

    @SafeVarargs
    /*package*/ TupleN(X...xs) {
        // Avoid leakage?
        tuple = Arrays.copyOf(xs, xs.length) ;
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
    public String toString() {
        return Arrays.asList(tuple).toString() ;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(tuple);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TupleN<?> other = (TupleN<?>)obj;
        if ( !Arrays.equals(tuple, other.tuple) )
            return false;
        return true;
    }
}
