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

import java.io.InputStream ;
import java.util.concurrent.atomic.AtomicInteger ;

import log_dsg.StreamChangesReader ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.riot.web.HttpOp ;

public class LibPatchFetcher {
    static private AtomicInteger epoch = new AtomicInteger(0) ;
    
    public static StreamChangesReader fetch1(String url, int idx) {
        String s = url+"?id="+idx ;
        try {
            InputStream in = HttpOp.execHttpGet(s) ;
            if ( in == null )
                return null ;
            return new StreamChangesReader(in) ;
        } catch (HttpException ex) {
            System.err.println("HTTP Exception: "+ex.getMessage()) ;
            return null ;
        }
    }
    
}
