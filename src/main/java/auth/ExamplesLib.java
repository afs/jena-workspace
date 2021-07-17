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
import java.net.Socket;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.*;

import org.apache.jena.http.auth.AuthLib;

/** Shared code in examples. */
public class ExamplesLib {
    /** HttpClient with user/password */
    static HttpClient httpClient(String user, String password) {
        Authenticator authenticator = AuthLib.authenticator(user, password);
        return  HttpClient.newBuilder().authenticator(authenticator).connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** Create an SSL Context that trusts certificates in a keystore. */
    public static SSLContext trustOneCert(String keystore, String keystorePassword) throws Exception {
        InputStream in = new FileInputStream(keystore);

        // Java12
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(in, keystorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    /** Create an SSL Context that trusts any certificate. */
    static SSLContext trustAll() {
        // Setup a TrustManager that trusts anything (for self-signed certificates).
        // Use with great care.
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted (X509Certificate [] chain, String authType, Socket socket) {}
                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, Socket socket) {}
                @Override
                public void checkClientTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {}
                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {}
                // X509TrustManager
                @Override
                public void checkClientTrusted (X509Certificate [] certs, String authType) {}
                @Override
                public void checkServerTrusted (X509Certificate [] certs, String authType) {}
                @Override
                public java.security.cert.X509Certificate [] getAcceptedIssuers () { return new X509Certificate[0];}
            }
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
