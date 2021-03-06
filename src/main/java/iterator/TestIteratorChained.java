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

package iterator;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.junit.Test ;

public class TestIteratorChained {
    @Test public void iterator_chained_01() {
        
        IteratorChained.Generator<String> gen = new IteratorChained.Generator<String>() {
            int k = 0 ; 
            @Override
            public Iterator<String> next() {
                if ( k >= 3 )
                    return null ;
                k++ ;
                return Iter.singleton(""+k) ;
            }
        } ;
        
        for ( int i = 0 ; i < 5; i++ ) {
            String label = ""+i ;
            Iterator<String> iter = new IteratorChained<>(gen) ;
            Iter.print(iter) ;
        }
    }
}

