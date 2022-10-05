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

package tools;

public class CountExperimentResults {}
// 2018-06-11 with TDB graph size fix.

//    ** Database: TDB1
//
//    Graph.size()
//    [0] Count = 5,000,599 in 0.250s
//    [1] Count = 5,000,599 in 0.206s
//    [2] Count = 5,000,599 in 0.204s
//    [3] Count = 5,000,599 in 0.198s
//    [4] Count = 5,000,599 in 0.227s
//      Average 0.217s
//
//    Find count
//    [0] Count = 5,000,599 in 11.428s
//    [1] Count = 5,000,599 in 11.923s
//    [2] Count = 5,000,599 in 11.446s
//    [3] Count = 5,000,599 in 12.119s
//    [4] Count = 5,000,599 in 11.986s
//      Average 11.780s
//
//    SPARQL count(*)
//      SELECT (count(*) AS ?c) { ?s ?p ?o }
//    [0] Count = 5,000,599 in 1.717s
//    [1] Count = 5,000,599 in 1.295s
//    [2] Count = 5,000,599 in 1.356s
//    [3] Count = 5,000,599 in 1.302s
//    [4] Count = 5,000,599 in 1.242s
//      Average 1.382s
//
//    SPARQL */materialize
//      SELECT * { ?s ?p ?o }
//    [0] Count = 5,000,599 in 18.456s
//    [1] Count = 5,000,599 in 18.819s
//    [2] Count = 5,000,599 in 18.507s
//    [3] Count = 5,000,599 in 18.972s
//    [4] Count = 5,000,599 in 18.255s
//      Average 18.601s
//
//    SPARQL count(var)
//      SELECT (count(?s) AS ?c) (count(?p) AS ?c2) (count(?o) AS ?c3)  { ?s ?p ?o }
//    [0] Count = 5,000,599 in 13.884s
//    [1] Count = 5,000,599 in 14.206s
//    [2] Count = 5,000,599 in 14.091s
//    [3] Count = 5,000,599 in 14.259s
//    [4] Count = 5,000,599 in 14.136s
//      Average 14.115s
//
//    SPARQL count(materialize)
//      SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s = ?s && ?p = ?p && ?o = ?o) ) }
//    [0] Count = 5,000,599 in 18.095s
//    [1] Count = 5,000,599 in 18.037s
//    [2] Count = 5,000,599 in 17.898s
//    [3] Count = 5,000,599 in 17.602s
//    [4] Count = 5,000,599 in 17.126s
//      Average 17.751s
//
//    Tuple count (tdb1)
//    [0] Count = 5,000,599 in 0.155s
//    [1] Count = 5,000,599 in 0.159s
//    [2] Count = 5,000,599 in 0.156s
//    [3] Count = 5,000,599 in 0.156s
//    [4] Count = 5,000,599 in 0.237s
//      Average 0.172s
//
//    ** Database: TDB2
//
//    Graph.size()
//    [0] Count = 5,000,599 in 0.230s
//    [1] Count = 5,000,599 in 0.208s
//    [2] Count = 5,000,599 in 0.203s
//    [3] Count = 5,000,599 in 0.203s
//    [4] Count = 5,000,599 in 0.193s
//      Average 0.207s
//
//    Find count
//    [0] Count = 5,000,599 in 13.066s
//    [1] Count = 5,000,599 in 13.993s
//    [2] Count = 5,000,599 in 13.805s
//    [3] Count = 5,000,599 in 13.155s
//    [4] Count = 5,000,599 in 13.517s
//      Average 13.507s
//
//    SPARQL count(*)
//      SELECT (count(*) AS ?c) { ?s ?p ?o }
//    [0] Count = 5,000,599 in 2.076s
//    [1] Count = 5,000,599 in 1.421s
//    [2] Count = 5,000,599 in 1.420s
//    [3] Count = 5,000,599 in 1.438s
//    [4] Count = 5,000,599 in 1.343s
//      Average 1.539s
//
//    SPARQL */materialize
//      SELECT * { ?s ?p ?o }
//    [0] Count = 5,000,599 in 20.631s
//    [1] Count = 5,000,599 in 21.598s
//    [2] Count = 5,000,599 in 21.237s
//    [3] Count = 5,000,599 in 21.222s
//    [4] Count = 5,000,599 in 20.853s
//      Average 21.108s
//
//    SPARQL count(var)
//      SELECT (count(?s) AS ?c) (count(?p) AS ?c2) (count(?o) AS ?c3)  { ?s ?p ?o }
//    [0] Count = 5,000,599 in 16.642s
//    [1] Count = 5,000,599 in 16.095s
//    [2] Count = 5,000,599 in 16.762s
//    [3] Count = 5,000,599 in 16.365s
//    [4] Count = 5,000,599 in 16.856s
//      Average 16.544s
//
//    SPARQL count(materialize)
//      SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s = ?s && ?p = ?p && ?o = ?o) ) }
//    [0] Count = 5,000,599 in 20.633s
//    [1] Count = 5,000,599 in 20.967s
//    [2] Count = 5,000,599 in 20.514s
//    [3] Count = 5,000,599 in 21.371s
//    [4] Count = 5,000,599 in 20.673s
//      Average 20.831s
//
//    Tuple count (tdb2)
//    [0] Count = 5,000,599 in 0.346s
//    [1] Count = 5,000,599 in 0.199s
//    [2] Count = 5,000,599 in 0.197s
//    [3] Count = 5,000,599 in 0.196s
//    [4] Count = 5,000,599 in 0.189s
//      Average 0.225s
//
//    Database - TIM
//    Database - TIM (48.379s)
//    ** Database: TIM
//
//    Graph.size()
//    [0] Count = 5,000,599 in 18.009s
//    [1] Count = 5,000,599 in 4.484s
//    [2] Count = 5,000,599 in 3.683s
//    [3] Count = 5,000,599 in 5.860s
//    [4] Count = 5,000,599 in 3.782s
//      Average 7.163s
//
//    Find count
//    [0] Count = 5,000,599 in 3.742s
//    [1] Count = 5,000,599 in 6.245s
//    [2] Count = 5,000,599 in 3.884s
//    [3] Count = 5,000,599 in 3.802s
//    [4] Count = 5,000,599 in 3.849s
//      Average 4.304s
//
//    SPARQL count(*)
//      SELECT (count(*) AS ?c) { ?s ?p ?o }
//    [0] Count = 5,000,599 in 12.740s
//    [1] Count = 5,000,599 in 9.091s
//    [2] Count = 5,000,599 in 6.887s
//    [3] Count = 5,000,599 in 9.560s
//    [4] Count = 5,000,599 in 6.818s
//      Average 9.019s
//
//    SPARQL */materialize
//      SELECT * { ?s ?p ?o }
//    [0] Count = 5,000,599 in 16.373s
//    [1] Count = 5,000,599 in 16.215s
//    [2] Count = 5,000,599 in 13.709s
//    [3] Count = 5,000,599 in 16.339s
//    [4] Count = 5,000,599 in 13.730s
//      Average 15.273s
//
//    SPARQL count(var)
//      SELECT (count(?s) AS ?c) (count(?p) AS ?c2) (count(?o) AS ?c3)  { ?s ?p ?o }
//    [0] Count = 5,000,599 in 12.561s
//    [1] Count = 5,000,599 in 10.342s
//    [2] Count = 5,000,599 in 12.720s
//    [3] Count = 5,000,599 in 10.494s
//    [4] Count = 5,000,599 in 10.563s
//      Average 11.336s
//
//    SPARQL count(materialize)
//      SELECT (count(*) AS ?c) { ?s ?p ?o . FILTER( (?s = ?s && ?p = ?p && ?o = ?o) ) }
//    [0] Count = 5,000,599 in 16.881s
//    [1] Count = 5,000,599 in 15.071s
//    [2] Count = 5,000,599 in 17.479s
//    [3] Count = 5,000,599 in 14.775s
//    [4] Count = 5,000,599 in 17.011s
//      Average 16.243s
