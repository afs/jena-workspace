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

package test4.next;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

// Update to Junit 4.

import junit.framework.TestSuite;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.sparql.ARQTestSuite;
import org.apache.jena.sparql.junit.EarlTestCase;
import org.apache.jena.sparql.junit.ScriptTestSuiteFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

//public class TC_Scripted extends TestSuite
//{
//    static public TestSuite suite()
//    {
//        TestSuite ts = new TC_Scripted() ;
//        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/Syntax/manifest-syntax.ttl")) ;
//        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/manifest-arq.ttl")) ;
//        ts.addTest(ScriptTestSuiteFactory.make(ARQTestSuite.testDirARQ+"/Serialization/manifest.ttl")) ;
//
//        String testDirRDFStar = "testing/ARQ/RDF-Star";
//        ts.addTest(FactoryTestRiot.make(testDirRDFStar+"/Turtle-Star/manifest.ttl"));
//        ts.addTest(ScriptTestSuiteFactory.make(testDirRDFStar+"/SPARQL-Star/manifest.ttl")) ;
//
//        return ts ;
//    }
//
//    public TC_Scripted()
//    {
//        super("Scripted") ;
//        NodeValue.VerboseWarnings = false ;
//        E_Function.WarnOnUnknownFunction = false ;
//    }
//}

@RunWith(Parameterized.class)
public class TSx_Scripted {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        String manifest = ARQTestSuite.testDirARQ+"/manifest-arq.ttl";
        return SparqlTests.junitParameters(manifest);
    }

    private final EarlTestCase test;

    //ScriptTestSuiteFactory -> list of (name, test)

    public TSx_Scripted(String name,  EarlTestCase test) {
        this.test = test;
    }

    @Test
    public void test() {
        //EarlTestCase.runTest(test);
    }
}

class SparqlTests {

    public static Collection<Object[]> junitParameters(String manifestFile) {
        // Crude - build test suite, and unbundle.
        // How to get hierarchy
        ScriptTestSuiteFactory tFact = new ScriptTestSuiteFactory() ;
        TestSuite ts =  tFact.process(manifestFile) ;
        List<EarlTestCase> testCases = new ArrayList<>();
        addAll(ts, testCases);
        return testCases.stream().map(stc->new Object[] {decideName(stc), stc}).collect(toList());
    }

    private static void addAll(TestSuite ts, List<EarlTestCase> testCases) {
        Enumeration<junit.framework.Test> en = ts.tests();

        while(en.hasMoreElements()) {
            junit.framework.Test test = en.nextElement();
            if ( test instanceof TestSuite ) {
                TestSuite ts2 = (TestSuite)test;
                addAll(ts2, testCases);
                continue;
            }
            EarlTestCase earlTestCase = (EarlTestCase)test;
            testCases.add(earlTestCase);
        }
    }

    private static String decideName(EarlTestCase stc) {
        return FileOps.basename(stc.getName());
    }

}
