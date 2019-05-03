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

import static org.apache.jena.fuseki.server.CounterName.Requests;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.CounterName;
import org.apache.jena.fuseki.server.CounterSet;
import org.apache.jena.fuseki.server.Counters;
import org.apache.jena.fuseki.servlets.ActionErrorException;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.query.QueryCancelledException;

public abstract class ActionService2 extends ActionServiceBasic {

    protected ActionService2(String... methods) {
        super(methods);
    }

    /**
     * Standard execution lifecycle for a SPARQL Request.
     * <ul>
     * <li>{@link #startRequest(HttpAction)}</li>
     * <li>initial statistics,</li>
     * <li>{@link #validate(HttpAction)} request,</li>
     * <li>{@link #perform(HttpAction)} request,</li>
     * <li>completion/error statistics,</li>
     * <li>{@link #finishRequest(HttpAction)}
     * </ul>
     * 
     * @param action
     */
    @Override
    protected void executeLifecycle(HttpAction action) {
        // And also HTTP counter
        CounterSet csService = action.getDataService().getCounters();
        CounterSet csOperation = null;
        if ( action.getEndpoint() != null )
            // Direct naming GSP does not have an "endpoint".
            csOperation = action.getEndpoint().getCounters();

        incCounter(csService, Requests);
        incCounter(csOperation, Requests);
        // Either exit this via "bad request" on validation
        // or in execution in perform.
        try {
            validate(action);
        }
        catch (ActionErrorException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }

        try {
            perform(action);
            // Success
            incCounter(csOperation, RequestsGood);
            incCounter(csService, RequestsGood);
        }
        catch (ActionErrorException | QueryCancelledException | RuntimeIOException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }
    }

    /** Increment counter */
    protected static void incCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        incCounter(counters.getCounters(), name);
    }

    /** Decrement counter */
    protected static void decCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        decCounter(counters.getCounters(), name);
    }

    protected static void incCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return;
        try {
            if ( counters.contains(name) )
                counters.inc(name);
        }
        catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex);
        }
    }

    protected static void decCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return;
        try {
            if ( counters.contains(name) )
                counters.dec(name);
        }
        catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex);
        }
    }

}
