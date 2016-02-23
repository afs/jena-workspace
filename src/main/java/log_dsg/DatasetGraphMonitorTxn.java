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

package log_dsg;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphMonitor ;
import org.apache.jena.sparql.core.Transactional ;

class DatasetGraphMonitorTxn extends DatasetGraphMonitor implements Transactional {
    private DatasetChangesTxn receiver;

    DatasetGraphMonitorTxn(DatasetGraph dsg, DatasetChangesTxn receiver) {
        super(dsg, receiver) ;
        this.receiver = receiver ;
    }

    @Override
    public void begin(ReadWrite readWrite) {
        receiver.begin1(readWrite);
        get().begin(readWrite);
        receiver.begin2(readWrite);
    }

    @Override
    public void commit() {
        receiver.commit1() ;
        get().commit();
        receiver.commit2() ;
    }

    @Override
    public void abort() {
        receiver.abort1() ;
        get().abort();
        receiver.abort2() ;
    }

    @Override
    public boolean isInTransaction() {
        return get().isInTransaction();
    }

    @Override
    public void end() {
        receiver.end1(); 
        get().end();
        receiver.end2();
    }
}