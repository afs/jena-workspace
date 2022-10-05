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

package assembler2;

import org.apache.jena.assembler.JA;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

public class ConstructorLib {

    /** Look for and merge in context declarations.
     * e.g.
     * <pre>
     * root ... ;
     *   ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "10000" ] ;
     *   ...
     * </pre>
     * Short name forms of context parameters can be used.
     * Setting as string "undef" will remove the context setting.
     */
    public static void mergeContext(Graph graph, Node r, Context context) {
        String qs = "PREFIX ja: <"+JA.getURI()+">\nSELECT * { ?x ja:context [ ja:cxtName ?name ; ja:cxtValue ?value ] }" ;
        PrefixMap pmap = Prefixes.adapt(graph.getPrefixMapping());
        try ( QueryExec qExec = QueryExec.graph(graph)
                .query(qs)
                .substitution("x", r)
                .build() ) {
            RowSet rs = qExec.select();
            rs.forEachRemaining(row->{
                String name = Nodes.nodeAsString(row, "name", pmap);
                String value = Nodes.nodeAsString(row, "value", pmap);
//            name = MappingRegistry.mapPrefixName(name) ;
//            Symbol symbol = Symbol.create(name) ;
//            if ( "undef".equalsIgnoreCase(value) )
//                context.remove(symbol) ;
//            else
//                context.set(symbol, value) ;
//            }
            });
        }
    }

}
