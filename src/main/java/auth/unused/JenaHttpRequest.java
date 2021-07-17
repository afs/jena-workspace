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

package auth.unused;

import java.net.http.HttpRequest;

import org.apache.jena.http.auth.AuthLib;

class JenaHttpRequest extends HttpRequestBuilderWrapper {
        private final String serviceEndpoint;
        private HttpRequest request = null;

        public JenaHttpRequest(String serviceEndpoint) {
            this(serviceEndpoint, HttpRequest.newBuilder());
        }

        public JenaHttpRequest(String serviceEndpoint, HttpRequest.Builder builder) {
            super(builder);
            this.serviceEndpoint = serviceEndpoint;
            applyAuth(serviceEndpoint);
        }


        public String getEndpoint() { return serviceEndpoint; }

        // Overkill.
//        @Override
//        protected HttpRequest.Builder get() {
//            request = null;
//            return super.get();
//        }
//
//        @Override
//        public HttpRequest build() {
//            if ( request == null )
//                request = super.build();
//            return request;
//        }

        private void applyAuth(String serviceEndpoint) {
            AuthLib.addAuth(get(), serviceEndpoint);
        }

//        public HttpRequest.Builder auth(String endpoint) {
//            // XXX
//            return null;
//        }

        // applyAuth
    }