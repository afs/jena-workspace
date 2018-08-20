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

import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_GSP_R;
import org.apache.jena.sparql.util.Context;

public class Filtered_SPARQL_GSP_R extends SPARQL_GSP_R {
    
    @Override
    protected void doGet(HttpAction action) {
        // For this, mask on target.
        Target target = determineTarget(action) ;
//        Node gn = target.graphName;
//        boolean dftGraph = target.isDefault;
        // Yes/no based on graph.
        
        // 1:: DatsetGraphWrapper and modify  action.activeDGS;
        // 2:: action.getContext().set(...)
        
        Context context = action.getActiveDSG().getContext();
        //action.getContext(); // Is this the DGS? No. copied.
        action.getContext().set(VocabSecurity.symControlledAccess, true);
        // XXX
        SecurityRegistry securityRegistry = null;
        action.getContext().set(VocabSecurity.symSecurityRegistry, securityRegistry); 
        super.doGet(action);
    }

}
