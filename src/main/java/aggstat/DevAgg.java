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
import org.apache.jena.sparql.util.QueryExecUtils ;


public class DevAgg {
        // SQL names.
//        STDEV  (N-1)
//        STDEVP
//        VAR
//        VARP
        // DISTINCT not DISTINCT

    public static void main(String[] args) throws java.io.IOException {

        AggStat_Setup.setup() ;

        DatasetGraph ds = SSE.parseDatasetGraph("(dataset (graph (:x :p1 1) (:x :p2 2) (:x :p3 3) ))") ;

        System.out.println(ds);

        String NL = "\n" ;
        String PRE = StrUtils.strjoinNL
            ("PREFIX math: <http://www.w3.org/2005/xpath-functions/math#>",
                "PREFIX afn:     <http://jena.apache.org/ARQ/function#>") ;

        String qs1 = PRE+"SELECT (count(?x) AS ?N) (sum(?x) AS ?SX) ( afn:sqrt(sum(?x*?x)-?SX*?SX/?N) AS ?SD) {?s ?p ?x}" ;

        //String qs2 = "SELECT ( (sum(?x*?x)-sum(?x)*sum(?x)/count(?x)) AS ?SD) {?s ?p ?x}" ;

        // https://en.wikipedia.org/wiki/Standard_deviation#Basic_examples
            
//            double x = 14-(6*6/3) ;
//            double x2 = x/3 ;
//            System.out.println(x2) ;
            
        String qs3 = PRE+"SELECT ( afn:sqrt(SUM(?x*?x)-SUM(?x)*AVG(?x)) AS ?stddev) {?s ?p ?x}" ;
        String qs = StrUtils.strjoinNL
            (PRE
             , "SELECT "
//             , "(sum(?x) AS ?SUM)"
//             , "(sum(?x*?x) AS ?SUM2)"
//             , "(count(?x) AS ?N)"
             ,"(<urn:arq:stddev>(?x) AS ?SD)"
             ,"(<urn:arq:var>(?x) AS ?VAR)"
             ,"( ( ( sum(?x*?x)-sum(?x)*sum(?x)/count(?x) ) / (count(?x)-1) ) AS ?calcVAR) "
             ,"( afn:sqrt(?calcVAR) AS ?calcSTDEV)"
             ,"(<urn:arq:stddevp>(?x) AS ?SDP)"
             ,"(<urn:arq:varp>(?x) AS ?VARP) "
             ,"( ( ( sum(?x*?x)-sum(?x)*sum(?x)/count(?x) ) / count(?x) ) AS ?calcVAR_Pop) "
             ,"( afn:sqrt(?calcVAR_Pop) AS ?calcSTDEV_Pop)"
             ," {?s ?p ?x}") ;

        qs = qs.replace("urn:arq:", AggStat_Setup.BASE) ;
            
        System.out.println(qs) ;
            
//      exec(qs1, ds) ;
//      exec(qs3, ds) ;
        exec(qs, ds) ;
    }

    private static void exec(String qs, DatasetGraph ds) {
        Query query = QueryFactory.create(qs) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(ds)) ) {
            QueryExecUtils.executeQuery(qExec); 
        }
    }
}
