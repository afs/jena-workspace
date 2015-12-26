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

import java.util.function.Predicate ;
import java.util.stream.Stream ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;

public class Gravity {
    // Start
    
    static B create() {
        return null ;
    }
    
//    static B create() {
//        return new B() ;
//    }
    
    interface B {
        // map
        // flatMap
        // filter
        // sideEffect
        // branch
        
        // aggregate == collect
        
        // fold
        
        
        // Start
        
        
        // Move 
        B fwd(Node p) ;
        B bkd(Node p) ;
        
        // Filter
        B filter(String expr) ;
        B filter(Expr expr) ;
        B filter(Predicate<Node> predicate) ;
    
    
    // "Table building"
    // Add col.
    
        Stream<Binding> exec() ;
    }
    
    /// Execute while you build.
    public static class Exec 
    //implements B
    {
        ResultSet results ;
        
        
    }
}
