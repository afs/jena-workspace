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

import java.util.Enumeration;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ext.com.google.common.base.Predicate;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;

/** 
 * Functions relating to {@link HttpAction} objects, including the standard execute with logging process ({@link #execAction})
 */
public class ActionExecLib {
    // Merge with ActionLib?
    /**
     * Common process for handling HTTP requests with logging and Java error handling.
     * @param action
     * @param methodAccept
     * @param executeAction
     */
    public static void execAction(HttpAction action, Predicate<String> methodAccept, Consumer<HttpAction> executeAction)
    {
        try {
            logRequest(action) ;
            action.setStartTime() ;
    
            initResponse(action) ;

            // The response may be have beenchanged to a HttpServletResponseTracker
            HttpServletResponse response = action.response ;
            try {
                startRequest(action);
                
                //if ( ! acceptMethod(action.getMethod()) ) {
                if ( methodAccept != null && ! methodAccept.apply(action.getMethod()) ) {
                    // Can't use ServletOps.error* here not inside the try-catch-ActionErrorException below.
                    ServletOps.errorMethodNotAllowed(action.getMethod());
                    return;
                }
                
                executeAction.accept(action) ;
            } catch (QueryCancelledException ex) {
                // To put in the action timeout, need (1) global, (2) dataset and (3) protocol settings.
                // See
                //    global -- cxt.get(ARQ.queryTimeout) 
                //    dataset -- dataset.getContect(ARQ.queryTimeout)
                //    protocol -- SPARQL_Query.setAnyTimeouts
    
                String message = String.format("Query timed out");
                // Possibility :: response.setHeader("Retry-after", "600") ;    // 5 minutes
                ServletOps.responseSendError(response, HttpSC.SERVICE_UNAVAILABLE_503, message);
            } catch (ActionErrorException ex) {
                if ( ex.getCause() != null )
                    ex.getCause().printStackTrace(System.err) ;
                // Log message done by printResponse in a moment.
                if ( ex.getMessage() != null )
                    ServletOps.responseSendError(response, ex.getRC(), ex.getMessage()) ;
                else
                    ServletOps.responseSendError(response, ex.getRC()) ;
            } catch (HttpException ex) {
                // Some code is passing up its own HttpException.
                if ( ex.getMessage() == null )
                    ServletOps.responseSendError(response, ex.getResponseCode());
                else
                    ServletOps.responseSendError(response, ex.getResponseCode(), ex.getMessage());
            } catch (RuntimeIOException ex) {
                FmtLog.warn(action.log, ex, "[%d] Runtime IO Exception (client left?) RC = %d : %s", action.id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
                ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            } catch (Throwable ex) {
                // This should not happen.
                //ex.printStackTrace(System.err) ;
                FmtLog.warn(action.log, ex, "[%d] RC = %d : %s", action.id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
                ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            } finally {
                action.setFinishTime() ;
                finishRequest(action);
            }
            logResponse(action) ;
            archiveHttpAction(action) ;
        } catch (Throwable th) {
            FmtLog.error(action.log, th, "Internal error") ;
        }
    }
    
    /**
     * Begin handling an {@link HttpAction}  
     * @param action
     */
    private static void startRequest(HttpAction action) {
        action.startRequest() ;
    }

    /**
     * Stop handling an {@link HttpAction}  
     */
    private static void finishRequest(HttpAction action) {
        action.finishRequest() ;
    }


    /** Log an {@link HttpAction} request. */
    public static void logRequest(HttpAction action) {
        String url = ActionLib.wholeRequestURL(action.request) ;
        String method = action.request.getMethod() ;
    
        FmtLog.info(action.log, "[%d] %s %s", action.id, method, url) ;
        if ( action.verbose ) {
            Enumeration<String> en = action.request.getHeaderNames() ;
            for (; en.hasMoreElements();) {
                String h = en.nextElement() ;
                Enumeration<String> vals = action.request.getHeaders(h) ;
                if ( !vals.hasMoreElements() )
                    FmtLog.info(action.log, "[%d]   => %s", action.id, h+":") ;
                else {
                    for (; vals.hasMoreElements();)
                        FmtLog.info(action.log, "[%d]   => %-20s %s", action.id, h+":", vals.nextElement()) ;
                }
            }
        }
    }

    /** Log an {@link HttpAction} response. */
    public static void logResponse(HttpAction action) {
        long time = action.getTime() ;
    
        HttpServletResponseTracker response = action.response ;
        if ( action.verbose ) {
            if ( action.responseContentType != null )
                FmtLog.info(action.log,"[%d]   <= %-20s %s", action.id, HttpNames.hContentType+":", action.responseContentType) ;
            if ( action.responseContentLength != -1 )
                FmtLog.info(action.log,"[%d]   <= %-20s %d", action.id, HttpNames.hContentLengh+":", action.responseContentLength) ;
            for (Map.Entry<String, String> e : action.headers.entrySet()) {
                // Skip already printed.
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentType) && action.responseContentType != null)
                    continue;
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentLengh) && action.responseContentLength != -1)
                    continue;
                FmtLog.info(action.log,"[%d]   <= %-20s %s", action.id, e.getKey()+":", e.getValue()) ;
            }
        }
    
        String timeStr = fmtMillis(time) ;
    
        if ( action.message == null )
            FmtLog.info(action.log, "[%d] %d %s (%s)", 
                action.id, action.statusCode, HttpSC.getMessage(action.statusCode), timeStr) ;
        else
            FmtLog.info(action.log,"[%d] %d %s (%s)", action.id, action.statusCode, action.message, timeStr) ;
    
        // See also HttpAction.finishRequest - request logging happens there.
    }

    /** Set headers for the response. */ 
    public static void initResponse(HttpAction action) {
        ServletBase.setCommonHeaders(action.response) ;
        String method = action.request.getMethod() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            ServletBase.setVaryHeader(action.response) ;
    }

    /**
     * <p>Given a time point, return the time as a milli second string if it is less than 1000,
     * otherwise return a seconds string.</p>
     * <p>It appends a 'ms' suffix when using milli secoOnds,
     *  and 's' for seconds.</p>
     * <p>For instance: </p>
     * <ul>
     * <li>10 emits 10 ms</li>
     * <li>999 emits 999 ms</li>
     * <li>1000 emits 1.000 s</li>
     * <li>10000 emits 10.000 s</li>
     * </ul>
     * @param time the time in milliseconds
     * @return the time as a display string
     */
    private static String fmtMillis(long time) {
        // Millis only? seconds only?
        if ( time < 1000 )
            return String.format("%,d ms", time) ;
        return String.format("%,.3f s", time / 1000.0) ;
    }

    /**
     * Archives the HTTP Action.
     * @param action HTTP Action
     * @see HttpAction#minimize()
     */
    private static void archiveHttpAction(HttpAction action) {
        action.minimize() ;
    }

}
