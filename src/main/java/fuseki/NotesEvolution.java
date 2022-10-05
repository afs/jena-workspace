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

public class NotesEvolution {
    /*
    ** Evolving Fuseki
    [ ] Create modules package in Fuseki main - code load in FusekiMain[Cmd].
    [ ] Fork mgt package, or mgt code to core or to a new maven module.
        - Fork tests
        - fuseki-cmp
    [ ] mgt changes:
        No system database
        No online/offline.
        Better template naming.
        More templates!
    + Tidy (and split?) FusekiInfo - command line version (webapp), server.logServer versions
*/


    // Plan:
    // Accept restriction that one JVM can not have servers with different modules.

    // Goal 1: replace fuseki-server becomes Fuseki Main.
    // [ ] New mgt2 package as module in jena-fuseki-main
    //     --admin, --admin-passwd @file. --passwd
    //     Validate verisons of modules
    // [ ] New script
    // Goal 2: split into plugins.
    // [ ] lib/
    // Goal 3:

    // NB Modules are operative - need to package (FMbox_Blank) carefully.

    // [ ] Graph assemblers - FusekiConfig to graph

    // GOAL:
    // + Cloud and containers.
    // + Better development

    // Possible Modules:
    // * Admin
    // * Workbench/UI
    // * FusekiKafka bridge : https://github.com/Telicent-io/jena-fuseki-kafka (self-build)
    // * ABAC (WIP)
    // * Metrics (less --ping, --stats, --metrics) : add OpenTelemtry
    // * Other security (LDAP)
    // * Bearer authentication : which JWT validation environment.
    // * Delta?
    // * Alt logging

    // Potentially,
    // * Existing functionality repackaged: FMod_GraphAccessCtl
    //     Geosparql
    //     Text (and hence make Lucene optional) - about 13M

}
