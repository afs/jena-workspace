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

import javax.servlet.http.HttpServlet;

import fuseki.router.ActionTestService;
import fuseki.router.ServletBase2;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.web.HttpOp;

/** Change Fuseki configuration on-the-fly */
public class DevRouter {
    
    public static void main(String...a) {
        FusekiLogging.setLogging();
        int port = WebLib.choosePort();
        HttpServlet servlet = new ServletBase2(new ActionTestService(), Fuseki.serverLog);
        
        try {
            FusekiServer server = FusekiServer.create()
                .addServlet("/test", servlet)
                .port(port)
                .build();
            server.start();

            String x = HttpOp.execHttpGetString("http://localhost:"+port+"/test");
            System.out.println("==> "+x);
            
            TypedInputStream s = HttpOp.execHttpPostStream("http://localhost:"+port+"/test", "text/plain", "",  "text/plain");
            
            System.out.println("==> "+IO.readWholeFileAsUTF8(s));
            
        } catch ( Throwable th) {
            th.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

}
