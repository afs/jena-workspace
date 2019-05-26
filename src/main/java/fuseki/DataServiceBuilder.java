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

package fuseki;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.sparql.core.DatasetGraph;

class DataServiceBuilder {
    private DatasetGraph dataset;
    private AuthPolicy authPolicy;
    private List<Endpoint> endpoints = new ArrayList<>();

    public DataServiceBuilder() {}
    
    public DataServiceBuilder dataset(DatasetGraph dataset) {
        this.dataset = dataset;
        return this;
    }
    
    public DataServiceBuilder addEndpoint(Endpoint endpoint) {
        return this;
    }
    
    public DataServiceBuilder addEndpoint(Operation operation) {
        return this;
    }
    
    public DataServiceBuilder addEndpoint(Operation operation, String name) {
        return this;
    }
    
    public DataServiceBuilder addEndpoint(Operation operation, AuthPolicy autPolicy) {
        return this;
    }
    
    public DataServiceBuilder addEndpoint(Operation operation, String name, AuthPolicy autPolicy) {
        return this;
    }
    
    // Endpoint

    public DataServiceBuilder setAuthPolicy(AuthPolicy authPolicy) {
        this.authPolicy = authPolicy;
        return this;
    }
    
    public DataService build() {
        Objects.requireNonNull(dataset, "dataset not set");
        DataService dSrv = new DataService(dataset);
        dSrv.getEndpoints().addAll(endpoints);
        for ( Endpoint ep : endpoints )
            dSrv.addEndpoint(null, null);
        if ( authPolicy != null )
            dSrv.setAuthPolicy(authPolicy);
        return dSrv;
    }
}