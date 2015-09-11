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

package join;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator ;
import org.apache.jena.sparql.engine.join.JoinKey ;

/** Hash left join. 
 *  v1
 * 
 * This code materializes the right into a probe table
 * then hash joins from the left.
 * 
 *
 * Alternative:
"Hash left. Stream right"
Need to find unused left.
1/ Mark left matched.
2/ move to another hash table, check both each time.
Set<Binding> (IdentityHashMap<Binding, Null>) used 
 */

//* This code materializes the left into a probe table
//* then hash joins from the right.

public class QueryIterHashLeftJoin extends AbstractIterHashJoin {
    
    /**
     * Create a hashjoin QueryIterator.
     * @param joinKey  Join key - if null, one is guessed by snooping the input QueryIterators
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator create(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        // Easy cases.
        if ( ! left.hasNext() ) {
            left.close() ;
            right.close() ;
            return QueryIterNullIterator.create(execCxt) ;
        }
        if ( ! right.hasNext() ) {
            right.close() ;
            return left ;
        }

        if ( joinKey != null && joinKey.length() > 1 )
            Log.warn(QueryIterHashLeftJoin.class, "Multivariable join key") ; 
        
        return new QueryIterHashLeftJoin(joinKey, left, right, execCxt) ; 
    }
    
    /**
     * Create a hashjoin QueryIterator.
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
 
    public static QueryIterator create(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return create(null, left, right, execCxt) ;
    }
    
    private QueryIterHashLeftJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        super(joinKey, left, right, execCxt) ;
    }
}


