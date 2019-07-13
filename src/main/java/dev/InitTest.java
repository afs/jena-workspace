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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class InitTest implements JenaSubsystemLifecycle {
    @Override
    public void start() {
        if ( JenaSystem.DEBUG_INIT )
            System.err.println("InitTEST -- start");
        if ( RDF.type == null ) {
            System.err.println("RDF#type is null => attempt to load a graph here will fail");
        }
        //assert RDF.type != null : "RDF#type is null => attempt to load a graph here will fail";
    }

    @Override
    public void stop() {
        if ( JenaSystem.DEBUG_INIT )
            System.err.println("InitTEST -- finish");
    }

    @Override
    public int level() {
        
        return 500;
    }

    public static void main(String...args) { // run VM option: -ea
        //JenaSystem.init();
        //JenaSystem.DEBUG_INIT = true;
        // RDFNode r = ResourceFactory.createProperty("X"); // this works fine
        RDFNode r = ResourceFactory.createTypedLiteral("Y"); // this causes a problem
        System.out.println(r);
    }
}
