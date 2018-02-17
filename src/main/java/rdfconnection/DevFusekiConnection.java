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

package rdfconnection;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.embedded.FusekiServer ;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;

public class DevFusekiConnection
{
    public static void main(String[] args) throws Exception {
        if ( true ) {
            //FusekiLogging.setLogging();
            LogCtl.setLog4j() ;
        }
        
        int PORT = 3037;

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = 
            FusekiServer.create()
                .setPort(PORT)
                //.setStaticFileBase("/home/afs/ASF/jena-fuseki-cmds/sparqler")
                .add("/ds", dsg)
                .build()
                .start();
        Triple t = SSE.parseTriple("(:s :p <_:b3456>)");
        Model model = ModelFactory.createDefaultModel();
        model.getGraph().add(t);
        
        try {
            try (RDFConnection conn = new RDFConnectionFuseki("http://localhost:"+PORT+"/ds")) {
                conn.put(model);
                Model model2 = conn.fetch();
                model2.getGraph().find().forEachRemaining(System.out::println);
            }
        } finally { server.stop(); } 
    }
}
