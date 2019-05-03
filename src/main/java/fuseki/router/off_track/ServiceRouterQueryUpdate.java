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

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;

/**
 * This servlet makes the routing decisions for SPARQL query and SPARQL update only.
 * <p>
 * The two routing operations are {@link #chooseOperation(HttpAction, DataService)} for
 * operation on the dataset and {@link #chooseOperationByName(HttpAction, DataService, String)}
 * for operations by service endpoint.
 * <p>
 * It works in conjunction with {@link ActionService#execCommonWorker} to decide where to
 * route requests.
 */
public class ServiceRouterQueryUpdate extends ServiceRouter {
    
    public ServiceRouterQueryUpdate() {
        super();
    }

    // These calls should not happen because ActionService calls chooseOperation(),
    // looks that up in the ServiceDispatchRegistry for the servlet context,
    // then calls executeLifecycle() on that servlet.
    // These exceptions catch any loops. 
    @Override
    protected void validate(HttpAction action) {
        throw new FusekiException("Call to ServiceRouterQueryUpdate.validate");
    }

    @Override
    protected void perform(HttpAction action) {
        throw new FusekiException("Call to ServiceRouterQueryUpdate.perform");
    }

    @Override final
    protected Operation chooseOperationByName(HttpAction action, DataService dataService, String endpointName) {
        // Development: Ignore endpoint!
        return chooseOperationImpl(action, dataService);
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
    @Override 
    final protected Operation chooseOperation(HttpAction action, DataService dataService) {
        // Default implementation in ActionService:
        //      Endpoint ep = dataService.getEndpoint(endpointName);
        //      Operation operation = ep.getOperation();
        //      action.setEndpoint(ep);
        return chooseOperationImpl(action, dataService);
    }

    // Simplified, query/update only dispatch.
    private static Operation chooseOperationQueryUpdate(HttpAction action, DataService dataService) {
        HttpServletRequest request = action.getRequest();

        // ---- Dispatch based on HttpParams
        // -- Query
        boolean isQuery = request.getParameter(HttpNames.paramQuery) != null;
        if ( isQuery ) {
            if ( !ServiceRouterAll.allowQuery(action) )
                ServletOps.errorMethodNotAllowed("SPARQL query : " + action.getMethod());
            return Operation.Query;
        }

        // -- Update
        // Standards name "update", non-standard name "request" (old use by Fuseki)
        boolean isUpdate = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null;
        if ( isUpdate ) {
            if ( !ServiceRouterAll.allowUpdate(action) )
                ServletOps.errorMethodNotAllowed("SPARQL update : " + action.getMethod());
            // The SPARQL_Update servlet will deal with using GET.
            return Operation.Update;
        }
        
        // ---- Content-type
        // HTML forms are handled by request.getParameter() above.
        String ct = request.getContentType();
        if ( ct != null ) {
            if ( ct.equals(WebContent.contentTypeSPARQLQuery ) ) {
                if ( !ServiceRouterAll.allowQuery(action) )
                    ServletOps.errorMethodNotAllowed("SPARQL query : " + action.getMethod());
                return Operation.Query;
            }
                
            if ( ct.equals(WebContent.contentTypeSPARQLUpdate ) ) {
                if ( !ServiceRouterAll.allowUpdate(action) )
                    ServletOps.errorMethodNotAllowed("SPARQL update : " + action.getMethod());
                // The SPARQL_Update servlet will deal with using GET.
                return Operation.Update;
                
            }
        }
        
        ServletOps.errorBadRequest("Not a SPARQL Query nor a SPARQL Update request");
        return null;
    }
}
