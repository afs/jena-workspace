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
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter2 ;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek ;
import org.apache.jena.sparql.engine.join.JoinKey ;

/** Hash join algorithm
 *  
 * This code materializes one input into the probe table
 * then hash joins the other input from the stream side.
 */

public abstract class AbstractIterHashJoin extends QueryIter2 {
    private long s_countProbe           = 0 ;       // Count of the probe data size
    private long s_countScan            = 0 ;       // Count of the scan data size
    private long s_countResults         = 0 ;       // Overall result size.
    private long s_trailerResults       = 0 ;       // Results from the trailer iterator.
    // See also stats in the probe table.
    
    private final JoinKey               joinKey ;
    private final HashProbeTable        hashTable ;

    private Iterator<Binding>           iterStream ;
    private Binding                     rowStream       = null ;
    private Iterator<Binding>           iterCurrent ;
    // Hanlde any "post join" additions.
    private Iterator<Binding>           iterTail        = null ;
    
    private Binding slot = null ;
    private boolean finished = false ; 

    protected AbstractIterHashJoin(JoinKey joinKey, QueryIterator probeIter, QueryIterator streamIter, ExecutionContext execCxt) {
        super(probeIter, streamIter, execCxt) ;
        
        if ( joinKey == null ) {
            QueryIterPeek pProbe = QueryIterPeek.create(probeIter, execCxt) ;
            QueryIterPeek pStream = QueryIterPeek.create(streamIter, execCxt) ;
            
            Binding bLeft = pProbe.peek() ;
            Binding bRight = pStream.peek() ;
            
            List<Var> varsLeft = Iter.toList(bLeft.vars()) ;
            List<Var> varsRight = Iter.toList(bRight.vars()) ;
            joinKey = JoinKey.createVarKey(varsLeft, varsRight) ;
            probeIter = pProbe ;
            streamIter = pStream ;
        }
        
        this.joinKey = joinKey ;
        this.iterStream = streamIter ;
        this.hashTable = new HashProbeTable(joinKey) ;
        this.iterCurrent = null ;
        phase1(probeIter) ;
    }
        
    private void phase1(Iterator<Binding> iter1) {
        // Phase 1 : Build hash table. 
        for (; iter1.hasNext();) {
            Binding row1 = iter1.next() ;
            s_countProbe ++ ;
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
        // iterCurrent is the iterator of entries in the
        // probe hashed table for the current stream row.     
        // iterStream is the stream of incoming rows.
        
        Binding b = doOneTail() ;
        if ( b != null )
            return b ;
        
        for(;;) {
            // Ensure we are processing a row. 
            while ( iterCurrent == null ) {
                // Move on to the next row from the right.
                if ( ! iterStream.hasNext() ) {
                    iterTail = joinFinished() ;
                    return doOneTail() ;
                }
                
                rowStream = iterStream.next() ;    
                s_countScan ++ ;
                iterCurrent = hashTable.getCandidates(rowStream) ;
            }
            
            // Emit one row using the rightRow and the current matched left rows. 
            if ( ! iterCurrent.hasNext() ) {
                iterCurrent = null ;
                continue ;
            }

            Binding rowCurrentProbe = iterCurrent.next() ;
            Binding r = Algebra.merge(rowCurrentProbe, rowStream) ;
            if (r != null) {
                s_countResults ++ ;
                yieldOneResult(rowCurrentProbe, rowStream, r) ;
                return r ;
            }
        }
    }        

    private Binding doOneTail() {
        if ( iterTail == null )
            return null ;
        if ( iterTail.hasNext() ) {
            s_countResults ++ ;
            s_trailerResults ++ ;
            return iterTail.next() ;
        }
        // Completely finished now.
        iterTail = null ;
        return null ;
    }
    
    /**
     * Signal about to return a result.
     * @param rowCurrentProbe
     * @param rowStream
     * @param rowResult
     */
    protected abstract void yieldOneResult(Binding rowCurrentProbe, Binding rowStream, Binding rowResult) ;

    /**
     * Signal the end of the hash join.
     * Outer joins can now add any "no matche" results.
     * @return QueryIterator or null
     */
    protected abstract QueryIterator joinFinished() ;
        
    @Override
    protected void closeSubIterator() {
        finished = true ;
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format(
                         "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                         s_countProbe, s_countScan, s_countResults, 
                         hashTable.s_countScanMiss, hashTable.s_maxBucketSize, hashTable.s_noKeyBucketSize) ;
            System.out.println(x) ;
        }
    }

    @Override
    protected void requestSubCancel() { 
        finished = true ;
    }
}


