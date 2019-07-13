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

package fuseki.examples;

import java.util.ServiceLoader;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;

/**
 * Example of adding a new operation to a Fuseki server by registering it with the
 * global Fuseki registries.
 *
 * Doing this, adding the jar to the classpath, including the {@link ServiceLoader}
 * setup, will automatically add it to the server.
 */
public class Ex_FusekiCustomOperation {

    static {
        JenaSystem.init();
        // Imitate Service loader behaviour
        new InitFusekiCustomOperation().start();
    }
    
    // Example usage.
    public static void main(String...args) {
        FusekiLogging.setLogging();
        
        // Also get the operation from the registry to add under a different name.
        Operation op = Operation.register("ExtraService", "Test");
        
        FusekiServer server = FusekiServer.create()
            .add("/ds", DatasetGraphFactory.createTxnMem())

            // No need to do this. It is added as a standard endpoint service by FusekiConfig.addDefaultEndpoint.
            //.addEndpoint("/ds", "extra", op)
            
            // Also put the operation in under a unregistered name.
            .addEndpoint("/ds", "abc", op)
            
            .port(3030)
            //.verbose(true)
            .build();
        try {
            server.start();
            callOperation("extra");
            callOperation("abc");
        }
        finally {
            server.stop();
        }
    }
    
    private static void callOperation(String name) {
        String x = HttpOp.execHttpGetString("http://localhost:3030/ds/"+name);
        if ( x == null ) {
            System.out.println("Not found : <null>");
        } else {
            System.out.print(x);
            if ( ! x.endsWith("\n") )
                System.out.println();
        }
    }

}