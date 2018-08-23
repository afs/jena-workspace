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

import java.util.function.Function;

import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;

/** A Query {@link ActionService} that inserts a security filter on each query. */
final
public class Filtered_SPARQL_QueryDataset extends SPARQL_QueryDataset {
    private final Function<HttpAction, String> requestUser;

    public Filtered_SPARQL_QueryDataset(Function<HttpAction, String> requestUser) {
        this.requestUser = requestUser; 
    }

    private String determineUser(HttpAction action) {
        return requestUser.apply(action);
    }

    @Override
    protected QueryExecution createQueryExecution(HttpAction action, Query query, Dataset dataset) {
        // Server database, not the possibly dynamically built "dataset"
        DatasetGraph dsg = action.getDataset();
        if ( dsg == null )
            return super.createQueryExecution(action, query, dataset);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return super.createQueryExecution(action, query, dataset);
        
        SecurityRegistry registry = getSecurityRegistry(action, query, dataset);
        if ( registry == null )
            ServletOps.errorOccurred("Internal Server Error");

        SecurityPolicy sCxt = null;
        String user = determineUser(action);
        sCxt = registry.get(user);
        if ( sCxt == null )
            sCxt = noSecurityPolicy();
        
        if ( dsg instanceof DatasetGraphAccessControl ) {
            // Take off one layer.
            dsg = DatasetGraphAccessControl.unwrap(dsg);
            // Add back the Dataset for the createQueryExecution call.
            dataset = DatasetFactory.wrap(dsg);
        }
        
        QueryExecution qExec = super.createQueryExecution(action, query, dataset);
        if ( sCxt != null )
            sCxt.filterTDB(dsg, qExec);
        return qExec;
    }

    private SecurityRegistry getSecurityRegistry(HttpAction action, Query query, Dataset dataset) {
        DatasetGraph dsg = action.getDataset();
        if ( dsg instanceof DatasetGraphAccessControl )
            return ((DatasetGraphAccessControl)dsg).getRegistry();
        return dsg.getContext().get(DataAccessCtl.symSecurityRegistry);
    }

    private SecurityPolicy noSecurityPolicy() {
        ServletOps.errorForbidden();
        // Should not get here.
        throw new InternalError();
    }
}