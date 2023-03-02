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

import arq.rdftests;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;

public class ReportTest {

    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        //rdftests.main("/home/afs/W3C/SPARQL-1.2/afs-sparql-12/tests/Lateral/manifest.ttl");
        rdftests.main("/home/afs/tmp/kasai-sparql-12/tests/xsd_functions/manifest.ttl");

        //arq.sparql.main("--file", "/home/afs/tmp/kasai-sparql-12/tests/xsd_functions/construct_time-01.rq");
    }
}

