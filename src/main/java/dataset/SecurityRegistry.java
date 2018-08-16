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

package dataset;

import org.apache.jena.atlas.lib.Registry;

/**
 * A {@link SecurityRegistry} is mapping from a string (typically a user name or role
 * name) to a {@link SecurityContext}, where the {@link SecurityContext}
 * is the access control operations for the user/role.
 */ 
public class SecurityRegistry extends Registry<String, SecurityContext>{
    
    // Singletons eventually get use into trouble! (Multiple server in one JVM)
    // but they are better than statics.
    private static SecurityRegistry singleton = new SecurityRegistry();

    public static SecurityRegistry get() {
        return singleton;
    }
    
//    // Key (e.g. user, role) to  
//    private Map<String, SecurityContext> securityContexts = new ConcurrentHashMap<>();
    
    public SecurityRegistry() {}
}
