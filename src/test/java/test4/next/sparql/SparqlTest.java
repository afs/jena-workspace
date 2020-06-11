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

package test4.next.sparql;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.junit.QueryTestException;
import org.apache.jena.sparql.vocabulary.TestManifest;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11;
import org.apache.jena.sparql.vocabulary.TestManifestX;
import org.apache.jena.sparql.vocabulary.TestManifest_11;
import test4.next.manifest.ManifestEntry;
import test4.next.sparql.tests.*;

public class SparqlTest {

    static public Runnable makeSPARQLTest(ManifestEntry entry) {
        if ( entry.getAction() == null )
        {
            System.out.println("Null action: "+entry) ;
            return null ;
        }

        // Defaults.
        Syntax querySyntax = Syntax.syntaxSPARQL_11; // TestQueryUtils.getQuerySyntax(manifest)  ;

        if ( querySyntax != null )
        {
            if ( ! querySyntax.equals(Syntax.syntaxARQ) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_10) &&
                 ! querySyntax.equals(Syntax.syntaxSPARQL_11) )
                throw new QueryTestException("Unknown syntax: "+querySyntax) ;
        }

        Resource testType = entry.getTestType() ;
        if ( testType == null )
            testType = TestManifest.QueryEvaluationTest;

        if ( testType != null )
        {
            // == Good syntax
            if ( testType.equals(TestManifest.PositiveSyntaxTest) )
                return new SyntaxQueryTest(entry, querySyntax, true);
            if ( testType.equals(TestManifest_11.PositiveSyntaxTest11) )
                return new SyntaxQueryTest(entry, Syntax.syntaxSPARQL_11, true) ;
            if ( testType.equals(TestManifestX.PositiveSyntaxTestARQ) )
                return new SyntaxQueryTest(entry, Syntax.syntaxARQ, true) ;

            // == Bad
            if ( testType.equals(TestManifest.NegativeSyntaxTest) )
                return new SyntaxQueryTest(entry, querySyntax, false) ;
            if ( testType.equals(TestManifest_11.NegativeSyntaxTest11) )
                return new SyntaxQueryTest(entry, Syntax.syntaxSPARQL_11, false) ;
            if ( testType.equals(TestManifestX.NegativeSyntaxTestARQ) )
                return new SyntaxQueryTest(entry, Syntax.syntaxARQ, false) ;

            // ---- Update tests
            if ( testType.equals(TestManifest_11.PositiveUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(entry, querySyntax, true) ;
            if ( testType.equals(TestManifestX.PositiveUpdateSyntaxTestARQ) )
                return new SyntaxUpdateTest(entry, Syntax.syntaxARQ, true) ;

            if ( testType.equals(TestManifest_11.NegativeUpdateSyntaxTest11) )
                return new SyntaxUpdateTest(entry, querySyntax, false) ;
            if ( testType.equals(TestManifestX.NegativeUpdateSyntaxTestARQ) )
                return new SyntaxUpdateTest(entry, Syntax.syntaxARQ, false) ;

            // Two names for same thing.
            // Note item is not passed down.
            if ( testType.equals(TestManifestUpdate_11.UpdateEvaluationTest) )
                return new ExecUpdateTest(entry);
            if ( testType.equals(TestManifest_11.UpdateEvaluationTest) )
                return new ExecUpdateTest(entry);

            // ----

            if ( testType.equals(TestManifestX.TestSerialization) )
                return new SerializationTest(entry) ;

            if ( testType.equals(TestManifest.QueryEvaluationTest)
                || testType.equals(TestManifestX.TestQuery)
                )
                return new ExecQueryTest(entry, querySyntax) ;

            // Reduced is funny.
            if ( testType.equals(TestManifest.ReducedCardinalityTest) )
                return new ExecQueryTest(entry, querySyntax);

            if ( testType.equals(TestManifestX.TestSurpressed) )
                return new SurpressedTest(entry);

            if ( testType.equals(TestManifest_11.CSVResultFormatTest) )
            {
                Log.warn("Tests", "Skip CSV test: "+entry.getName()) ;
                return null ;
            }

            System.err.println("Test type '"+testType+"' not recognized") ;
        }
        // Default.
        System.err.println("Warning: test has not test type : "+entry.getURI());
        return new ExecQueryTest(entry, querySyntax);
    }
//
//    public static RunnerOneManifest build(EarlReport report, Manifest manifest) {
//        //System.err.println("Manifest: "+manifest.getName());
//        Description description = Description.createSuiteDescription(manifest.getName());
//
//        RunnerOneManifest thisLevel = new RunnerOneManifest(description);
//
//        Iterator<String> sub = manifest.includedManifests();
//        while(sub.hasNext() ) {
//            String mf = sub.next();
//            Manifest manifestSub = Manifest.parse(mf);
//            Runner runner = build(report, manifestSub);
//            thisLevel.add(runner);
//        }
//        prepareTests(report, thisLevel, manifest);
//        return thisLevel;
//    }

}
