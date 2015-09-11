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

public class AbstractIterHashJoin extends QueryIter2 {
    private long s_countProbe           = 0 ;       // Probe side size
    private long s_countScan            = 0 ;       // Scan side size
    private long s_countResults         = 0 ;       // Result size.
    private long s_bucketCount          = 0 ;
    private long s_maxBucketSize        = 0 ;
    private long s_noKeyBucketSize      = 0 ;
    private long s_maxMatchGroup        = 0 ;
    private long s_countRightMiss       = 0 ;
    
    private final JoinKey               joinKey ;
    private final HashProbeTable        hashTable ;

    private Iterator<Binding>           iterStream ;
    private Binding                     rowStream          = null ;
    private Iterator<Binding>           iterCurrent ;
    
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
        // Gather stats
        // Internal IteratorSlotted.ended call?
        // iterCurrent is the iterator of entries in the left hashed table
        // for the right row.    
        // iterRight is the stream of incoming rows.
        for(;;) {
            // Ensure we are processing a row. 
            while ( iterCurrent == null ) {
                // Move on to the next row from the right.
                if ( ! iterStream.hasNext() ) {
                    joinFinished() ;
                    return null ;
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

            Binding rowLeft = iterCurrent.next() ;
            Binding r = Algebra.merge(rowLeft, rowStream) ;
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
        if ( JoinLib.JOIN_EXPLAIN ) {
            String x = String.format(
                         "HashJoin: LHS=%d RHS=%d Results=%d RightMisses=%d MaxBucket=%d NoKeyBucket=%d",
                         s_countProbe, s_countScan, s_countResults, 
                         s_countRightMiss, s_maxBucketSize, s_noKeyBucketSize) ;
            System.out.println(x) ;
        }
    }

    @Override
    protected void requestSubCancel() { 
        finished = true ;
    }
}


