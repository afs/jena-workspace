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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;
import arq.cmdline.ModDatasetAssembler;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiLogging;
import org.apache.jena.fuseki.embedded.FusekiEmbeddedServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.TransactionManager;
import org.slf4j.Logger;

/** Fuseki command that runs a Fuseki server with no UI, just SPARQL services.
 * <p>
 * Use {@code --conf=} for multiple datasets and specific service names. 
 * <p>
 * The command line dataset setup only supports a single dataset.
 */

public class FusekiEmbeddedCmd {
    // Uses command line code.
    // May cause jena-text, jena-spatial to be needed.
    // ==> put in jena-fuseki-server.
    // Ditto normal command?
    
    static {
        // Triggers FUSEKI_HOME
        //FusekiEnv.mode = FusekiEnv.INIT.EMBEDDED;
        FusekiLogging.setLogging();
    }

    static public void main(String... argv) {
        //argv = new String[] {"--mem", "/ds"};
        argv = new String[] {
            //"-v",
            "--conf=config.ttl"};
        FusekiCmdInner.innerMain(argv);
    }
    
    /** Dataset setup (command line, config file) for a dataset (or several if config file) */
    static class ServerConfig {
        public String    datasetPath      = null;              // Dataset name on the command line.
        public boolean   allowUpdate      = false;             // Command line --update.
        public DatasetGraph dsg           = null;              // This is set ...
        public String serverConfig        = null;              // or this.
        public int port;
        public boolean loopback           = false;
        public String datasetDescription;
    }
    
    static class FusekiCmdInner extends CmdARQ {
        private static int defaultPort = 3030;
        
        private static ArgDecl  argMem          = new ArgDecl(ArgDecl.NoValue,  "mem");
        // This does not apply to empty in-memory setups. 
        private static ArgDecl  argUpdate       = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate");
        private static ArgDecl  argFile         = new ArgDecl(ArgDecl.HasValue, "file");
        private static ArgDecl  argMemTDB       = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB", "tdbmem");
        private static ArgDecl  argTDB          = new ArgDecl(ArgDecl.HasValue, "loc", "location", "tdb");
        private static ArgDecl  argPort         = new ArgDecl(ArgDecl.HasValue, "port");
        private static ArgDecl  argLocalhost    = new ArgDecl(ArgDecl.NoValue,  "localhost", "local");
        private static ArgDecl  argTimeout      = new ArgDecl(ArgDecl.HasValue, "timeout");
        private static ArgDecl  argConfig       = new ArgDecl(ArgDecl.HasValue, "config", "conf");
        private static ArgDecl  argGZip         = new ArgDecl(ArgDecl.HasValue, "gzip");
        // private static ModLocation modLocation = new ModLocation();
        private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

        static public void innerMain(String... argv) {
            JenaSystem.init();
            new FusekiCmdInner(argv).mainRun();
        }

        private final ServerConfig serverConfig  = new ServerConfig();
        
        public FusekiCmdInner(String... argv) {
            super(argv);

            if ( false )
                // Consider ...
                TransactionManager.QueueBatchSize = TransactionManager.QueueBatchSize / 2;

            getUsage().startCategory("Fuseki");
            addModule(modDataset);
            add(argMem, "--mem",
                "Create an in-memory, non-persistent dataset for the server");
            add(argFile, "--file=FILE",
                "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file");
            add(argTDB, "--loc=DIR",
                "Use an existing TDB database (or create if does not exist)");
            add(argMemTDB, "--memTDB",
                "Create an in-memory, non-persistent dataset using TDB (testing only)");
            add(argPort, "--port",
                "Listen on this port number");
            add(argLocalhost, "--localhost",
                "Listen only on the localhost interface");
            add(argTimeout, "--timeout=",
                "Global timeout applied to queries (value in ms) -- format is X[,Y] ");
            add(argUpdate, "--update",
                "Allow updates (via SPARQL Update and SPARQL HTTP Update)");
            add(argConfig, "--config=",
                "Use a configuration file to determine the services");
            add(argGZip, "--gzip=on|off",
                "Enable GZip compression (HTTP Accept-Encoding) if request header set");

            super.modVersion.addClass(TDB.class);
            super.modVersion.addClass(Fuseki.class);
        }

