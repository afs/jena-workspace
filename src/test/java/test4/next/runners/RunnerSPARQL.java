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

package test4.next.runners;

import org.junit.runners.model.InitializationError;
import test4.next.sparql.SparqlTest;

/**
 * Runner for SPARQL Manifests. Annotations supported:
 * <ul>
 * <li><tt>@Label("Some name")</tt></li>
 * <li><tt>@Manifests({"manifest1","manifest2",...})</tt></li>
 * </ul>
 * This class sorts out the annotations, including providing before/after class, then
 * creates a hierarchy of tests to run.
 *
 * @see RunnerOneTest
 */
public class RunnerSPARQL extends RunnerOfTests {
    public RunnerSPARQL(Class<? > klass) throws InitializationError {
        super(klass, SparqlTest::makeSPARQLTest);
    }
}
