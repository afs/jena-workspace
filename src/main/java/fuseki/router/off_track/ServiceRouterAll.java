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

package fuseki.router.off_track;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.HttpAction;

/**
 * This servlet makes the routing decisions for all service operations (not admin, not
 * task handling).
 * <p>
 * The two routing operations are {@link #chooseOperation(HttpAction, DataService)} for
 * operation on the dataset and {@link #chooseOperationByName(HttpAction, DataService, String)}
 * for operations by service endpoint.
 * <p>
 * Normal use is to route all service operations to this servlet via {@link ActionService}.
 * It will route for operations on the dataset.
 * <p>
 * It be attached to a dataset location and acts as a router for all SPARQL operations
 * (query, update, graph store, both direct and indirect naming, quads operations on a
 * dataset and ?query and ?update directly on a dataset.). Then specific service servlets
 * attached to each service endpoint.
 * <p>
 * It work in conjunction with {@link ActionService#execCommonWorker} to decide where to
 * route requests.
 */
public class ServiceRouterAll extends ServiceRouter {
    
    public ServiceRouterAll() {
        super();
    }
    
    // These calls should not happen because ActionService calls chooseOperation(),
    // looks that up in the ServiceDispatchRegistry for the servlet context,
    // then calls executeLifecycle() on that servlet.
    // These exceptions catch any loops. 
    @Override
    protected void validate(HttpAction action) {
        throw new FusekiException("Call to ServiceRouter.validate");
    }

    @Override
    protected void perform(HttpAction action) {
        throw new FusekiException("Call to ServiceRouter.perform");
    }
    
    /**
     * Choose dispatch when {@code serviceName} is not the empty string.
     * <p>
     * Example {@code /dataset/sparql} has dataset URI {@code /dataset} and service name
     * {@code sparql}. The {@code serviceName} is the empty string which is handled by
     * {@link #chooseOperation(HttpAction, DataService)}.
     * <p>
     * If the service name isn't recognized, drops through to GSP Direct Naming (the graph
     * name is the whole URI). This is not usually enabled; it is controlled by
     * {@link Fuseki#GSP_DIRECT_NAMING}.
     */
    @Override
    protected Operation chooseOperationByName(HttpAction action, DataService dataService, String endpointName) {
        return chooseOperationByNameImpl(action, dataService, endpointName);
    }
    
    /**
     * Choose dispatch when {@code serviceName} is the empty string.
     * <p>
     * Example {@code /dataset} has dataset URI {@code /dataset}.
     * <p>
     * Dispatch is based on:
     * <ul>
     * <li>HTTP params (for ?query= and ?update=)</li>
     * <li>Content type</li>
     * <li>
     * </ul>
     */
    @Override final
    protected Operation chooseOperation(HttpAction action, DataService dataService) {
        // Default implementation in ActionService:
        //      Endpoint ep = dataService.getEndpoint(endpointName);
        //      Operation operation = ep.getOperation();
        //      action.setEndpoint(ep);
        return chooseOperationImpl(action, dataService);
    }
}
