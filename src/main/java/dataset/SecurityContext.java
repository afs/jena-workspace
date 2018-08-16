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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

/** A {@link SecurityContext} is the things actor (user, role) is allowed to do. 
 * Currently version: the set of graphs, by graph name, they can access.
 */ 
public class SecurityContext {

    private Collection<Node> graphNames = ConcurrentHashMap.newKeySet();
    
    public SecurityContext(String...graphNames) {
        this(NodeUtils.convertToNodes(graphNames));
    }

    public SecurityContext(Node...graphNames) {
        this(Arrays.asList(graphNames));
    }

    public SecurityContext(Collection<Node> graphNames) {
        this.graphNames.addAll(graphNames);
    }
    
    public void filterTDB(DatasetGraph dsg, QueryExecution qExec) {
        if ( TDBFactory.isTDB1(dsg) ) {
            SecurityFilterTDB1 sf1 = FiltersTDB1.securityFilter(dsg, graphNames);
            sf1.apply(qExec.getContext());
            return;
        }
        if ( DatabaseMgr.isTDB2(dsg) ) {
            SecurityFilterTDB2 sf2 = FiltersTDB2.securityFilter(dsg, graphNames);
            sf2.apply(qExec.getContext());
            return;
        }            
            
        throw new IllegalArgumentException("Not a TDB1 or TDB2 database: "+dsg.getClass().getSimpleName());
    }

    public SecurityFilterTDB2 filterTDB2(DatasetGraph dsg) {
        SecurityFilterTDB2 f = FiltersTDB2.securityFilter(dsg, graphNames);
        //if ( f == null ) {}
        return f; 
    }
    
    public SecurityFilterTDB1 filterTDB1(DatasetGraph dsg) {
        SecurityFilterTDB1 f = FiltersTDB1.securityFilter(dsg, graphNames);
        //if ( f == null ) {}
        return f; 
    }
}
