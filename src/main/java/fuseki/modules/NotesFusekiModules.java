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

package fuseki.modules;

public class NotesFusekiModules {
    // ** Moving modules into core.
    // But "FusekiServer.Builder"
    // core->kernel; main->core; add ->main (cmd)
    // Would like FusekiServer.Builder "addModule".
    // Then InitSecurity can "add module"?

    // FusekiServer.Builder.buildFusekiContext

    // Manual and automatic modules --> services/
    //   Can code-add in JenaSystem init.

    // FusekiServer.Builder.addModule

    // Naming: "name()" and "instanceId()"

    //Rename in jena-fuseki-access -  SecurityContext -> SecurityGraphContext
}
