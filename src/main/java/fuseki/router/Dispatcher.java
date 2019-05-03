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

package fuseki.router;

import static java.lang.String.format;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Dispatch on registered datasets. This is the entry point into Fuseki for dataset
 * operations.
 * 
 * Administration operations, and directly registered servlets and static content are
 * called through the usual web server process.
 * 
 * HTTP Request URLs, after servlet context removed, take the form {@code /dataset} or {@code /dataset/service}.
 * The most general URL is {@code /context/dataset/service}.
 * The {@link DataAccessPointRegistry} maps {@code /dataset} to a {@link DataAccessPoint}.
 */
public class Dispatcher {

    // Development debugging only. Excessive for normal operation.
    private static final boolean LogDispatch = true; // false
    private static Logger        LOG         = Fuseki.serverLog;

    /**
     * Handle an HTTP request if it is sent to a registered dataset.
     * 
     * Fuseki uses dynamic dispatch, the set of registered datasets can change while
     * the server is running, so dispatch is driven off Fuseki system registries.
     * 
     * If the request URL matches a registered dataset, process the request, and send
     * the response.
     * 
     * @param request
     *            HttpServletRequest
     * @param response
     *            HttpServletResponse
     * @return Returns {@code true} if the request has been handled, else false (no
     *         response sent).
     */
    public static boolean dispatch(HttpServletRequest request, HttpServletResponse response) {
        // Path component of the URI, without context path
        String uri = ActionLib.actionURI(request);
        String datasetUri = ActionLib.mapActionRequestToDataset(uri);

        if ( LogDispatch ) {
            LOG.info("Filter: Request URI = " + request.getRequestURI());
            LOG.info("Filter: Action URI  = " + uri);
            LOG.info("Filter: Dataset URI = " + datasetUri);
        }

        if ( datasetUri == null )
            return false;

        DataAccessPointRegistry registry = DataAccessPointRegistry.get(request.getServletContext());
        if ( !registry.isRegistered(datasetUri) ) {
            if ( LogDispatch )
                LOG.debug("No dispatch for '"+datasetUri+"'");
            return false;
        }
        DataAccessPoint dap = registry.get(datasetUri);
        process(dap, request, response);
        return true;
    }

    /** Set up and handle a HTTP request for a dataset. */
    private static void process(DataAccessPoint dap, HttpServletRequest request, HttpServletResponse response) {
        try {
            long id = ActionLib2.allocRequestId(request, response);
            HttpAction action = allocHttpAction(id, dap, request, response);
            dispatchAction(action);
        } catch (Throwable th) {
            LOG.error("Internal error (dispatch)", th);
            // No point trying. It should been handled.
            // try { response.sendError(HttpSC.INTERNAL_SERVER_ERROR_500, "Internal server error"); }
            // catch (IOException ex) {}
        }
    }

    /**
     * Returns a fresh HTTP Action for this request.
     * @param id the Request ID
     * @param dap 
     * @param request HTTP request
     * @param response HTTP response
     * @return a new HTTP Action
     */
    private static HttpAction allocHttpAction(long id, DataAccessPoint dap, HttpServletRequest request, HttpServletResponse response) {
        // Need a way to set verbose logging on a per servlet and per request basis. 
        HttpAction action = new HttpAction(id, Fuseki.actionLog, request, response);
        if ( dap != null ) {
            // TODO remove setRequest? 
            DataService dataService = dap.getDataService();
            action.setRequest(dap, dataService);
        }
        return action;
    }

    
    /**
     * Determine and call the {@link ActionProcessor} to handle this
     * {@link HttpAction} including access control at the dataset and service levels.
     */
    public static void dispatchAction(HttpAction action) {
        DataAccessPoint dataAccessPoint = action.getDataAccessPoint();
        DataService dSrv = action.getDataService();

        if ( !dSrv.isAcceptingRequests() ) {
            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
            return;
        }

        // ---- Determine Endpoint.
        String endpointName = mapRequestToOperation(action, dataAccessPoint);

        Operation operation;
        if ( !endpointName.isEmpty() ) {
            operation = chooseOperationByName(action, dSrv, endpointName);
            if ( operation == null )
                if ( !Fuseki.GSP_DIRECT_NAMING )
                    ServletOps.errorBadRequest(format("dataset=%s, service=%s", dataAccessPoint.getName(), endpointName));
                else
                    throw new InternalErrorException("Inconsistent: GSP_DIRECT_NAMING but no operation");
        } else {
            // Endpoint ""
            operation = chooseOperation(action, dSrv);
        }

        // ---- Security.

        // ---- Handler.

        // Decide the code to execute the request.
        // ActionProcessor handler = target(action, operation);
        ActionProcessor handler = target(action, operation);
        if ( handler == null )
            ServletOps.errorBadRequest(format("dataset=%s: op=%s", dataAccessPoint.getName(), operation.getName()));
        executeAction(handler, action);
        // handler.executeAction(action);

// handler.executeLifecycle(action);
// return;

    }

