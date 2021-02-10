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

package rdf_star.v2;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;

public class DevRxFind {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // (find) is PG
    // PG <<:s :p ?o>> :q 123 is
    //  ?X :q 123 . FILTER(

//    afn:subject(?t)     Return the subject of the triple term
//    afn:predicate(?t)   Return the predicate (property) of the triple term
//    afn:object(?t)  Return the object of the triple term
//    afn:triple(?s, ?p, ?o)  Create a triple term from s/p/o
//    afn:isTriple(?t)


    public static void main(String...a) {
        String PREFIXES = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,"PREFIX afn:     <http://jena.apache.org/ARQ/function#>"
            );
        // FIND - PG
        String queryStr1 = StrUtils.strjoinNL
            (PREFIXES
            ,"SELECT * { FIND(<<:s :p ?o>> AS ?X) }"
            );
        // Scanning - SA
        String queryStr2 = StrUtils.strjoinNL
            (PREFIXES
            ,"SELECT * {"
            ,"   ?X :q 456"
            ,"   FILTER( :s = afn:subject(?X) && :p = afn:predicate(?X) )"
            ,"   BIND( afn:object(?X) AS ?o )"
            ,"   ?s ?p ?o"
            ,"}"
            );

        Op op = SSE.parseOp("(find ?X (:s :p ?o))");
        Graph graph = SSE.parseGraph(
            "(graph (:s :p :ox) (:s1 :p :o) (<<:s :p 123>> :q 456) )"
            );
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        QueryExecUtils.execute(op, dsg);

        String[] x = { queryStr1, queryStr2 };
        for ( String qs : x ) {
            Query query = QueryFactory.create(qs);
            System.out.println(query);
            QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(dsg));
            QueryExecUtils.executeQuery(qExec);
        }

    }
}

