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

import org.apache.jena.fuseki.servlets.HttpAction;

/** Basic service super class.
 * @see ActionService2 for the normal serverice execution, with counters base class. 
 */
public abstract class ActionServiceBasic extends ActionBase2 {

    protected ActionServiceBasic(String... methods) {
        super(methods);
    }

    /** The validation step of a request */
    protected abstract void validate(HttpAction action);

    /** The perform step of a request */
    protected abstract void perform(HttpAction action);
    
    @Override
    final
    protected void executeAction(HttpAction action) {
        // Now inside logging and error handling.
        executeLifecycle(action);
    }

    /**
     * Simple execution lifecycle for a SPARQL Request.
     * No statistics.
     * 
     * @param action
     */
    protected void executeLifecycle(HttpAction action) {
        validate(action);
        perform(action);
    }

}
