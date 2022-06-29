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

package auth;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServlet;

import org.apache.jena.fuseki.main.JettyServer;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevAttrStore {
    static { FusekiLogging.setLogging(); }

    private static AtomicLong requestIdAlloc = new AtomicLong(0);
    private static Logger LOG = LoggerFactory.getLogger("AttrStore");

    // Store in RDF vs store as strings.
    //   Strings is more flexible.

    // * Trellis.
    // * Fuseki, GSP
    // * PutGet RDF
    // * PutGet String.
    // * etcd is JSON only
    //
    // Java client for etcd
    // https://github.com/etcd-io/jetcd

    public static void main(String ...args) {
        HttpServlet servletActionService = adapter(new AttrStoreAction(), LOG);
        JettyServer server = JettyServer.create()
                .port(1248)
                .verbose(true)
                .addServlet("/*", servletActionService)
                .build();
        server.start();
        String x1 = HttpOp.httpGetString("http://localhost:1248/PLAIN/target");
        String x2 = HttpOp.httpGetString("http://localhost:1248/ATTR/target");
        server.stop();
    }

    /** Create a servlet from an {@link ActionProcessor} . */
    public static HttpServlet adapter(ActionProcessor processor, Logger log) {
        return new ServletAction(processor, log);
    }

    // The joining between ActionBase < (ActionProcessor, ActionLifecycle) and servlets is ServletAction
    // ActionService < ActionBase) adds counters to ActionBase
    static class AttrStoreAction extends ActionBase {

        @Override
        public void validate(HttpAction action) {}

        @Override
        public void execute(HttpAction action) {
            // Split into execGet, execPost etc.
            process(action);
        }

        @Override
        public void execGet(HttpAction action) {
            LOG.info("***** AttrStore_ActionService.execGet");
            ServletOps.success(action);
        }
    }
}
