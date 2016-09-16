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

import java.nio.file.Paths ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.jetty.JettyFuseki ;
import org.apache.jena.fuseki.jetty.JettyServerConfig ;
import org.apache.jena.fuseki.server.FusekiEnv ;
import org.apache.jena.fuseki.server.FusekiServerListener ;
import org.apache.jena.fuseki.server.ServerInitialConfig ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;

public class FusekiFullEmbedded
{
    public static void main(String[] args) throws Exception {
        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core" ;
        String fusekiBase = "/home/afs/tmp/run" ;
//        System.setProperty("FUSEKI_HOME", fusekiHome) ;
//        System.setProperty("FUSEKI_BASE", fusekiBase) ;

        /* Two modes 
         * : run the server on a run/ area
         * : run this dataset (and this dataset only) 
         */

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        JettyFuseki server = Builder.create()
            .setFusekiHome(fusekiHome)
            .setFusekiBase(fusekiBase)
            .setPort(3036)
            .addDataset("/rdf", dsg)
            .build();
        server.start() ;
        server.join() ;

    }
    
    public static class Builder {
        public static Builder create() {
            return new Builder() ;
        }
        
        private String fusekiHome ;
        private String fusekiBase ;

        private JettyServerConfig jettyServerConfig = new JettyServerConfig() ;
        {
            jettyServerConfig.port = 3034 ;
            jettyServerConfig.contextPath = "/" ;
            jettyServerConfig.jettyConfigFile = null ;
            jettyServerConfig.enableCompression = true ;
            jettyServerConfig.verboseLogging = false ;
        }
        
        private ServerInitialConfig fusekiConfig = new ServerInitialConfig() ;
        {
            fusekiConfig.argTemplateFile  = null ;
            // Dev
            fusekiConfig.datasetPath = null ;
            fusekiConfig.allowUpdate = false ;
            fusekiConfig.dsg = null ;
            fusekiConfig.fusekiCmdLineConfigFile = null ;         // Command line --conf.
            //config.fusekiServerConfigFile = null ;
        }
        
        /** Pages in webapp */
        public Builder setFusekiHome(String home) {
            this.fusekiHome= home ;
            return this ;
        }

        /** Run area */
        public Builder setFusekiBase(String base) {
            this.fusekiBase = base ;
            return this ;
        }
        
        public Builder setContextpath(String path) {
            jettyServerConfig.contextPath = path ;
            return this ;
        }

        public Builder setPort(int port) {
            jettyServerConfig.port = port ;
            return this ;
        }
        
        public Builder addDataset(String name, DatasetGraph dsg) {
            return addDataset(name, dsg, true) ;
        }

        public Builder addDataset(String name, DatasetGraph dsg, boolean withUpdate) {
            fusekiConfig.datasetPath = name ;
            fusekiConfig.allowUpdate = withUpdate ;
            fusekiConfig.dsg = dsg ;
            return this ;
        }
        
        public Builder setConfigFile(String filename) {
            fusekiConfig.fusekiCmdLineConfigFile = filename ;
            return this ;
        }        
        
        public JettyFuseki build() {
            if ( fusekiHome == null )
                Log.warn(this, "Not set: fusekiHome");
            if ( fusekiBase == null )
                Log.warn(this, "Not set: fusekiBase");
            FusekiEnv.FUSEKI_HOME = Paths.get(fusekiHome) ;
            FusekiEnv.FUSEKI_BASE = Paths.get(fusekiBase) ;
            // Triggers init 
            FusekiLoggingUpgrade.setLogging();
            FusekiLoggingUpgrade.allowLoggingReset(false);
            FusekiServerListener.initialSetup = fusekiConfig ;
            JettyFuseki.initializeServer(jettyServerConfig) ;
            return JettyFuseki.instance ;
        }
    }
}
