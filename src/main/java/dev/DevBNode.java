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

package dev ;

public class DevBNode {
    
    public static void main(String ... a) {
        // Problems:
        // When strict, no combining of a stack of OpAssign is done.
        // which means each assign is a new binding so "same binding" is false.
        // Solution? 
        // 1:: Have a "rowkey" in a binding.  Same for assign (can't be parent-shared)
        // 2:; Flag on a Binding "can extend".
        
//        System.out.println("**** Plain") ;
//        arq.sparql.main("--data=/home/afs/tmp/D.nt", "SELECT (BNODE('BAZ') AS ?b1) (BNODE('BAZ') AS ?b2) {}") ;
        System.out.println("**** Strict") ;
        arq.sparql.main("--strict", "--data=/home/afs/tmp/D.nt", "SELECT (BNODE('BAZ') AS ?b1) (BNODE('BAZ') AS ?b2) {}") ;
        System.exit(0) ;
    }
}
