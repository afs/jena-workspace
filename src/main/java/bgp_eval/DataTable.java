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

package bgp_eval;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.sse.SSE;

/** N-way table (+ overflow?)
 */
public class DataTable {
    public static void main(String[] args) {
        BasicPattern bgp = SSE.parseBGP("(bgp (:s :p ?o) (:s :p ?v) (?o :q ?x))") ;
        // Replace (_ :p ?X) (?X :q _) with a table access.
        // Table is 3 columns
        
        // Cases:
        //  ?s :p1 ?v1 
        //     :p2 ?v2
        //     :p3 ?v3
        
        //  ?s :p1 ?x 
        //  ?x :p2 ?v2
        
    }
    
}
