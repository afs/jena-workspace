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



//import java.util.Collection;
//
//import org.apache.jena.atlas.logging.LogCtl;
//import org.apache.jena.atlas.web.HttpException;
//import org.apache.jena.atlas.web.TypedInputStream;
//import org.apache.jena.fuseki.build.FusekiBuilder;
//import org.apache.jena.fuseki.embedded.FusekiServer;
//import org.apache.jena.fuseki.server.DataAccessPointRegistry;
//import org.apache.jena.fuseki.server.DataService;
//import org.apache.jena.fuseki.server.Operation;
//import org.apache.jena.fuseki.servlets.ActionService;
//import org.apache.jena.query.QueryExecution;
//import org.apache.jena.rdfconnection.RDFConnection;
//import org.apache.jena.rdfconnection.RDFConnectionFactory;
//import org.apache.jena.riot.web.HttpOp;
//import org.apache.jena.sparql.core.DatasetGraph;
//import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.sparql.util.QueryExecUtils;
//import org.apache.jena.util.FileUtils;
//
//public class DevCustom {
//    static {
//        LogCtl.setLog4j();
//        LogCtl.disable("org.eclipse.jetty");
//    }
//    
//    // ** Fix : IniRealm. (1.2.6-1.3.0 change).
//    
//    // *** Counters
//    
//    // ** ActionSPARQL
//    // 2: Parse request URI in one go : HttpAction.setRequest (rename -> "target"), HttpAction.setEndpoint
//    //     Builder pattern for HttpAction.
//    // 4: Replace action.setEndpoint
//    
//    // ** SPARQL_UberServlet
//    // Delete original.
//
//    // -----------------------------------
//    
//    public static void main(String[] args) {
//        
//        // New service.
//        Operation patchOpName1 = Operation.register("Patch1", "Patch Processor");
//        Operation patchOpName2 = Operation.register("Patch2", "Patch Processor");
//        ActionService patchService = new CustomService();
//
//        // Custom names.
//        // FusekiBuilder.buildDataService().add...
//        // Expose FusekiBuilder.addServiceEP
//        
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        DataService dataService = new DataService(dsg);
//        FusekiBuilder.populateStdServices(dataService, true);
//        FusekiBuilder.addServiceEP(dataService, patchOpName2, "patch");
//        // At this point, there isn't an associated handler for the operation yet. 
//        
//        FusekiServer server = 
//            FusekiServer.create()
//                .setPort(2022)
//                .registerOperation(patchOpName1, "app/patch", patchService)
//                // No content type.
//                .registerOperation(patchOpName2, patchService)
//                
//                //.setVerbose(true)
//                
//                .add("/ds", DatasetGraphFactory.createTxnMem(), true)
//                .addOperation("/ds", "patch", patchOpName1)
//
//                // No Content-Type.
//                .add("/ds2", dataService)
//                .addOperation("/ds2", "patch", patchOpName2)
//                
//                // No custom extension.
//                .add("/ds3", DatasetGraphFactory.createTxnMem(), true)
//                
//                .build();
//        
//        server.start();
//        try { 
//            
//          // Content-type : should fail.
//            try {
//                TypedInputStream stream = HttpOp.execHttpPostStream("http://localhost:2022/ds3", "app/patch", "", "text/plain");
//                throw new RuntimeException();
//            } catch (HttpException ex) {}
//            
//            // Content-type : should fail.
//            try {
//                TypedInputStream stream = HttpOp.execHttpPostStream("http://localhost:2022/ds2", "app/patch", "", "text/plain");
//                throw new RuntimeException();
//            } catch (HttpException ex) {}
//
//            // Content-type : should succeed
//            TypedInputStream stream = HttpOp.execHttpPostStream("http://localhost:2022/ds", "app/patch", "", "text/plain");
//            String x = FileUtils.readWholeFileAsUTF8(stream);
//            System.out.print(x);
//            if ( x == null )
//                throw new RuntimeException("*** BAD *******");
//            
//            
//            try(RDFConnection rconn = RDFConnectionFactory.connect("http://localhost:2022/ds")) {
//                try(QueryExecution qExec = rconn.query("SELECT * {}")) {
//                    //ResultSet rs = qExec.execSelect();
//                    QueryExecUtils.executeQuery(qExec);
//                }
//            }
//            
//            // Service endpoint name.
//            String s1 = HttpOp.execHttpGetString("http://localhost:2022/ds/patch");
//            if ( s1 == null )
//                throw new RuntimeException();
//
//            // Service endpoint name.
//            String s2 = HttpOp.execHttpGetString("http://localhost:2022/ds2/patch");
//            if ( s2 == null )
//                throw new RuntimeException();
//
//            // Service endpoint name. Should fail.
//            String s3 = HttpOp.execHttpGetString("http://localhost:2022/ds3/patch");
//            if ( s3 != null )
//                throw new RuntimeException();
//
//            
//            // Content-type
//
//            // Service endpoint name. DELETE -> fails 405
//            try { 
//                HttpOp.execHttpDelete("http://localhost:2022/ds/patch");
//                throw new IllegalStateException("DELETE succeeded");
//            } catch (Exception ex) {
//                System.out.println(ex.getMessage());
//            }
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            //System.out.println();
//            server.stop();
//        }
//    }
//    static void info(FusekiServer server) {
//        DataAccessPointRegistry registry = server.getDataAccessPointRegistry();
//        registry.forEach((name, accessPt) -> {
//            Collection<Operation> x = accessPt.getDataService().getOperations();
//            System.out.printf("%s\n", name);
//            x.forEach((op)->System.out.printf("  %-8s  %s\n", op.getName(), op.getDescription()));
//            //, name, accessPt.getDataService().getOperations());
//        });
//    }
//}
