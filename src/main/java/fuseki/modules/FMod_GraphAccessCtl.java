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

import java.util.UUID;

import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.access.VocabSecurity;
import org.apache.jena.fuseki.main.FusekiLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

public class FMod_GraphAccessCtl implements FusekiModule {

    @Override
    public String name() {
        return "GraphAccessCtl-"+UUID.randomUUID();
    }

    @Override
    public void start() {
        VocabSecurity.init();
    }

    @Override
    public void configuration(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
        dapRegistry.forEach((name, dap) -> {
            // Override for graph-level access control.
            if ( DataAccessCtl.isAccessControlled(dap.getDataService().getDataset()) ) {
                dap.getDataService().forEachEndpoint(ep->
                    FusekiLib.modifyForAccessCtl(ep, DataAccessCtl.requestUserServlet));
            }
        });
    }
}
