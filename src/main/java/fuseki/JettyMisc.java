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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** A memory of things related to Jetty */
public class JettyMisc {
    
    public static void x_twoHandlers() {
        Server jettyServer = new Server(); //server.getJettyServer();
        ServletContextHandler handler1 = new ServletContextHandler();
        /*ServletContext*/ handler1.getServletContext(); 
        handler1.setContextPath("/path1");
        handler1.addServlet((ServletHolder)null, "/action");

        ServletContextHandler handler2 = new ServletContextHandler();
        handler2.setContextPath("/path2");
        handler2.addServlet((ServletHolder)null, "/action");
        
        HandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(handler1);
        handlers.addHandler(handler2);
        jettyServer.setHandler(handlers);

    }

}
