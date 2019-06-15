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

public class NotesFuseki {
    // Other
    //   To archive: fuseki1, ... 
    
    // Wrapper - same endpoints and configuration, different dadasets 
    
    // MUST
    
    // SHOULD
    //   ActionLib.doOptionsGet etc.
    //   Check use of ActionLib2.setCommonHeaders
    
    
    // New builder:
    //     fuseki:endpoint [ fuseki:operation fuseki:Query ; fuseki:name "" ; fuseki:allowedUsers (....) ] ; 
    /*     fuseki:endpoint [ 
                     fuseki:operation fuseki:Query ;
                     fuseki:allowedUsers (....) ;
                     fuseki:timeout "1000,1000" ;
                     fuseki:queryLimit 1000;
                     arq:unionGraph true;
                     ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "1000" ] ;
                     ] ;
    */
    /*
                  [ fuseki:operation my:op ; fuseki:opImlementation <java:org.me.MyActionProcessor> ]; 
     */
    //     fuseki:serviceQuery [ fuseki:allowedUsers ]

    // --- Other
    
    // Shiro filter?
    // keycloak: SAML, OAuth2
    // New UI, port UI, use RDF4J
    
    // Full test coverage in "main"
    
    // [DISPATCH LEGACY] - only in Dispatcher. If miss a dispatch lookup, look more widely.

    // ++ OTHER
    // XXX
    
    // ++ Sort out / maybe
    // ActionProcessor, ActionLifecycle
    //    ActionProcessor.process - splits by method.
    //    ActionLifecycle -- validate, execute
    
    //    ActionBase.process
    //        Enforce splitting to be "exec" for each HTTP method.
    //        ActionProcessor.super.process(action);
    
    //   Better before, after calls in the lifecycle to add counters.
    //     executeLifecycle

    // ActionBase         implements ActionProcessor, ActionLifecycle
    // ActionCtl          extends ServletProcessor implements ActionLifecycle
    // ServletProcessor   extends HttpServlet implements ActionProcessor
    //   -> Combine ActionProcessor and ActionLifecycle?

    // ActionProcessor : process(HttpAction action) default split into HTTP calls
    //   But ActionService hopes back to ActionLifecycle
    // --> need better way to say "I support"

    // -------------
    

    // ----------------------------------------

    /*
     * -- TIM not shared in assemblers -> Fuseki assembler problem.
     * FusekiConfig.getDataset only works for top level. TIM - named datasets?
     *
     * -- RDFConnection and ping. (ASK{}) RDFConnectionFuseki : ActionDatasetPing,
     * ActionServerPing.
     *
     * -- Fuseki/HTTPS documentation.
     */

    // TODO https port only.
    // Example: HTTPS + auth

    /*
     * passwords:: java -cp ../lib/jetty-util-9.4.7.v20170914.jar
     * org.eclipse.jetty.util.security.Password username
     */
}
