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
@Label("SPARQL [dev]")
@Manifests({
    "/home/afs/Jena/jena-arq/testing/DAWG-Final/manifest-syntax.ttl",
    "/home/afs/Jena/jena-arq/testing/DAWG-Final/manifest-evaluation.ttl",
})

//    public static final String testDirARQ                  = "testing/ARQ";
//    public static final String testDirUpdate               = "testing/Update";


public class T_Dev {
    @BeforeClass static public void beforeClass() {
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
//        ARQ.setStrictMode();
    }

    @AfterClass static public void afterClass() {
        ARQ.setNormalMode();
        E_Function.WarnOnUnknownFunction = true;
        NodeValue.VerboseWarnings = true;
    }
}

