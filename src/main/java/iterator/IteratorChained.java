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

package iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.Objects ;

import org.apache.jena.atlas.iterator.IteratorConcat ;

/** A chained iterator 
 *  
 *  It is like {@link IteratorConcat} except it delays the creation of
 *  the next iterator. 
 */
public class IteratorChained<X> implements Iterator<X> {
    interface Generator<X> {
        /** Return the next iterator, or null for finished. */
        Iterator<X> next() ;
    }

    private Iterator<X> current = null ;
    boolean finished = false ;
    private final Generator<X> generator;
    
    public IteratorChained(Generator<X> generator) {
        Objects.requireNonNull(generator) ;
        this.generator = generator ;
    }
    
    @Override
    public boolean hasNext() {
        if ( finished )
            return false;
        
        for ( ;; ) {
            if ( current == null )
                current = generator.next( ) ;
            if ( current == null ) {
                finished = true ;
                return false ;
            }

            if ( current.hasNext() )
                return true ;
            current = null ;
        }
        
    }

    @Override
    public X next() {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        return current.next() ;
    }
}

