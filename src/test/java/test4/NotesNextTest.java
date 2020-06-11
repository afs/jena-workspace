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

public class NotesNextTest {
    // test4.next
    //   Clean up ExecQueryTest.

    // ----

    // @Parameterised - unwrap a leaf manifest
    // @RunWith(AllTests.class) -- looks for public static junit.framework.Test suite() {
    // @RunWith(Suite.class) --Only with fixed.class?

//    Current ARQ TC_Scripted
//    "jena-arq/testing/ARQ/Syntax/manifest-syntax.ttl"
//    "jena-arq/testing/ARQ/manifest-arq.ttl")) ;
//    "jena-arq/testing/ARQ/Serialization/manifest.ttl")) ;
    // RDF*
    // 1087 tests.

    // next: 1044, not RDF* (43)



    // ** RunnerSPARQL --> RunnerOfScripts > RunnerSPARQL, RunnerRIOT
    // + Review ExecQueryTest ; dead code?
    // + Delete BaseTest2
    // + Check if a test failure is noticed! Blue/fail
    //   Do we need to catch the exception in the run(Notifier)
    // + Remove CheckerLiterals.WarnOnBadLiterals
    // + DAWG-Final : strict must be off - why?

    // Parser. Only StreamRDF version?

    // RIOT tests ==>
    //   Clean langeval tests : no Model.
    // Merge tests.

    // Make sure the WG SPARQl 1.1 test that matter get run.

    // Check tests - do setup early.
}

