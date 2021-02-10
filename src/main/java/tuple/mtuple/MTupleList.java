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

import java.util.AbstractList ;
import java.util.List ;
import java.util.RandomAccess ;

import org.apache.jena.atlas.lib.tuple.Tuple;

/** Wrap a {@link Tuple} as an immutable Java collection {@link List} */
/*package*/ class MTupleList<X> extends AbstractList<X> implements RandomAccess {

    private final MTuple<X> tuple;

    /*package*/ MTupleList(MTuple<X> tuple) {
        this.tuple = tuple ;
    }

    @Override
    public X get(int index) {
        return tuple.get(index) ;
    }

    @Override
    public X set(int index, X x) {
        X old = get(index);
        tuple.set(index, x) ;
        return old;
    }

    @Override
    public int size() {
        return tuple.len() ;
    }
}