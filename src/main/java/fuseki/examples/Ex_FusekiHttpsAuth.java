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

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.web.AuthSetup;

/** Run a Fuseki server with HTTPS and Authentication, programmatic. */ 
public class Ex_FusekiHttpsAuth {
    // Setup
    static String    USER     = "user1";
    static String    PASSWORD = "pw1";
    // When using "digest", this must agree with the password file
    // for MD5 and CRYPT entries.
    static String    REALM    = "TripleStore";
    static String    HOST     = "localhost";
    static int       PORT     = 3443;
    static AuthSetup auth     = new AuthSetup(HOST, PORT, USER, PASSWORD, REALM);
    
    // curl -k -d 'query=ASK{}' --basic --user 'user:password' https://localhost:3443/ds

    public static void main(String...argv) {
        try {
            // By code, with client.
            codeHttpsAuth();
            client();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static FusekiServer codeHttpsAuth() {
        FusekiLogging.setLogging();
        // Some empty dataset
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
            //.verbose(true)
            .https(3443, /*certStore*/"certs/mykey.jks", /*certStorePassword*/"cert-pw")
            .port(3030)
            .auth(AuthScheme.BASIC)
            .passwordFile("Examples/passwd-basic")
            .add("/ds", dsg)
            .build();
        server.start();
        //server.join();
        return server;
    }
    
    private static HttpClientBuilder httpClientBuilder() {
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) {
                return true;
            }
        };

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        try {
            return HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(
                                        new SSLContextBuilder().loadTrustMaterial(trustStrategy).build(),
                                        hostnameVerifier));
                
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static HttpClient httpClient(String user, String password) {
    
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(user, password);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        
        HttpClientBuilder builder = httpClientBuilder()
            .setDefaultCredentialsProvider(credsProvider);
        return builder.build(); 
    }
    
    private static void client() {
        //RDFConnection connSingle = RDFConnectionFactory.connect("https://localhost:3443/ds");
        
        // Allow self-signed
        HttpClient hc = httpClient(USER, PASSWORD);
        
        RDFConnection connSingle = RDFConnectionFuseki.create()
            .httpClient(hc)
            .destination("https://localhost:3443/ds")
            .build();
            
        try ( RDFConnection conn = connSingle ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        }
    }
}
