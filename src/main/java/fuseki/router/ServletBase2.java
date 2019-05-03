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

package fuseki.router ;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.slf4j.Logger;

/**
 * Adapter of ActionProcessor to a plain servlet. 
 */
public class ServletBase2 extends HttpServlet {

    private ActionProcessor actionProcessor;
    private Logger log;
    
    public ServletBase2(ActionProcessor actionProcessor, Logger log) {
        this.actionProcessor = actionProcessor;
        this.log = log ;
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        long id = ActionLib2.allocRequestId(request, response);
        HttpAction action = allocHttpAction(id, request, response);
        actionProcessor.process(action);
    }
    
//    protected void  
    
    private HttpAction allocHttpAction(long id, HttpServletRequest request, HttpServletResponse response) {
        // Need a way to set verbose logging on a per servlet and per request basis. 
        return new HttpAction(id, Fuseki.actionLog, request, response);
    }
}
