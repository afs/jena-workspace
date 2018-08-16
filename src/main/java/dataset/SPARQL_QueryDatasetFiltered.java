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

package dataset;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/** A Query {@link ActionService} that inserts a security filter on each query. */
final
public class SPARQL_QueryDatasetFiltered extends SPARQL_QueryDataset {
        private final SecurityRegistry registry;
        private final Function<HttpAction, String> requestUser;

        public SPARQL_QueryDatasetFiltered(SecurityRegistry registry, Function<HttpAction, String> requestUser) {
            this.registry = registry;
            this.requestUser = requestUser; 
        }
        
        private String determineUser(HttpAction action) {
            return requestUser.apply(action);
        }
        
        @Override
        // DEV ONLY!!!!
        protected Collection<String> customParams() {
            return Arrays.asList("user"); 
        }
        
        // Alt: part of this class
///        private String determineUser(HttpAction action) {
//          boolean b = false;
//          try {
//              b = action.request.authenticate(action.response);
//          } catch (IOException | ServletException e) {
//              e.printStackTrace();
//              b = false;
//          }
//          if ( !b )
//              ServletOps.errorForbidden();
//          
//          action.request.getUserPrincipal();
//          action.request.getRemoteUser();
//          
//          // DEV ONLY
//          String user = action.request.getParameter("user");
//          return user;
//      }

        

        // Jena 3.8.0
//            @SuppressWarnings("deprecation")
//            @Override
//            protected QueryExecution createQueryExecution(Query query, Dataset dataset) {
//                super.log.info("** Intercepted **");
//                QueryExecution qExec = super.createQueryExecution(query, dataset);
//                if ( TDB2Factory.isBackedByTDB(dataset) )
//                    FiltersTDB2.withFilter(qExec.getContext(), sf);
//                return qExec;
//            }
        
        // Jena 3.9.0 : intercept at createQueryExecution(HttpAction action, Query query, Dataset dataset)
        @Override
        protected QueryExecution createQueryExecution(HttpAction action, Query query, Dataset dataset) {
            SecurityContext sCxt = null;
            String user = determineUser(action);
            if ( user != null )
                sCxt = registry.get(user);
            if ( sCxt == null )
                sCxt = worldReadable();
            QueryExecution qExec = super.createQueryExecution(action, query, dataset);
            sCxt.filterTDB(dataset.asDatasetGraph(), qExec);
            return qExec;
        }

        private SecurityContext worldReadable() {
            ServletOps.errorForbidden();
            // Should not get here.
            throw new InternalError();
        }
    }