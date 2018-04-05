/*
 * Licensed to the A
pache Software Foundation (ASF) under one
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

package httpquery;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.sparql.engine.http.HttpQuery;

public class HttpQueryBuilder {
 // Specific handling of:
    // * Headers
    // * Query string params.
    // * Accept:
    // Builder?
    
    RequestBuilder requestBuilder ;
    
    public HttpQueryBuilder(String uri) {
        requestBuilder = RequestBuilder.get(uri);
    }
    
    public void addHeader(String field, String value) {
        requestBuilder.addHeader(field, value);
    }
    
    public void addParam(String name, String value) {
        requestBuilder.addParameter(name, value);
    }
    
    public HttpQuery build() {
        HttpQuery httpQuery = new HttpQuery((String)null);
        httpQuery.setAccept(null);
        httpQuery.setAllowCompression(false);
        httpQuery.addParam(null, null);
        httpQuery.setClient(null);
        httpQuery.setForcePOST();
        httpQuery.setContext(null);
        httpQuery.setConnectTimeout(0);
        httpQuery.setReadTimeout(0);
        return httpQuery;
    }
    
    public static void main(String ... a) throws URISyntaxException {
        String target = "http://example/query?x=y" ;
        URIBuilder uriBuilder = new URIBuilder(target);
        List<NameValuePair> x = uriBuilder.getQueryParams();
        System.out.println(x);
        HttpUriRequest r =
            RequestBuilder
                .get(target)
                .addParameter("foo",  "bar")
                .build();
        String qs = r.getURI().getQuery();
        System.out.println(qs);
        System.out.println(r);
           
    }
}
