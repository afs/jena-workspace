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

package dev;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.VarFinder;
import org.apache.jena.sparql.sse.SSE;

public class DevAssignSequence {
    
    // "assignFree"
    
    public static void main(String... args) {
        lookat("(extend ((?name2 (+ 1 ?name))) (bgp (triple ?person :name ?name) ))");
        // This is not linearizable.
        lookat("(join (extend ((?name2 ?name)) (table unit))  (bgp (triple ?person :name ?name) ))");
    }
    
    public static void lookat(String arg) {
        Op op = SSE.parseOp(arg);
        System.out.print(op);
        VarFinder vf = VarFinder.process(op);
        System.out.println(vf);
        if ( op instanceof OpJoin ) {
            OpJoin opj = (OpJoin)op;
            boolean b = JoinClassifier.isLinear(opj);
            System.out.println("isLinear = "+b);
        }
        
        System.out.println();
    }
        
    
    public static void main1(String... args) {
        Query query = QueryFactory.read("/home/afs/TQ/PLAT-1475/Q.rq");
        System.out.println(query);
        Op op = Algebra.compile(query);
        System.out.println(op);
        JoinClassifier.print = true;
        Op op1 = Transformer.transform(new TransformJoinStrategy(), op);
        System.out.println(op1);
        /*
boolean bad3 = SetUtils.intersectionP(vRightAssign, vLeftFixed) ;
Add the other way round.

Check that "assign" iff not in fixed already.

VarFinder
 
PREFIX  ex:   <http://example.org/>

SELECT  *
WHERE
  { BIND("Alice" AS ?name)
    { BIND(?name AS ?name2)
      ?person  ex:name  ?name
    }
  }
  
Left::
(extend ((?name2 ?name))
  (table unit))

Right::
(bgp (triple ?person <http://example.org/name> ?name))
  

Left
  Filter:       []
  Filter only:  []
  Fixed :       [?name]
  Opt:          []
  Assign:       []
Right
  Filter:       []
  Filter only:  []
  Fixed :       [?person, ?name2, ?name]
  Opt:          []
  Assign:       [?name] 
         */
    }

}
