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

import java.io.IOException;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.sse.SSE;

public class Report {

    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) throws IOException {
        Graph g = SSE.parseGraph("(graph (?x :p :o))");
        Model model = ModelFactory.createModelForGraph(g);
        model.listStatements().forEachRemaining(System.out::println);
        System.out.println("DONE");
        System.exit(0);





        System.out.println(Integer.MAX_VALUE);
        long z = ((long)Integer.MAX_VALUE)+1;
        System.out.println(z);
        int i = (int)z;
        System.out.println(i);
        long z2 = i;
        System.out.println(z2);

        System.out.println("DONE");
        System.exit(0);


        IRIResolver.resolveString("urn:x:abc", "http://base");
        System.out.println("DONE");
        System.exit(0);
    }
}
