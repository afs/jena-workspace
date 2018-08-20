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

package fuseki.security;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.ServletContext;

import fuseki.JettyLib;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;
import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;

public class DataAccessControlledFuseki {
    
    // XXX with SecurityRegistry
    // --> SecurityBuilder.
    
    /**
     * Create a {@link FusekiServer} with service access control
     * and a query handler that uses the logged in use to filter data access. 
     * The returned server has not been started.
     */
    public static FusekiServer fuseki(int port, UserStore userStore, Consumer<FusekiServer.Builder> config) {
        // The "find the user" function
        Function<HttpAction, String> determineUser = (action)->action.request.getRemoteUser();

        ActionService queryServletAccessFilter = new Filtered_SPARQL_QueryDataset(determineUser);
        SecurityHandler sh = null;
        if ( userStore != null )
            sh = JettyLib.makeSecurityHandler("/*", "DatasetRealm", userStore);
        
        FusekiServer.Builder builder = FusekiServer.create().port(port);
        
        if ( sh != null )
            builder.securityHandler(sh);
        // Different name
        Operation q2 = Operation.register("AltQuery", "Alt Query Service");
        builder.registerOperation(q2, queryServletAccessFilter);
        // Fails due to check that operations are registered once.
        //.registerOperation(Operation.Query, queryServlet2)
        //builder.registerOperation(Operation.Query, queryServletAccessFilter);

        // Add datasets.
        config.accept(builder);
        
        FusekiServer server = builder.build();
        enable(server, determineUser);
        return server;
    }
    
    public static void enable(FusekiServer server, Function<HttpAction, String> determineUser) {
        /* 
         * Reconfigure standard Jena Fuseki, replacing the default implementation of "query"
         * with a filtering one.  This for this server only. 
         */
        // The mapping operation to handler is in the ServiceDispatchRegistry and is per
        // server (per servlet context). "registerOrReplace" would be a better name,
        ActionService queryServletAccessFilter = new Filtered_SPARQL_QueryDataset(determineUser);
        ServletContext cxt = server.getServletContext();
        ServiceDispatchRegistry.get(cxt).register(Operation.Query, WebContent.contentTypeSPARQLQuery, queryServletAccessFilter);
    }
}
