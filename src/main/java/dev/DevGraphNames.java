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

import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;

public class DevGraphNames {
    static {
        // JenaSystem.DEBUG_INIT = true;
        JenaSystem.init();
        FusekiLogging.setLogging();
        // LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // [ ] Iter.first ; Leave the iterator usable. -- change to terminating.
    // findFirst - remove?

    // GRAPH ( iri() | DEFAULT | NAMED | ALL | UNION ) pattern
    // --> Target in modify.

    // tdbloader2
    // Data load phase
    // CmdNodeTableBuilder --> ProcNodeTableBuilder
    // Tree write phase
    // CmdIndexBuild --> ProcIndexBuild
    // New data phase.

    /* gYear, gYearMonth, xs:dateTime template "asDateTime" Leave API as-is and
     * change SPARQL
     *
     * [x] Extract XMLChar and XML11Char [ ] Xerces Regex
     *
     * [ ] Year 0 and calculations
     * [ ] New test suite
     * [ ] gYear difference = years
     */

    public static void main(String...args) { }
}
