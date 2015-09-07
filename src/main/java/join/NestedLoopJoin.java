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

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter2 ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

/** Simple, dependable join.  
 * Two versions: one materializing the whole results, then returning a RowList and
 * one that streams the right-hand-side.  
 */
public class NestedLoopJoin
{
    private static final boolean JOIN_EXPLAIN = false;

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
        return new RowsInnerLoopJoin(left, right, cxt) ;
    }

    private static class RowsInnerLoopJoin extends QueryIter2 {
        private long s_countLHS = 0 ;
        private long s_countRHS = 0 ;
        private long s_countResults = 0 ;

        private final List<Binding> leftRows ;
        private Iterator<Binding> left = null ;
        private QueryIterator right ;
        private Binding rowRight = null ;

        private Binding slot = null ;
        private boolean finished = false ; 

        public RowsInnerLoopJoin(QueryIterator left, QueryIterator right, ExecutionContext cxt) {
            super(left, right, cxt) ;
            List<Binding> rowsLeftList = Iter.toList(left) ;
            leftRows = rowsLeftList ;
            s_countLHS = rowsLeftList.size() ;
            this.right = right ;
        }

        @Override
        protected boolean hasNextBinding() {
            if ( finished ) 
                return false ;
            if ( slot == null ) {
                slot = moveToNextBindingOrNull() ;
                if ( slot == null ) {
                    close() ;
                    return false;
                }
            }
            return true ;
        }

        @Override
        protected Binding moveToNextBinding() {
            Binding r = slot ;
            slot = null ;
            return r ;
        }

        protected Binding moveToNextBindingOrNull() {
            if ( isFinished() )
                return null ;

            for ( ;; ) {    // For rows from the right.
                if (rowRight == null ) {
                    if ( right.hasNext() ) {
                        rowRight = right.next();
                        s_countRHS ++ ;
                        left = leftRows.iterator() ;
                    } else 
                        return null ;
                }

                // There is a rowRight; it maybe the same as last time.
                while(left.hasNext()) {
                    Binding rowLeft = left.next() ;
                    Binding r = Algebra.merge(rowLeft, rowRight) ; 
                    if ( r != null ) {
                        s_countResults++ ;
                        return r ;
                    }
                }
                // Nothing more for this rowRight.
                rowRight = null ;
            }
        }

        @Override
        protected void requestSubCancel() {}

        @Override
        protected void closeSubIterator() {
            if ( JOIN_EXPLAIN ) {
                String x = String.format(
                                         "InnerLoopJoin: LHS=%d RHS=%d Results=%d",
                                         s_countLHS, s_countRHS, s_countResults) ;
                System.out.println(x) ;
            }
        }
    }
}

