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

import org.apache.jena.fuseki.jetty.JettyLib;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class JettyHttps {
    // HTTPS
    private static void http() {
        Server server = new Server();
        
        // HTTP Connector
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme(HttpScheme.HTTPS.asString());
        http_config.setSecurePort(8443);
//        http_config.setOutputBufferSize(32768);
//        http_config.setRequestHeaderSize(8192);
//        http_config.setResponseHeaderSize(8192);
//        http_config.setSendServerVersion(true);
//        http_config.setSendDateHeader(false);
        http_config.addCustomizer(new SecureRequestCustomizer());

//        ServerConnector plainConnector = new ServerConnector(server, new HttpConnectionFactory(http_config));
//        server.addConnector(plainConnector);
        
    }
    
    private static Server https() {
        Server server = new Server();
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme(HttpScheme.HTTPS.asString());
        http_config.setSecurePort(8443);
        
        String jetty_home = "JETTY_HOME";
        
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(jetty_home + "/etc/keystore");
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

//        sslContextFactory.setTrustStorePath(jetty_home + "/etc/keystore");
//        sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
//        
//        sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
//                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
//                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
//                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
//                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
//                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

        // HTTPS Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        https_config.addCustomizer(src);

        // HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(8443);
        // Only https
        server.addConnector(sslConnector);
        
        // ????
        // Info -   how to redirect if HTTP
        SecuredRedirectHandler srh = new SecuredRedirectHandler();
        JettyLib.addHandler(server, srh);
        return server; 
    }
    

    public class ServerX extends org.eclipse.jetty.server.Server {

        public ServerX(int httpPort, boolean enableSsl, int httpsPort, 
                       String keystorePath, String keystorePassword, String keyManagerPassword) {
            initConnector(httpPort, enableSsl, httpsPort, keystorePath, keystorePassword, keyManagerPassword);
            //...
        }

        private void initConnector(int httpPort, boolean enableSsl, int httpsPort, String keystorePath, String keystorePassword, String keyManagerPassword) {
            if (enableSsl) {
                final HttpConfiguration httpConfig = getHttpConfig(httpsPort);
                final HttpConfiguration httpsConfig = getHttpsConfig(httpConfig);
                final ServerConnector httpConnector = getHttpConnector(httpConfig, httpPort);
                final ServerConnector httpsConnector = getHttpsConnector(httpsConfig, httpsPort, keystorePath, keystorePassword, keyManagerPassword);

                setConnectors(httpConnector, httpsConnector);
                JettyLib.addHandler(this, new SecuredRedirectHandler());
            } else {
                final ServerConnector serverConnector = new ServerConnector(this);

                serverConnector.setPort(httpPort);

                addConnector(serverConnector);
            }
        }

        private void setConnectors(ServerConnector httpConnector, ServerConnector httpsConnector) {
            setConnectors(new Connector[]{httpConnector, httpsConnector});
        }

        private ServerConnector getHttpsConnector(HttpConfiguration httpsConfig, int httpsPort, String keystorePath, String keystorePassword, String keyManagerPassword) {
            final SslContextFactory sslContextFactory = new SslContextFactory();

            sslContextFactory.setKeyStorePath(keystorePath);
            sslContextFactory.setKeyStorePassword(keystorePassword);
            sslContextFactory.setKeyManagerPassword(keyManagerPassword);
            sslContextFactory.setTrustStorePath(keystorePath);
            sslContextFactory.setTrustStorePassword(keystorePassword);

            final ServerConnector httpsConnector = new ServerConnector(this,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfig));

            httpsConnector.setPort(httpsPort);

            return httpsConnector;
        }

        private ServerConnector getHttpConnector(HttpConfiguration httpConfig, int httpPort) {
            final ServerConnector httpConnector = new ServerConnector(this);

            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));
            httpConnector.setPort(httpPort);

            return httpConnector;
        }

        private HttpConfiguration getHttpsConfig(HttpConfiguration httpConfig) {
            final HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);

            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            return httpsConfig;
        }

        private HttpConfiguration getHttpConfig(int httpsPort) {
            final HttpConfiguration httpConfig = new HttpConfiguration();

            httpConfig.addCustomizer(new SecureRequestCustomizer());
            httpConfig.setSecureScheme(HttpScheme.HTTPS.asString());
            httpConfig.setSecurePort(httpsPort);

            return httpConfig;
        }

    }

}
