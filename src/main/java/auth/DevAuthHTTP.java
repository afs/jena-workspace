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

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.auth.*;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecHTTP;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevAuthHTTP {
    static {
        LogCtl.setLog4j2();
        //FusekiLogging.setLogging();
    }

    private static String user = "u";
    private static String password = "p";

    public static void main(String...args) throws IOException, InterruptedException {
//        testDigestCalc(); System.exit(0);



        // Digest auth server
        UserStore userStore = JettyLib.makeUserStore(user, password);

//        AuthScheme authScheme = null;
//        AuthScheme authScheme = AuthScheme.DIGEST;
        AuthScheme authScheme = AuthScheme.BASIC;
        LOG.info("Scheme = "+ ( (authScheme==null)?"none":authScheme.toString() ));
        SecurityHandler sh =
                (authScheme==null)
                ? null
                : JettyLib.makeSecurityHandler("TripleStore",  userStore, authScheme);

        FusekiServer.Builder builder = FusekiServer.create()
            .add("/ds", DatasetGraphFactory.createTxnMem())
            //.verbose(true)
            .port(3030);
        if ( authScheme != null )
            builder.securityHandler(sh).serverAuthPolicy(Auth.policyAllowSpecific(user));

        FusekiServer server = builder.build();
        server.start();
        //server.start().join();

        try {
            client();
            //clientJDK(authScheme);
            //clientQueryExec();
            server.stop();
        }
        catch (Exception ex) {
            System.out.flush();
            ex.printStackTrace();
            System.err.flush();
        } finally  {
            //server.stop();
            System.exit(0);
        }

    }

    private static void testDigestCalc() {
        // Test digest calculations.
        String authHeaderStr = "Digest"
                +", realm=\"testrealm@host.com\""
                +", qop=\"auth,auth-int\""
                +", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\""
                +", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        AuthChallenge aHeader = AuthChallenge.parse(authHeaderStr);
        String cnonce="0a4f113b";
        String nc = "00000001";
        String USER = "Mufasa";
        String PASSWORD = "Circle Of Life";
        String x = DigestLib.calcDigestChallengeResponse(aHeader, USER, PASSWORD,
                                                         "GET", "/dir/index.html",
                                                         cnonce, nc, "auth");
        String expected = "6629fae49393a05397450978507c4ef1";
        System.out.println("C: "+x);
        System.out.println("E: "+expected);
        if ( ! expected.equals(x) )
            System.out.println("DIFFERENT");
    }

    static Logger LOG = LoggerFactory.getLogger("APP");
    static { LogCtl.enable(LOG); }
    // Registry two: active

    // [ ] Migrate
    // [ ] javadoc for registries. package-info.java.
    // [ ] Multiuser support.

    private static void client() throws IOException, InterruptedException {
        // ---- Setup.
        HttpClient hc = HttpClient.newBuilder().build();

        String requestURI = "/ds";
        String endpoint = "http://localhost:3030"+requestURI;
        //String endpoint = HttpLib.endpoint(uri);

        // Prefix
        URI urix = URI.create("http://localhost:3030/");
        String requestTarget = HttpLib.requestTarget(urix);

        AuthDomain domain = new AuthDomain(urix, null);
        AuthEnv.registerUsernamePassword(urix, user, password);

        // Register early.

        // ---- Request

        HttpRequest request = HttpRequest.newBuilder().uri(HttpLib.toRequestURI(endpoint)).GET().build();
        BodyHandler<Void> bodyHandler = BodyHandlers.discarding();

        // ---- Response after auth.
        // [QExec] no use of endpoint?
        HttpResponse<Void> httpResponse = AuthLib.authExecute(hc, request, bodyHandler);

        //if ( null )
        System.err.println("A: "+httpResponse.statusCode());

        // ---- Subsequent call.
//        AuthEnv.clearAuthRequestModifiers();
//        AuthEnv.unregisterUsernamePassword(urix);

        // Should be able to call again.
        HttpRequest.Builder builder = HttpLib.newBuilderFor(endpoint);
        HttpRequest request99 = builder.uri(HttpLib.toRequestURI(endpoint)).GET().build();
//        if ( request99.headers().map().isEmpty() )
//            System.out.println("  <empty>");
//        request99.headers().map().forEach((k,list)->{
//            System.out.printf("  %s: %s\n", k, list);
//        });

        HttpResponse<Void> httpResponse99 = AuthLib.authExecute(hc, request99, bodyHandler);
        System.err.println("X: "+httpResponse99.statusCode());
    }

    private static void clientJDK(AuthScheme authScheme) throws IOException, InterruptedException {
        // Will JDK add digest auth?
        if ( authScheme == AuthScheme.DIGEST ) {
            System.out.println("Not supported by JDK: "+authScheme);
            return;
        }

        HttpURLConnection x;
        // Basic - yes.
        // Digest - no.
        String requestURI = "/ds";
        String endpoint = "http://localhost:3030"+requestURI;
        //String endpoint = HttpLib.endpoint(uri);
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                //System.err.println("**** getPasswordAuthentication");
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
        HttpClient hc = HttpClient.newBuilder().authenticator(authenticator).build();
        HttpRequest request = HttpRequest.newBuilder().uri(HttpLib.toRequestURI(endpoint)).GET().build();
        HttpResponse<Void> response = hc.send(request, BodyHandlers.discarding());
        System.out.println("--> "+response.statusCode());
    }

    private static void clientQueryExec() {
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                //System.err.println("**** getPasswordAuthentication");
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };

        // -- --
        LOG.info("-- Update with service tuning");
        protect(()->{
            HttpRequestModifier mods = (params, headers) ->
                headers.put(HttpNames.hAuthorization, HttpLib.basicAuth("u", "p"));

            // [QExec] Wrong.
            // Need to do that for update.
            RegistryRequestModifier svcReg = new RegistryRequestModifier();
            svcReg.add("http://localhost:3030/ds", mods);
            ARQ.getContext().put(ARQ.httpRegistryRequestModifer, svcReg);

            try {
            UpdateExec uExec = UpdateExecHTTP.newBuilder()
                .service("http://localhost:3030/ds")
                .updateString("INSERT DATA { <x:s> <x:q> 123}")
                //.httpHeader(HttpNames.hAuthorization, HttpLib.basicAuth("u", "p"))
                //.httpClient(hc)
                .build();
            uExec.execute();
            } finally { ARQ.getContext().remove(ARQ.httpRegistryRequestModifer); }
        });

        //LOG.info("-- query with global modification");

        String[] x = {
            "SELECT * { ?s ?p ?o }"
//            , "ASK  {}"
//            , "CONSTRUCT WHERE { ?s ?p ?o }"
        };

        // -- --
        LOG.info("-- Query with custom HttpClient");

        protect(()->{
            HttpClient hc = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(authenticator)
                .build();
            for ( var qs : x ) {
                try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .httpClient(hc)
                    .service("http://localhost:3030/ds/query")
                    .queryString(qs)
                    .build()) {
                    QueryExecUtils.executeQuery(QueryExecutionAdapter.adapt(qexec));
                }
            }
        });
    }

    static void protect(Runnable action) {
        try {
            action.run();
        } catch (Throwable th) {
            LOG.warn("** "+th.getMessage());
            //th.printStackTrace();
        }
    }
}
