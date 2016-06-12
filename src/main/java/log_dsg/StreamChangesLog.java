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

package log_dsg;

import static log_dsg.L.print ;
import static log_dsg.L.str ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;

public class StreamChangesLog implements StreamChanges {
    @Override
    public void start() {}
    @Override
    public void finish() {}
    
    @Override
    public void add(Node g, Node s, Node p, Node o) {
        print("%-3s  %s %s %s %s", "Add", strOr(g, "_"), str(s), str(p), str(o)) ;
    }
    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        print("%-3s  %s %s %s %s", "Del", strOr(g, "_"), str(s), str(p), str(o)) ;
    }
    
    public static String strOr(Node n, String alt) {
        if ( n == null )
            return alt ;
        else
            return str(n) ;
    }
    
    @Override
    public void addPrefix(Node graph, String prefix, String uriStr) {
        print("Add prefix  %s %s", prefix, uriStr) ;
    }
    
    @Override
    public void deletePrefix(Node graph, String prefix) {
        print("Del prefix  %s %s", prefix) ;
    }
    
    @Override
    public void setBase(String uriStr) {
        print("Set base %s", uriStr) ;
    }

    @Override
    public void txnBegin(ReadWrite mode) {
        print("Begin") ;
    }
    
    @Override
    public void txnPromote() {
        print("Promote") ;
    }
    
    @Override
    public void txnCommit() {
        print("Commit") ;
    }
    
    @Override
    public void txnAbort() {
        print("Abort") ;
    }
}
