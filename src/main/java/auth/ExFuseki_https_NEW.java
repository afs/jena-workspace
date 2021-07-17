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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.security.KeyStore;

import javax.net.ssl.*;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.web.AuthSetup;

/** Run a Fuseki server with HTTPS and Authentication, programmatic. */
public class ExFuseki_https_NEW {
    // Setup
    // Aligned with the testing/Access/passwd file.
    static String    USER     = "user1";
    static String    PASSWORD = "pw1";
    // When using "digest", this must agree with the password file
    // for MD5 and CRYPT entries.
    static String    REALM    = "TripleStore";
    static String    HOST     = "localhost";
    static int       PORT     = 3443;
    static AuthSetup auth     = new AuthSetup(HOST, PORT, USER, PASSWORD, REALM);

    private static final String DIR = "/home/afs/ASF/jena-workspace/Fuseki-https/";
    private static final String KEYSTORE = DIR+"certs/localhost-test.jks";
    private static final String KEYSTOREPASSWORD = "store-pw";
    private static final String PASSWD = DIR+"passwd";

    // curl -k -d 'query=ASK{}' --basic --user 'user:password' https://localhost:3443/ds

    public static void main(String...argv) {

        try {
            codeHttpsAuth(KEYSTORE, KEYSTOREPASSWORD, PASSWD);
            SSLContext sslContext = ExamplesLib.trustOneCert(KEYSTORE, KEYSTOREPASSWORD);
            //SSLContext  sslContext = ExamplesLib.trustAll();
            client(sslContext);
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static FusekiServer codeHttpsAuth(String keystore, String keystorePassword, String passwd) {
        FusekiLogging.setLogging();
        // Some empty dataset
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        // HTTPS, user/password.
        FusekiServer server = FusekiServer.create()
            //.verbose(true)
            .https(3443, keystore, keystorePassword)
            .passwordFile(passwd).auth(AuthScheme.BASIC)
            .add("/ds", dsg)
            .build();
        server.start();
        //server.join();
        return server;
    }

    static void client(SSLContext sslContext) {
        Authenticator authenticator1 = AuthLib.authenticator("user1", "pw1");
        Authenticator authenticator2 = AuthLib.authenticator("user1", "wrong-password");

        HttpClient hc1 = HttpClient.newBuilder()
                .authenticator(authenticator1)
                .sslContext(sslContext)
                .build();

        RDFConnection connSingle = RDFConnectionFuseki.create()
                .httpClient(hc1)
                .destination("https://localhost:3443/ds")
                .build();

        try ( RDFConnection conn = connSingle ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        }

        // No. Password fails.
        HttpClient hc2 = HttpClient.newBuilder()
                .authenticator(authenticator2)
                .sslContext(sslContext)
                .build();
        try ( RDFConnection conn = RDFConnectionFuseki.create()
                .httpClient(hc2)
                .destination("https://localhost:3443/ds")
                .build(); ) {
            QueryExecution qExec = conn.query("ASK{}");
            QueryExecUtils.executeQuery(qExec);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }


}