        static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName";

        @Override
        protected String getSummary() {
            return getCommandName() + " " + argUsage;
        }

        @Override
        protected void processModulesAndArgs() {
            int x = 0;

            Logger log = Fuseki.serverLog;

            // ---- Checking

            if ( contains(argMem) )             
                x++;
            if ( contains(argFile) )
                x++;
            if ( contains(ModAssembler.assemblerDescDecl) )
                x++;
            if ( contains(argTDB) )
                x++;
            if ( contains(argMemTDB) )
                x++;
            if ( contains(argConfig) )
                x++;

            if ( x == 0 )
                throw new CmdException("No dataset specified on the command line.");

            if ( x > 1 )
                throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --desc");
            
            //---- check: Invalid: --conf + service name.
            if ( contains(argConfig) ) {
                if ( getPositional().size() != 0 )
                    throw new CmdException("Can't have both a configutation file and a service name");
            } else {
                if ( getPositional().size() == 0 )
                    throw new CmdException("Missing service name");
                if ( getPositional().size() > 1 )
                    throw new CmdException("Multiple dataset path names given");
                serverConfig.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
            }
            
            serverConfig.datasetDescription = "<unset>";
            
            // ---- check: Invalid: --update + --conf
            if ( contains(argUpdate) && contains(argConfig) )
                throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
            boolean allowUpdate = contains(argUpdate);
            serverConfig.allowUpdate = allowUpdate;
            

            // ---- Port
            serverConfig.port = defaultPort;
            
            if ( contains(argPort) ) {
                String portStr = getValue(argPort);
                try {
                    int port = Integer.parseInt(portStr);
                    serverConfig.port = port;
                } catch (NumberFormatException ex) {
                    throw new CmdException(argPort.getKeyName() + " : bad port number: " + portStr);
                }
            }
            if ( contains(argLocalhost) )
                serverConfig.loopback = true;

            // ---- Dataset
            // Only one of these is choose from the checking above.
            
            // Fuseki config file 
            if ( contains(argConfig) ) {
                String file = getValue(argConfig);
                Path path = Paths.get(file);
                if ( ! Files.exists(path) )
                    throw new CmdException("File not found: "+file);
                if ( Files.isDirectory(path) )
                    throw new CmdException("Is a directory: "+file);
                serverConfig.datasetDescription = "Configuration: "+path.toAbsolutePath();
                serverConfig.serverConfig = getValue(argConfig);
            }
            
            // Ways to setup a dataset.
            if ( contains(argMem) ) {
                serverConfig.datasetDescription = "in-memory";
                // Only one setup should be called by the test above but to be safe
                // and in case of future changes, clear the configuration.  
                serverConfig.dsg = DatasetGraphFactory.createTxnMem();
                // Always allow.
                serverConfig.allowUpdate = true;
            }

            if ( contains(argFile) ) {
                String filename = getValue(argFile);
                serverConfig.datasetDescription = "in-memory :  load file: " + filename;
                if ( !FileOps.exists(filename) )
                    throw new CmdException("File not found: " + filename);
                serverConfig.dsg = DatasetGraphFactory.createTxnMem();
                
                // INITIAL DATA.
                Lang language = RDFLanguages.filenameToLang(filename);
                if ( language == null )
                    throw new CmdException("Can't guess language for file: " + filename);
                Txn.executeWrite(serverConfig.dsg,  ()->RDFDataMgr.read(serverConfig.dsg, filename));
            }

            if ( contains(argMemTDB) ) {
                serverConfig.datasetDescription = "TDB dataset in-memory";
                serverConfig.dsg = TDBFactory.createDatasetGraph();
                serverConfig.allowUpdate = true;
            }

            if ( contains(argTDB) ) {
                String dir = getValue(argTDB);
                serverConfig.datasetDescription = "TDB dataset: "+dir;
                serverConfig.dsg = TDBFactory.createDatasetGraph(dir);
            }

            if ( contains(ModAssembler.assemblerDescDecl) ) {
                serverConfig.datasetDescription = "Assembler: "+ getValue(ModAssembler.assemblerDescDecl);
                // Need to add service details.
                Dataset ds = modDataset.createDataset();
                serverConfig.dsg = ds.asDatasetGraph();
            }

            // ---- Misc features.
            if ( contains(argTimeout) ) {
                String str = getValue(argTimeout);
                ARQ.getContext().set(ARQ.queryTimeout, str);
            }

//            if ( contains(argGZip) ) {
//                if ( !hasValueOfTrue(argGZip) && !hasValueOfFalse(argGZip) )
//                    throw new CmdException(argGZip.getNames().get(0) + ": Not understood: " + getValue(argGZip));
//                jettyServerConfig.enableCompression = super.hasValueOfTrue(argGZip);
//            }
        }

//        private static String sort_out_dir(String path) {
//            path.replace('\\', '/');
//            if ( !path.endsWith("/") )
//                path = path + "/";
//            return path;
//        }

