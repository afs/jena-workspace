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
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.JenaSystem;

public class DevBGP {

    public static void main(String[] args) {
        // Add connectivity
        JenaSystem.init();
        
        ReorderTransformation xform = new ReorderFixed2();
        BasicPattern bgp = SSE.parseBGP("(bgp (:s :p ?o) (:s :p ?v) (?o :q ?x))") ;
        
        BasicPattern bgp2 = xform.reorder(bgp);
        System.out.println(bgp2);
    }

}