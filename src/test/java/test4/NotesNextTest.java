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

    // [2]
    // Checking phase : SPARQL 1.1 tests
    // + SPARQL 1.1 tests.
    // Make sure the WG SPARQL 1.1 test that matter get run.
    // Move test4.next  TextTestRunner

    // -----

    // 3: Get rid of core-TURTLE
    //   JENA- old writer names,
    //    Take out of readers and writers.
    //    Delete from tests.
    // Assembler uses Turtle - move tests to "integration"
    // jena-core FileManager, LocationMapper
    // JA -- convert schema to RDF/XML. Does it need it?

    // ----

    // Separate:
    //   Issue old Turtle. Remove TURTLE from core?
    //   core FileUtils. Deprecate constants. private?
}

