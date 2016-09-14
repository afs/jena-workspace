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

import org.apache.jena.fuseki.jetty.JettyFuseki ;
import org.apache.jena.fuseki.jetty.JettyServerConfig ;
import org.apache.jena.fuseki.server.FusekiServerListener ;
import org.apache.jena.fuseki.server.ServerInitialConfig ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;

public class FusekiFullEmbedded
{
    public static void main(String[] args) throws Exception {
        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core" ;
        String fusekiBase = "/home/afs/tmp/run" ;
        
        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;

//        // Dev
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ; 
//
//        JettyServerConfig jettyServerConfig = new JettyServerConfig() ;
//        // Dev
//        jettyServerConfig.port = 3035 ;
//        jettyServerConfig.contextPath = "/" ;
//        jettyServerConfig.jettyConfigFile = null ;
//        jettyServerConfig.enableCompression = true ;
//        jettyServerConfig.verboseLogging = false ;
//        
//        ServerInitialConfig config = new ServerInitialConfig() ;
//        config.argTemplateFile  = null ;
//        // Dev
//        config.datasetPath = "/rdf" ;
//        config.allowUpdate = false ;
//        config.dsg = dsg ;
//        config.fusekiCmdLineConfigFile = null ;         // Command line --conf.
//        config.fusekiServerConfigFile = null ;          // Calculated config.ttl from run area (if not --conf)
//        
//        FusekiCmd.runFuseki(config, jettyServerConfig);
        
        /* Two modes 
         * : run the server on a run/ area
         * : run this dataset (and this dataset only) 
         *  
         */
        
        FusekiLoggingUpgrade.setLogging();
        FusekiLoggingUpgrade.allowLoggingReset(false);
        
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        JettyFuseki server = Builder.create()
            .setPort(3035)
            .addDataset("/rdf", dsg)
            .build();
        
        server.start() ;
        server.join() ;

    }
    
    public static class Builder {
        public static Builder create() { return new Builder() ; }
        
        JettyServerConfig jettyServerConfig = new JettyServerConfig() ;
        {
            jettyServerConfig.port = 3035 ;
            jettyServerConfig.contextPath = "/" ;
            jettyServerConfig.jettyConfigFile = null ;
            jettyServerConfig.enableCompression = true ;
            jettyServerConfig.verboseLogging = false ;
        }
        
        ServerInitialConfig fusekiConfig = new ServerInitialConfig() ;
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
        public Builder setFusekiHome(String home) { return this ; }
        /** Run area */
        public Builder setFusekiBase(String base) { return this ; }
        
        public Builder setContextpath(String path) { return this ; }
        public Builder setPort(int port) { return this ; }
        
        public Builder addDataset(String name, DatasetGraph dsg) { return this ; }
        public Builder addDataset(String name, DatasetGraph dsg, boolean withUpdate) { return this ; }
        
        public Builder setConfigFile(String filename) { return this ; }
        
        
        public JettyFuseki build() {
            // check dsg+dataPath OR fusekiCmdLineConfigFile  (or fusekiServerConfigFile) OR none.
            
            //FusekiCmd.runFuseki(fusekiConfig, jettyServerConfig);
            
            FusekiServerListener.initialSetup = fusekiConfig ;
            JettyFuseki.initializeServer(jettyServerConfig) ;
            return JettyFuseki.instance ;
        }
        
        
        
        
        
    }
}
