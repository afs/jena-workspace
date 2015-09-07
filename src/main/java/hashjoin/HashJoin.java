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

package hashjoin;

import java.util.Iterator ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter2 ;

/** General hash join (creates the probe table from the left hand side) */
public class HashJoin
{
    // No hash key marker.
    public static final Object noKeyHash = new Object() ;
    public static final boolean JOIN_EXPLAIN = false;

    /** Evaluate a hash join. */
    public static QueryIterator hashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext cxt) {
        return new RowsHashJoin(joinKey, left, right, cxt) ;
    }
    
    private static class RowsHashJoin extends QueryIter2 {
        private long s_countLHS             = 0 ;       // Left input side size
        private long s_countRHS             = 0 ;       // Right input side size
        private long s_countResults         = 0 ;       // Result size.
        private long s_bucketCount          = 0 ;
        private long s_maxBucketSize        = 0 ;
        private long s_noKeyBucketSize      = 0 ;
        private long s_maxMatchGroup        = 0 ;
        private long s_countRightMiss       = 0 ;
        
        private final JoinKey               joinKey ;
        private final HashProbeTable        hashTable ;

        private Iterator<Binding>           iterRight ;
        private Binding                     rowRight          = null ;
        private Iterator<Binding>           iterCurrent ;
        
        private Binding slot = null ;
        private boolean finished = false ; 

        public RowsHashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext cxt) {
            super(left, right, cxt) ;
            this.joinKey = joinKey ;
            this.iterRight = right ;
            this.hashTable = new HashProbeTable(joinKey) ;
            this.iterCurrent = null ;
            phase1(left) ;
        }
            
        private void phase1(Iterator<Binding> iter1) {
            // Phase 1 : Build hash table. 
            for (; iter1.hasNext();) {
                Binding row1 = iter1.next() ;
                s_countLHS ++ ;
                hashTable.put(row1) ;
            }
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
            // Gather stats
            // Internal IteratorSlotted.ended call?
            // iterCurrent is the iterator of entries in the left hashed table
            // for the right row.    
            // iterRight is the stream of incoming rows.
            for(;;) {
                // Ensure we are processing a row. 
                while ( iterCurrent == null ) {
                    // Move on to the next row from the right.
                    if ( ! iterRight.hasNext() ) {
                        joinFinished() ;
                        return null ;
                    }
                    
                    rowRight = iterRight.next() ;    
                    s_countRHS ++ ;
                    iterCurrent = hashTable.getCandidates(rowRight) ;
                }
                
                // Emit one row using the rightRow and the current matched left rows. 
                if ( ! iterCurrent.hasNext() ) {
                    iterCurrent = null ;
                    continue ;
                }

                Binding rowLeft = iterCurrent.next() ;
                Binding r = Algebra.merge(rowLeft, rowRight) ;
                if (r != null) {
                    s_countResults ++ ;
                    return r ;
                }
            }
        }        

        private void joinFinished() {
        }
            
        @Override
        protected void closeSubIterator() {
            finished = true ;
            if ( JOIN_EXPLAIN ) {
                String x = String.format(
                             "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                             s_countLHS, s_countRHS, s_countResults, 
                             s_countRightMiss, s_maxBucketSize, s_noKeyBucketSize) ;
                System.out.println(x) ;
            }
        }

        @Override
        protected void requestSubCancel() { 
            finished = true ;
        }
    }
    
    public static long hash(Var v, Node x) {
        long h = 17;
        if ( v != null )
            h = h ^ v.hashCode();
        if ( x != null )
            h = h ^ x.hashCode();
        return h;
    }

    public static final long nullHashCode = 5 ; 
    public static Object hash(JoinKey joinKey, Binding row) {
          long x = 31 ;
          boolean seenJoinKeyVar = false ; 
          // Neutral to order in the set.
          for ( Var v : joinKey ) {
              Node value = row.get(v) ;
              long h = nullHashCode ;
              if ( value != null ) {
                  seenJoinKeyVar = true ;
                  h = hash(v, value) ;
              } else {
                  // In join key, not in row.
              }
                  
              x = x ^ h ;
          }
          if ( ! seenJoinKeyVar )
              return noKeyHash ;
          return x ;
      }
}
