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

package test4;

import org.apache.jena.sparql.ARQTestSuite;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.junit.EarlReport;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import riotcmd.rdflangtest;
import test4.next.manifest.Manifest;
import test4.next.runners.RunnerOfTests;
import test4.next.runners.RunnerOneManifest;
import test4.next.sparql.SparqlTest;

public class DevJUnitUpdate {

    public static void main(String ...args) {

        rdflangtest.main("/home/afs/ASF/afs-jena/jena-arq/testing/RIOT/Lang/TurtleStd/manifest.ttl");
        System.exit(0);

        String name = "ARQ";
        String releaseName = "ARQ";
        String version = "3.99.0";
        String homepage = "http://jena.apache.org/";
        String systemURI = "http://jena.apache.org/#arq";

        String manifestFile = "/home/afs/ASF/afs-jena/jena-arq/"+ARQTestSuite.testDirARQ+"/manifest-arq.ttl";
        //String manifestFile = "/home/afs/ASF/afs-jena/jena-arq/"+ARQTestSuite.testDirARQ+"/BasicPatterns/manifest.ttl";

        //String manifestFile = "/home/afs/ASF/afs-jena/jena-arq/"+ARQTestSuite.testDirARQ+"/Syntax/manifest-syntax.ttl";

        Manifest manifest = Manifest.parse(manifestFile);

        // Include information later.
        EarlReport report = new EarlReport(systemURI, name, version, homepage);
        RunnerOneManifest top = RunnerOfTests.build(report, manifest, SparqlTest::makeSPARQLTest);

        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        // Better: silent warning error handler.
        // Get rid of CheckerLiterals.WarnOnBadLiterals?
        // CheckerLiterals.WarnOnBadLiterals = false ;

        JUnitCore junit = new JUnitCore();
        //junit.addListener(new TextListener2(System.out));
        Result result = junit.run(top);
        System.out.println("Run: "+result.getRunCount());
        System.out.println("Failures: "+result.getFailureCount());
    }
}
