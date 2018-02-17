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

package rdfconnection;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;

class RDFConnectionRemoteBuilder {
    private Transactional txnLifecycle;
    private HttpClient httpClient;
    private HttpContext httpContext;
    private String destination;
    private String sQuery;
    private String sUpdate;
    private String sGSP;
    
    RDFConnectionRemoteBuilder(String destination, String sQuery, String sUpdate, String sGSP) {
        this.destination = destination;
        this.sQuery      = sQuery;
        this.sUpdate     = sUpdate;
        this.sGSP        = sGSP;
    }
    
    RDFConnectionRemoteBuilder(RDFConnectionRemote2 base) {
        Objects.requireNonNull(base);
        txnLifecycle = base.txnLifecycle;
        if ( txnLifecycle == null )
            txnLifecycle = TransactionalLock.createMRPlusSW();
        httpClient  = base.httpClient;
        httpContext = base.httpContext;
        destination = base.destination;
        sQuery      = base.svcQuery;
        sUpdate     = base.svcUpdate;
        sGSP        = base.svcGraphStore;
    }
    
    public void setHttpAcceptSelect(String header) {
    }
    
    public RDFConnectionRemote2 build() {
        requireNonNull(txnLifecycle);
        requireNonNull(destination);
        return new RDFConnectionRemote2(txnLifecycle, httpClient, httpContext, destination, sQuery, sUpdate, sGSP);
    }
}