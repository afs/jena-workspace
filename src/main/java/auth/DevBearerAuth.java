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

import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;

public class DevBearerAuth {
    /*
     * [x] Documentation
     * [ ] Does not record the 401 because that is by the servlet filter.

     * maven: io.jsonwebtoken / jjwt-root / 0.11.1
     */
    /*
     * <dependency>
     *   <groupId>io.jsonwebtoken</groupId>
     *   <artifactId>jjwt</artifactId>
     *   <version>0.9.1</version>
     * </dependency>
     */
    /*
    Nice:
        fast determination of tokens.
          RiotChars
          boolean[256] indexed by char
          example: jdk.internal.net.http.common.utils.tchar
     */

    static {
        // JenaSystem.DEBUG_INIT = true;
        JenaSystem.init();
        //LogCtl.setLog4j2();
        FusekiLogging.setLogging();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...args) {
        // [x] Base64 encoding - no padding.
        // [ ] Filter, if no user, pass through untouched.

        // [ ] Check : registration.
    }
}
