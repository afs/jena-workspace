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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

/** Wrapper for {@code HttpRequest.Builder} */
public class HttpRequestBuilderWrapper implements HttpRequest.Builder {
    private Builder other;
    protected Builder get() { return other; }
    protected HttpRequestBuilderWrapper(HttpRequest.Builder other) {
        this.other = other;
    }

    @Override
    public Builder uri(URI uri) {
        get().uri(uri);
        return this;
    }
    @Override
    public Builder expectContinue(boolean enable) {
        get().expectContinue(enable);
        return this;
    }
    @Override
    public Builder version(Version version) {
        get().version(version);
        return this;
    }
    @Override
    public Builder header(String name, String value) {
        get().header(name, value);
        return this;
    }
    @Override
    public Builder headers(String...headers) {
        get().headers(headers);
        return this;
    }
    @Override
    public Builder timeout(Duration duration) {
        get().timeout(duration);
        return this;
    }
    @Override
    public Builder setHeader(String name, String value) {
        get().setHeader(name, value);
        return this;
    }
    @Override
    public Builder GET() {
        get().GET();
        return this;
    }
    @Override
    public Builder POST(BodyPublisher bodyPublisher) {
        get().POST(bodyPublisher);
        return this;
    }
    @Override
    public Builder PUT(BodyPublisher bodyPublisher) {
        get().PUT(bodyPublisher);
        return this;
    }
    @Override
    public Builder DELETE() {
        get().DELETE();
        return this;
    }
    @Override
    public Builder method(String method, BodyPublisher bodyPublisher) {
        get().method(method, bodyPublisher);
        return this;
    }
    @Override
    public HttpRequest build() {
        return get().build();
    }
    @Override
    public Builder copy() {
        get().copy();
        return this;
    }
}