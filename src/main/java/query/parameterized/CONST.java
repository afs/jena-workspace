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

package query.parameterized;

import org.apache.jena.atlas.lib.StrUtils ;

public class CONST {

    static String x0 = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT ?Z ?x { ?Z :p ?x . FILTER ( bound(?x) ) }"
    ) ;
    static String x0a = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT * { ?Z :p ?x . FILTER ( bound(?x) ) }"
    ) ;
    static String x1 = StrUtils.strjoinNL
    ( CONST.PRE,  "# Comment ?x"
      , "SELECT (str(?x) as ?xs) { ?s :p ?x . OPTIONAL { ?x :r '?x' }  FILTER ( ?x > ?y ) }"
    );
    static String x2 = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT ?x { ?s :p ?x . FILTER NOT EXISTS { ?x :r ?x }} GROUP BY ?x ORDER BY ?x"
    );
    static String x3 = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT (123 as ?x) { }"
    );
    static String x4 = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT * { :s :p :o\\?x}"
    );
    // ==> Double SELECT (:X AS ?x)
    static String x5 = StrUtils.strjoinNL
        ( CONST.PRE
        , "SELECT ?x { {SELECT ?x { ?s ?p ?x } } ?s ?p ?o }"
        );
    static String x6 = StrUtils.strjoinNL
    ( CONST.PRE
    , "DESCRIBE ?x {}"
    );
    static String x7 = StrUtils.strjoinNL
    ( CONST.PRE
    , "SELECT * { VALUES ?x { 123 } ?s ?p ?o }"
    );
    static String x8 = "SELECT ?x { BIND(1 AS ?x) }" ;
    static String x99 = CONST.PRE+"\n"+"ASK { FILTER (?x = <http://example/X>) }";
    static String PRE = "PREFIX : <http://example/> " ;

}
