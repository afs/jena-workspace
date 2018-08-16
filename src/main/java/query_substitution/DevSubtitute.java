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

package query_substitution;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

// See also package: syntaxtransform
public class DevSubtitute {
    public static void main(String ... a) {
        String qs = StrUtils.strjoinNL
            (""
            ,"SELECT * { ?s ?p ?o}"
            );
        Query query = QueryFactory.create(qs);
        Node s = SSE.parseNode(":s");
        Map<Var, Node> map = new HashMap<>();
        map.put(Var.alloc("s"), s);
        Query q2 = QueryTransformOps.transform(query, map);
        System.out.println(q2);
        // Q:
        // What do do about "illegal" transformations?
        //    bound(?v)
        //    sparql xistsliterals where literal are not allowed.
        //    bnodes where bnodes are not allowed.
        //    BIND(... AS ?var); VALUES
        //    SELECT <value>
        //    BNode in BGPs (not vars!)
        // How to have mandatory transformations? Use of $var$
        // VALUES?
            
    }
}
