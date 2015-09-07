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

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class Join {

    /** Evaluate a hash join. */
    public static QueryIterator hashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext cxt) {
        return new QueryIterHashJoin(joinKey, left, right, cxt) ;
    }

    /** Simple, materializing version - useful for debugging */ 
    public static QueryIterator innerLoopJoinBasic(QueryIterator left, QueryIterator right, ExecutionContext cxt) {
        List<Binding> leftRows = Iter.toList(left) ;
        List<Binding> output = DS.list() ;
        for ( ; right.hasNext() ; ) {
            Binding row2 = right.next() ;
            for ( Binding row1 : leftRows ) {
                Binding r = Algebra.merge(row1, row2) ;
                if ( r != null )
                    output.add(r) ;
            }
        }
        return new QueryIterPlainWrapper(output.iterator(), cxt) ;
    }

    /** Streams on the right table, having materialised the left */ 
    public static QueryIterator innerLoopJoin(QueryIterator left, QueryIterator right, ExecutionContext cxt) {
        return new QueryIterNestedLoopJoin(left, right, cxt) ;
    }

    public static final boolean JOIN_EXPLAIN = false;

}

