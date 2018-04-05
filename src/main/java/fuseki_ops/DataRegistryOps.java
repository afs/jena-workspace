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

package fuseki_ops;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiSystem;

/** Operations on databases.
 *  This class assumes the databases are in /databases and /configuration
 */
public class DataRegistryOps {
    
//    public static Path ROOT = FusekiEnv.FUSEKI_BASE;
//    public static Path DATABASE = FusekiSystem.dirDatabases;
//    public static Path CONFIGRUATION = FusekiSystem.dirConfiguration;
    public static Path ROOT = Paths.get("RUN");
    public static Path DATABASE = ROOT.resolve(FusekiSystem.databasesLocationBase);
    public static Path CONFIGRUATION = ROOT.resolve(FusekiSystem.configDirNameBase);
    static {
        FileOps.ensureDir(ROOT.toString());
        FileOps.ensureDir(DATABASE.toString());
        FileOps.ensureDir(CONFIGRUATION.toString());
    }

    public static void createDatabase(DataAccessPointRegistry registry, String dbName) {
        
    }

    public static void deleteDatabase(DataAccessPointRegistry registry, String dbName) {
        
    }
    
    public static void renameDatabase(DataAccessPointRegistry registry, String fromName, String toName) {
        
    }

}
