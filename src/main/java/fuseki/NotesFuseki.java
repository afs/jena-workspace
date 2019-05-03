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

import javax.servlet.ServletContext;

import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;

public class NotesFuseki {

    // WIP: SPARQL Query/update only routing.

    // Actions are functions.
    // ActionBase2 is concreate and takes a function to call at "executeAction" (inheritance -> function injection) 
    // OR ActionBase2.execAxction is static and take a function.
    
    // ActionCtl - share HttpAction creation
    //   Auto servler?
    //   ServletCtl = Servlet and Actionbase 
    
    // Class ActionBaseServlet = new ServletBase2(ActionCtl)
    
    // Need ActionCtl to be a servlet.
    
    // org.apache.jena.fuseki.mgt.ActionDatasets must work as a servlet 
    
    // ActionBase2 -- both service and admin processor.
    // ServletBase(ActionProcessor) to convert to a plain servlet
    
    /* Two dispatchers: 
     * (1) for stand alone services - basic setup of the HttpAction.
     * (2) for general dispatch. 
     */

    // Rename ServiceDispatchRegistry as OperationDispatchRegistry?

    // Admin uses ActionBase > ActionCtl

    // Check that /ds/sparql does go through ServiceRouter.

    static { HttpAction httpAction; }

    static { 
        ServiceDispatchRegistry sdr = ServiceDispatchRegistry.get(null);
        sdr.findHandler((Operation)null);
    }

    static {
        DataAccessPointRegistry dapr = DataAccessPointRegistry.get((ServletContext)null);
        DataAccessPoint dap = dapr.get("dataset");
        DataService dataService = dap.getDataService();
        dataService.getDataset();
        dataService.getOperations();
        dataService.getEndpoint("epName");
        dataService.getOperations();
    }
    
    //
    // Then all ActionService have a "router" function.
    // But some routers have no action ==>> separate thing.
    // Injectable.
    
    // Fuseki examples. [done]

    // Need to attach FusekiFilter ServiceRouter to a 
    // FusekiFilter : registry.get(datasetUri).getDataService().getRouter()
    // Could/should routers be separate from Action service?
    // Router calls service(request,response), no HttpAction. 
    //    Misses logging in ActionBase.
    //    Misses try-catch protection.
    // protected void doCommon(HttpServletRequest request, HttpServletResponse response)
    //   ==> 
    // new "protected void doCommon(HttpAction)"
    // and a router < ActionBase not ActionService.
    
    // Handlers with a "already dispatched" function?
    
    
    

    // Register Endpoints with no service name 
    //   But does "" means quads?
    //     Endpoint.serviceName
    //     Endpoint.displayName
    //     Endpoint.isNamedService.

    /*
     * -- TIM not shared in assemblers -> Fuseki assembler problem.
     * FusekiConfig.getDataset only works for top level. TIM - named datasets?
     * 
     * -- RDFConnection and ping. (ASK{}) RDFConnectionFuseki : ActionDatasetPing,
     * ActionServerPing.
     *
     * -- Fuseki/HTTPS documentation. 
     */

    // TODO https port only.
    // Example: HTTPS + auth

    /*
     * passwords:: java -cp ../lib/jetty-util-9.4.7.v20170914.jar
     * org.eclipse.jetty.util.security.Password username 
     */
}