        @Override
        protected void exec() {
            try {
                FusekiEmbeddedServer server = buildServer(serverConfig);
                info(server, serverConfig);
                server.start();
                server.join();
                System.exit(0);
            }
            catch (AssemblerException ex) {
                if ( ex.getCause() != null )
                    System.err.println(ex.getCause().getMessage());
                else
                    System.err.println(ex.getMessage());
            }
        }

        // ServerConfig -> Setup the builder.
        private static FusekiEmbeddedServer buildServer(ServerConfig serverConfig) {
            FusekiEmbeddedServer.Builder builder = FusekiEmbeddedServer.create();
            // Loopback.
            builder.setPort(serverConfig.port);
            builder.setLoopback(serverConfig.loopback);
            
            if ( serverConfig.serverConfig != null )
                // Config file.
                builder.parseConfigFile(serverConfig.serverConfig);
            else
                // One dataset.
                builder.add(serverConfig.datasetPath, serverConfig.dsg, serverConfig.allowUpdate);
            return builder.build();
        }

        private void info(FusekiEmbeddedServer server, ServerConfig serverConfig) {
            Logger log = Fuseki.serverLog;
            // Dataset -> Endpoints
            Map<String, List<String>> z = description(DataAccessPointRegistry.get(server.getServletContext()));
            
            if ( serverConfig.datasetPath != null ) {
                if ( z.size() != 1 )
                    log.error("Expected only one dataset");
                List<String> endpoints = z.get(serverConfig.datasetPath); 
                FmtLog.info(log,  "Path = %s; Type = %s; Services = %s", serverConfig.datasetPath, serverConfig.datasetDescription, endpoints);
            } else if ( serverConfig.serverConfig != null ) {
                // May be many datsets and services.
                FmtLog.info(log,  "Configuration file %s", serverConfig.serverConfig);
                z.forEach((name, endpoints)->{
                    FmtLog.info(log,  "Path = %s; Services = %s", name, endpoints);
                });
            } else 
                log.error("No dataset path nor server configuration file");
            if ( super.isVerbose() )
                SystemInfo.logDetailsVerbose(log);
            else if ( !super.isQuiet() )
                SystemInfo.logDetails(log);
        }

        private static Map<String, List<String>> description(DataAccessPointRegistry reg) {
            Map<String, List<String>> desc = new LinkedHashMap<>();
            reg.forEach((ds,dap)->{
                List<String> endpoints = new ArrayList<>();
                desc.put(ds, endpoints);
                DataService dSrv = dap.getDataService();
                dSrv.getOperations().forEach((opName)->{
                    dSrv.getOperation(opName).forEach(ep-> {
                        String x = ep.getEndpoint();
                        if ( x.isEmpty() )
                            x = "quads";
                        endpoints.add(x);   
                    });
                });
            });
            return desc;
        }
        
        @Override
        protected String getCommandName() {
            return "fuseki";
        }
    }
}