    // operation to code for operation.
    private static ActionProcessor target(HttpAction action, Operation operation) {
        System.err.println("**** Dispatcher.target ****");
        // TODO Operation -> code registry.
        //return action.getServiceDispatchRegistry().findHandler(operation);
        return null;
    }

    /** Execute */
    private static void executeAction(ActionProcessor handler, HttpAction action) {
        System.err.println("**** Dispatcher.executeAction ****");
        // handler.executeAction(action);
    }

    /**
     * Map request to operation name.
     * Returns the service name (the part after the "/" of the dataset part) or "".
     */
    protected static String mapRequestToOperation(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToOperation(action, dataAccessPoint);
    }
    
    // POLICY
    /**
     * Return the operation that corresponds to the endpoint name for a given data
     * service. Side effect: This operation should set the selected endpoint in the
     * HttpAction if this operation is determined to be a specific endpoint.
     */
    protected static Operation chooseOperationByName(HttpAction action, DataService dataService, String endpointName) {
        // Overridden by the ServiceRouter.
        // This default implementation is plain service name to operation based on
        // the
        // DataService as would be used by operation servlets bound by web.xml
        // except Fuseki can add and delete mapping while running.
        Endpoint ep = dataService.getEndpoint(endpointName);
        Operation operation = ep.getOperation();
        action.setEndpoint(ep);
        return operation;
    }

    /** look up endpoint name - nothing more */
    protected static Operation chooseOperationByNameImpl_1(HttpAction action, DataService dataService, String endpointName) {
        Endpoint ep = dataService.getEndpoint(endpointName);
        Operation operation = ep.getOperation();
        action.setEndpoint(ep);
        return operation;
    }

    // As _1 adding:
    // 1:: GSP -> Quads.
    // 2:: GSP_DIRECT_NAMING
    /**
     * General purpose chooser: name -> endpoint by data service, endpoint ->
     * operation
     */
    protected static Operation chooseOperationByNameImpl_2(HttpAction action, DataService dataService, String endpointName) {
        Endpoint ep = dataService.getEndpoint(endpointName);
        if ( ep != null ) {
            Operation operation = ep.getOperation();
            action.setEndpoint(ep);
            if ( operation != null ) {
                // Can this be null?
                // If a GSP operation, then no params means Quads operation.
                if ( operation.equals(Operation.GSP_R) || operation.equals(Operation.GSP_RW) ) {
                    // Look for special case. Quads on the GSP service endpoint.
                    boolean hasParamGraph = action.request.getParameter(HttpNames.paramGraph) != null;
                    boolean hasParamGraphDefault = action.request.getParameter(HttpNames.paramGraphDefault) != null;
                    if ( !hasParamGraph && !hasParamGraphDefault ) {
                        if ( operation.equals(Operation.GSP_RW) )
                            return Operation.Quads_RW;
                        else
                            return Operation.Quads_R;
                    }
                }
                return operation;
            }
            FmtLog.warn(action.log, "Notice: endpoint %s but no operation", endpointName);
        }
        return null;
    }

    // POLICIES
    // General.
    // SPARQL Query, Update only.
    // Note quads case.
    /**
     * Return the operation that corresponds to the request when there is no endpoint
     * name. This operation does not set the selected endpoint in the HttpAction.
     */
    protected static Operation chooseOperation(HttpAction action, DataService dataService) {
        // Service Router.
        // No default implementation for directly bound services operation servlets.
        return null;
    }

    // ActionService.
}
