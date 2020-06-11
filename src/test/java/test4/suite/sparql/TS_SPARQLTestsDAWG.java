/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package test4.suite.sparql;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import test4.next.manifest.Label;
import test4.next.manifest.Manifests;
import test4.next.runners.RunnerSPARQL;

@RunWith(RunnerSPARQL.class)
@Label("DAWG")
@Manifests
  ({
      // DAWG
      "/home/afs/Jena/jena-arq/testing/DAWG-Final/manifest-syntax.ttl",
      "/home/afs/Jena/jena-arq/testing/DAWG-Final/manifest-evaluation.ttl",
  })

public class TS_SPARQLTestsDAWG
{
    @BeforeClass static public void beforeClass() {
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        // No! ARQ.setStrictMode();
    }

    @AfterClass static public void afterClass() {
        ARQ.setNormalMode();
        NodeValue.VerboseWarnings = true;
        E_Function.WarnOnUnknownFunction = true;
    }
}
