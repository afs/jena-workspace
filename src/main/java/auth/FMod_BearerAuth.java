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

import java.util.Set;
import java.util.function.Function;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FMod_BearerAuth implements FusekiModule {

    private static Logger LOG = LoggerFactory.getLogger("BearerAuth");

    Function<String, String> verifiedUser = token -> { throw new FusekiException("Must provide a token verifier function");};

    public FMod_BearerAuth() {
        // Required to have the no arg constructor to be a module. But.
        //throw new FusekiException("Must create FMod_BearerAuth with a user verification fucntion");
    }

    public FMod_BearerAuth(Function<String, String> verifiedUser) {
        this.verifiedUser = verifiedUser;
    }

    @Override
    public String name() {
        return "Bearer authentication";
    }

    @Override
    public void start() {
        FmtLog.info(LOG, "Module loaded (%s)", name());
    }

    @Override
    public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        // Inject custom authenticator.
        serverBuilder.addFilter("/*", new AuthBearerFilter(verifiedUser));
    }
}
