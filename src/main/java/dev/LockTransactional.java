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

package dev;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.Lock ;
import org.apache.jena.sparql.core.Transactional ;

/** A Lock that is backed by a Transactional */
public class LockTransactional implements Lock {

    private final Transactional transactional;
    private ThreadLocal<ReadWrite> mode = ThreadLocal.withInitial(()->null) ;

    public LockTransactional(Transactional transactional) {
        this.transactional = transactional ;
    }
    
    @Override
    public void enterCriticalSection(boolean readLockRequested) {
        ReadWrite lockMode = readLockRequested ? ReadWrite.READ : ReadWrite.WRITE ;
        transactional.begin(lockMode);
        if ( readLockRequested ) {
            mode.set(lockMode) ;
        }
        
    }

    @Override
    public void leaveCriticalSection() {
        ReadWrite lockMode = mode.get() ;
        
        if ( lockMode == null )
            throw new JenaException("No lock associated with this thread") ;
        
        switch(lockMode) {
            case READ :
                transactional.end();
            case WRITE :
                transactional.commit();
                transactional.end();
        }
        mode.remove();
    }

}
