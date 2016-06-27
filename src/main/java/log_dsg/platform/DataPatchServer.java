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

package log_dsg.platform;

import javax.servlet.Servlet ;

import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.handler.ErrorHandler ;
import org.eclipse.jetty.servlet.ServletHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;

/** A simple packaging of Jetty to provide an embeddable HTTP server that just support servlets */ 
public class DataPatchServer {
    
    public static void main(String...arg) {
        DataPatchServer server = new DataPatchServer(1066) ;
        //server.addServlet("/foo", new ServletOne());
        server.addServlet("/fetch", new S_Fetch()) ;
        server.addServlet("/patch", new S_Patch()) ;
        server.start();
        server.join();
    }
    
    private final boolean loopback = false ;
    private final Server server ;
    private ServletHandler handler ;

    public DataPatchServer(int port) {
        server = new Server(port) ;
        ErrorHandler eh = new ErrorHandlerDataPatch() ;
        eh.setServer(server);
        handler = new ServletHandler();
        server.setHandler(handler);
        server.addBean(eh) ;
    }
    
    public void addServlet(String path, Servlet servlet) {
        handler.addServletWithMapping(new ServletHolder(servlet), path);
    }
    
    public void start() {
        try {
            server.start();
            //server.dumpStdErr();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void join() {
        try {
            server.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
