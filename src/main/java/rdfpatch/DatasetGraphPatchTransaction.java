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

package rdfpatch;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetChangesCapture ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphMonitor ;
import org.apache.jena.sparql.core.DatasetGraphWithLock ;
import rdfpatch.DatasetGraphPlayer.Direction ;

/** Provide transactional semantics to a DatasetGraph.
 *  Changes are made the dataset immediately and an undo log is kept.
 *  If {@code abort()} is called, the changes are undone.
 *  Implicitly uses MRSW locking; there is no true parallel readers
 *  when a write transaction is active.
 */

public class DatasetGraphPatchTransaction extends DatasetGraphWithLock { 
    private final DatasetChangesCapture delta ;
    // The original dataset.  Used as the replay target.  
    private final DatasetGraph dataset ;
    // The dataset with a change monitor. 
    private final DatasetGraph datasetMonitor ;
    
    public DatasetGraphPatchTransaction(DatasetGraph dsg) {
        // Instead of passing in the dataset, we override get()
        super(null) ;
        delta = new DatasetChangesCapture() ;
        this.dataset = dsg ;
        DatasetGraphMonitor dsgm = new DatasetGraphMonitor(dsg, delta) ;
        datasetMonitor = dsgm  ;
    }
    
    @Override
    protected DatasetGraph get() { return datasetMonitor ; }
    
    @Override
    protected void _begin(ReadWrite readWrite) {
        delta.reset() ;
        switch (readWrite) {
            case READ : break ;
            case WRITE :
                delta.start() ;
                break ;
            default :
        }
        super._begin(readWrite) ; 
    }

    @Override
    protected void _commit() {
        delta.finish() ;
        super._commit() ; 
    }

    @Override
    protected void _abort() {
        if ( isTransactionType(ReadWrite.WRITE) )
            DatasetGraphPlayer.play(delta.getActions(), dataset, Direction.BACKWARDS) ;
        super._abort() ;
    }

    @Override
    protected void _end() {
        delta.finish() ;
        super._end() ;
        delta.reset() ;
    }
    
    @Override
    protected boolean abortImplemented() { return true ; }
}
