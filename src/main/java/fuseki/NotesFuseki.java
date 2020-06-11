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
    // Does not work: empty dataset name and named services ("/sparql" looks like a dataset name.)
    //   ActionLib.mapRequestToDataset is registry oblivious


    // Next:
    //   Modular Fuseki
    //     With admin, with backup.
    //   Query string dispatch [??]

    // -----------------------
    // [DISPATCH LEGACY] - only in Dispatcher. If miss a dispatch lookup, look more widely.
    //   2 occurrences.
    //    Searching named services in Dispatch.chooseEndpoint
    //    Checking access control.


    // [] Check stats.

    // --- Later
    // [] Admin!

    // Shiro filter?
    // keycloak: SAML, OAuth2
    // New UI, port UI, use RDF4J

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
