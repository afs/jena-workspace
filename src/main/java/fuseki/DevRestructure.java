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

import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.mgt.ActionDatasets;
import org.apache.jena.fuseki.system.FusekiLogging;

public class DevRestructure {
    
    // [DONE]
    // src/main/java:
    // cmd/  FusekiCmd.java  FusekiConfigException.java  mgt_x/  webapp/ authz
    // src/test/java:
    
    // jena-fuseki-core -> jena-fuseki-engine?
    
    // mgt - webapp dependencies
    // ActionDatasets -- SystemState.getDataset() -- needs a file area. /configuration, /databases
    // ActionBackupList -- FusekiSystem.dirBackups
    
    /* 
     * Fuseki restructure ; DevRestructure
     * 
     * jetty-webapp -> jetty-xml + jetty-servlet + jetty-servlets
     *  + jetty-servlets
     * 9.4.9.v20180320
     */ 
    
/*
    Y <url-pattern>/$/tasks/*</url-pattern>
    
    // Disk related
    ? <url-pattern>/$/backup/*</url-pattern>
    ? <url-pattern>/$/backups/*</url-pattern>         <!-- Alt spelling -->
    ?  <url-pattern>/$/backups-list</url-pattern>      <!-- Lists backups --> N
    ? <url-pattern>/$/server</url-pattern> ?
    // Disk related
    ? <url-pattern>/$/datasets/*</url-pattern>
      Rethink for programmatic adding while running.
      
      server.addDataservice
      server.removeDataservice
*/    
    public static void main(String...a) {
        FusekiLogging.setLogging();
        FusekiServer server =
            FusekiServer.create()
                // No? This is disk related.
                .addServlet("/$/datasets", new ActionDatasets())
                .build();
        server.start().join();
        
    }
}
