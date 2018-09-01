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

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class JettyHttps {
    
    public static void main(String ...a) throws Exception {
        // Server - two connectors on http://8080 and https://8443
        Server jettyServer = server();
        SecuredRedirectHandler srh = new SecuredRedirectHandler();
        ServletContextHandler handler = buildContextHandler();

        // Bounce to https as first choice.
        JettyLib.addHandler(jettyServer, srh);
        JettyLib.addHandler(jettyServer, handler);

        jettyServer.start();
        jettyServer.join();
        Lib.sleep(50000);
        System.exit(0);
    }
    
    private static ServletContextHandler buildContextHandler() {
        //ServletContextHandler handler = buildServletContext("/");
        ServletContextHandler contextHandler = new ServletContextHandler();
        DefaultServlet staticServlet = new DefaultServlet();
        ServletHolder staticContent = new ServletHolder(staticServlet);
        staticContent.setInitParameter("resourceBase", ".");
        contextHandler.addServlet(staticContent, "/");
        return contextHandler;
    }

    //  curl -v -k https://localhost:8443/
    // https://medium.com/vividcode/enable-https-support-with-self-signed-certificate-for-embedded-jetty-9-d3a86f83e9d9
    // keytool -keystore mykey.jks -alias mykey -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -genkey -validity 3650
    
    private static Server server() {
        Server server = new Server();
        ServerConnector plainConnector = httpConnector(server);
        ServerConnector httpsConnector = httpsConnector(server);
        
        server.addConnector(plainConnector);
        server.addConnector(httpsConnector);
        
        return server;
        
    }
    
    private static ServerConnector httpConnector(Server server) {
        HttpConfiguration http_config = new HttpConfiguration();
//      http_config.setOutputBufferSize(32768);
//      http_config.setRequestHeaderSize(8192);
//      http_config.setResponseHeaderSize(8192);
//      http_config.setSendServerVersion(true);
        http_config.setSecureScheme(HttpScheme.HTTPS.asString());
        http_config.setSecurePort(8443);
        ServerConnector plainConnector = new ServerConnector(server, new HttpConnectionFactory(http_config));
        plainConnector.setPort(8080);
        return plainConnector;
    }
    
    private static ServerConnector httpsConnector(Server server) {
        String KEYSTORE = "certs/mykey.jks";

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme(HttpScheme.HTTPS.asString());
        http_config.setSecurePort(8443);
        
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(KEYSTORE);
        sslContextFactory.setKeyStorePassword("cert-pw");
        
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
        return sslConnector;
    }

    // -----------------------------------------------------

    // https://medium.com/vividcode/enable-https-support-with-self-signed-certificate-for-embedded-jetty-9-d3a86f83e9d9
    private static void httpsConnectorEx1(Server server) {
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(8443);
        final ServerConnector http = new ServerConnector(server,
            new HttpConnectionFactory(httpConfiguration));
        http.setPort(8080);
        server.addConnector(http);
    }

    private static void httpsConnectorEx2(Server server) {
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme(HttpScheme.HTTPS.asString());
        httpConfiguration.setSecurePort(8443);

        final SslContextFactory sslContextFactory = new SslContextFactory("keystore");
        sslContextFactory.setKeyStorePassword("PASSWORD");
        final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        final ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(8443);
        server.addConnector(httpsConnector);
    }
    
    // -----------------------------------------------------
    
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
        String KEYSTORE = "certs/mykey.jks";
        
        Server server = new Server();
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme(HttpScheme.HTTPS.asString());
        http_config.setSecurePort(8443);
        
        String jetty_home = "JETTY_HOME";
        
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(KEYSTORE);
//        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
//        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setKeyStorePassword("cert-pw");
        sslContextFactory.setKeyManagerPassword("cert-pw");

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
