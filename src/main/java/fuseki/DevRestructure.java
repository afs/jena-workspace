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
    // mgt - webapp dependencies
    // ActionDatasets -- SystemState.getDataset() -- needs a file area. /configuration, /databases
    // ActionBackupList -- FusekiSystem.dirBackups
    
    
/*
    Y <url-pattern>/$/tasks/*</url-pattern>
    Y <url-pattern>/$/backup/*</url-pattern>
    Y <url-pattern>/$/backups/*</url-pattern>         <!-- Alt spelling -->
      <url-pattern>/$/backups-list</url-pattern>      <!-- Lists backups --> N
    ? <url-pattern>/$/server</url-pattern> ?
    Y <url-pattern>/$/datasets/*</url-pattern>
      Rethink for programmatic adding while running.
      
      server.addDataservice
      server.removeDataservice
      
/*
    <servlet-name>PingServlet</servlet-name>
    <servlet-class>org.apache.jena.fuseki.ctl.ActionPing</servlet-class>
    <url-pattern>/$/ping</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.ctl.ActionStats</servlet-class>
    <servlet-name>ActionStats</servlet-name>
    <url-pattern>/$/stats/*</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.ctl.ActionTasks</servlet-class>
    <servlet-name>ActionTasks</servlet-name>
    <url-pattern>/$/tasks/*</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.ctl.ActionSleep</servlet-class>
    <servlet-name>ActionSleep</servlet-name>
    <url-pattern>/$/sleep/*</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.mgt.ActionBackup</servlet-class>
    <servlet-name>ActionBackup</servlet-name>
    <url-pattern>/$/backup/*</url-pattern>
    <url-pattern>/$/backups/*</url-pattern>         <!-- Alt spelling -->
--
    <servlet-class>org.apache.jena.fuseki.mgt.ActionBackupList</servlet-class>
    <servlet-name>BackupListServlet</servlet-name>
    <url-pattern>/$/backups-list</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.mgt.DumpServlet</servlet-class>
    <servlet-name>DumpServlet</servlet-name>
    <url-pattern>/$/dump</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.mgt.ActionServerStatus</servlet-class>
    <servlet-name>ServerStatusServlet</servlet-name>
    <url-pattern>/$/server</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.mgt.ActionDatasets</servlet-class>
    <servlet-name>ActionDatasets</servlet-name>
    <url-pattern>/$/datasets/*</url-pattern>
--
    <servlet-class>org.apache.jena.fuseki.mgt.ActionLogs</servlet-class>
    <servlet-name>ActionLogs</servlet-name>
    <url-pattern>/$/logs</url-pattern>


*/
    
    public static void main(String...a) {
        FusekiLogging.setLogging();
        FusekiServer server =
            FusekiServer.create()
                .addServlet("/$/datasets", new ActionDatasets())
                .build();
        server.start().join();
        
    }
}
