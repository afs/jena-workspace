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

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.web.HttpNames;

public class ActionLib2 {
    private static AtomicLong     requestIdAlloc = new AtomicLong(0) ;
    
    /**
     * Helper method which gets a unique request ID and appends it as a header
     * to the response
     * 
     * @param request
     *            HTTP Request
     * @param response
     *            HTTP Response
     * @return Request ID
     */
    public static long allocRequestId(HttpServletRequest request, HttpServletResponse response) {
        long id = requestIdAlloc.incrementAndGet() ;
        addRequestId(response, id) ;
        return id ;
    }

    /**
     * Helper method for attaching a request ID to a response as a header
     * 
     * @param response
     *            Response
     * @param id
     *            Request ID
     */
    public static void addRequestId(HttpServletResponse response, long id) {
        response.addHeader("Fuseki-Request-ID", Long.toString(id)) ;
    }

    public static boolean CORS_ENABLED = false ;
    
    public static void setCommonHeadersForOptions(HttpServletResponse httpResponse) {
        if ( CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowHeaders, "X-Requested-With, Content-Type, Authorization") ;
        setCommonHeaders(httpResponse) ;
    }

    public static void setCommonHeaders(HttpServletResponse httpResponse) {
        if ( CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*") ;
        if ( Fuseki.outputFusekiServerHeader )
            httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName) ;
    }

    /**
     * Extract the name after the container name (servlet name).
     * @param action an HTTP action
     * @return item name as "/name" or {@code null}
     */
    private /*unused*/ static String extractItemName(HttpAction action) {
//          action.log.info("context path  = "+action.request.getContextPath()) ;
//          action.log.info("pathinfo      = "+action.request.getPathInfo()) ;
//          action.log.info("servlet path  = "+action.request.getServletPath()) ;
        // if /name
        //    request.getServletPath() otherwise it's null
        // if /*
        //    request.getPathInfo() ; otherwise it's null.

        // PathInfo is after the servlet name. 
        String x1 = action.request.getServletPath() ;
        String x2 = action.request.getPathInfo() ;

        String pathInfo = action.request.getPathInfo() ;
        if ( pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") )
            // Includes calling as a container. 
            return null ;
        String name = pathInfo ;
        // pathInfo starts with a "/"
        int idx = pathInfo.lastIndexOf('/') ;
        if ( idx > 0 )
            name = name.substring(idx) ;
        // Returns "/name"
        return name ; 
    }
}
