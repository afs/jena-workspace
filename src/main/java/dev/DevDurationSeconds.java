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

import java.time.Duration;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;

public class DevDurationSeconds {

    // NodeValueDuation to carry both javax.xml.datatype.Duration (for fields and dayTime vs yearMonth)
    // and java.time.Duration for (some) calculations.

    public static void main(String[] args) {
        // op:divide-dayTimeDuration-by-dayTimeDuration
        Node n1 = SSE.parseNode("'P1D'^^xsd:dayTimeDuration");
        Node n2 = SSE.parseNode("'PT1H'^^xsd:dayTimeDuration");
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        long s1 = toMilliSeconds(nv1);
        long s2 = toMilliSeconds(nv2);
        // Ratio in seconds.
        System.out.println(s1/(1000.0*s2));
    }

    private static long toMilliSeconds(NodeValue nv) {
        Duration d = Duration.parse(nv.getNode().getLiteralLexicalForm());
        return d.toMillis();
    }

    // Ignores fractional seconds.
    // getField() returns BigInteger/BigDecimal

    private static long toSeconds(NodeValue nv) {
        javax.xml.datatype.Duration d = nv.getDuration();
        // Day
        long x = d.getDays();
        x = x*24;
        // Hour
        x += d.getHours();
        x = x*60;
        // Minutes
        x += d.getMinutes();
        x = x*60;
        // Seconds, ignoring fractional.
        x += d.getSeconds();
        //BigDecimal fractionalSeconds = d.getField(DatatypeConstants.SECONDS);
        return x;
    }

}
