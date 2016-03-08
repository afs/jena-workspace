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

package aggstat;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestAggStat {
    static DatasetGraph ds = SSE.parseDatasetGraph("(dataset (graph (:x :p1 1) (:x :p2 2) (:x :p3 3) ))") ;
    
    @BeforeClass public static void setupClass() { 
        AggStat_Setup.setup();
    }
    
    @Test public void agg_stat_stdev_01() {
        test("( <urn:arq:stdev>(?x) AS ?X )",  1.0e0) ; 
    }

    private void test(String qsAgg, double expected) {
        qsAgg = qsAgg.replace("urn:arq:", AggStat_Setup.BASE) ;
        String NL = "\n" ;
        String PRE = StrUtils.strjoinNL
            ("PREFIX math: <http://www.w3.org/2005/xpath-functions/math#>",
             "PREFIX afn:     <http://jena.apache.org/ARQ/function#>") ;
        String qs = PRE+NL+"SELECT "+qsAgg+NL+" {?s ?p ?x}" ;
        Query query = QueryFactory.create(qs) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            double result = qExec.execSelect().next().getLiteral("X").getDouble() ;
            Assert.assertEquals(expected, result, 0.00001);
        }
    }
}
