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

package dev;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.ArgModuleGeneral;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionDumpRequest;
import org.apache.jena.fuseki.main.FusekiMainInfo;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.servlets.UploadRDF;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.DSP;
import org.slf4j.Logger;

public class RunFuseki
{
    // SEE ALSO fuseki.evolution.*
    public static void main(String ... a) throws Exception {
        //System.setProperty("fuseki.loglogging", "true");
        // Warning - this can pick up log4j.properties files from test jars.
        // Skip test-classes

        FusekiLogging.setLogging();

        try {
            mainDetails();
            //mainFModArgs();
            //mainExternal();
            //maingeo();

        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void mainDetails() {
        // Would like to have:
        // * Command line to serverConfig
        // * serviceConfig to FusekiServer.Builder
        // * info(serviceConfig)
        // or * info(server)

        //ServerConfig serverConfig = FusekiMain.parseCommandLine("--admin=user:pw", "--mem", "/ds");
        if ( false ) {
            FusekiServer server = FusekiServer.construct("-v", "--mem", "/ds");
            //Fuseki.setVerbose(server.getServletContext(), true);

            System.out.println("** Server (cmd line)");
            printDetails(server);
        }
        if ( true ) {
            DataService dataSrv = DataService.newBuilder(DatasetGraphFactory.createTxnMem())
                    .addEndpoint(Operation.Query)
                    .addEndpoint(Operation.Shacl, "shacl")
                    .build();
            FusekiServer server = FusekiServer.create()
                    .port(3456)
                    .add("ds1",  DatasetGraphFactory.empty())
                    .add("ds2",  DatasetGraphFactory.empty(), false)
                    .add("/ds3", dataSrv)
                    .staticFileBase("FilesGoHere")
                    .build();
            //Fuseki.setVerbose(server.getServletContext(), true);

            System.out.println("** Server (builder)");
            printDetails(server);
        }
    }

    private static void printDetails(FusekiServer server) {
        Logger log = Fuseki.serverLog;
        FusekiMainInfo.logCode(log);
        FusekiMainInfo.logServer(log, server, true);
        System.out.println();
        FusekiMainInfo.logServer(log, server, false);
    }

    private static void mainFModArgs() {

        ArgModuleGeneral args = new ArgModuleGeneral() {
            private ArgDecl argAdminPW = new ArgDecl(true, "--admin");

            @Override
            public void registerWith(CmdGeneral cmdLine) {
                cmdLine.add(argAdminPW);
            }

            @Override
            public void processArgs(CmdArgModule cmdLine) {
                if ( cmdLine.hasArg(argAdminPW) )
                    System.err.println("Argument: value = "+cmdLine.getValue(argAdminPW));
                else
                    System.err.println("No argument");
            }
        };

        FusekiModule fmod = new FusekiModule() {
            @Override
            public String name() { return "args" ;}

            @Override
            public void start() {
                FusekiMain.addArgModule(args);
            }
        };

        FusekiModules.add(fmod);
        FusekiServer server = FusekiMain.build("-v", "--admin=user:pw", "--mem", "/ds");
    }

    private static void maingeo() {
        // Fails when jena-geosparql is not on the classpath
        //  but if it is, the server prints warnings including for SIS_DATA.

        FusekiServer server = FusekiServer.create()
                .verbose(true)
                .parseConfigFile("/home/afs/tmp/c.ttl")
                .port(3030)
                .build()
                .start();
    }

    public static void mainServer(String ... a) {
        OperationRegistry.get().register(Operation.Upload, new UploadRDF());

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
                .verbose(true)
                .add("/ds", dsg)
                .addEndpoint("/ds", "upload", Operation.Upload)
                .port(3030)
                .build()
                .start();
        String URL = "http://localhost:"+server.getPort();
        DSP.service(URL+"/ds/upload").POST("D.trig");
        DatasetGraph dsg2 = DSP.service(URL+"/ds/").GET();
        RDFDataMgr.write(System.out, dsg2,  Lang.TRIG);
    }

    public static void mainWebapp() {
        RunFusekiFull.mainWebapp();
    }

    public static void mainSparqler() {
        // --sparqler pages/ == --empty
//        FusekiMainCmd.main(
//            "--sparqler=/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-main/sparqler/pages"
//            );

        FusekiLogging.setLogging();
        FusekiServer.create()
            .addServlet("/dump", new ActionDumpRequest())
            // -- SPARQLer.
            .addServlet("/sparql",  new SPARQL_QueryGeneral())
            .staticFileBase("/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-main/sparqler/pages")
            .addServlet("/validate/query",  new QueryValidator())
            .addServlet("/validate/update", new UpdateValidator())
            .addServlet("/validate/iri",    new IRIValidator())
            .addServlet("/validate/data",   new DataValidator())
            // -- SPARQLer.
            .build().start().join();
    }

}
