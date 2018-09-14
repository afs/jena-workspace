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

import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.main.cmds.*;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sys.JenaSystem;

public class FusekiServerSecuredCmd {
    
    static { FusekiLogging.setLogging(); }

    /**
     * Build and run, a server based on command line syntax. This operation does not
     * return. See {@link FusekiMain#build} to build a server using command line
     * syntax but not start it.
     */
    static public void main(String... argv) {
        FusekiLogging.setLogging();
        JenaSystem.init();
        FusekiServerSecuredMain.innerMain(argv);
    }

    static class FusekiServerSecuredMain extends FusekiMain {
        
        static void innerMain(String... argv) {
            JenaSystem.init();
            new FusekiServerSecuredMain(argv).mainRun();
        }
        
        protected FusekiServerSecuredMain(String... argv) {
            super(argv);
            // Inject DataAccessCtl.fusekiBuilder()
            // Limits args.
        }

        @Override
        protected FusekiServer.Builder builder() {
            //return FusekiServer.create();
            // TESTING
            return DataAccessCtl.fusekiBuilder(DataAccessCtl.requestUserServlet);
        }
    }
}
