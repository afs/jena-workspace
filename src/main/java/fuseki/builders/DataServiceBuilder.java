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

package fuseki.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.sparql.core.DatasetGraph;

/** {@link DataService} builder */
public class DataServiceBuilder {
    // Endpoints by configuration.
    private List<EndpointConfig> endpointConfs = new ArrayList<>();
    // Endpoints created elsewhere.
    private List<Endpoint>       endpoints     = new ArrayList<>();
    private DatasetGraph         dataset;
    private AuthPolicy           authPolicy;
    private OperationRegistry    operationRegistry;

    public DataServiceBuilder() {}

    public DataServiceBuilder dataset(DatasetGraph dataset) {
        this.dataset = dataset;
        return this;
    }

    public DataServiceBuilder operationRegistry(OperationRegistry operationRegistry) {
        this.operationRegistry = operationRegistry;
        return this;
    }

    public DataServiceBuilder addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
        return this;
    }

    public DataServiceBuilder addEndpoint(Operation operation) {
        addEndpointCfg(operation, null, null);
        return this;
    }

    public DataServiceBuilder addEndpoint(Operation operation, String name) {
        addEndpointCfg(operation, name, null);
        return this;
    }

    public DataServiceBuilder addEndpoint(Operation operation, AuthPolicy autPolicy) {
        addEndpointCfg(operation, null, authPolicy);
        return this;
    }

    public DataServiceBuilder addEndpoint(Operation operation, String name, AuthPolicy authPolicy) {
        addEndpointCfg(operation, name, authPolicy);
        return this;
    }

    // Delay creating endpoints because the OperationRegistry may not be
    // ready at the point of the addEndpoint call.

    private static class EndpointConfig {
        Operation operation;
        String name;
        AuthPolicy authPolicy;
        public EndpointConfig(Operation operation, String name, AuthPolicy authPolicy) {
            super();
            this.operation = operation;
            this.name = name;
            this.authPolicy = authPolicy;
        }
    }

    private void addEndpointCfg(Operation operation, String name, AuthPolicy authPolicy) {
        EndpointConfig epCfg = new EndpointConfig(operation, name, authPolicy);
        endpointConfs.add(epCfg);
    }

    /**
     * Set the {@link AuthPolicy} for the {@link DataService}.
     * <p>
     * This policy is applied to all requests; any authorization policy
     * on an endpoint is also applied so they both have to authorize the request.
     */
    public DataServiceBuilder authPolicy(AuthPolicy authPolicy) {
        this.authPolicy = authPolicy;
        return this;
    }

    public DataService build() {
        Objects.requireNonNull(dataset, "dataset not set");
        DataService.Builder dSrv = DataService.newBuilder(dataset);
        if ( authPolicy != null )
            dSrv.setAuthPolicy(authPolicy);

        OperationRegistry registry = (operationRegistry == null) ? OperationRegistry.get() : operationRegistry;
        // Endpoint configurations. Create and add endpoint.
        for (EndpointConfig epc : endpointConfs ) {
            ActionService proc = registry.findHandler(epc.operation);
            if ( proc == null )
                throw new FusekiConfigException("No implementation for operation "+epc.operation+" at '"+epc.name+"'");
            Endpoint ep = Endpoint.create()
                .operation(epc.operation)
                .endpointName(epc.name)
                .authPolicy(epc.authPolicy)
                .processor(proc)
                .build();
            dSrv.addEndpoint(ep);
        }
        // Directly added endpoints - ensure each has a processor.
        endpoints.stream().forEach(ep->{
            if ( ep.getProcessor() == null ) {
                ActionService proc = registry.findHandler(ep.getOperation());
                if ( proc == null )
                    throw new FusekiConfigException("No implementation for endpoint "+ep.getOperation()+" at '"+ep.getName()+"'");
                ep.setProcessor(proc);
            }
            dSrv.addEndpoint(ep);
        });
        return dSrv.build();
    }
}