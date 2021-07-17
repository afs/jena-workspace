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

package dev;

import java.net.http.HttpClient;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;

public class DevFuseki {

    public static void main(String...a) {
//        FusekiMainCmd.main("--port=", "--https=certs/https-details", "--mem", "/ds");
//        System.exit(0);

        mainHTTPS();
        System.exit(0);
        Graph graph = Factory.createDefaultGraph();
        ExtendedIterator<Triple> x1 = graph.find(null,null,null);
        ExtendedIterator<Triple> x2 = WrappedIterator.create(x1.toList().iterator());
    }

    public static void mainGeneral() {
        try {
            FusekiLogging.setLogging();
            FusekiServer server = FusekiServer.create()
                                              // .jettyServerConfig("jetty.xml")
                                              .add("/ds", DatasetGraphFactory.createTxnMem())
                                              .port(3333)
                                              .build();
            server.start();
            String URL = server.datasetURL("/ds");
            try (RDFConnection conn = RDFConnectionFactory.connect(URL)) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK=" + b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }

    public static void mainHTTPS() {
        try {
            FusekiLogging.setLogging();

            FusekiServer server = FusekiServer
                    .create()
                    .add("/ds", DatasetGraphFactory.createTxnMem())
                    .port(3030)
                    // .https(3043, "certs/mykey.jks","cert-pw")
                    .https(3043, "certs/https-details").build();
            server.start();

            String URL = server.datasetURL("/ds");
//            org.apache.http.client.HttpClient httpClient = (server.getHttpsPort() > 0) ? JettyLib.trustLocalhostUnsigned().build() : null;
//             ExFuseki_Https_3_Auth

//          java.security.cert.X509Certificate x1;
//          javax.security.cert.X509Certificate x2; - deprecated
          // Create a trust manager that does not validate certificate chains
          TrustManager[] trustAllCerts = new TrustManager[]{
              new X509TrustManager() {
                  @Override public X509Certificate[] getAcceptedIssuers() {
                      return null;
                  }
                  @Override public void checkClientTrusted(X509Certificate[] certs, String authType) {
                  }
                  @Override  public void checkServerTrusted(X509Certificate[] certs, String authType) {
                  }
              }
          };

          // Install the all-trusting trust manager
          SSLContext sslContext;
          try {
              sslContext = SSLContext.getInstance("SSL");
              sslContext.init(null, trustAllCerts, new SecureRandom());
          } catch (Exception e) {
              throw new RuntimeException(e);
          }

          var sslParameters = new SSLParameters();
//          // This should prevent host validation - need the system property as well.
          sslParameters.setEndpointIdentificationAlgorithm(null);

          // Before HttpRequest and HttpClient!
          Properties props = System.getProperties();
          props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

          HttpClient hc = HttpClient.newBuilder()
              .sslContext(sslContext).sslParameters(sslParameters)
              //.version(Version.HTTP_2)
              //.followRedirects(Redirect.NORMAL)
              .build();

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(10 * 1000))
                    .sslContext(sslContext) // SSL context 'sc' initialised as earlier
                    //.sslParameters(parameters) // ssl parameters if overriden
                    .build();

            try (RDFConnection conn = RDFConnectionRemote
                    .newBuilder()
                    .httpClient(hc)
                    .destination(URL)
                    .build();) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK=" + b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }

    public static void mainSecure() {
        try {
            // System.setProperty("fuseki.loglogging", "true");
            FusekiLogging.setLogging();

            UserStore userStore = JettyLib.makeUserStore("u", "p");
            SecurityHandler sh = JettyLib.makeSecurityHandler("TripleStore", userStore, AuthScheme.BASIC);

            FusekiServer server = FusekiServer.create().port(3330).add("/ds", DatasetGraphFactory.createTxnMem())
                                              // .verbose(true)
                                              .serverAuthPolicy(Auth.ANY_USER).securityHandler(sh).build();
            server.start();

            String URL = "http://localhost:3330/ds";
            try (RDFConnection conn = RDFConnectionFactory.connectPW(URL, "u", "p")) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK=" + b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.exit(0);
        }

    }

    private static void runFusekiFull() {
        String BASE = "/home/afs/tmp";
        // String BASE = "/home/afs/Desktop/JENA-1302";

        String fusekiHome = "/home/afs/Jena/jena-fuseki2/jena-fuseki-core";
        String fusekiBase = BASE + "/run";

        System.setProperty("FUSEKI_HOME", fusekiHome);
        System.setProperty("FUSEKI_BASE", fusekiBase);

        String runArea = Paths.get(fusekiBase).toAbsolutePath().toString();
        FileOps.ensureDir(runArea);
        FileOps.clearAll(runArea);
        FusekiCmd.main(
        // "-v"
        // ,"--conf=/home/afs/tmp/config-tdb2-model.ttl"
        // "--conf="+BASE+"/config.ttl"
        // "--conf=/home/afs/tmp/conf.ttl"
        // , "--mem", "/ds"
        // "--update", "--file=/home/afs/tmp/D.ttl", "/ds"
        // "--update", "--file=/home/afs/tmp/D.trig", "/ds"
        // "--mem", "/ds"
        // "--memtdb", "--set=tdb:unionDefaultGraph=true", "/ds"
        // --loc=/home/afs/tmp/DB", "/ds"

        );
    }
}
