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

package log_dsg.platform;

import java.io.* ;
import java.nio.charset.StandardCharsets ;

import log_dsg.StreamChangesWriter ;
import org.apache.http.Header ;
import org.apache.http.HttpEntity ;
import org.apache.http.client.ClientProtocolException ;
import org.apache.http.client.methods.CloseableHttpResponse ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.entity.ByteArrayEntity ;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.http.impl.client.HttpClients ;

public class LibPatchSender {
    
    public static StreamChangesCollect create1(String url) {
        // TODO Need to make streaming.
        StreamChangesCollect scc = new StreamChangesCollect(url) ;
        return scc ;
    }

    
    
    CloseableHttpClient httpClient = HttpClients.createDefault();
    //httpClient.execute(httpPost) ;

    public static class StreamChangesCollect extends StreamChangesWriter {
        private final CloseableHttpClient httpClient = HttpClients.createDefault();
        private final ByteArrayOutputStream bytes  ;
        private final String url ;

        public StreamChangesCollect(String url) {
            this(url, new ByteArrayOutputStream(100*1024)) ;
        }
        
        private StreamChangesCollect(String url, ByteArrayOutputStream out) {
            super(out) ;
            this.url = url ;
            this.bytes = out ;
        }
        
        @Override
        public void start() { 
            
        }

        
        byte[] collected() { 
            return bytes.toByteArray() ;
        }
        
        public void send() {
            HttpPost postRequest = new HttpPost(url) ;
            byte[] bytes = collected() ;
            String s = new String(bytes, StandardCharsets.UTF_8) ;
            System.out.println(s) ;
            postRequest.setEntity(new ByteArrayEntity(bytes)) ;
            
            try(CloseableHttpResponse r = httpClient.execute(postRequest)) {
                System.out.println("SC="+r.getStatusLine().getStatusCode()) ;
            }
            catch (IOException e) { e.printStackTrace(); }
            this.bytes.reset(); 
        }
    }
    
}
