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

import arq.qtest;
import org.apache.jena.arq.junit.riot.ParseForTest;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.sys.JenaSystem;
import riotcmd.rdflangtest;

public class ReportTest {

    public static void main(String...a) {
        JenaSystem.init();
        mainSPARQL();
    }

    public static void mainSPARQL() {
        String FN = "/home/afs/W3C/rdf-star-afs/tests/sparql/syntax/manifest.jsonld";
        qtest.main(FN);
    }

    public static void mainLang() {
        if ( true )
        {
            org.apache.jena.riot.lang.extra.TurtleJCC.register();
            //RDFParserRegistry.registerLangTriples(Lang.TURTLE, TurtleJCC.factory);
            ParseForTest.alternativeReaderFactories.put(Lang.TURTLE, TurtleJCC.factory);
        }

        String[] manifests = {
            // The ARQ Test suite.
            "/home/afs/ASF/afs-jena/jena-arq/testing/RIOT/Lang/TurtleStd/manifest.ttl"
//            "/home/afs/ASF/afs-jena/jena-arq/testing/RIOT/Lang/Turtle2/manifest.ttl",
//            "/home/afs/ASF/afs-jena/jena-arq/testing/RIOT/Lang/TurtleSubm/manifest.ttl",

//            "/home/afs/W3C/rdf-star-afs/tests/nt/syntax/manifest.ttl",
//            "/home/afs/W3C/rdf-star-afs/tests/turtle/syntax/manifest.ttl",
//            "/home/afs/W3C/rdf-star-afs/tests/turtle/eval/manifest.ttl"

            // rdf-tests: Issues with:  6 errors.
            // + Graphs not isomorphic (setting the base?)
            // + Not checking IRIs <http:g>
            //
//            "/home/afs/W3C/rdf-tests/turtle/manifest.ttl"
        };

        for ( String x : manifests ) {
            System.out.println("==== Manifest: "+x);
            rdflangtest.main(x);
        }
    }

}

