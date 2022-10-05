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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.atlas.lib.tuple.Tuple;

/**
 * Mutable tuple. The tuple is fixed length - its slot values can be changed.
 * <p>
 * These are not an extension of {@link Tuple}. {@code Tuple}s have value-equality and can
 * be used in sets and as keys in maps. Mutable tuples can not be used in sets by-value
 * and do not support value-equality. Their {@code .equals} is object identity.
 */
interface MTuple<X> extends Iterable<X> {
    // Use Tuplefactory

    /** Get the i'th element, for i in the range 0 to len()-1
     * @throws IndexOutOfBoundsException for i out of range
     */
    public X get(int i);

    /**
     * Set the i'th element.
     * @throws IndexOutOfBoundsException for i out of range
     */
    public void set(int i, X x);

    /** length : elements are 0 to len()-1 */
    public int len() ;

    /** Return true if this is a zero-length mtuple */
    public default boolean isEmpty() {
        return len() == 0 ;
    }

    /** stream */
    public default Stream<X> stream() {
        return StreamSupport.stream(spliterator(), false) ;
    }

    /** forEach */
    @Override
    public default void forEach(Consumer<? super X> action) {
        int N = len();
        for ( int i = 0 ; i < N ; i++ )
            action.accept(get(i));
    }

    /** map */
    public <Y> MTuple<Y> map(Function<X,Y> function);

    /** Convert to a List */
    public List<X> asList();

    /** Iterable */
    @Override
    public Iterator<X> iterator();
}