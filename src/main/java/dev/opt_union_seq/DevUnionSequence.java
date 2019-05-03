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

package dev.opt_union_seq;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.LeftJoinClassifier;
import org.apache.jena.sparql.sse.SSE;

public class DevUnionSequence {
    
    static String queryString = StrUtils.strjoinNL
        ("PREFIX : <http://example/>"
        ,"SELECT * {"
        ,"    ?a :p1 ?b"
        ,"  { ?a :p2 ?x } UNION { ?b :p3 ?y }"
        ,"}"
        );

    static String queryString1 = StrUtils.strjoinNL
        ("PREFIX : <http://example/>"
        ,"SELECT * {"
        ,"    ?a :p1 ?b"
        // IKs conditional
        ,"    OPTIONAL { ?a :p2 ?x }"
        ,"    OPTIONAL { ?b :p3 ?y }"
        ,"}"
        );
    static String x = StrUtils.strjoinNL
        ("(join"
        ,"  (bgp (triple ?z :p1 ?x))"
        ,"  (union"
        ,"    (bgp (triple ?a :p2 ?z))"
        ,"    (bgp (triple ?b :p3 ?z))"
        ,"))"
        );
    
    // Order
    //  TransformFilterImplicitJoin
    //  TransformImplicitLeftJoin
    //  TransformFilterDisjunction - filter ?X = a or b { P } => { ?X = a, { P } } UNION { ?X = b, { P } }  
    //  TransformTopN
    
    /*
PREFIX : <http://example/>

SELECT * {
    ?a :p1 ?b
    { ?a :p2 ?x } UNION { ?b :p3 ?y }
}
 
     */
    
    public static void main(String... args) {
        lookAtQuery(queryString);
        //lookAtOp(x);
    }
    
    public static void lookAtQuery(String arg) {
        Query query = QueryFactory.create(arg);
        Op op = Algebra.compile(query);
        JoinClassifier.print = true;
        LeftJoinClassifier.print = true;
        Op op1 = Transformer.transform(new TransformJoinStrategy(), op);
        System.out.println(op1);
    }        

    public static void lookAtOp(String arg) {
        Op op = SSE.parseOp(arg);
        JoinClassifier.print = true;
        LeftJoinClassifier.print = true;
        Op op1 = Transformer.transform(new TransformJoinStrategy(), op);
        System.out.println(op1);
    }        
}
