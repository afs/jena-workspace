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
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.eclipse.jetty.util.ssl.X509;

public class ReadAuth {

//    private static final String DIR = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-main/testing/Access/";
//    private static final String KEYSTORE = DIR+"certs/mykey.jks";
//    private static final String KEYSTOREPASSWORD = "cert-pw";

  private static final String DIR = "/home/afs/ASF/jena-workspace/Fuseki-https/";
  private static final String KEYSTORE = DIR+"keystore.jks";
  private static final String KEYSTOREPASSWORD = "password";

    public static void main(String[] args) throws Exception {
        try {
            readKeystore();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }


    }

    private static void readKeystore() throws Exception {
        InputStream in = new FileInputStream(KEYSTORE);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(in, KEYSTOREPASSWORD.toCharArray());

        if ( ! keyStore.aliases().hasMoreElements() ) {
            System.err.println("No aliases");
            return;
        }

        Enumeration<String> e = keyStore.aliases();

        while(e.hasMoreElements()) {
            String alias = e.nextElement();
            System.out.println("Alias: "+alias);
            Certificate cert = keyStore.getCertificate(alias);
            System.out.println("  Type = "+cert.getType());
            X509Certificate x509cert = (X509Certificate)cert;
            System.out.println("  "+x509cert.getSubjectAlternativeNames());

            // Jetty code
            X509 x509 = new X509("", x509cert);

            System.out.println("  "+x509.getHosts());
            System.out.println("  "+x509);

        }


//        CertificateFactory fac = CertificateFactory.getInstance("X509");
//        FileInputStream is = new FileInputStream(
//        X509Certificate cert = (X509Certificate) fac.generateCertificate(is);
//        System.out.println("From: " + cert.getNotBefore());
//        System.out.println("Until: " + cert.getNotAfter());

    }


}
