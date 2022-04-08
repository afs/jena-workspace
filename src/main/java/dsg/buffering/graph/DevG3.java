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

package dsg.buffering.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.sse.SSE;

public class DevG3 {

    public static void main(String[] args) {
        Graph g = SSE.parseGraph("""
                (graph
                    (:s :p :o)
                    (:s :p 1)
                    (:s :p 'a')
                    (:s :p 'A'@En)
                )""");

        dwim(g, "1", true);
        dwim(g, "2", false);
        dwim(g, "'abc'@EN", false);
        dwim(g, "'a'@EN", false);
        dwim(g, "'A'@EN", true);
    }

    private static void dwim(Graph g, String ns2, boolean expected) {

        Node data = SSE.parseNode(ns2);
        boolean actual = G3.containsByEquals(g, null, null, data);
        if ( actual != expected )
            System.out.printf("** \"%s\" expected=%s, actual=%s\n", ns2, expected, actual);
        else
            System.out.printf("++ \"%s\" %s\n", ns2, actual);

    }

}
