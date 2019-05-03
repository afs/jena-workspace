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

import java.util.*;

import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/** Base of all implementations of an {@link HttpAction}. */
public abstract class ActionBase2 implements ActionProcessor { 
    /* This is not a servlet. */
    private Collection<String> methods;

    /** The collection of methods should be upper case strings. */
    protected ActionBase2(String... methodNames) {
        Objects.requireNonNull(methodNames, "methods") ;
        methods = new HashSet<String>();
        for ( String m : methodNames )
            methods.add(m);
    }
        
    /**
     * Execute this request. 
     * Subclasses provide {@link #executeAction} which called inside logging and error handling. 
     * @param action HTTP Action
     */
    @Override
    final
    public void process(HttpAction action) {
        // TODO Fakery for testing. Makes ActionService2 work for minimal setup standalone servlets 
        if ( action.getDataService() == null ) {
            DataService dSrv = new DataService(DatasetGraphFactory.createTxnMem());
            DataAccessPoint dap = new DataAccessPoint("TEST", dSrv);
            action.setRequest(dap, dap.getDataService());
            action.setControlRequest(dap, "TEST");
        }
        ActionExecLib.execAction(action, x->acceptMethod(x), a->executeAction(a));
    }
    
    protected abstract void executeAction(HttpAction action);
    
    /** Check whether this ActionProcessor handles anHTTP method */  
    protected boolean acceptMethod(String methodName) {
        return methods.contains(methodName);
    }
}
