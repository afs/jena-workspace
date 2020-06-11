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

package archive;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.TransformOrderByDistinctApplication;
import org.apache.jena.sparql.core.Var;

public class Jena1771_sort_spill {

    /**
     * False if ORDER BY has a variable not in the project
     * No sort-hidden variables.
     * @param cond
     * @param projectVars
     * @return
     */
    private static boolean isValidSortCondition(SortCondition cond, List<Var> projectVars) {
        if (cond.getExpression().isVariable()) {
            return projectVars.contains(cond.getExpression().asVar());
        } else {
            for (Var v : cond.getExpression().getVarsMentioned()) {
                if (!projectVars.contains(v))
                    return false;
            }
            return true;
        }
    }


    public static void main(String ...a) {
        /* The modifier order in algebra is:
         *
         * Limit/Offset
         *   Distinct/reduce
         *     project
         *       OrderBy
         *         Bindings
         *           having
         *             select expressions
         *               group
         */

        // Partial fix: rewrite to (order (distinct))
        //   What if !isValidSortCondition ??

        // If DISTINCT has a non-sort var AND sort has a non-distinct var, and spilling, then can be wrong.
        // Next:
        //   DISTINCT code. Add sort.

        // Interaction with distinct=>reduced optimization (which happens when...)
        //  DISTINCT is only sort vars.  NB This is not perfect "adjacent".

        String qs = StrUtils.strjoinNL
            ("PREFIX  :     <http://example/>"
            ,"SELECT DISTINCT *"
            ,"{ ?x  :p  ?v }"
            ,"ORDER BY ASC(?v)"
            //,"LIMIT   5"
            );

        TransformOrderByDistinctApplication transform;

        // With projection: OK
        /*
         *
            SELECT DISTINCT  ?x ?v
            WHERE
              { ?x  :p  ?v }
            ORDER BY ASC(?v)

            (distinct
              (project (?x ?v)
                (order ((asc ?v))
                  (bgp (triple ?x <http://example/p> ?v)))))

            (order ((asc ?v))
              (distinct
                (project (?x ?v)
                  (bgp (triple ?x <http://example/p> ?v)))))

        */
        // Without projection: BAD
        /*
            SELECT DISTINCT  *
            WHERE
              { ?x  :p  ?v }
            ORDER BY ASC(?v)

            (distinct
              (order ((asc ?v))
                (bgp (triple ?x <http://example/p> ?v))))

            (distinct
              (order ((asc ?v))
                (bgp (triple ?x <http://example/p> ?v))))
         */


        ARQ.getContext().set(ARQ.optTopNSorting, false);
        ARQ.getContext().set(ARQ.spillToDiskThreshold, 2);

        Query query = QueryFactory.create(qs);
        System.out.println(query);
        Op op = Algebra.compile(query);
        Op op1 = Algebra.optimize(op);
        System.out.println(op);
        System.out.println(op1);
        Model graph = RDFDataMgr.loadModel("sort-distinct-data.ttl");
        QueryExecution qExec = QueryExecutionFactory.create(query, graph);
        //QueryExecUtils.executeQuery(qExec);

        List<QuerySolution> x = ResultSetFormatter.toList(qExec.execSelect());
        List<Integer> z = x.stream().map(qsoln->qsoln.getLiteral("v").getInt()).collect(Collectors.toList());
        System.out.println(z);
        System.out.flush();
        for ( int i = 0 ; i < z.size()-1; i++ ) {
            int v1 = z.get(i);
            int v2 = z.get(i+1);
            if ( v2 < v1 )
                System.out.printf("%d  %d\n", v1, v2);
        }




        System.out.println("DONE");
    }
}
